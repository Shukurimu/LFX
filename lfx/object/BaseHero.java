package lfx.object;

import java.util.List;
import java.util.Map;
import javafx.scene.image.Image;
import lfx.base.Controller;
import lfx.base.Input;
import lfx.base.Scope;
import lfx.base.Viewer;
import lfx.component.Effect;
import lfx.component.Frame;
import lfx.component.Itr;
import lfx.component.State;
import lfx.component.Wpoint;
import lfx.object.AbstractObject;
import lfx.object.Hero;
import lfx.util.Const;
import lfx.util.Looper;
import lfx.util.Point;
import lfx.util.Util;
import lfx.util.Tuple;

class BaseHero extends AbstractObject implements Hero {
  // Hidden action frame counters.
  private final Looper walkingIndexer = new Looper(2, 3, 2, 1, 0, 1);
  private final Looper runningIndexer = new Looper(0, 1, 2, 1);

  private final Image portrait;
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
  private int flyingFlag = NO_FLYING;
  // dp & fp will not recovere if hit in 2 timeunit.
  private int hitSpan = 0;
  private int dp = 0;  // defend point
  private int fp = 0;  // fall point
  private Input input = new Input();
  private Controller controller = null;
  private Weapon weapon = null;

  protected BaseHero(String identifier, List<Frame> frameList,
                     Map<String, Double> stamina, Image portrait) {
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

  protected BaseHero(BaseHero base) {
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
  public BaseHero makeClone(int teamId, boolean faceRight) {
    BaseHero clone = new BaseHero((BaseHero) heroMapping.get(identifier));
    clone.teamId = teamId;
    clone.faceRight = faceRight;
    return clone;
  }

  @Override
  protected void registerObjectMap() {
    heroMapping.putIfAbsent(identifier, this);
    return;
  }

  @Override
  public double getInputZ() {
    return input.do_Z ? (input.do_U ? 1.0 : -1.0) : 0.0;
  }

  @Override
  public Wpoint getWpoint() {
    return frame.wpoint;
  }

  @Override
  protected int getDefaultActNumber() {
    return py < 0.0 ? ACT_JUMPAIR :
           (weapon != null && weapon.isHeavy() ? ACT_HEAVY_WALK : ACT_STANDING);
  }

  // Most of the time, potential HP is reduced by one-third of the received damage.
  // Use `sync=true` in the situations not following this rule (e.g., throwinjury).
  // Note that the hp lower bound is zero in LFX, which is different from LF2.
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

  // Return remaining hp and mp, or null if no cost.
  protected double[] pseudoCast(Frame target, boolean passive) {
    // TODO: Louis transformation has hp limitation.
    if (target == null || target.cost == 0 || env.isUnlimitedMode()) {
      return null;
    }
    if (passive) {  // Followed by next-tag.
      return new double[] {hp, mp + target.cost};
    } else {
      int hpCost = target.cost / 1000 * 10;
      int mpCost = target.cost - hpCost * 100;
      return new double[] {hp - hpCost, mp - mpCost};
    }
  }

  private Weapon confirmPicking(Observable that) {
    Weapon target = (Weapon) that;
    if (target.getHolder() == this) {
      weapon = target;
    }
    return target;
  }

  @Override
  public void react() {
    int nextAct = ACT_TBA;

    for (Tuple<Observable, Itr> tuple: sendItrList) {
      Itr itr = tuple.second;
      switch (itr.kind) {
        case GRASP_DOP:
        case GRASP_BDY:
          break;
        case PICK:
          // You always perform the action even if picking failed.
          nextAct = confirmPicking(tuple.first).isHeavy() ? ACT_PICK_HEAVY : ACT_PICK_LIGHT;
          break;
        case ROLL_PICK:
          confirmPicking(tuple.first);
          break;
        default:
          if (itr.kind.callback) {
            System.err.printf("Itr %s does not process callback.%n", itr);
          }
      }
    }
    sendItrList.clear();

    for (Tuple<Observable, Itr> tuple: recvItrList) {
      Observable that = tuple.first;
      Itr itr = tuple.second;
      switch (itr.kind) {
        case LET_SPUNCH:
          buff.put(Effect.ATTACK_SPUNCH, Effect.Value.once());
          break;
        case BLOCK:
          buff.put(Effect.MOVE_BLOCKING, Effect.Value.once());
          break;
        case HEAL:
          buff.put(Effect.HEALING, Effect.Value.last(itr.dvy, (double) itr.injury / itr.dvy));
          break;
        default:
          System.out.println("NotImplemented Effect: " + itr.kind);
      }

      actLag = itr.calcLag(actLag);
      Tuple<Double, Boolean> dvxTuple = itr.calcDvx(px, faceRight);  // (dvx, sameDirection)
      hitSpan = 2;
      if (frame.state == State.DEFEND && dvxTuple.second) {
        hpLost(itr.injury * DEFEND_INJURY_REDUCTION, false);
        vx += dvxTuple.first * DEFEND_DVX_REDUCTION;
        dp += itr.bdefend;
        if (hp <= 0.0) {
          nextAct = ACT_FORWARD_FALL1;
        } else if (dp > 30) {
          transitFrame(ACT_BROKEN_DEF);
        } else if (frame.curr == ACT_DEFEND) {
          transitFrame(ACT_DEFEND_HIT);
        }
      } else {
        hpLost(itr.injury, false);
        vx += dvxTuple.first;
        dp = Math.max(dp + itr.bdefend, NODEF_DP);
        fp = (fp + itr.fall + 19) / 20 * 20;
        if (frame.state == State.ICE || fp > 60 || (fp > 20 && py < 0.0) || hp <= 0.0) {
          vy += itr.dvy;
          nextAct = dvxTuple.second ? ACT_BACKWARD_FALL1 : ACT_FORWARD_FALL1;
        } else if (fp > 40) {
          nextAct = ACT_DOP;
        } else if (fp > 20) {
          nextAct = dvxTuple.second ? ACT_INJURE3 : ACT_INJURE2;
        } else if (fp >= 0) {
          nextAct = ACT_INJURE1;
        } else {
          // Negative fp causes nothing.
        }
      }
    }
    recvItrList.clear();
    return;
  }

  @Override
  protected int updateAction(int nextAct) {
    switch (frame.state) {
      case STAND:       nextAct = moveStand(nextAct);      break;
      case WALK:        nextAct = moveWalk(nextAct);       break;
      case HEAVY_WALK:  nextAct = moveHeavyWalk(nextAct);  break;
      case RUN:         nextAct = moveRun(nextAct);        break;
      case HEAVY_RUN:   nextAct = moveHeavyRun(nextAct);   break;
      case JUMP:        nextAct = moveJump(nextAct);       break;
      case DASH:        nextAct = moveDash(nextAct);       break;
      case LAND:        nextAct = moveLand(nextAct);       break;
      case FIRE:        nextAct = moveFire(nextAct);       break;
      case DRINK:       nextAct = moveDrink(nextAct);      break;
      case ROW:         nextAct = moveRow(nextAct);        break;
      case FALL:        nextAct = moveFall(nextAct);       break;
      case LYING:       nextAct = moveLying(nextAct);      break;
      case NORMAL:
        if (py >= 0.0) {
          flyingFlag = NO_FLYING;
        }
        break;
      default:
        flyingFlag = NO_FLYING;
        System.err.println("Unexpected state: " + frame);
    }
    // You can change facing in this action number.
    if (frame.curr == ACT_DEFEND && input.do_F) {
      faceRight = input.do_R;
    }
    return nextAct;
  }

  private int moveStand(int nextAct) {
    flyingFlag = NO_FLYING;
    if (input.do_a) {
      if (weapon == null) {
        nextAct = buff.containsKey(Effect.ATTACK_SPUNCH) ?
                  ACT_SUPER_PUNCH : (Util.randomBool() ? ACT_PUNCH1 : ACT_PUNCH2);
      } else if (weapon.isLight()) {
        nextAct = Util.randomBool() ? ACT_WEAPON_ATK1 : ACT_WEAPON_ATK2;
      } else if (weapon.isSmall()) {
        nextAct = ACT_LIGHT_WEAPON_THROW;
      } else if (weapon.isDrink()) {
        nextAct = ACT_DRINK;
      } else {
        System.err.printf("%s hold %s at %d%n", this, weapon, frame.curr);
        weapon = null;
      }
    } else if (input.do_j) {
      nextAct = ACT_JUMP;
      if (input.do_R) {
        flyingFlag = JUMP_VP_1;
        faceRight = true;
      } else if (input.do_L) {
        flyingFlag = JUMP_VN_1;
        faceRight = false;
      } else {
        flyingFlag = JUMP_V0_1;
      }
    } else if (input.do_d) {
      nextAct = ACT_DEFEND;
    } else if (input.do_RR | input.do_LL)  {
      nextAct = ACT_RUNNING + runningIndexer.reset();
      faceRight = input.do_RR;
    } else if (input.do_F | input.do_Z) {
      nextAct = ACT_WALKING + walkingIndexer.reset();
      faceRight = input.do_R || (input.do_L ? false : faceRight);
    }
    return nextAct;
  }

  private int moveWalk(int nextAct) {
    flyingFlag = NO_FLYING;
    if (input.do_a) {
      if (weapon == null) {
        nextAct = buff.containsKey(Effect.ATTACK_SPUNCH) ?
                  ACT_SUPER_PUNCH : (Util.randomBool() ? ACT_PUNCH1 : ACT_PUNCH2);
      } else if (weapon.isLight()) {
        nextAct = Util.randomBool() ? ACT_WEAPON_ATK1 : ACT_WEAPON_ATK2;
      } else if (weapon.isSmall()) {
        nextAct = ACT_LIGHT_WEAPON_THROW;
      } else if (weapon.isDrink()) {
        nextAct = ACT_DRINK;
      } else {
        System.err.printf("%s hold %s at %d%n", this, weapon, frame.curr);
        weapon = null;
      }
    } else if (input.do_j) {
      nextAct = ACT_JUMP;
      if (input.do_R) {
        flyingFlag = JUMP_VP_1;
        faceRight = true;
      } else if (input.do_L) {
        flyingFlag = JUMP_VN_1;
        faceRight = false;
      } else {
        flyingFlag = JUMP_V0_1;
      }
    } else if (input.do_d) {
      nextAct = ACT_DEFEND;
    } else if (input.do_RR | input.do_LL)  {
      nextAct = ACT_RUNNING + runningIndexer.reset();
      faceRight = input.do_RR;
    } else if (input.do_F | input.do_Z) {
      if (input.do_R) {
        vx = Value_walking_speed;
        faceRight = true;
      } else if (input.do_L) {
        vx = -Value_walking_speed;
        faceRight = false;
      }
      if (input.do_D) {
        vz = Value_walking_speedz;
      } else if (input.do_U) {
        vz = -Value_walking_speedz;
      }
      if (transition == 0) {
        nextAct = ACT_WALKING + walkingIndexer.next();
      }
    }
    return nextAct;
  }

  private int moveHeavyWalk(int nextAct) {
    flyingFlag = NO_FLYING;
    if (input.do_a) {
      nextAct = ACT_HEAVY_WEAPON_THROW;
    } else if (input.do_RR | input.do_LL)  {
      nextAct = ACT_HEAVY_RUN + runningIndexer.reset();
      faceRight = input.do_RR;
    } else if (input.do_F | input.do_Z) {
      if (input.do_R) {
        vx = Value_heavy_walking_speed;
        faceRight = true;
      } else if (input.do_L) {
        vx = -Value_heavy_walking_speed;
        faceRight = false;
      }
      if (input.do_D) {
        vz = Value_heavy_walking_speedz;
      } else if (input.do_U) {
        vz = -Value_heavy_walking_speedz;
      }
      if (transition == 0) {
        nextAct = ACT_HEAVY_WALK + walkingIndexer.next();
      }
    }
    return nextAct;
  }

  private int moveRun(int nextAct) {
    flyingFlag = NO_FLYING;
    vx = faceRight ? Value_running_speed : -Value_running_speed;
    if (input.do_Z) {
      vz = input.do_D ? Value_running_speedz : -Value_running_speedz;
    }
    if (input.do_a) {
      if (weapon == null) {
        nextAct = ACT_RUN_ATK;
      } else if (weapon.isLight()) {
        nextAct = input.do_F ? ACT_LIGHT_WEAPON_THROW : ACT_RUN_WEAPON_ATK;
      } else if (weapon.isSmall()) {
        nextAct = ACT_LIGHT_WEAPON_THROW;
      } else if (weapon.isDrink()) {
        nextAct = input.do_F ? ACT_LIGHT_WEAPON_THROW : ACT_DRINK;
      } else {
        System.err.printf("%s hold %s at %d%n", this, weapon, frame.curr);
        weapon = null;
      }
    } else if (input.do_j) {
      nextAct = ACT_DASH1;
      flyingFlag = faceRight ? DASH_RP_1 : DASH_LN_1;
    } else if (input.do_d) {
      nextAct = ACT_ROLLING;
    } else if ((faceRight && input.do_L) || (!faceRight && input.do_R)) {
      nextAct = ACT_STOPRUN;
    } else if (transition == 0) {
      nextAct = ACT_RUNNING + runningIndexer.next();
    }
    return nextAct;
  }

  private int moveHeavyRun(int nextAct) {
    flyingFlag = NO_FLYING;
    vx = faceRight ? Value_heavy_running_speed : -Value_heavy_running_speed;
    if (input.do_Z) {
      vz = input.do_D ? Value_heavy_running_speedz : -Value_heavy_running_speedz;
    }
    if (input.do_a) {
      nextAct = ACT_HEAVY_WEAPON_THROW;
    } else if ((faceRight && input.do_L) || (!faceRight && input.do_R)) {
      nextAct = ACT_HEAVY_STOP_RUN;
    } else if (transition == 0) {
      nextAct = ACT_HEAVY_RUN + runningIndexer.next();
    }
    return nextAct;
  }

  private int moveJump(int nextAct) {
    if (((flyingFlag & 1) == 1) && frame.curr == ACT_JUMPAIR) {
      --flyingFlag;
      // dvx is applied after friction reduction.
      switch (flyingFlag) {
        case JUMP_V0_0:
          vx = env.applyFriction(vx);
          break;
        case JUMP_VP_0:
          vx = env.applyFriction(vx + Value_jump_distance);
          break;
        case JUMP_VN_0:
          vx = env.applyFriction(vx - Value_jump_distance);
          break;
        default:
          System.err.println("State_jump: `flyingFlag' default");
      }
      vy += Value_jump_height;
      if (input.do_Z) {
        vz = input.do_D ? Value_jump_distancez : -Value_jump_distancez;
      }
    } else if ((flyingFlag & 1) == 0) {
      if (input.do_F) {
        faceRight = input.do_R;
      }
      if (input.do_a) {
        if (weapon == null) {
          nextAct = ACT_JUMP_ATK;
        } else if (weapon.isLight()) {
          nextAct = input.do_F ? ACT_SKY_WEAPON_THROW : ACT_JUMP_WEAPON_ATK;
        } else if (weapon.isSmall() || weapon.isDrink()) {
          nextAct = ACT_SKY_WEAPON_THROW;
        } else {
          System.err.printf("%s hold %s at %d%n", this, weapon, frame.curr);
          weapon = null;
        }
      }
    }
    return nextAct;
  }

  private int moveDash(int nextAct) {
    if ((flyingFlag & 1) == 1) {
      --flyingFlag;
      switch (flyingFlag) {
        case DASH_RP_0:
        case DASH_LP_0:
          vx = env.applyFriction(+Value_dash_distance);
          break;
        case DASH_LN_0:
        case DASH_RN_0:
          vx = env.applyFriction(-Value_dash_distance);
          break;
        default:
          System.err.println("State_dash: `flyingFlag' default");
      }
      vy += Value_dash_height;
      if (input.do_Z) {
        vz = input.do_D ? Value_dash_distancez : -Value_dash_distancez;
      }
    } else if ((flyingFlag & 1) == 0) {
      switch (flyingFlag) {
        case DASH_RP_0:
          if (input.do_L) {
            faceRight = false;
            nextAct = ACT_DASH2;
            flyingFlag = DASH_LP_0;
          }
          break;
        case DASH_LN_0:
          if (input.do_R) {
            faceRight = true;
            nextAct = ACT_DASH2;
            flyingFlag = DASH_RN_0;
          }
          break;
        case DASH_LP_0:
          if (input.do_R) {
            faceRight = true;
            nextAct = ACT_DASH1;
            flyingFlag = DASH_RP_0;
          }
          break;
        case DASH_RN_0:
          if (input.do_L) {
            faceRight = false;
            nextAct = ACT_DASH1;
            flyingFlag = DASH_LN_0;
          }
          break;
        default:
          System.err.println("State_dash: `flyingFlag' default");
      }
      if (input.do_a && (flyingFlag == DASH_RP_0 || flyingFlag == DASH_LN_0)) {
        if (weapon == null) {
          nextAct = ACT_DASH_ATK;
        } else if (weapon.isLight()) {
          nextAct = ACT_DASH_WEAPON_ATK;
        } else if (weapon.isSmall() || weapon.isDrink()) {
          nextAct = ACT_SKY_WEAPON_THROW;
        } else {
          System.err.printf("%s hold %s at %d%n", this, weapon, frame.curr);
          weapon = null;
        }
      }
    }
    return nextAct;
  }

  private int moveLand(int nextAct) {
    if (input.do_d) {
      flyingFlag = NO_FLYING;
      nextAct = ACT_ROLLING;
    } else if (input.do_j) {
      if (input.do_F) {
        nextAct = ACT_DASH1;
        if (input.do_R) {
          faceRight = true;
          flyingFlag = DASH_RP_1;
        } else {
          faceRight = false;
          flyingFlag = DASH_LN_1;
        }
      } else if (vx != 0.0) {
        switch (flyingFlag) {
          case JUMP_VP_0:
          case ROW_VP_0:
            if (faceRight) {
              nextAct = ACT_DASH1;
              flyingFlag = DASH_RP_1;
            } else {
              nextAct = ACT_DASH2;
              flyingFlag = DASH_LP_1;
            }
            break;
          case JUMP_VN_0:
          case ROW_VN_0:
            if (faceRight) {
              nextAct = ACT_DASH2;
              flyingFlag = DASH_RN_1;
            } else {
              nextAct = ACT_DASH1;
              flyingFlag = DASH_LN_1;
            }
            break;
          case JUMP_V0_0:
            /* this situation may happen because of the friction */
            nextAct = ACT_JUMP;
            flyingFlag = JUMP_V0_1;
            break;
          default:
            System.err.printf("State_land: `flyingFlag' %d\n", flyingFlag);
        }
      } else if (transition < 2) {  // TODO: Why 2?
        nextAct = ACT_JUMP;
        flyingFlag = JUMP_V0_1;
      }
    } else if (input.do_F | input.do_Z) {
      flyingFlag = NO_FLYING;
      if (input.do_Z) {
        vz = input.do_D ? Value_walking_speedz : -Value_walking_speedz;
      }
      if (transition < 2) {
        nextAct = ACT_WALKING + walkingIndexer.reset();
      }
    } else {
      flyingFlag = NO_FLYING;
    }
    return nextAct;
  }

  private int moveFall(int nextAct) {
    flyingFlag = NO_FLYING;
    if (ACT_BACKWARD_FALLR >= frame.curr && frame.curr >= ACT_BACKWARD_FALL1) {
      if (frame.curr == ACT_BACKWARD_FALLR) {
        // Can do nothing
      } else if (vy < -10.0) {
        if (frame.curr != ACT_BACKWARD_FALL1) {
          nextAct = ACT_BACKWARD_FALL1;
        }
      } else if (vy < 0.0) {
        if (frame.curr != ACT_BACKWARD_FALL2) {
          nextAct = ACT_BACKWARD_FALL2;
        }
      } else if (vy < 6.0) {
        // TODO: Check buff.containsKey(Effect.SONATA)
        if (input.do_j) {
          nextAct = ACT_ROWING2;
          flyingFlag = faceRight ? ROW_VP_1 : ROW_VN_1;
        } else
        if (frame.curr != ACT_BACKWARD_FALL3) {
          nextAct = ACT_BACKWARD_FALL3;
        }
      } else {
        if (frame.curr != ACT_BACKWARD_FALL4) {
          nextAct = ACT_BACKWARD_FALL4;
        }
      }
    } else {
      if (frame.curr == ACT_FORWARD_FALLR) {
        // Can do nothing
      } else if (vy < -10.0) {
        if (frame.curr != ACT_FORWARD_FALL1) {
          nextAct = ACT_FORWARD_FALL1;
        }
      } else if (vy < 0.0) {
        if (frame.curr != ACT_FORWARD_FALL2) {
          nextAct = ACT_FORWARD_FALL2;
        }
      } else if (vy < 6.0) {
        if (input.do_j) {
          nextAct = ACT_ROWING1;
          flyingFlag = faceRight ? ROW_VN_1 : ROW_VP_1;
        } else
        if (frame.curr != ACT_FORWARD_FALL3) {
          nextAct = ACT_FORWARD_FALL3;
        }
      } else {
        if (frame.curr != ACT_FORWARD_FALL4) {
          nextAct = ACT_FORWARD_FALL4;
        }
      }
    }
    return nextAct;
  }

  private int moveDrink(int nextAct) {
    flyingFlag = NO_FLYING;
    List<Double> regen = weapon == null ? null : weapon.consume();
    if (regen != null) {
      mp += regen.get(0);
      hp += regen.get(1);
      hp2nd = Math.max(hp, hp2nd + regen.get(2));
    } else {
      nextAct = ACT_STANDING;
    }
    return nextAct;
  }

  private int moveFire(int nextAct) {
    flyingFlag = NO_FLYING;
    if (vy < 0.0) {
      if (frame.curr == ACT_DOWNWARD_FIRE || frame.curr == ACT_DOWNWARD_FIRE + 1) {
        nextAct = ACT_UPWARD_FIRE;
      }
    } else {
      if (frame.curr == ACT_UPWARD_FIRE || frame.curr == ACT_UPWARD_FIRE + 1) {
        nextAct = ACT_DOWNWARD_FIRE;
      }
    }
    return nextAct;
  }

  private int moveRow(int nextAct) {
    if ((flyingFlag & 1) == 1) {
      --flyingFlag;
      vy = Value_rowing_height;
      vx = flyingFlag == ROW_VP_0 ? Value_rowing_distance : -Value_rowing_distance;
    }
    buff.remove(Effect.LANDING_INJURY);
    return nextAct;
  }

  private int moveLying(int nextAct) {
    flyingFlag = NO_FLYING;
    if (0.0 >= hp) {
      nextAct = frame.curr;
    }
    return nextAct;
  }

  private int landing(boolean reboundable, boolean forward, double damage) {
    int nextAct = Const.TBA;
    if (reboundable && py < 0.0 && (vx > 10.0 || vx < -10.0 || vy > 1.0)) {
      vy = FALLING_BOUNCE_VY;
      nextAct = forward ? ACT_FORWARD_FALLR : ACT_BACKWARD_FALLR;
      hpLost(damage, false);
    } else {
      vy = 0.0;
      nextAct = forward ? ACT_LYING1 : ACT_LYING2;
    }
    if (buff.containsKey(Effect.LANDING_INJURY)) {
      hpLost(buff.remove(Effect.LANDING_INJURY).intValue, true);
    }
    return nextAct;
  }

  @Override
  protected int updateKinetic(int nextAct) {
    if (actLag != 0)
      return ACT_TBA;

    vx = frame.calcVX(vx, faceRight);
    px = buff.containsKey(Effect.MOVE_BLOCKING) ? px : (px + vx);
    // In LF2 even the frame with dvy = -1 causes the character flying for a while,
    // so dvy takes effect before the calculation of gravity.
    vy = frame.calcVY(vy);
    // dvz is not directly added to vz.
    if (buff.containsKey(Effect.MOVE_BLOCKING) || frame.dvz == Const.DV_550) {
      vz = 0.0;
    } else {
      pz += vz + (input.do_Z ? (input.do_D ? frame.dvz : -frame.dvz) : 0.0);
    }

    if (py + vy < 0.0) {
      py += vy;
      vy = env.applyGravity(vy);
      return ACT_TBA;
    }

    vx = env.applyFriction(vx * LANDING_VELOCITY_REMAIN);
    vz = env.applyFriction(vz * LANDING_VELOCITY_REMAIN);
    if (frame.state == State.FALL) {
      boolean reboundable = frame.curr != ACT_FORWARD_FALLR &&
                            frame.curr != ACT_FORWARD_FALL1;
      boolean forward = ACT_FORWARD_FALLR >= frame.curr && frame.curr >= ACT_FORWARD_FALL1;
      nextAct = landing(reboundable, forward, 0.0);
    } else if (frame.state == State.FIRE) {
      nextAct = landing(true, false, 0.0);
    } else if (frame.state == State.ICE) {
      // TODO: small fall
      nextAct = landing(true, false, ICED_FALLDOWN_DAMAGE);
    } else if (buff.containsKey(Effect.LANDING_ACT)) {
      nextAct = buff.remove(Effect.LANDING_ACT).intValue;
      vy = 0.0;
      flyingFlag = NO_FLYING;
    } else if ((frame.state == State.JUMP || frame.state == State.ROW) && (flyingFlag & 1) == 0) {
      nextAct = ACT_CROUCH1;
      vy = 0.0;
    } else if ((py < 0.0) || ((frame.state == State.DASH) && ((flyingFlag & 1) == 0))) {
      nextAct = ACT_CROUCH2;
      vy = 0.0;
      flyingFlag = NO_FLYING;
    } else {
      vy = 0.0;
    }
    if (buff.remove(Effect.SONATA) != null) {
      hpLost(SONATA_FALLDOWN_DAMAGE, false);
    }
    py = 0.0;
    return nextAct;
  }

  @Override
  protected int updateHealth(int nextAct) {
    if (0.0 >= hp) {
      hp = 0.0;
      hp2nd = Math.max(hp2nd, 0.0);
      return py < 0.0 ? ACT_TBA : vx >= 0.0 ? ACT_LYING1 : ACT_LYING2;
    }
    if (hp < hp2nd) {
      hp = Math.min(hp2nd, hp + hpReg);
    }
    if (mp < mpMax) {
      mp = Math.min(mpMax, mp + mpReg + Math.max(0.0, (hpMax - hp) / 300.0));
    }
    if (--hitSpan < 0) {
      if (dp > 0)  --dp;
      if (fp > 0)  --fp;
    }
    return ACT_TBA;
  }

  @Override
  protected int getNextActNumber() {
    Frame nextFrame = getFrame(frame.next);
    double[] hpmp = pseudoCast(nextFrame, true);
    if (hpmp == null) {  // no cost
      return frame.next;
    }
    Integer spare = frame.combo.get(Input.Combo.hit_d);
    if (hpmp[1] >= 0.0 || spare == null) {
      hp = hpmp[0];
      mp = hpmp[1];
      return frame.next;
    } else {
      return spare.intValue();
    }
  }

  @Override
  protected boolean adjustBoundary() {
    List<Double> xBound = env.getHeroXBound();
    List<Double> zBound = env.getZBound();
    px = Util.clamp(px, xBound.get(0), xBound.get(1));
    pz = Util.clamp(pz, zBound.get(0), zBound.get(1));
    return true;
  }

  @Override
  protected void updateItrs() {
    super.updateItrs();
    if (weapon != null) {
      itrList.addAll(weapon.getStrengthItrs(frame.wpoint.usage));
    }
    return;
  }

  @Override
  public Image getPortrait() {
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
  public void updateViewer(Viewer viewer) {
    if (hp > 0.0) {
      viewer.mpRatio = mp / mpMax;
      viewer.hpRatio = hp / hpMax;
      viewer.hp2ndRatio = hp2nd / hpMax;
    } else {
      viewer.mpRatio = 0.0;
      viewer.hpRatio = 0.0;
      viewer.hp2ndRatio = 0.0;
    }
    return;
  }

  @Override
  public void setController(Controller controller) {
    this.controller = controller;
    return;
  }

}
