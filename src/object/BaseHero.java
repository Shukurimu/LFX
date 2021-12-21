package object;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import base.Controller;
import base.KeyOrder;
import base.Region;
import base.Type;
import component.Action;
import component.Cost;
import component.Effect;
import component.Frame;
import component.Itr;
import component.State;
import component.Wpoint;
import util.Util;
import util.Tuple;
import util.Vector;

public class BaseHero extends AbstractObject implements Hero {
  private static final System.Logger logger = System.getLogger("");

  private final HiddenFrameCounter walkingFrameCounter = HiddenFrameCounter.forWalking();
  private final HiddenFrameCounter runningFrameCounter = HiddenFrameCounter.forRunning();
  /**
   * Interactions are asynchronous, so we have to store them for later use.
   */
  protected final List<Tuple<Observable, Itr>> sendItrList = new ArrayList<>();
  protected final List<Tuple<Observable, Itr>> recvItrList = new ArrayList<>();

  private final String portrait;
  private Controller controller = Controller.NULL_CONTROLLER;
  private Weapon weapon = null;

  // initialization block
  private double Value_walking_speed;
  private double Value_walking_speedz;
  private double Value_walking_speedx;
  private double Value_running_speed;
  private double Value_running_speedz;
  private double Value_running_speedx;
  private double Value_heavy_walking_speed;
  private double Value_heavy_walking_speedz;
  private double Value_heavy_walking_speedx;
  private double Value_heavy_running_speed;
  private double Value_heavy_running_speedz;
  private double Value_heavy_running_speedx;
  private double Value_jump_height;
  private double Value_jump_distance;
  private double Value_jump_distancez;
  private double Value_dash_height;
  private double Value_dash_distance;
  private double Value_dash_distancez;
  private double Value_rowing_height;
  private double Value_rowing_distance;
  private double hp2nd = 500.0;
  private double hpReg = 1.0 / 12.0;
  private double mpReg = 1.0 / 3.00;
  private int defendPoint = 0;
  private int fallPoint = 0;

  protected BaseHero(String identifier, List<Frame> frameList, String portrait) {
    super(identifier, Type.HERO, frameList);
    this.portrait = portrait;
  }

  private BaseHero(BaseHero base) {
    super(base);
    portrait = base.portrait;
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
  public Wpoint getWpoint() {
    return frame.wpoint;
  }

  @Override
  protected Action getDefaultAction() {
    return py < 0.0 ? Action.HERO_JUMPAIR : weapon.isHeavy() ? Action.HERO_HEAVY_WALK : Action.HERO_STANDING;
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
      if (frame.curr == Action.HERO_DASH1.index) {
        vx = faceRight ? Value_dash_distance : -Value_dash_distance;
      } else if (frame.curr == Action.HERO_DASH2.index) {
        vx = faceRight ? -Value_dash_distance : Value_dash_distance;
      } else {
        return;
      }
      vy += Value_dash_height;
      vz = controller.valueZ() * Value_dash_distancez;
    }
    Cost cost = frameList.get(frame.next.index).cost;
    if (cost == Cost.FREE || env.isUnlimitedMode()) {
      super.transitFrame(frame.next);
      return;
    }
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
   * Most of the time, potential HP is reduced by one-third of the received
   * damage.
   * Use super.hpLost() in the situations not following this rule (e.g.,
   * throwinjury).
   */
  @Override
  protected void hpLost(double injury) {
    hp -= injury;
    hp2nd -= Math.floor(injury / 3.0);
    return;
  }

  @Override
  public Vector getStamina() {
    return new Vector(hp2nd / hpMax, hp / hpMax, mp / mpMax);
  }

  @Override
  public boolean tryGrab(Observable actor) {
    return false;
  }

  public Action react() {
    Action nextAct = Action.UNASSIGNED;

    // for (Tuple<Observable, Itr> tuple : sendItrList) {
    // Itr itr = tuple.second;
    // if (itr.kind.damage) {
    // actPause = itr.calcPause(actPause);
    // } else if (itr.kind == component.Kind.PICK) {
    // // You always perform the action even if picking failed.
    // nextAct = target.isHeavy() ? Action.HERO_PICK_HEAVY : Action.HERO_PICK_LIGHT;
    // } else if (itr.kind == component.Kind.ROLL_PICK) {
    // confirmPicking(tuple.first);
    // } else if (itr.kind == component.Kind.GRAB_DOP || itr.kind ==
    // component.Kind.GRAB_BDY) {
    // // TODO: GRAB
    // }
    // }
    sendItrList.clear();

    for (Tuple<Observable, Itr> tuple : recvItrList) {
      // Itr itr = tuple.second;
      // switch (itr.kind) {
      // case FORCE_ACT:
      // buff.put(Effect.FORCE_ACT, Effect.FORCE_ACT.of());
      // break;
      // case BLOCK:
      // buff.put(Effect.MOVE_BLOCKING, Effect.MOVE_BLOCKING.of());
      // break;
      // case HEAL:
      // buff.put(Effect.HEALING, Effect.HEALING.of());
      // break;
      // default:
      // System.out.println("NotImplemented Effect: ");
      // }
      // if (!itr.kind.damage) {
      // continue;
      // }
      // itr.calcPause(actPause);
      logger.log(Level.TRACE, tuple);
      double dvx = 0; // itr.calcDvx(px, tuple.first.isFaceRight());
      boolean face2face = faceRight == (dvx < 0.0);
      if (frame.state == State.DEFEND && face2face) {
        // hpLost(itr.injury * DEFEND_INJURY_REDUCTION, false);
        vx += dvx * DEFEND_DVX_REDUCTION;
        // dp += itr.bdefend;
        if (hp <= 0.0) {
          nextAct = Action.HERO_FORWARD_FALL1;
        } else if (defendPoint > 30) {
          transitFrame(Action.HERO_BROKEN_DEF);
        } else if (frame.curr == Action.HERO_DEFEND.index) {
          transitFrame(Action.HERO_DEFEND_HIT);
        }
      } else {
        // hpLost(itr.injury, false);
        vx += dvx;
        // dp = Math.max(dp + itr.bdefend, 45);
        // fp = (fp + itr.fall + 19) / 20 * 20;
        if (frame.state == State.ICE || fallPoint > 60 || (fallPoint > 20 && py < 0.0) || hp <= 0.0) {
          // vy += itr.dvy;
          nextAct = face2face ? Action.HERO_BACKWARD_FALL1 : Action.HERO_FORWARD_FALL1;
        } else if (fallPoint > 40) {
          nextAct = Action.HERO_DOP;
        } else if (fallPoint > 20) {
          nextAct = face2face ? Action.HERO_INJURE3 : Action.HERO_INJURE2;
        } else if (fallPoint >= 0) {
          nextAct = Action.HERO_INJURE1;
        } else {
          // Negative fp causes nothing.
        }
      }
    }
    recvItrList.clear();
    return nextAct;
  }

  @Override
  protected Action updateByState() {
    controller.update();
    Action nextAct = switch (frame.state) {
      case STAND -> moveStand();
      case WALK -> moveWalk();
      case HEAVY_WALK -> moveHeavyWalk();
      case RUN -> moveRun();
      case HEAVY_RUN -> moveHeavyRun();
      case JUMP -> moveJump();
      case DASH -> moveDash();
      case FLIP -> moveFlip();
      case DRINK -> moveDrink();
      case FALL -> moveFall();
      case FIRE -> moveFire();
      case LYING -> moveLying();
      case NORMAL, DEFEND, ICE, GRAB, GRABBED -> Action.UNASSIGNED;
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

  private void releaseUnexpectedWeapon() {
    logger.log(Level.WARNING, "Unknown Weapon %s at %s", weapon, frame);
    weapon.release();
    weapon = null;
  }

  private Action moveStandWalkPressA() {
    if (weapon == null) {
      return buff.containsKey(Effect.FORCE_SUPER_PUNCH) ? Action.HERO_SUPER_PUNCH
          : Util.randomBool() ? Action.HERO_PUNCH1 : Action.HERO_PUNCH2;
    } else if (weapon.isLight()) {
      return Util.randomBool() ? Action.HERO_WEAPON_ATK1 : Action.HERO_WEAPON_ATK2;
    } else if (weapon.isSmall()) {
      return Action.HERO_LIGHT_WEAPON_THROW;
    } else if (weapon.isDrink()) {
      return Action.HERO_DRINK;
    } else {
      releaseUnexpectedWeapon();
    }
    return Action.UNASSIGNED;
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
      return Action.HERO_RUNNING.shifts(runningFrameCounter.reset());
    } else if (controller.pressWalk()) {
      faceRight = controller.getFacing(faceRight);
      return Action.HERO_WALKING.shifts(walkingFrameCounter.reset());
    }
    return Action.UNASSIGNED;
  }

  private Action moveWalk() {
    if (controller.pressZ()) {
      px += controller.valueX() * Value_walking_speedx;
      pz += controller.valueZ() * Value_walking_speedz;
    } else {
      px += controller.valueX() * Value_walking_speed;
    }
    if (controller.press_a()) {
      return moveStandWalkPressA();
    } else if (controller.press_j()) {
      return Action.HERO_JUMP;
    } else if (controller.press_d()) {
      return Action.HERO_DEFEND;
    } else if (controller.pressRun()) {
      faceRight = controller.getFacing(faceRight);
      return Action.HERO_RUNNING.shifts(runningFrameCounter.reset());
    } else if (controller.pressWalk()) {
      faceRight = controller.getFacing(faceRight);
      if (isLastTimeunit()) {
        return Action.HERO_WALKING.shifts(walkingFrameCounter.next());
      }
    }
    return Action.UNASSIGNED;
  }

  private Action moveHeavyWalk() {
    if (controller.pressZ()) {
      px += controller.valueX() * Value_heavy_walking_speedx;
      pz += controller.valueZ() * Value_heavy_walking_speedz;
    } else {
      px += controller.valueX() * Value_heavy_walking_speed;
    }
    if (weapon == null || !weapon.isHeavy()) {
      releaseUnexpectedWeapon();
      return Action.DEFAULT;
    }
    if (controller.press_a()) {
      return Action.HERO_HEAVY_WEAPON_THROW;
    } else if (controller.pressRun()) {
      faceRight = controller.getFacing(faceRight);
      return Action.HERO_HEAVY_RUN.shifts(runningFrameCounter.reset());
    } else if (controller.pressWalk()) {
      faceRight = controller.getFacing(faceRight);
      if (isLastTimeunit()) {
        return Action.HERO_HEAVY_WALK.shifts(walkingFrameCounter.next());
      }
    }
    return Action.UNASSIGNED;
  }

  private Action moveRun() {
    if (controller.pressZ()) {
      vx = (faceRight ? 1.0 : -1.0) * Value_running_speedx;
      pz += controller.valueZ() * Value_running_speedz;
    } else {
      vx = (faceRight ? 1.0 : -1.0) * Value_running_speed;
    }
    if (controller.press_a()) {
      if (weapon == null) {
        return Action.HERO_RUN_ATK;
      } else if (weapon.isLight()) {
        return controller.pressWalk() ? Action.HERO_LIGHT_WEAPON_THROW : Action.HERO_RUN_WEAPON_ATK;
      } else if (weapon.isSmall()) {
        return Action.HERO_LIGHT_WEAPON_THROW;
      } else if (weapon.isDrink()) {
        return controller.pressWalk() ? Action.HERO_LIGHT_WEAPON_THROW : Action.HERO_DRINK;
      } else {
        releaseUnexpectedWeapon();
      }
    } else if (controller.press_j()) {
      return Action.HERO_DASH1;
    } else if (controller.press_d()) {
      return Action.HERO_ROLLING;
    } else if (controller.reverseFacing(faceRight)) {
      return Action.HERO_STOPRUN;
    } else if (isLastTimeunit()) {
      return Action.HERO_RUNNING.shifts(runningFrameCounter.next());
    }
    return Action.UNASSIGNED;
  }

  private Action moveHeavyRun() {
    if (controller.pressZ()) {
      vx = (faceRight ? 1.0 : -1.0) * Value_heavy_running_speedx;
      pz += controller.valueZ() * Value_heavy_running_speedz;
    } else {
      vx = (faceRight ? 1.0 : -1.0) * Value_heavy_running_speed;
    }
    if (weapon == null || !weapon.isHeavy()) {
      releaseUnexpectedWeapon();
      return Action.DEFAULT;
    }
    if (controller.press_a()) {
      return Action.HERO_HEAVY_WEAPON_THROW;
    } else if (controller.reverseFacing(faceRight)) {
      return Action.HERO_HEAVY_STOP_RUN;
    } else if (isLastTimeunit()) {
      return Action.HERO_HEAVY_RUN.shifts(runningFrameCounter.next());
    }
    return Action.UNASSIGNED;
  }

  private Action moveJump() {
    // jump kinetic energy; jumpAttack is followed by Action.HERO_JUMPAIR.
    if (frame.next.index == Action.HERO_JUMPAIR.index) {
      // dvx is applied after friction reduction.
      vx += controller.valueX() * (faceRight ? Value_jump_distance : -Value_jump_distance);
      vy += Value_jump_height;
      vz = controller.valueZ() * Value_jump_distancez;
    }
    faceRight = controller.getFacing(faceRight);
    if (controller.press_a()) {
      if (weapon == null) {
        return Action.HERO_JUMP_ATK;
      } else if (weapon.isLight()) {
        return controller.pressWalk() ? Action.HERO_SKY_WEAPON_THROW : Action.HERO_JUMP_WEAPON_ATK;
      } else if (weapon.isSmall() || weapon.isDrink()) {
        return Action.HERO_SKY_WEAPON_THROW;
      } else {
        releaseUnexpectedWeapon();
      }
    }
    return Action.UNASSIGNED;
  }

  private Action moveDash() {
    if (frame.curr == Action.HERO_DASH2.index) {
      if (controller.reverseFacing(faceRight)) {
        return Action.HERO_DASH1;
      }
    }
    if (frame.curr != Action.HERO_DASH1.index) {
      logger.log(Level.WARNING, "Unexpected dash %s", frame);
    }
    if (controller.reverseFacing(faceRight)) {
      return Action.HERO_DASH2;
    }
    if (controller.press_a()) {
      if (weapon == null) {
        return Action.HERO_DASH_ATK;
      } else if (weapon.isLight()) {
        return Action.HERO_DASH_WEAPON_ATK;
      } else if (weapon.isSmall() || weapon.isDrink()) {
        return Action.HERO_SKY_WEAPON_THROW;
      } else {
        releaseUnexpectedWeapon();
      }
    }
    return Action.UNASSIGNED;
  }

  private Action moveFall() {
    if (Action.HERO_BACKWARD_FALL.includes(frame.curr)) {
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
    if (weapon != null) {
      Vector regen = weapon.consume();
      mp = Math.min(mpMax, mp + regen.x());
      hp = Math.min(hpMax, hp + regen.y());
      hp2nd = Math.max(hp, hp2nd + regen.z());
    }
    return Action.UNASSIGNED;
  }

  private Action moveFire() {
    return vy < 0.0 ? Action.HERO_UPWARD_FIRE : Action.HERO_DOWNWARD_FIRE;
  }

  private Action moveFlip() {
    if (isActionFirstTimeunit()) {
      vy = Value_rowing_height;
      vx += faceRight ? Value_rowing_distance : -Value_rowing_distance;
    }
    buff.remove(Effect.LANDING_INJURY);
    return Action.UNASSIGNED;
  }

  private Action moveLying() {
    return hp > 0.0 ? Action.UNASSIGNED : Action.REPEAT;
  }

  private Action dashableLanding() {
    vy = 0.0;
    if (controller.press_j()) {
      if (controller.pressX()) {
        faceRight = controller.press_R();
        return Action.HERO_DASH1;
      }
      if (Math.abs(vx) > 0.1) {
        return isSameFacingVelocity() ? Action.HERO_DASH1 : Action.HERO_DASH2;
      } else {
        return Action.HERO_JUMP;
      }
    }
    if (controller.pressWalk() && !isFirstTimeunit()) {
      return Action.HERO_WALKING.shifts(walkingFrameCounter.reset());
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

    if (buff.containsKey(Effect.LANDING_INJURY)) {
      super.hpLost(buff.remove(Effect.LANDING_INJURY));
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
    px = Util.clamp(px, boundary.x1(), boundary.x2());
    pz = Util.clamp(pz, boundary.z1(), boundary.z2());
    return true;
  }

  @Override
  protected List<Tuple<Itr, Region>> computeItrList() {
    List<Tuple<Itr, Region>> itrList = super.computeItrList();
    if (weapon != null) {
      // itrList.addAll(weapon.getStrengthItrs(frame.wpoint.usage));
      logger.log(Level.INFO, "Implement weapon strength.");
    }
    return itrList;
  }

  @Override
  public void sendItr(Observable target, Itr itr) {
    // TODO Auto-generated method stub

  }

  @Override
  public void receiveItr(Observable source, Itr itr, Region absoluteRegion) {
    // TODO Auto-generated method stub

  }

  @Override
  public String getPortrait() {
    return portrait;
  }

}
