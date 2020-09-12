package lfx.game.object;

import java.util.List;
import java.util.Map;
import lfx.base.Controller;
import lfx.base.Order;
import lfx.base.Scope;
import lfx.component.Action;
import lfx.component.Cost;
import lfx.component.Effect;
import lfx.component.Frame;
import lfx.component.Itr;
import lfx.component.State;
import lfx.component.Wpoint;
import lfx.game.Hero;
import lfx.game.Library;
import lfx.game.Observable;
import lfx.game.Weapon;
import lfx.util.ImageCell;
import lfx.util.Point;
import lfx.util.Util;
import lfx.util.Tuple;

class BaseHero extends AbstractObject implements Hero {
  // Hidden action frame counters.
  private final Indexer walkingIndexer = new Indexer(2, 3, 2, 1, 0, 1);
  private final Indexer runningIndexer = new Indexer(0, 1, 2, 1);

  private final ImageCell portrait;
  private final double Value_walking_speed;
  private final double Value_walking_speedz;
  private final double Value_walking_speedx;
  private final double Value_running_speed;
  private final double Value_running_speedz;
  private final double Value_running_speedx;
  private final double Value_heavy_walking_speed;
  private final double Value_heavy_walking_speedz;
  private final double Value_heavy_walking_speedx;
  private final double Value_heavy_running_speed;
  private final double Value_heavy_running_speedz;
  private final double Value_heavy_running_speedx;
  private final double Value_jump_height;
  private final double Value_jump_distance;
  private final double Value_jump_distancez;
  private final double Value_dash_height;
  private final double Value_dash_distance;
  private final double Value_dash_distancez;
  private final double Value_rowing_height;
  private final double Value_rowing_distance;
  // Regeneration rates
  private double hpReg = 1.0 / 12.0;
  private double mpReg = 1.0 / 3.00;
  private int dp = 0;  // defend point
  private int fp = 0;  // fall point
  private Controller controller = null;
  private Weapon weapon = null;

  protected BaseHero(String identifier, List<Frame> frameList,
                     Map<String, Double> stamina, ImageCell portrait) {
    super(identifier, frameList, Scope.HERO);
    this.portrait = portrait;
    Value_walking_speed  = stamina.get(Key_walking_speed);
    Value_walking_speedz = stamina.get(Key_walking_speedz);
    Value_walking_speedx = Value_walking_speed * DIAGONAL_VX_RATIO;
    Value_running_speed  = stamina.get(Key_running_speed);
    Value_running_speedz = stamina.get(Key_running_speedz);
    Value_running_speedx = Value_running_speed * DIAGONAL_VX_RATIO;
    Value_heavy_walking_speed  = stamina.get(Key_heavy_walking_speed);
    Value_heavy_walking_speedz = stamina.get(Key_heavy_walking_speedz);
    Value_heavy_walking_speedx = Value_heavy_walking_speed * DIAGONAL_VX_RATIO * 0.5;
    Value_heavy_running_speed  = stamina.get(Key_heavy_running_speed);
    Value_heavy_running_speedz = stamina.get(Key_heavy_running_speedz);
    Value_heavy_running_speedx = Value_heavy_running_speed * DIAGONAL_VX_RATIO * 0.5;
    Value_jump_height     = stamina.get(Key_jump_height);
    Value_jump_distance   = stamina.get(Key_jump_distance);
    Value_jump_distancez  = stamina.get(Key_jump_distancez);
    Value_dash_height     = stamina.get(Key_dash_height);
    Value_dash_distance   = stamina.get(Key_dash_distance);
    Value_dash_distancez  = stamina.get(Key_dash_distancez);
    Value_rowing_height   = stamina.get(Key_rowing_height);
    Value_rowing_distance = stamina.get(Key_rowing_distance);
    hpReg = stamina.getOrDefault(Key_hp_reg, hpReg);
    mpReg = stamina.getOrDefault(Key_mp_reg, mpReg);
  }

  private BaseHero(BaseHero base) {
    super(base);
    portrait = base.portrait;
    Value_walking_speed  = base.Value_walking_speed;
    Value_walking_speedz = base.Value_walking_speedz;
    Value_walking_speedx = base.Value_walking_speedx;
    Value_running_speed  = base.Value_running_speed;
    Value_running_speedz = base.Value_running_speedz;
    Value_running_speedx = base.Value_running_speedx;
    Value_heavy_walking_speed  = base.Value_heavy_walking_speed;
    Value_heavy_walking_speedz = base.Value_heavy_walking_speedz;
    Value_heavy_walking_speedx = base.Value_heavy_walking_speedx;
    Value_heavy_running_speed  = base.Value_heavy_running_speed;
    Value_heavy_running_speedz = base.Value_heavy_running_speedz;
    Value_heavy_running_speedx = base.Value_heavy_running_speedx;
    Value_jump_height     = base.Value_jump_height;
    Value_jump_distance   = base.Value_jump_distance;
    Value_jump_distancez  = base.Value_jump_distancez;
    Value_dash_height     = base.Value_dash_height;
    Value_dash_distance   = base.Value_dash_distance;
    Value_dash_distancez  = base.Value_dash_distancez;
    Value_rowing_height   = base.Value_rowing_height;
    Value_rowing_distance = base.Value_rowing_distance;
    hpReg = base.hpReg;
    mpReg = base.mpReg;
  }

  @Override
  public BaseHero makeClone() {
    return new BaseHero(this);
  }

  @Override
  protected void registerLibrary() {
    Library.instance().register(this);
    return;
  }

  @Override
  public double getInputZ() {
    return controller.pressZ() ? (controller.press_U() ? 1.0 : -1.0) : 0.0;
  }

  @Override
  public Wpoint getWpoint() {
    return frame.wpoint;
  }

  @Override
  protected Action getDefaultAct() {
    return py < 0.0 ? Action.HERO_JUMPAIR :
           (weapon != null && weapon.isHeavy() ? Action.HERO_HEAVY_WALK : Action.HERO_STANDING);
  }

  @Override
  protected void transitFrame(Action action) {
    Action orderAction = frame.combo.getOrDefault(controller.getOrder(), Action.UNASSIGNED);
    if (orderAction == Action.UNASSIGNED) {
      super.transitFrame(action);
      return;
    }
    Cost cost = frameList.get(orderAction.index).cost;
    if (cost == Cost.FREE || env.isUnlimitedMode()) {
      super.transitFrame(orderAction);
    } else if (mp >= cost.mp && hp >= cost.hp) {
      mp -= cost.mp;
      hp -= cost.hp;
      super.transitFrame(orderAction);
    } else {
      super.transitFrame(action);
    }
    return;
  }

  @Override
  protected void transitNextFrame() {
    Cost cost = frameList.get(frame.next.index).cost;
    if (cost == Cost.FREE || env.isUnlimitedMode()) {
      super.transitFrame(frame.next);
      return;
    }
    Action spareAction = frame.combo.getOrDefault(Order.hit_d, Action.UNASSIGNED);
    if (spareAction == Action.UNASSIGNED || mp + cost.mp >= 0) {
      mp = mp + cost.mp;
      super.transitFrame(frame.next);
    } else {
      super.transitFrame(spareAction);
    }
    return;
  }

  /**
   * Most of the time, potential HP is reduced by one-third of the received damage.
   * Use `sync=true` in the situations not following this rule (e.g., throwinjury).
   * Note that the hp lower bound is zero in LFX, which is different from LF2.
   */
  @Override
  protected void hpLost(double injury, boolean sync) {
    hp -= injury;
    hp2nd -= sync ? injury : Math.floor(injury / 3.0);
    return;
  }

  @Override
  public boolean isAlive() {
    return hp > 0.0;
  }

  @Override
  public Point getChasingPoint() {
    return new Point(px, py - frame.centery / 2.0);
  }

  @Override
  protected void addRaceCondition(Observable competitor) {
    // TODO: race condition
    return;
  }

  private Action confirmPicking(Observable that) {
    Weapon target = (Weapon) that;
    if (target.getHolder() == this) {
      weapon = target;
    }
    return target.isHeavy() ? Action.HERO_PICK_HEAVY : Action.HERO_PICK_LIGHT;
  }

  @Override
  public void react() {
    Action nextAct = Action.UNASSIGNED;

    for (Tuple<Observable, Itr> tuple : sendItrList) {
      Itr itr = tuple.second;
      if (itr.kind.damage) {
        actPause = itr.calcPause(actPause);
      } else if (itr.kind == Itr.Kind.PICK) {
        // You always perform the action even if picking failed.
        nextAct = confirmPicking(tuple.first);
      } else if (itr.kind == Itr.Kind.ROLL_PICK) {
        confirmPicking(tuple.first);
      } else if (itr.kind == Itr.Kind.GRAB_DOP || itr.kind == Itr.Kind.GRAB_BDY) {
        // TODO: GRAB
      }
    }
    sendItrList.clear();

    for (Tuple<Observable, Itr> tuple : recvItrList) {
      Itr itr = tuple.second;
      switch (itr.kind) {
        case FORCE_ACT:
          buff.put(Effect.FORCE_ACT, Effect.FORCE_ACT.of());
          break;
        case BLOCK:
          buff.put(Effect.MOVE_BLOCKING, Effect.MOVE_BLOCKING.of());
          break;
        case HEAL:
          buff.put(Effect.HEALING, Effect.HEALING.of());
          break;
        default:
          System.out.println("NotImplemented Effect: " + itr.kind);
      }
      if (!itr.kind.damage) {
        continue;
      }
      actPause = itr.calcPause(actPause);
      double dvx = itr.calcDvx(px, tuple.first.getFacing());
      boolean face2face = faceRight == (dvx < 0.0);
      if (frame.state == State.DEFEND && face2face) {
        hpLost(itr.injury * DEFEND_INJURY_REDUCTION, false);
        vx += dvx * DEFEND_DVX_REDUCTION;
        dp += itr.bdefend;
        if (hp <= 0.0) {
          nextAct = Action.HERO_FORWARD_FALL1;
        } else if (dp > 30) {
          transitFrame(Action.HERO_BROKEN_DEF);
        } else if (frame.curr == Action.HERO_DEFEND.index) {
          transitFrame(Action.HERO_DEFEND_HIT);
        }
      } else {
        hpLost(itr.injury, false);
        vx += dvx;
        dp = Math.max(dp + itr.bdefend, 45);
        fp = (fp + itr.fall + 19) / 20 * 20;
        if (frame.state == State.ICE || fp > 60 || (fp > 20 && py < 0.0) || hp <= 0.0) {
          vy += itr.dvy;
          nextAct = face2face ? Action.HERO_BACKWARD_FALL1 : Action.HERO_FORWARD_FALL1;
        } else if (fp > 40) {
          nextAct = Action.HERO_DOP;
        } else if (fp > 20) {
          nextAct = face2face ? Action.HERO_INJURE3 : Action.HERO_INJURE2;
        } else if (fp >= 0) {
          nextAct = Action.HERO_INJURE1;
        } else {
          // Negative fp causes nothing.
        }
      }
    }
    recvItrList.clear();
    return;
  }

  @Override
  protected Action updateAction(Action nextAct) {
    switch (frame.state) {
      case STAND:       nextAct = moveStand(nextAct);      break;
      case WALK:        nextAct = moveWalk(nextAct);       break;
      case HEAVY_WALK:  nextAct = moveHeavyWalk(nextAct);  break;
      case RUN:         nextAct = moveRun(nextAct);        break;
      case HEAVY_RUN:   nextAct = moveHeavyRun(nextAct);   break;
      case JUMP:        nextAct = moveJump(nextAct);       break;
      case DASH:        nextAct = moveDash(nextAct);       break;
      case LANDING:     nextAct = moveLanding(nextAct);    break;
      case FLIP:        nextAct = moveFlip(nextAct);       break;
      case DRINK:       nextAct = moveDrink(nextAct);      break;
      case FALL:        nextAct = moveFall(nextAct);       break;
      case FIRE:        nextAct = moveFire(nextAct);       break;
      case LYING:       nextAct = moveLying(nextAct);      break;
      case NORMAL:
        // fall-through
      case DEFEND:
        // fall-through
      case GRAB:
        // fall-through
      case ICE:
        break;
      default:
        System.err.println("Unexpected state: " + frame);
    }
    // You can change facing in this action number.
    if (frame.curr == Action.HERO_DEFEND.index && controller.pressX()) {
      faceRight = controller.press_R();
    }
    return nextAct;
  }

  private Action moveStandWalkPressA(Action nextAct) {
    if (weapon == null) {
      nextAct = buff.containsKey(Effect.FORCE_ACT) ? Action.HERO_SUPER_PUNCH :
                Util.randomBool() ? Action.HERO_PUNCH1 : Action.HERO_PUNCH2;
    } else if (weapon.isLight()) {
      nextAct = Util.randomBool() ? Action.HERO_WEAPON_ATK1 : Action.HERO_WEAPON_ATK2;
    } else if (weapon.isSmall()) {
      nextAct = Action.HERO_LIGHT_WEAPON_THROW;
    } else if (weapon.isDrink()) {
      nextAct = Action.HERO_DRINK;
    } else {
      weapon.release();
      weapon = null;
    }
    return nextAct;
  }

  private Action moveStand(Action nextAct) {
    if (controller.press_a()) {
      nextAct = moveStandWalkPressA(nextAct);
    } else if (controller.press_j()) {
      nextAct = Action.HERO_JUMP;
    } else if (controller.press_d()) {
      nextAct = Action.HERO_DEFEND;
    } else if (controller.pressRun())  {
      nextAct = Action.HERO_RUNNING.shifts(runningIndexer.reset());
      faceRight = controller.getFacing(faceRight);
    } else if (controller.pressWalk()) {
      nextAct = Action.HERO_WALKING.shifts(walkingIndexer.reset());
      faceRight = controller.getFacing(faceRight);
    }
    return nextAct;
  }

  private Action moveWalk(Action nextAct) {
    if (controller.press_a()) {
      nextAct = moveStandWalkPressA(nextAct);
    } else if (controller.press_j()) {
      nextAct = Action.HERO_JUMP;
    } else if (controller.press_d()) {
      nextAct = Action.HERO_DEFEND;
    } else if (controller.pressRun())  {
      nextAct = Action.HERO_RUNNING.shifts(runningIndexer.reset());
      faceRight = controller.getFacing(faceRight);
    } else if (controller.pressWalk()) {
      if (isFirstTimeunit()) {
        nextAct = Action.HERO_WALKING.shifts(walkingIndexer.next());
      }
      faceRight = controller.getFacing(faceRight);
      vx = controller.valueX() * Value_walking_speed;
      vz = controller.valueZ() * Value_walking_speedz;
    }
    return nextAct;
  }

  private Action moveHeavyWalk(Action nextAct) {
    if (controller.press_a()) {
      nextAct = Action.HERO_HEAVY_WEAPON_THROW;
    } else if (controller.pressRun())  {
      nextAct = Action.HERO_HEAVY_RUN.shifts(runningIndexer.reset());
      faceRight = controller.getFacing(faceRight);
    } else if (controller.pressWalk()) {
      if (isFirstTimeunit()) {
        nextAct = Action.HERO_HEAVY_WALK.shifts(walkingIndexer.next());
      }
      faceRight = controller.getFacing(faceRight);
      vx = controller.valueX() * Value_heavy_walking_speed;
      vz = controller.valueZ() * Value_heavy_walking_speedz;
    }
    return nextAct;
  }

  private Action moveRun(Action nextAct) {
    vx = faceRight ? Value_running_speed : -Value_running_speed;
    vz = controller.valueZ() * Value_running_speedz;
    if (controller.press_a()) {
      if (weapon == null) {
        nextAct = Action.HERO_RUN_ATK;
      } else if (weapon.isLight()) {
        nextAct = controller.pressWalk() ?
                  Action.HERO_LIGHT_WEAPON_THROW : Action.HERO_RUN_WEAPON_ATK;
      } else if (weapon.isSmall()) {
        nextAct = Action.HERO_LIGHT_WEAPON_THROW;
      } else if (weapon.isDrink()) {
        nextAct = controller.pressWalk() ? Action.HERO_LIGHT_WEAPON_THROW : Action.HERO_DRINK;
      } else {
        weapon.release();
        weapon = null;
      }
    } else if (controller.press_j()) {
      nextAct = Action.HERO_DASH1;
    } else if (controller.press_d()) {
      nextAct = Action.HERO_ROLLING;
    } else if (controller.reverseFacing(faceRight)) {
      nextAct = Action.HERO_STOPRUN;
    } else if (isFirstTimeunit()) {
      nextAct = Action.HERO_RUNNING.shifts(runningIndexer.next());
    }
    return nextAct;
  }

  private Action moveHeavyRun(Action nextAct) {
    vx = faceRight ? Value_heavy_running_speed : -Value_heavy_running_speed;
    vz = controller.valueZ() * Value_heavy_running_speedz;
    if (controller.press_a()) {
      nextAct = Action.HERO_HEAVY_WEAPON_THROW;
    } else if (controller.reverseFacing(faceRight)) {
      nextAct = Action.HERO_HEAVY_STOP_RUN;
    } else if (isFirstTimeunit()) {
      nextAct = Action.HERO_HEAVY_RUN.shifts(runningIndexer.next());
    }
    return nextAct;
  }

  private Action moveJump(Action nextAct) {
    if (frame.curr == Action.HERO_JUMPAIR.index && isActionFirstTimeunit()) {
      // dvx is applied after friction reduction.
      vx += Math.copySign(Value_jump_distance, vx);
      vy += Value_jump_height;
      vz = controller.valueZ() * Value_jump_distancez;
    } else {
      faceRight = controller.getFacing(faceRight);
      if (controller.press_a()) {
        if (weapon == null) {
          nextAct = Action.HERO_JUMP_ATK;
        } else if (weapon.isLight()) {
          nextAct = controller.pressWalk() ?
                    Action.HERO_SKY_WEAPON_THROW : Action.HERO_JUMP_WEAPON_ATK;
        } else if (weapon.isSmall() || weapon.isDrink()) {
          nextAct = Action.HERO_SKY_WEAPON_THROW;
        } else {
          weapon.release();
          weapon = null;
        }
      }
    }
    return nextAct;
  }

  private Action moveDash(Action nextAct) {
    if (frame.curr == Action.HERO_DASH1.index && isActionFirstTimeunit()) {
      vx = Math.copySign(Value_dash_distance, vx);
      vy += Value_dash_height;
      vz = controller.valueZ() * Value_dash_distancez;
      return nextAct;
    }
    if (controller.reverseFacing(faceRight)) {  // turn facing
      faceRight = controller.press_R();
      nextAct = faceRight == (vx >= 0.0) ? Action.HERO_DASH1 : Action.HERO_DASH2;
    }
    if (controller.press_a() && (faceRight == (vx >= 0.0))) {
      if (weapon == null) {
        nextAct = Action.HERO_DASH_ATK;
      } else if (weapon.isLight()) {
        nextAct = Action.HERO_DASH_WEAPON_ATK;
      } else if (weapon.isSmall() || weapon.isDrink()) {
        nextAct = Action.HERO_SKY_WEAPON_THROW;
      } else {
        weapon.release();
        weapon = null;
      }
    }
    return nextAct;
  }

  private Action moveLanding(Action nextAct) {
    if (controller.press_j()) {
      if (controller.pressX()) {
        faceRight = controller.press_R();
        nextAct = Action.HERO_DASH1;
      } else if (Math.abs(vx) > 0.1) {
        nextAct = faceRight == (vx >= 0.0) ? Action.HERO_DASH1 : Action.HERO_DASH2;
      } else if (!isFirstTimeunit()) {  // TODO: transition < 2 ?
        nextAct = Action.HERO_JUMP;
      }
      return nextAct;
    }
    if (controller.pressWalk()) {
      vz = controller.valueZ() * Value_walking_speedz;
      if (!isFirstTimeunit()) {
        nextAct = Action.HERO_WALKING.shifts(walkingIndexer.reset());
      }
    } else if (controller.press_d()) {
      nextAct = Action.HERO_ROLLING;
    }
    return nextAct;
  }

  private Action moveFall(Action nextAct) {
    if (Action.HERO_BACKWARD_FALL.includes(frame.curr)) {
      if (Action.HERO_BACKWARD_FALLR.index == frame.curr) {
        // Can do nothing
      } else if (vy < -10.0) {
        nextAct = Action.HERO_BACKWARD_FALL1;
      } else if (vy < 0.0) {
        nextAct = Action.HERO_BACKWARD_FALL2;
      } else if (vy < 6.0) {
        // TODO: Check buff.containsKey(Effect.SONATA)
        nextAct = controller.press_j() ? Action.HERO_FLIP2 :
                  Action.HERO_BACKWARD_FALL3;
      } else {
        nextAct = Action.HERO_BACKWARD_FALL4;
      }
    } else {
      if (Action.HERO_FORWARD_FALLR.index == frame.curr) {
        // Can do nothing
      } else if (vy < -10.0) {
        nextAct = Action.HERO_FORWARD_FALL1;
      } else if (vy < 0.0) {
        nextAct = Action.HERO_FORWARD_FALL2;
      } else if (vy < 6.0) {
        nextAct = controller.press_j() ? Action.HERO_FLIP1 :
                  Action.HERO_FORWARD_FALL3;
      } else {
        nextAct = Action.HERO_FORWARD_FALL4;
      }
    }
    return nextAct;
  }

  private Action moveDrink(Action nextAct) {
    List<Double> regen = weapon == null ? Weapon.OTHERS_REGENERATION : weapon.consume();
    mp = Math.min(mpMax, mp + regen.get(0));
    hp = Math.min(hpMax, hp + regen.get(1));
    hp2nd = Math.max(hp, hp2nd + regen.get(2));
    // You can be forced into these actions without holding anything.
    return nextAct;
  }

  private Action moveFire(Action nextAct) {
    return vy < 0.0 ? Action.HERO_UPWARD_FIRE : Action.HERO_DOWNWARD_FIRE;
  }

  private Action moveFlip(Action nextAct) {
    if (isActionFirstTimeunit()) {
      vy = Value_rowing_height;
      vx = Math.copySign(Value_rowing_distance, vx);
    }
    buff.remove(Effect.LANDING_INJURY);
    return nextAct;
  }

  private Action moveLying(Action nextAct) {
    if (0.0 >= hp) {
      nextAct = Action.REPEAT;
    }
    return nextAct;
  }

  private Action landing(boolean reboundable, boolean faceForward, double damage) {
    if (reboundable && (vx > 10.0 || vx < -10.0 || vy > 1.0)) {
      vy = FALLING_BOUNCE_VY;
      hpLost(damage, false);
      return faceForward ? Action.HERO_FORWARD_FALLR : Action.HERO_BACKWARD_FALLR;
    } else {
      vy = 0.0;
      return faceForward ? Action.HERO_LYING1 : Action.HERO_LYING2;
    }
  }

  @Override
  protected Action updateKinetic(Action nextAct) {
    if (actPause != 0) {
      return Action.UNASSIGNED;
    }

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
      vy = frame.dvy;
    }
    if (frame.dvz == Frame.RESET_VELOCITY) {
      vz = 0.0;
    } else {
      vz = controller.valueZ() * frame.dvz;
    }
    if (!buff.containsKey(Effect.MOVE_BLOCKING)) {
      px += vx;
      py += vy;
      pz += vz;
    }
    if (py < 0.0) {  // You are still flying.
      vy = env.applyGravity(vy);
      return nextAct;
    }

    py = 0.0;  // You are on the ground.
    if (frame.state == State.FALL) {
      boolean reboundable = frame.curr != Action.HERO_FORWARD_FALLR.index &&
                            frame.curr != Action.HERO_FORWARD_FALL1.index;
      boolean faceForward = frame.curr <= Action.HERO_FORWARD_FALLR.index &&
                            frame.curr >= Action.HERO_FORWARD_FALL1.index;
      nextAct = landing(reboundable, faceForward, 0.0);
    } else if (frame.state == State.FIRE) {
      nextAct = landing(true, false, 0.0);
    } else if (frame.state == State.ICE) {
      // TODO: not break for small fall
      nextAct = landing(true, false, ICED_FALLDOWN_DAMAGE);
    } else if (frame.state == State.JUMP || frame.state == State.FLIP) {
      nextAct = Action.HERO_CROUCH1;
      vy = 0.0;
    } else {
      nextAct = Action.HERO_CROUCH2;
      vy = 0.0;
    }
    vx = env.applyFriction(vx * LANDING_VELOCITY_REMAIN);
    vz = env.applyFriction(vz * LANDING_VELOCITY_REMAIN);

    if (buff.containsKey(Effect.LANDING_ACT)) {
      // TODO: Action from int
      nextAct = new Action(buff.remove(Effect.LANDING_ACT).intValue);
    }
    if (buff.containsKey(Effect.LANDING_INJURY)) {
      hpLost(buff.remove(Effect.LANDING_INJURY).intValue, true);
    }
    return nextAct;
  }

  @Override
  protected Action updateStamina(Action nextAct) {
    if (hp <= 0.0) {
      return py < 0.0 ? nextAct : vx >= 0.0 ? Action.HERO_LYING1 : Action.HERO_LYING2;
    }
    double bonus = hpMax > hp ? (hpMax - hp) / 300.0 : 0.0;
    hp = Math.min(hp2nd, hp + hpReg);
    mp = Math.min(mpMax, mp + mpReg + bonus);
    if (actPause < 0) {  // TODO: or another counter
      dp = dp > 0 ? dp - 1 : dp;
      fp = fp > 0 ? fp - 1 : dp;
    }
    return nextAct;
  }

  @Override
  protected boolean fitBoundary() {
    List<Double> xBound = env.getHeroXBound();
    List<Double> zBound = env.getZBound();
    px = Util.clamp(px, xBound.get(0), xBound.get(1));
    pz = Util.clamp(pz, zBound.get(0), zBound.get(1));
    return true;
  }

  // @Override
  // protected void updateItrs() {
  //   super.updateItrs();
  //   if (weapon != null) {
  //     itrList.addAll(weapon.getStrengthItrs(frame.wpoint.usage));
  //   }
  //   return;
  // }

  @Override
  public ImageCell getPortrait() {
    return portrait;
  }

  @Override
  public String getName() {
    return identifier;
  }

  @Override
  public Point getViewpoint() {
    return new Point(px, faceRight ? 1.0 : -1.0);
  }

  @Override
  public void setController(Controller controller) {
    this.controller = controller;
    return;
  }

}
