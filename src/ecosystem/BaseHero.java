package ecosystem;

import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import base.Controller;
import base.KeyOrder;
import base.Region;
import base.Scope;
import base.Type;
import base.Vector;
import component.Action;
import component.Bdy;
import component.Cost;
import component.Effect;
import component.Frame;
import component.Itr;
import component.State;
import util.Tuple;

public class BaseHero extends AbstractObject implements Hero {
  private static final System.Logger logger = System.getLogger("");
  protected static final int DEFAULT_ITR_SCOPE =
      Scope.ENEMY_HERO | Scope.ALL_WEAPON | Scope.ALL_ENERGY;

  private final HiddenFrameCounter walkingFrameCounter = HiddenFrameCounter.forWalking(3);
  private final HiddenFrameCounter runningFrameCounter = HiddenFrameCounter.forRunning(3);

  /**
   * Stores asynchronous {@code Action} after receiving an {@code Itr}.
   */
  protected Action receivedItrAction = Action.UNASSIGNED;

  /**
   * Stores asynchronous {@code Action} after sending an {@code Itr}.
   */
  protected Action sentItrAction = Action.UNASSIGNED;

  private final String portrait;
  private Controller controller = Controller.NULL_CONTROLLER;
  private Weapon weapon = NullObject.WEAPON;

  private Vector walkingSpeed;
  private Vector runningSpeed;
  private Vector heavyWalkingSpeed;
  private Vector heavyRunningSpeed;
  private Vector jumpSpeed;
  private Vector dashSpeed;
  private Vector flipSpeed;
  private double mpReg = 1.0 / 3.00;
  private double hpReg = 1.0 / 12.0;
  private double hp2nd = 500.0;
  private int defendPoint = 0;
  private int fallPoint = 0;

  protected BaseHero(Frame.Collector collector, String portrait, Map<String, Double> stamina) {
    super(Type.HERO, collector);
    this.portrait = portrait;

    double wx = stamina.get("walking_speed").doubleValue();
    double wz = stamina.get("walking_speedz").doubleValue();
    walkingSpeed = Vector.of(wx, Vector.findComponent(0, wx, wz), wz);

    double rx = stamina.get("running_speed").doubleValue();
    double rz = stamina.get("running_speedz").doubleValue();
    runningSpeed = Vector.of(rx, Vector.findComponent(0, rx, rz), rz);

    double hwx = stamina.get("heavy_walking_speed").doubleValue();
    double hwz = stamina.get("heavy_walking_speedz").doubleValue();
    heavyWalkingSpeed = Vector.of(hwx, Vector.findComponent(0, hwx, hwz), hwz);

    double hrx = stamina.get("heavy_running_speed").doubleValue();
    double hrz = stamina.get("heavy_running_speedz").doubleValue();
    heavyRunningSpeed = Vector.of(hrx, Vector.findComponent(0, hrx, hrz), hrz);

    jumpSpeed = Vector.of(stamina.get("jump_distance").doubleValue(),
                          stamina.get("jump_height").doubleValue(),
                          stamina.get("jump_distancez").doubleValue());
    dashSpeed = Vector.of(stamina.get("dash_distance").doubleValue(),
                          stamina.get("dash_height").doubleValue(),
                          stamina.get("dash_distancez").doubleValue());
    flipSpeed = Vector.of(stamina.get("rowing_distance").doubleValue(), 0.0,
                          stamina.get("rowing_height").doubleValue());
  }

  private BaseHero(BaseHero base) {
    super(base);
    portrait = base.portrait;
    walkingSpeed = base.walkingSpeed;
    runningSpeed = base.runningSpeed;
    heavyWalkingSpeed = base.heavyWalkingSpeed;
    heavyRunningSpeed = base.heavyRunningSpeed;
    jumpSpeed = base.jumpSpeed;
    dashSpeed = base.dashSpeed;
    flipSpeed = base.flipSpeed;
    mpReg = base.mpReg;
    hpReg = base.hpReg;
  }

  @Override
  public BaseHero makeClone() {
    return new BaseHero(this);
  }

  @Override
  public void setController(Controller controller) {
    this.controller = controller;
    return;
  }

  @Override
  public double getInputZ() {
    return controller.valueZ();
  }

  @Override
  public void revive() {
    hp = hp2nd = hpMax;
    mp = mpMax;
    return;
  }

  @Override
  public void fillStatus(double[] status) {
    status[0] = hp2nd;
    status[1] = hpMax;
    status[2] = hp;
    status[3] = hpMax;
    status[4] = mp;
    status[5] = mpMax;
    return;
  }

  @Override
  protected Action getDefaultAction() {
    if (weapon.isHeavy()) {
      return Action.HERO_HEAVY_WALK;
    } else {
      return py < 0.0 ? Action.HERO_JUMP_AIR : Action.HERO_STANDING;
    }
  }

  @Override
  protected void transitNextFrame(Frame targetFrame, boolean changeFacing) {
    Cost cost = targetFrame.cost;
    if (cost.mp() >= 0 || terrain.isUnlimitedMode()) {
      super.transitNextFrame(targetFrame, changeFacing);
      return;
    }
    // If spare action is unspecified, one still goes to `next` and runs out of mp.
    Action fallbackAction = frame.combo.getOrDefault(KeyOrder.hit_d, Action.UNASSIGNED);
    if (fallbackAction == Action.UNASSIGNED || mp + cost.mp() >= 0) {
      mp = mp + cost.mp();
      super.transitNextFrame(targetFrame, changeFacing);
    } else {
      super.transitGoto(fallbackAction);
    }
    return;
  }

  /**
   * Most of the time, potential HP is reduced by one-third of the received damage.
   * Use super.hpLost() in the situations not following this rule (e.g., throwinjury).
   */
  protected void hpLost(double injury) {
    hp -= injury;
    hp2nd -= Math.floor(injury / 3.0);
    return;
  }

  private void reflectItrAction() {
    if (receivedItrAction != Action.UNASSIGNED) {
      transitGoto(receivedItrAction);
      receivedItrAction = Action.UNASSIGNED;
    }
    if (sentItrAction != Action.UNASSIGNED) {
      transitGoto(sentItrAction);
      sentItrAction = Action.UNASSIGNED;
    }
    return;
  }

  @Override
  public Frame getCurrentFrame() {
    reflectItrAction();
    return frame;
  }

  @Override
  public void run(int timestamp, List<Observable> allObjects) {
    reflectItrAction();
    Optional<SyncPick> osp = SyncPick.getPicker(this);
    weapon = osp.isPresent() ? osp.get().victim : NullObject.WEAPON;
    super.run(timestamp, allObjects);
    return;
  }

  @Override
  protected Action updateByState() {
    controller.update();
    Action nextAct = switch (frame.state) {
      case STAND -> moveStand();
      case WALK -> moveWalk();
      case RUN -> moveRun();
      case JUMP -> moveJump();
      case DASH -> moveDash();
      case FLIP -> moveFlip();
      case DRINK -> moveDrink();
      case FALL -> moveFall();
      case FIRE -> moveFire();
      case LYING -> moveLying();
      case GRABBING -> moveGrabbing();
      case GRABBED -> moveGrabbed();
      case NORMAL, DEFEND, DANCE_OF_PAIN, ICE -> Action.UNASSIGNED;
      default -> {
        logger.log(Level.WARNING, "Unexpected {0}", frame.state);
        yield Action.UNASSIGNED;
      }
    };
    // Special case: you can change facing in this action number.
    if (frame.curr == Action.HERO_DEFEND.index) {
      faceRight = controller.getFacing(faceRight);
    }
    // Do combo overwrite action.
    Action orderAction = frame.combo.getOrDefault(controller.getKeyOrder(), Action.UNASSIGNED);
    if (orderAction == Action.UNASSIGNED) {
      return nextAct;
    }
    if (orderAction == Action.DEFAULT) {
      return Action.DEFAULT;
    }
    Cost cost = frameList.get(orderAction.index).cost;
    if (cost == Cost.FREE || terrain.isUnlimitedMode()) {
      controller.consume();
      return orderAction;
    } else if (mp >= cost.mp() && hp >= cost.hp()) {
      mp -= cost.mp();
      hp -= cost.hp();
      controller.consume();
      return orderAction;
    } else {
      return nextAct;
    }
  }

  private Action moveStandWalkPressA() {
    if (weapon.isLight()) {
      return random.nextBoolean() ? Action.HERO_WEAPON_ATK1 : Action.HERO_WEAPON_ATK2;
    } else if (weapon.isSmall()) {
      return Action.HERO_LIGHT_WEAPON_THROW;
    } else if (weapon.isDrink()) {
      return Action.HERO_DRINK;
    } else if (weapon.isHeavy()) {
      return Action.HERO_HEAVY_WEAPON_THROW;
    } else if (buff.getOrDefault(Effect.FORCE_SUPER_PUNCH, 0) > terrain.getTimestamp()) {
      return Action.HERO_SUPER_PUNCH;
    } else {
      return random.nextBoolean() ? Action.HERO_PUNCH1 : Action.HERO_PUNCH2;
    }
  }

  private Action moveStand() {
    if (controller.press_a()) {
      return moveStandWalkPressA();
    } else if (controller.press_j()) {
      return Action.HERO_JUMP;
    } else if (controller.press_d()) {
      return Action.HERO_DEFEND;
    } else if (controller.pressRun()) {
      faceRight = controller.getFacing(faceRight);
      return Action.HERO_RUNNING.shift(runningFrameCounter.reset());
    } else if (controller.pressWalk()) {
      faceRight = controller.getFacing(faceRight);
      return Action.HERO_WALKING.shift(walkingFrameCounter.reset());
    } else {
      return Action.UNASSIGNED;
    }
  }

  private Action moveWalk() {
    if (controller.pressZ()) {
      double speedX = weapon.isHeavy() ? heavyWalkingSpeed.y() : walkingSpeed.y();
      double speedZ = weapon.isHeavy() ? heavyWalkingSpeed.z() : walkingSpeed.z();
      vx = controller.valueX() * speedX;
      pz += controller.valueZ() * speedZ;
    } else {
      double speed = weapon.isHeavy() ? heavyWalkingSpeed.x() : walkingSpeed.x();
      vx = controller.valueX() * speed;
    }
    if (controller.press_a()) {
      return moveStandWalkPressA();
    } else if (controller.press_j()) {
      return weapon.isHeavy() ? Action.UNASSIGNED : Action.HERO_JUMP;
    } else if (controller.press_d()) {
      return weapon.isHeavy() ? Action.UNASSIGNED : Action.HERO_DEFEND;
    } else if (controller.pressRun()) {
      faceRight = controller.getFacing(faceRight);
      Action referenceAction = weapon.isHeavy() ? Action.HERO_HEAVY_RUN : Action.HERO_RUNNING;
      return referenceAction.shift(runningFrameCounter.reset());
    } else if (controller.pressWalk()) {
      faceRight = controller.getFacing(faceRight);
      Action referenceAction = weapon.isHeavy() ? Action.HERO_HEAVY_WALK : Action.HERO_WALKING;
      return referenceAction.shift(walkingFrameCounter.next());
    } else {
      return Action.UNASSIGNED;
    }
  }

  private Action moveRun() {
    if (controller.pressZ()) {
      double speedX = weapon.isHeavy() ? heavyRunningSpeed.y() : runningSpeed.y();
      double speedZ = weapon.isHeavy() ? heavyRunningSpeed.z() : runningSpeed.z();
      vx = faceRight ? speedX : -speedX;
      pz += controller.valueZ() * speedZ;
    } else {
      double speed = weapon.isHeavy() ? heavyRunningSpeed.x() : runningSpeed.x();
      vx = faceRight ? speed : -speed;
    }
    if (controller.press_a()) {
      if (weapon.isLight()) {
        return controller.pressWalk() ? Action.HERO_LIGHT_WEAPON_THROW : Action.HERO_RUN_WEAPON_ATK;
      } else if (weapon.isSmall()) {
        return Action.HERO_LIGHT_WEAPON_THROW;
      } else if (weapon.isDrink()) {
        return controller.pressWalk() ? Action.HERO_LIGHT_WEAPON_THROW : Action.HERO_DRINK;
      } else if (weapon.isHeavy()) {
        return Action.HERO_HEAVY_WEAPON_THROW;
      } else {
        return Action.HERO_RUN_ATK;
      }
    } else if (controller.press_j()) {
      return weapon.isHeavy() ? Action.UNASSIGNED : Action.HERO_DASH;
    } else if (controller.press_d()) {
      return weapon.isHeavy() ? Action.UNASSIGNED : Action.HERO_ROLLING;
    } else if (controller.reverseFacing(faceRight)) {
      return weapon.isHeavy() ? Action.HERO_HEAVY_STOP_RUN : Action.HERO_STOPRUN;
    } else {
      Action referenceAction = weapon.isHeavy() ? Action.HERO_HEAVY_RUN : Action.HERO_RUNNING;
      return referenceAction.shift(runningFrameCounter.next());
    }
  }

  private Action moveJump() {
    if (frame.curr != Action.HERO_JUMP_AIR.index) {
      return Action.UNASSIGNED;
    }
    if (isActionFirstTimeunit()) {
      if (py >= 0.0) {
        // NOTE: Original LF2 seems that velocity gained at the transition from 211 to 212.
        vx += controller.valueX() * jumpSpeed.x();
        vy += jumpSpeed.y();
        vz = controller.valueZ() * jumpSpeed.z();
      }
      return Action.UNASSIGNED;
    }
    faceRight = controller.getFacing(faceRight);
    if (controller.press_a()) {
      if (weapon.isLight()) {
        return controller.pressWalk() ? Action.HERO_SKY_WEAPON_THROW : Action.HERO_JUMP_WEAPON_ATK;
      } else if (weapon.isSmall() || weapon.isDrink()) {
        return Action.HERO_SKY_WEAPON_THROW;
      } else {
        return Action.HERO_JUMP_ATK;
      }
    }
    return Action.UNASSIGNED;
  }

  private Action dashAttack() {
    if (weapon.isLight()) {
      return Action.HERO_DASH_WEAPON_ATK;
    } else if (weapon.isSmall() || weapon.isDrink()) {
      return Action.HERO_SKY_WEAPON_THROW;
    } else {
      return Action.HERO_DASH_ATK;
    }
  }

  private Action moveDash() {
    if (frame.curr == Action.HERO_DASH.index) {
      if (py >= 0.0 && isActionFirstTimeunit()) {
        vx = faceRight ? dashSpeed.x() : -dashSpeed.x();
        vy += dashSpeed.y();
        vz = controller.valueZ() * dashSpeed.z();
      }
      if (controller.press_a()) {
        return dashAttack();
      }
      if (controller.reverseFacing(faceRight)) {
        faceRight ^= true;
        return Action.HERO_DASH_REVERSE;
      }
    } else if (frame.curr == Action.HERO_DASH2.index) {
      if (controller.press_a()) {
        return dashAttack();
      }
      if (controller.reverseFacing(faceRight)) {
        faceRight ^= true;
        return Action.HERO_DASH_REVERSE2;
      }
    } else if (frame.curr == Action.HERO_DASH_REVERSE.index) {
      if (py >= 0.0 && isActionFirstTimeunit()) {
        vx = vx >= 0.0 ? dashSpeed.x() : -dashSpeed.x();
        vy += dashSpeed.y();
        vz = controller.valueZ() * dashSpeed.z();
      }
      if (controller.reverseFacing(faceRight)) {
        faceRight ^= true;
        return Action.HERO_DASH;
      }
    } else if (frame.curr == Action.HERO_DASH_REVERSE2.index) {
      if (controller.reverseFacing(faceRight)) {
        faceRight ^= true;
        return Action.HERO_DASH2;
      }
    } else {
      logger.log(Level.WARNING, "Unexpected Dash Action {0}", frame);
    }
    return Action.UNASSIGNED;
  }

  private Action moveFall() {
    if (Action.HERO_FACEDOWN_FALL.contains(frame.curr)) {
      if (py >= 0.0) {
        return Action.HERO_FACEDOWN_LYING;
      } else if (Action.HERO_FACEDOWN_FALLR.index == frame.curr) {
        // Can do nothing.
      } else if (vy < -10.0) {
        return Action.HERO_FACEDOWN_FALL1;
      } else if (vy < 0.0) {
        return Action.HERO_FACEDOWN_FALL2;
      } else if (vy < 6.0) {
        // TODO: Check buff.containsKey(Effect.SONATA)
        return controller.press_j() ? Action.HERO_FLIP2 : Action.HERO_FACEDOWN_FALL3;
      } else {
        return Action.HERO_FACEDOWN_FALL4;
      }
    } else {
      if (py >= 0.0) {
        return Action.HERO_FACEUP_LYING;
      } else if (Action.HERO_FACEUP_FALLR.index == frame.curr) {
        // Can do nothing.
      } else if (vy < -10.0) {
        return Action.HERO_FACEUP_FALL1;
      } else if (vy < 0.0) {
        return Action.HERO_FACEUP_FALL2;
      } else if (vy < 6.0) {
        return controller.press_j() ? Action.HERO_FLIP1 : Action.HERO_FACEUP_FALL3;
      } else {
        return Action.HERO_FACEUP_FALL4;
      }
    }
    return Action.UNASSIGNED;
  }

  private Action moveDrink() {
    // You can be forced into these actions without holding anything.
    Vector regen = weapon.consume();
    mp = Math.min(mpMax, mp + regen.x());
    hp = Math.min(hpMax, hp + regen.y());
    hp2nd = Math.max(hp, hp2nd + regen.z());
    return Action.UNASSIGNED;
  }

  private Action moveFire() {
    return vy < 0.0 ? Action.HERO_UPWARD_FIRE : Action.HERO_DOWNWARD_FIRE;
  }

  private Action moveFlip() {
    if (isActionFirstTimeunit()) {
      vy = flipSpeed.y();
      vx += faceRight ? flipSpeed.x() : -flipSpeed.x();
    }
    buff.remove(Effect.THROWN_ATTACK);
    return Action.UNASSIGNED;
  }

  private Action moveLying() {
    fallPoint = defendPoint = 0;
    return hp > 0.0 ? Action.UNASSIGNED : Action.REPEAT;
  }

  @Override
  protected Action moveGrabbing() {
    if (frame.cpoint == null || frame.cpoint.isThrowing()) {
      return Action.UNASSIGNED;
    }
    Optional<SyncGrab> osg = SyncGrab.getGrabber(this);
    if (osg.isEmpty()) {
      return Action.DEFAULT;
    }
    if (osg.get().getInjury() > 0) {
      applyActionPause(Itr.DEFAULT_DAMAGE_PAUSE);
    }
    if (frame.cpoint.dircontrol && controller.pressX() && (controller.press_L() == faceRight)) {
      faceRight ^= true;
    }
    if (frame.cpoint.tAction != Action.UNASSIGNED && controller.press_a() && controller.pressX()) {
      return frame.cpoint.tAction;
    }
    if (frame.cpoint.aAction != Action.UNASSIGNED && controller.press_a()) {
      return frame.cpoint.aAction;
    }
    return Action.UNASSIGNED;
  }

  private Action moveGrabbed() {
    Optional<SyncGrab> osg = SyncGrab.getVictim(this);
    if (osg.isEmpty()) {
      return Action.DEFAULT;
    }
    SyncGrab sync = osg.get();
    transitGoto(sync.getVictimAction());
    Vector velocity = sync.getThrowVelocity();
    if (velocity == Vector.ZERO) {
      int injury = sync.getInjury();
      if (injury > 0) {
        hpLost(injury);
        applyActionPause(Itr.DEFAULT_DAMAGE_PAUSE);
      } else {
        hpLost(-injury);
      }
      faceRight = sync.updateVictim(frame.cpoint);
      return Action.CONTROLLED;
    } else {
      vx = velocity.x();
      vy = velocity.y();
      vz = velocity.z();
      // buff.put(Effect.THROWN_ATTACK, cpoint.injury);
      return Action.UNASSIGNED;
    }
  }

  private Action dashableLanding() {
    vy = 0.0;
    if (controller.press_j()) {
      if (controller.pressX()) {
        faceRight = controller.press_R();
        return Action.HERO_DASH;
      }
      if (Math.abs(vx) > 0.1) {
        return isSameFacingVelocity() ? Action.HERO_DASH : Action.HERO_DASH_REVERSE;
      } else {
        return Action.HERO_JUMP;
      }
    }
    if (controller.pressWalk() && !isFirstTimeunit()) {
      return Action.HERO_WALKING.shift(walkingFrameCounter.reset());
    } else if (controller.press_d()) {
      return Action.HERO_ROLLING;
    } else {
      return Action.HERO_CROUCH1;
    }
  }

  private Action knockOutLanding(boolean reboundable, boolean facingUp, double damage) {
    if (reboundable && (vx > 10.0 || vx < -10.0 || vy > 1.0)) {
      vy *= -0.333;  // no reference
      hpLost(damage);
      return facingUp ? Action.HERO_FACEUP_FALLR : Action.HERO_FACEDOWN_FALLR;
    } else {
      vy = 0.0;
      return facingUp ? Action.HERO_FACEUP_LYING : Action.HERO_FACEDOWN_LYING;
    }
  }

  @Override
  protected Action updateKinetic() {
    if (SyncGrab.getVictim(this).isPresent()) {
      return Action.UNASSIGNED;
    }
    vx = frame.calcVx(vx, faceRight);
    vy = frame.calcVy(vy);
    vz = frame.calcVz(vz, controller.valueZ());

    if (buff.getOrDefault(Effect.MOVE_BLOCKING, 0) < terrain.getTimestamp()) {
      px += vx;
      pz += vz;
    }
    boolean beforeOnGround = py >= 0.0;
    py += vy;
    boolean afterOnGround = py >= 0.0;
    if (beforeOnGround && afterOnGround) {
      py = 0.0;
      vx = terrain.applyFriction(vx);
      vz = terrain.applyFriction(vz);
      return Action.UNASSIGNED;
    }

    if (!afterOnGround) {
      vy += terrain.getGravity();
      return Action.UNASSIGNED;
    }

    py = 0.0;
    vx = terrain.applyLandingFriction(vx);
    vz = terrain.applyLandingFriction(vz);

    Integer storedValue = buff.remove(Effect.THROWN_ATTACK);
    if (storedValue != null) {
      hp -= storedValue.intValue();
    }

    return switch (frame.state) {
      case FALL -> {
        boolean reboundable = frame.curr != Action.HERO_FACEUP_FALLR.index
                           && frame.curr != Action.HERO_FACEDOWN_FALLR.index;
        boolean facingUp = Action.HERO_FACEUP_FALL.contains(frame.curr);
        yield knockOutLanding(reboundable, facingUp, 0.0);
      }
      case FIRE -> {
        yield knockOutLanding(true, true, 0.0);
      }
      case ICE -> {
        if (hp <= 0.0) {
          // TODO: not break for small fall
          yield Action.UNASSIGNED;
        }
        yield knockOutLanding(true, true, ICED_FALLDOWN_DAMAGE);
      }
      case JUMP, FLIP -> {
        logger.log(Level.INFO, "landing by HERO_CROUCH1 {0}", frame);
        yield dashableLanding();
      }
      default -> {
        vy = 0.0;
        logger.log(Level.INFO, "landing by {0}", frame);
        yield Action.HERO_CROUCH2;
      }
    };
  }

  @Override
  protected Action updateStamina() {
    if (hp <= 0.0) {
      hp = 0.0;
      if (frame.curr == Action.HERO_FACEDOWN_LYING.index) {
        return Action.HERO_FACEDOWN_LYING;
      } else if (frame.curr == Action.HERO_FACEUP_LYING.index) {
        return Action.HERO_FACEUP_LYING;
      } else {
        return Action.UNASSIGNED;
      }
    }
    // http://lf2.wikia.com/wiki/Health_and_mana
    double bonus = hpMax > hp ? (hpMax - hp) / 300.0 : 0.0;
    mp = Math.min(mpMax, mp + mpReg + bonus);
    hp = Math.min(hp2nd, hp + hpReg);
    // TODO: check if needs condition actPause < 0
    defendPoint = defendPoint > 0 ? defendPoint - 1 : defendPoint;
    fallPoint = fallPoint > 0 ? fallPoint - 1 : defendPoint;
    return Action.UNASSIGNED;
  }

  @Override
  protected boolean fitBoundary() {
    Region boundary = terrain.getHeroBoundary();
    px = Math.min(Math.max(px, boundary.x1()), boundary.x2());
    pz = Math.min(Math.max(pz, boundary.z1()), boundary.z2());
    return true;
  }

  @Override
  protected List<Tuple<Bdy, Region>> computeBdyList() {
    Optional<SyncGrab> osg = SyncGrab.getVictim(this);
    if (osg.isEmpty() || osg.get().isVictimHurtable()) {
      return super.computeBdyList();
    } else {
      return List.of();
    }
  }

  @Override
  protected List<Tuple<Itr, Region>> computeItrList() {
    List<Tuple<Itr, Region>> itrList = super.computeItrList();
    // itrList.addAll(weapon.getStrengthItrs(frame.wpoint.usage));
    // logger.log(Level.INFO, "Implement weapon strength.");
    return itrList;
  }

  @Override
  public void sendItr(Observable target, Itr itr) {
    switch (itr.kind) {
      case PUNCH:
      case STAB:
      case FIRE:
      case WEAK_FIRE:
      case ICE:
      case WEAK_ICE:
      case THROWN_DAMAGE:
        applyActionPause(((Itr.Damage) itr.param).actPause());
        return;
      case GRAB_DOP:
      case GRAB_BDY:
        receivedItrAction = ((Itr.Grab) itr.param).caughtingact();
        return;
      case PICK:
        if (target instanceof Weapon x) {
          logger.log(Level.INFO, "{0} successfully picked {1}.", this, x);
          // In the original LF2 you always perform picking action
          // no matter the competition successed or failed.  It is different here.
          receivedItrAction = x.isHeavy() ? Action.HERO_PICK_HEAVY : Action.HERO_PICK_LIGHT;
          return;
        }
        break;
      case ROLL_PICK:
        if (target instanceof Weapon x) {
          logger.log(Level.INFO, "{0} successfully picked {1}.", this, x);
          return;
        }
        break;
      case FORCE_ACTION:
      case SONATA:
      case BLOCK:
      case VORTEX:
        return;
      case WEAPON_STRENGTH:
      case SHIELD:
      case HEAL:
        break;
    }
    logger.log(Level.WARNING, "{0} sent unexpected: {1}", this, itr);
    return;
  }

  private Action reactDamage(boolean sourceFaceRight, Itr.Damage x) {
    applyActionPause(x.actPause());
    double dvx = x.calcDvx(sourceFaceRight);
    boolean face2face = faceRight != sourceFaceRight;
    if (frame.state == State.DEFEND && face2face) {
      hpLost(x.injury() * DEFEND_INJURY_REDUCTION);
      vx += dvx * DEFEND_DVX_REDUCTION;
      defendPoint += x.bdefend();
      if (hp <= 0.0) {
        return Action.HERO_FACEUP_FALL;
      } else if (defendPoint > 30) {
        return Action.HERO_BROKEN_DEF;
      } else if (frame.curr == Action.HERO_DEFEND.index) {
        return Action.HERO_DEFEND_HIT;
      }
    }

    hpLost(x.injury());
    fallPoint = (fallPoint + x.fall() + 19) / 20 * 20;

    if (frame.state == State.GRABBED && fallPoint <= 60) {
      Optional<SyncGrab> osg = SyncGrab.getVictim(this);
      if (osg.isPresent()) {
        return face2face ? frame.cpoint.frontHurtAction : frame.cpoint.backHurtAction;
      }
    }

    vx += dvx;
    defendPoint = Math.max(45, defendPoint + x.bdefend());
    if (frame.state == State.ICE || fallPoint > 60 || (fallPoint > 20 && py < 0.0) || hp <= 0.0) {
      vy += x.dvy();
      return face2face ? Action.HERO_FACEUP_FALL : Action.HERO_FACEDOWN_FALL;
    } else if (fallPoint > 40) {
      return Action.HERO_DOP;
    } else if (fallPoint > 20) {
      return face2face ? Action.HERO_INJURE3 : Action.HERO_INJURE2;
    } else if (fallPoint >= 0) {
      return Action.HERO_INJURE1;
    } else {
      // Negative fall causes nothing.
      return Action.UNASSIGNED;
    }
  }

  @Override
  public boolean receiveItr(Observable source, Itr itr, Region absoluteRegion) {
    switch (itr.kind) {
      case PUNCH:
      case STAB:
      case SHIELD:
      case THROWN_DAMAGE:
        receivedItrAction = reactDamage(source.isFaceRight(), (Itr.Damage) itr.param);
        return true;
      case WEAK_FIRE:
        if (frame.state == State.FIRE) {
          return false;
        }
      case FIRE:
        receivedItrAction = reactDamage(source.isFaceRight(), (Itr.Damage) itr.param);
        return true;
      case WEAK_ICE:
        if (frame.state == State.ICE) {
          return false;
        }
      case ICE:
        receivedItrAction = reactDamage(source.isFaceRight(), (Itr.Damage) itr.param);
        return true;
      case GRAB_DOP:
        if (frame.state != State.DANCE_OF_PAIN) {
          return false;
        }
      case GRAB_BDY:
        if (SyncGrab.tryRegister(source, this, terrain.getTimestamp())) {
          defendPoint = fallPoint = 0;
          receivedItrAction = ((Itr.Grab) itr.param).caughtact();
          return true;
        } else {
          return false;
        }
      case ROLL_PICK:
      case PICK:
      case WEAPON_STRENGTH:
        break;
      case FORCE_ACTION:
        buff.put(Effect.FORCE_SUPER_PUNCH, terrain.getTimestamp() + 1);
        return true;
      case HEAL:
        buff.put(Effect.HEALING, terrain.getTimestamp() + 100);
        return true;
      case BLOCK:
        buff.put(Effect.MOVE_BLOCKING, terrain.getTimestamp() + 1);
        return true;
      case SONATA:
      case VORTEX:
        logger.log(Level.INFO, "NotImplemented: {0}", itr);
        return true;
    }
    logger.log(Level.WARNING, "{0} received unexpected: {1}", this, itr);
    return false;
  }

  @Override
  public String getPortraitPath() {
    return portrait;
  }

}
