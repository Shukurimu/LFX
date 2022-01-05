package object;

import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Map;

import base.Controller;
import base.KeyOrder;
import base.Region;
import base.Scope;
import base.Type;
import component.Action;
import component.Cost;
import component.Cpoint;
import component.Effect;
import component.Frame;
import component.Itr;
import component.State;
import util.Tuple;
import util.Vector;

public class BaseHero extends AbstractObject implements Hero {
  private static final System.Logger logger = System.getLogger("");
  protected static final int DEFAULT_ITR_SCOPE =
      Scope.ENEMY_HERO | Scope.ALL_WEAPON | Scope.ALL_ENERGY;

  private final HiddenFrameCounter walkingFrameCounter = HiddenFrameCounter.forWalking(3);
  private final HiddenFrameCounter runningFrameCounter = HiddenFrameCounter.forRunning(3);
  /**
   * Interactions are asynchronous, so we have to store them for later use.
   */
  protected Action sentItrAction = Action.UNASSIGNED;
  protected Action receivedItrAction = Action.UNASSIGNED;

  private final String portrait;
  private Controller controller = Controller.NULL_CONTROLLER;
  private Weapon weapon = NullObject.WEAPON;
  private Observable grabbedBy = NullObject.DUMMY;
  private int grabbedTimestamp = 0;
  private Cpoint latestCpoint = null;

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

    jumpSpeed = Vector.of(stamina.get("jump_height").doubleValue(),
                          stamina.get("jump_distance").doubleValue(),
                          stamina.get("jump_distancez").doubleValue());
    dashSpeed = Vector.of(stamina.get("dash_height").doubleValue(),
                          stamina.get("dash_distance").doubleValue(),
                          stamina.get("dash_distancez").doubleValue());
    flipSpeed = Vector.of(stamina.get("rowing_height").doubleValue(), 0.0,
                          stamina.get("rowing_distance").doubleValue());
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
  public void setCpoint(Cpoint cpoint) {
    latestCpoint = cpoint;
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
  protected void transitFrame(Action action) {
    Action orderAction = frame.combo.getOrDefault(controller.getKeyOrder(), action);
    Cost cost = frameList.get(orderAction.index).cost;
    if (cost == Cost.FREE || env.isUnlimitedMode()) {
      super.transitFrame(orderAction);
    } else if (mp >= cost.mp() && hp >= cost.hp()) {
      mp -= cost.mp();
      hp -= cost.hp();
      super.transitFrame(orderAction);
    } else {
      super.transitFrame(action);
    }
    return;
  }

  @Override
  protected void transitNextFrame() {
    // dash kinetic energy
    boolean fromNonDashState = frame.state == State.DASH;
    if (fromNonDashState && isActionFirstTimeunit()) {
      if (frame.curr == Action.HERO_DASH.index) {
        vx = faceRight ? dashSpeed.x() : -dashSpeed.x();
      } else if (frame.curr == Action.HERO_DASH_REVERSE.index) {
        vx = faceRight ? -dashSpeed.x() : dashSpeed.x();
      } else {
        return;
      }
      vy += dashSpeed.y();
      vz = controller.valueZ() * dashSpeed.z();
    }
    Cost cost = frameList.get(frame.next.index).cost;
    if (cost.mp() >= 0 || env.isUnlimitedMode()) {
      super.transitFrame(frame.next);
      return;
    }
    // If spare action is unspecified, one still goes to `next` and runs out of mp.
    Action spareAction = frame.combo.getOrDefault(KeyOrder.hit_d, Action.UNASSIGNED);
    if (spareAction == Action.UNASSIGNED || mp + cost.mp() >= 0) {
      mp = mp + cost.mp();
      super.transitFrame(frame.next);
    } else {
      super.transitFrame(spareAction);
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

  @Override
  public void run(int timestamp, List<Observable> allObjects) {
    if (receivedItrAction != Action.UNASSIGNED) {
      transitFrame(receivedItrAction);
    } else if (sentItrAction != Action.UNASSIGNED) {
      transitFrame(sentItrAction);
    }
    receivedItrAction = sentItrAction = Action.UNASSIGNED;
    super.run(timestamp, allObjects);
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
      case NORMAL, DEFEND, ICE -> Action.UNASSIGNED;
      default -> {
        logger.log(Level.WARNING, "Unexpected State.%s", frame.state);
        yield Action.UNASSIGNED;
      }
    };
    // Special case: you can change facing in this action number.
    if (frame.curr == Action.HERO_DEFEND.index) {
      faceRight = controller.getFacing(faceRight);
    }
    // TODO: general combo
    return nextAct;
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
    } else {
      return buff.containsKey(Effect.FORCE_SUPER_PUNCH) ? Action.HERO_SUPER_PUNCH
          : random.nextBoolean() ? Action.HERO_PUNCH1 : Action.HERO_PUNCH2;
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
    // jump kinetic energy; jumpAttack is followed by Action.HERO_JUMP_AIR.
    if (frame.next.index == Action.HERO_JUMP_AIR.index) {
      // dvx is applied after friction reduction.
      vx += controller.valueX() * (faceRight ? jumpSpeed.x() : -jumpSpeed.x());
      vy += jumpSpeed.y();
      vz = controller.valueZ() * jumpSpeed.z();
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

  private Action moveDash() {
    if (frame.curr == Action.HERO_DASH_REVERSE.index) {
      if (controller.reverseFacing(faceRight)) {
        return Action.HERO_DASH;
      }
    }
    if (frame.curr != Action.HERO_DASH.index) {
      logger.log(Level.WARNING, "Unexpected dash %s", frame);
    }
    if (controller.reverseFacing(faceRight)) {
      return Action.HERO_DASH_REVERSE;
    }
    if (controller.press_a()) {
      if (weapon.isLight()) {
        return Action.HERO_DASH_WEAPON_ATK;
      } else if (weapon.isSmall() || weapon.isDrink()) {
        return Action.HERO_SKY_WEAPON_THROW;
      } else {
        return Action.HERO_DASH_ATK;
      }
    }
    return Action.UNASSIGNED;
  }

  private Action moveFall() {
    if (Action.HERO_BACKWARD_FALL.contains(frame.curr)) {
      if (Action.HERO_BACKWARD_FALLR.index == frame.curr) {
        // Can do nothing.
      } else if (vy < -10.0) {
        return Action.HERO_BACKWARD_FALL1;
      } else if (vy < 0.0) {
        return Action.HERO_BACKWARD_FALL2;
      } else if (vy < 6.0) {
        // TODO: Check buff.containsKey(Effect.SONATA)
        return controller.press_j() ? Action.HERO_FLIP2 : Action.HERO_BACKWARD_FALL3;
      } else {
        return Action.HERO_BACKWARD_FALL4;
      }
    } else {
      if (Action.HERO_FORWARD_FALLR.index == frame.curr) {
        // Can do nothing.
      } else if (vy < -10.0) {
        return Action.HERO_FORWARD_FALL1;
      } else if (vy < 0.0) {
        return Action.HERO_FORWARD_FALL2;
      } else if (vy < 6.0) {
        return controller.press_j() ? Action.HERO_FLIP1 : Action.HERO_FORWARD_FALL3;
      } else {
        return Action.HERO_FORWARD_FALL4;
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
    return hp > 0.0 ? Action.UNASSIGNED : Action.REPEAT;
  }

  private Action moveGrabbed() {
    if (grabbedBy == NullObject.DUMMY) {
      return buff.containsKey(Effect.THROWN_ATTACK) ? Action.DEFAULT : Action.UNASSIGNED;
    }
    if (latestCpoint.velocity != Vector.ZERO) {
      Vector velocity = grabbedBy.getAbsoluteVelocity(latestCpoint.velocity);
      vx = velocity.x();
      vy = velocity.y();
      vz = velocity.z();
      buff.put(Effect.THROWN_ATTACK, latestCpoint.injury);
      return Action.HERO_FORWARD_FALL2;
    }
    if (latestCpoint.injury > 0) {
      hp -= latestCpoint.injury;
      applyActionPause(Itr.DEFAULT_DAMAGE_PAUSE);
    } else {
      hp += latestCpoint.injury;
    }
    faceRight = grabbedBy.isFaceRight() ^ latestCpoint.opposideFacing;
    setRelativePosition(grabbedBy.getRelativePosition(latestCpoint),
                        frame.cpoint, latestCpoint.cover);
    return latestCpoint.vAction;
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

  private Action knockOffLanding(boolean reboundable, boolean faceForward, double damage) {
    if (reboundable && (vx > 10.0 || vx < -10.0 || vy > 1.0)) {
      vy = FALLING_BOUNCE_VY;
      hpLost(damage);
      return faceForward ? Action.HERO_FORWARD_FALLR : Action.HERO_BACKWARD_FALLR;
    } else {
      vy = 0.0;
      return faceForward ? Action.HERO_LYING1 : Action.HERO_LYING2;
    }
  }

  @Override
  protected Action updateKinetic() {

    if (frame.dvx == Frame.RESET_VELOCITY) {
      vx = 0.0;
    } else {
      vx = frame.calcVX(vx, faceRight);
    }
    if (frame.dvy == Frame.RESET_VELOCITY) {
      vy = 0.0;
    } else {
      // In LF2 even the frame with dvy = -1 causes the character flying for a while,
      // so dvy takes effect before the calculation of gravity.
      vy += frame.dvy;
    }
    if (frame.dvz == Frame.RESET_VELOCITY) {
      vz = 0.0;
    } else {
      vz += controller.valueZ() * frame.dvz;
    }
    if (!buff.containsKey(Effect.MOVE_BLOCKING)) {
      px += vx;
      pz += vz;
    }
    if (py >= 0.0) { // Already on the ground.
      py = 0.0;
      vx = env.applyFriction(vx * LANDING_VELOCITY_REMAIN);
      vz = env.applyFriction(vz * LANDING_VELOCITY_REMAIN);
      return Action.UNASSIGNED;
    }

    py += vy;
    if (py < 0.0) { // Still flying.
      vy = env.applyGravity(vy);
      return Action.UNASSIGNED;
    }

    py = 0.0; // Just landing.

    Integer storedValue = buff.remove(Effect.THROWN_ATTACK);
    if (storedValue != null) {
      hp -= storedValue.intValue();
    }

    return switch (frame.state) {
      case FALL -> {
        boolean reboundable = frame.curr != Action.HERO_FORWARD_FALLR.index
            && frame.curr != Action.HERO_FORWARD_FALL1.index;
        boolean faceForward = frame.curr <= Action.HERO_FORWARD_FALLR.index
            && frame.curr >= Action.HERO_FORWARD_FALL1.index;
        yield knockOffLanding(reboundable, faceForward, 0.0);
      }
      case FIRE -> {
        yield knockOffLanding(true, false, 0.0);
      }
      case ICE -> {
        if (hp <= 0.0) {
          // TODO: not break for small fall
          yield Action.UNASSIGNED;
        }
        yield knockOffLanding(true, false, ICED_FALLDOWN_DAMAGE);
      }
      case JUMP, FLIP -> {
        logger.log(Level.INFO, "landing by HERO_CROUCH1 %d%n", frame);
        yield dashableLanding();
      }
      default -> {
        logger.log(Level.INFO, "landing by %s%n", frame);
        yield Action.HERO_CROUCH2;
      }
    };
  }

  @Override
  protected Action updateStamina() {
    if (hp <= 0.0 && py >= 0.0) {
      return vx >= 0.0 ? Action.HERO_LYING1 : Action.HERO_LYING2;
    }
    // http://lf2.wikia.com/wiki/Health_and_mana
    double bonus = hpMax > hp ? (hpMax - hp) / 300.0 : 0.0;
    hp = Math.min(hp2nd, hp + hpReg);
    mp = Math.min(mpMax, mp + mpReg + bonus);
    // TODO: check if needs condition actPause < 0
    defendPoint = defendPoint > 0 ? defendPoint - 1 : defendPoint;
    fallPoint = fallPoint > 0 ? fallPoint - 1 : defendPoint;
    return Action.UNASSIGNED;
  }

  @Override
  protected boolean fitBoundary() {
    Region boundary = env.getHeroBoundary();
    px = Math.min(Math.max(px, boundary.x1()), boundary.x2());
    pz = Math.min(Math.max(pz, boundary.z1()), boundary.z2());
    return true;
  }

  @Override
  protected List<Tuple<Itr, Region>> computeItrList() {
    List<Tuple<Itr, Region>> itrList = super.computeItrList();
    // itrList.addAll(weapon.getStrengthItrs(frame.wpoint.usage));
    logger.log(Level.INFO, "Implement weapon strength.");
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
        if (grabbingHero == target) {
          receivedItrAction = ((Itr.Grab) itr.param).caughtingact();
          logger.log(Level.WARNING, "NotImplemented %s", itr);
        } else {
          logger.log(Level.WARNING, "Grab mismatched: %s <> %s", grabbingHero, target);
        }
        return;
      case PICK:
        if (target instanceof Weapon x) {
          weapon = x;
          logger.log(Level.TRACE, "%s successfully picked %s.", this, x);
          // In the original LF2 you always perform picking action
          // no matter the competition successed or failed.  It is different here.
          receivedItrAction = x.isHeavy() ? Action.HERO_PICK_HEAVY : Action.HERO_PICK_LIGHT;
          return;
        }
        break;
      case ROLL_PICK:
        if (target instanceof Weapon x) {
          weapon = x;
          logger.log(Level.TRACE, "%s successfully picked %s.", this, x);
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
    logger.log(Level.WARNING, "%s sent unexpected: %s", this, itr);
    return;
  }

  private Action reactDamage(Itr.Damage x) {
    applyActionPause(x.actPause());
    double dvx = 0; // itr.calcDvx(px, tuple.first.isFaceRight());
    boolean face2face = faceRight == (dvx < 0.0);
    if (frame.state == State.DEFEND && face2face) {
      // hpLost(itr.injury * DEFEND_INJURY_REDUCTION, false);
      vx += dvx * DEFEND_DVX_REDUCTION;
      // dp += itr.bdefend;
      if (hp <= 0.0) {
        return Action.HERO_FORWARD_FALL1;
      } else if (defendPoint > 30) {
        return Action.HERO_BROKEN_DEF;
      } else if (frame.curr == Action.HERO_DEFEND.index) {
        return Action.HERO_DEFEND_HIT;
      }
    } else {
      // hpLost(itr.injury, false);
      vx += dvx;
      // dp = Math.max(dp + itr.bdefend, 45);
      // fp = (fp + itr.fall + 19) / 20 * 20;
      if (frame.state == State.ICE || fallPoint > 60 || (fallPoint > 20 && py < 0.0) || hp <= 0.0) {
        // vy += itr.dvy;
        return face2face ? Action.HERO_BACKWARD_FALL1 : Action.HERO_FORWARD_FALL1;
      } else if (fallPoint > 40) {
        return Action.HERO_DOP;
      } else if (fallPoint > 20) {
        return face2face ? Action.HERO_INJURE3 : Action.HERO_INJURE2;
      } else if (fallPoint >= 0) {
        return Action.HERO_INJURE1;
      } else {
        // Negative fp causes nothing.
      }
    }
    return Action.UNASSIGNED;
  }

  /**
   * Deals with the race condition on grabbing.
   *
   * @param actor the object performs the grab action
   * @return true if successed
   */
  protected synchronized boolean checkBeingGrabbed(Observable actor) {
    if (grabbedTimestamp < env.getTimestamp()) {
      grabbedBy = actor;
      // TODO: release weapon and grabbingHero
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean receiveItr(Observable source, Itr itr, Region absoluteRegion) {
    switch (itr.kind) {
      case PUNCH:
      case STAB:
      case SHIELD:
      case THROWN_DAMAGE:
        reactDamage((Itr.Damage) itr.param);
        return true;
      case WEAK_FIRE:
        if (frame.state == State.FIRE) {
          return false;
        }
      case FIRE:
        reactDamage((Itr.Damage) itr.param);
        return true;
      case WEAK_ICE:
        if (frame.state == State.ICE) {
          return false;
        }
      case ICE:
        reactDamage((Itr.Damage) itr.param);
        return true;
      case GRAB_DOP:
      case GRAB_BDY:
        if (checkBeingGrabbed(source)) {
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
        buff.put(Effect.FORCE_SUPER_PUNCH, 0);
        return true;
      case HEAL:
        buff.put(Effect.HEALING, env.getTimestamp() + 100);
        return true;
      case BLOCK:
        buff.put(Effect.MOVE_BLOCKING, 0);
        return true;
      case SONATA:
      case VORTEX:
        logger.log(Level.INFO, "NotImplemented: %s", itr);
        return true;
    }
    logger.log(Level.WARNING, "%s received unexpected: %s", this, itr);
    return false;
  }

  @Override
  public String getPortrait() {
    return portrait;
  }

}
