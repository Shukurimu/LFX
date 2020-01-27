package lfx.object;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import lfx.object.AbstractObject;
import lfx.object.Hero;
import lfx.util.Act;
import lfx.util.Looper;

public class BaseHero extends AbstractObject implements Hero {
  /** Hidden flying-velocity status */
  private static final int NO_FLYING = 0;
  private static final int JUMP_V0_0 = 1 << 1;  // jump vertically
  private static final int JUMP_VP_0 = 2 << 1;  // jump with positive velocity
  private static final int JUMP_VN_0 = 3 << 1;  // jump with negative velocity
  private static final int DASH_RP_0 = 4 << 1;  // dash with positive velocity and facing right
  private static final int DASH_RN_0 = 5 << 1;  // dash with negative velocity and facing right
  private static final int DASH_LP_0 = 6 << 1;  // dash with positive velocity and facing left
  private static final int DASH_LN_0 = 7 << 1;  // dash with negative velocity and facing left
  private static final int  ROW_VP_0 = 8 << 1;  //  row with positive velocity
  private static final int  ROW_VN_0 = 9 << 1;  //  row with negative velocity
  private static final int JUMP_V0_1 = JUMP_V0_0 | 1;
  private static final int JUMP_VP_1 = JUMP_VP_0 | 1;
  private static final int JUMP_VN_1 = JUMP_VN_0 | 1;
  private static final int DASH_RP_1 = DASH_RP_0 | 1;
  private static final int DASH_RN_1 = DASH_RN_0 | 1;
  private static final int DASH_LP_1 = DASH_LP_0 | 1;
  private static final int DASH_LN_1 = DASH_LN_0 | 1;
  private static final int  ROW_VP_1 =  ROW_VP_0 | 1;
  private static final int  ROW_VN_1 =  ROW_VN_0 | 1;

  /** Hidden action frame counters */
  private final Looper walkingIndexer = new Looper(new int[] {2, 3, 2, 1, 0, 1});
  private final Looper runningIndexer = new Looper(new int[] {0, 1, 2, 1});

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
  /** Generation rates */
  private double hpReg = 1.0 / 12.0;
  private double mpReg = 1.0 / 3.00;
  private int flyingState = NO_FLYING;
  /** dp & fp will not be recovered if hit in 2 timeunit. */
  private int hitSpan = 0;
  private int dp = 0;  // defend point
  private int fp = 0;  // fall point
  private Input input = new Input();
  private Controller controller = null;
  private Weapon weapon = null;

  protected BaseHero(String identifier, Frame[] frameArray, String portraitPath,
                         Map<String, Double> stamina) {
    super(identifier, frameArray);
    portrait = loadImage(portraitPath);
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
    super(this);
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
  public Hero makeClone() {
    return new BaseHero(heroMapping.get(identifier));
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
  protected int getDefaultActNumber() {
    return py < 0.0 ? ACT_JUMPAIR :
           (weapon != null && weapon.isHeavy() ? ACT_HEAVY_WALK : ACT_STANDING);
  }

  /** Most of the time, potential HP is reduced by one-third of the received damage.
      Use `sync=true` to the situations not following this rule (e.g., throwinjury).
      Note that the lower bound is zero in LFX, which is different from LF2.
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

  /** Return remaining hp and mp, or null if no cost. */
  protected double[] pseudoCast(Frame target, boolean passive) {
    // TODO: Louis transform hp limit
    if (target == null || target.cost == 0 || env.isUnlimitedMode()) {
      return null;
    }
    if (passive) {  // Connected by next-tag.
      return new double[] {hp, mp + target.cost};
    } else {
      int hpCost = target.cost / 1000 * 10;
      int mpCost = target.cost - hpCost * 100;
      return new double[] {hp - hpCost, mp - mpCost};
    }
  }

  @Override
  protected int getScopeView(int targetTeamId) {
    return Global.getSideView(Global.SCOPE_VIEW_HERO, targetTeamId == this.teamId);
  }

  @Override
  protected void itrCallback() {
    System.out.println("itrCallback NotImplemented");
    return;
  }

  @Override
  public void react() {
    int nextAct = ACT_TBA;
    int bdefend = 0;
    int injury = 0;
    int fall = 0;
    int dvx = 0;
    int dvy = 0;
    int lag = 0;

    RESULT_LOOP:
    for (Tuple<Observable, Itr> tuple: resultItrList) {
      final Observable that = tuple.first;
      final Itr itr = tuple.second;
      switch (itr.effect) {
        case SONATA:
          vx = itr.sonataVxz(vx);
          vz = itr.sonataVxz(vz);
          vy = itr.sonataVy(py, vy);
          status.put(Extension.Kind.SONATA, new Extension(Integer.MAX_VALUE, 1.0));
          nextAct = vy < 0.0 ? ACT_FORWARD_FALL3 : ACT_FORWARD_FALL4;
          break;
        case LET_SPUNCH:
          status.put(Extension.Kind.ATTACK_SPUNCH, Extension.oneshot());
          continue RESULT_LOOP;
        case PICK:
          nextAct = weapon.type == Type.HEAVY ? ACT_PICK_HEAVY : ACT_PICK_LIGHT;
          // fall-through
        case ROLL_PICK:
          // will not affect current action
          continue RESULT_LOOP;
        case GRASP_DOP:
        case GRASP_BDY:
          if (graspee == this)
            fp = 0;
          continue RESULT_LOOP;
        case BLOCK:
          status.put(Extension.Kind.MOVE_BLOCKING, Extension.oneshot());
          continue RESULT_LOOP;
        case HEAL:
          status.put(Extension.Kind.HEALING, new Extension(itr.dvy, (double)(itr.injury / itr.dvy)));
          continue RESULT_LOOP;
        case VORTEX:
          vx += itr.vortexAx(px);
          vz += itr.vortexAz(pz);
          vy += itr.vortexAy(py, vy);
          continue RESULT_LOOP;
        default:
          System.out.printf("%s received unexpected Itr %s", this, itr);
      }
      bdefend += itr.bdefend;
      injury += itr.injury;
      fall += itr.fall;
      dvx += itr.calcDvx(vx);
      dvy += itr.dvy;
      lag = itr.calcLag(lag);
    }
    resultItrList.clear();

    hitSpan = 2;
    if (frame.state == State.ICED) {
    }
    // TODO: defend same direction
    if (frame.state == State.DEFEND) {
      hpLost(injury * DEFEND_INJURY_REDUCTION, false);
      vx = dvx * DEFEND_DVX_REDUCTION;
      if (hp == 0.0) {
        fp = dp = 0;
        nextAct = ACT_FORWARD_FALL1;
      } else if (dp > 30) {
        setCurr(Act_broken);
      } else if (frame.curr == Act_defend) {
        setCurr(Act_defendhit);
      }
    }
    hpLost(injury, false);
    vx = dvx;
    dp = NODEF_DP;
    fp = (fp + recvDmg.fall + 19) / 20 * 20;
    if (originalIced || (fp > 60) || ((fp > 20) && (py < 0.0)) || (hp == 0.0)) {
      fp = dp = 0;
      vy = recvDmg.dvy;
      setCurr(((recvDmg.dvx > 0.0) == faceRight) ? Act_bwfall1 : Act_fwfall1);
    } else if (fp > 40) {
      setCurr(Act_dop);
    } else if (fp > 20) {
      setCurr(((recvDmg.dvx > 0.0) == faceRight) ? Act_injure3 : Act_injure2);
    } else if (fp >= 0) {
      setCurr(Act_injure1);
    } else {
      /* negative fp causes NOP */
    }
    if (grasp != null) {
      grasp.state  = LFgrasp.State.DROP;
      grasp  = null;
    }

    if ((fp += recvDmg.fall) > 60) {
      grasp.state = LFgrasp.State.DROP;
      grasp = null;
      fp = 0;
      vx = recvDmg.dvx;
      vy = recvDmg.dvy;
      setCurr(((recvDmg.dvx > 0.0) == faceRight) ? Act_bwfall1 : Act_fwfall1);
    } else {
      setCurr((faceRight == (recvDmg.dvx > 0)) ?
        frame.cpoint.throwvx : frame.cpoint.throwvy);
    }
    if (frame.state != LFstate.DEFEND) {
      switch (recvDmg.effect) {
        case FIRE:
        case FIRE2:
        case EXFIRE:
        case SPFIRE:
          faceRight = recvDmg.dvx < 0.0;
          setCurr((vy < 0.0) ? Act_fireU : Act_fireD);
          break;
        case ICE:
        case ICE2:
        case EXICE:
          if (originalIced) {
            setCurr(((recvDmg.dvx > 0.0) == faceRight) ? Act_bwfall1 : Act_fwfall1);
          } else {
            faceRight = recvDmg.dvx < 0.0;
            setCurr(Act_ice);
          }
          break;
        case SPICE:
          faceRight = recvDmg.dvx < 0.0;
          setCurr(Act_ice);
            break;
          default:
            break;
      }
    }
    return nextAct;
  }

  @Override
  protected int updateAction(int nextAct) {
    switch (frame.state) {
      case STAND:  nextAct = moveStand();      break;
      case WALK:   nextAct = moveWalk();       break;
      case HWALK:  nextAct = moveHeavyWalk();  break;
      case RUN:    nextAct = moveRun();        break;
      case HRUN:   nextAct = moveHeavyRun();   break;
      case JUMP:   nextAct = moveJump();       break;
      case DASH:   nextAct = moveDash();       break;
      case LAND:   nextAct = moveLand();       break;
      case FIRE:   nextAct = moveFire();       break;
      case DRINK:  nextAct = moveDrink();      break;
      case ROW:    nextAct = moveRow();        break;
      case FALL:   nextAct = moveFall();       break;
      case LYING:  nextAct = moveLying();      break;
      default:
        flyingFlag = NO_FLYING;
    }
    /** You can change facing in this frame. */
    if (frame.curr == ACT_DEFEND && input.do_F) {
      faceRight = input.do_R;
    }
    return nextAct;
  }

  private int moveStand() {
    flyingFlag = NO_FLYING;
    if (ctrl.do_a) switch (weapon.type) {
      case NULL:
        nextAct = extra.containsKey(LFextra.Kind.LETSPUNCH) ?
              Act_spunch : ((Math.random() >= 0.5) ? Act_punch1 : Act_punch2);
        break;
      case LIGHT:
        nextAct = (Math.random() >= 0.5) ? Act_nrmwpatk1 : Act_nrmwpatk2;
        break;
      case SMALL:
        nextAct = Act_lgwpthw;
        break;
      case DRINK:
        nextAct = Act_drink;
        break;
      default:
        System.err.printf("%s hold %s in %s\n", identifier, weapon.type, frame.state);
        weapon = LFweapon.dummy;
    } else if (ctrl.do_j) {
      nextAct = Act_jump;
      if (ctrl.do_R) {
        flyingFlag = JUMP_VP_1;
        faceRight = true;
      } else if (ctrl.do_L) {
        flyingFlag = JUMP_VN_1;
        faceRight = false;
      } else {
        flyingFlag = JUMP_V0_1;
      }
    } else if (ctrl.do_d) {
      nextAct = Act_defend;
    } else if (ctrl.do_RR | ctrl.do_LL)  {
      nextAct = Act_running + runningIndexer.restart();
      faceRight = ctrl.do_RR;
    } else if (ctrl.do_F | ctrl.do_Z) {
      nextAct = Act_walking + walkingIndexer.restart();
      faceRight = ctrl.do_R || (ctrl.do_L ? false : faceRight);
    }
    return;
  }
  private int moveWalk() {
    flyingFlag = NO_FLYING;
    if (ctrl.do_a) switch (weapon.type) {
      case NULL:
        nextAct = extra.containsKey(LFextra.Kind.LETSPUNCH) ?
              Act_spunch : ((Math.random() >= 0.5) ? Act_punch1 : Act_punch2);
        break;
      case LIGHT:
        nextAct = (Math.random() >= 0.5) ? Act_nrmwpatk1 : Act_nrmwpatk2;
        break;
      case SMALL:
        nextAct = Act_lgwpthw;
        break;
      case DRINK:
        nextAct = Act_drink;
        break;
      default:
        System.err.printf("%s hold %s in %s\n", identifier, weapon.type, frame.state);
        weapon = LFweapon.dummy;
    } else if (ctrl.do_j) {
      nextAct = Act_jump;
      if (ctrl.do_R) {
        flyingFlag = JUMP_VP_1;
        faceRight = true;
      } else if (ctrl.do_L) {
        flyingFlag = JUMP_VN_1;
        faceRight = false;
      } else {
        flyingFlag = JUMP_V0_1;
      }
    } else if (ctrl.do_d) {
      nextAct = Act_defend;
    } else if (ctrl.do_RR | ctrl.do_LL)  {
      nextAct = Act_running + runningIndexer.restart();
      faceRight = ctrl.do_RR;
    } else if (ctrl.do_F | ctrl.do_Z) {
      if    (ctrl.do_R) { vx =  Value_walking_speed; faceRight = true;  }
      else if (ctrl.do_L) { vx = -Value_walking_speed; faceRight = false; }
      if    (ctrl.do_U)   vz = -Value_walking_speedz;
      else if (ctrl.do_D)   vz =  Value_walking_speedz;
      if (waitTU < 1) {
        nextAct = Act_walking + walkingIndexer.next();
      }
    }
    return;
  }
  private int moveHeavyWalk() {
    flyingFlag = NO_FLYING;
    if (ctrl.do_a) {
      nextAct = Act_hvwpthw;
    } else if (ctrl.do_RR | ctrl.do_LL)  {
      nextAct = Act_hvrunning + runningCycle[runningIndex = 0];
      faceRight = ctrl.do_RR;
    } else if (ctrl.do_F | ctrl.do_Z) {
      if    (ctrl.do_R) { vx =  Value_heavy_walking_speed; faceRight = true;  }
      else if (ctrl.do_L) { vx = -Value_heavy_walking_speed; faceRight = false; }
      if    (ctrl.do_U)   vz = -Value_heavy_walking_speedz;
      else if (ctrl.do_D)   vz =  Value_heavy_walking_speedz;
      if (waitTU < 1) {
        nextAct = Act_hvwalking + walkingIndexer.next();
      }
    }
    return;
  }
  private int moveRun() {
    flyingFlag = NO_FLYING;
    vx = faceRight ? Value_running_speed : (-Value_running_speed);
    if (ctrl.do_U) vz = -Value_running_speedz;
    if (ctrl.do_D) vz =  Value_running_speedz;
    if (ctrl.do_a) switch (weapon.type) {
      case NULL:
        nextAct = Act_runatk;
        break;
      case LIGHT:
        nextAct = ctrl.do_F ? Act_lgwpthw : Act_runwpatk;
        break;
      case SMALL:
        nextAct = Act_lgwpthw;
        break;
      case DRINK:
        nextAct = ctrl.do_F ? Act_lgwpthw : Act_drink;
        break;
    } else if (ctrl.do_j) {
      nextAct = Act_dash1;
      flyingFlag = faceRight ? DASH_RP_1 : DASH_LN_1;
    } else if (ctrl.do_d) {
      nextAct = Act_rolling;
    } else if ((faceRight && ctrl.do_L) || (!faceRight && ctrl.do_R)) {
      nextAct = Act_stoprun;
    } else if (waitTU < 1) {
      nextAct = Act_running + runningIndexer.next();
    }
    return;
  }
  private int moveHeavyRun() {
    flyingFlag = NO_FLYING;
    vx = faceRight ? Value_heavy_running_speed : (-Value_heavy_running_speed);
    if (ctrl.do_U)  vz = -Value_heavy_running_speedz;
    if (ctrl.do_D)  vz =  Value_heavy_running_speedz;
    if (ctrl.do_a) {
      nextAct = Act_hvwpthw;
    } else if ((faceRight && ctrl.do_L) || (!faceRight && ctrl.do_R)) {
      nextAct = Act_hvstoprun;
    } else if (waitTU < 1) {
      nextAct = Act_hvrunning + runningIndexer.next();
    }
    return;
  }
  private int moveJump() {
    if (((flyingFlag & 1) == 1) && (frame.curr == Act_jumpair)) {
      --flyingFlag;
      /* dvx is applied after friction reduction */
      switch (flyingFlag) {
        case JUMP_V0_0:
          vx = map.applyFriction(vx);
          break;
        case JUMP_VP_0:
          vx = map.applyFriction(vx + Value_jump_distance);
          break;
        case JUMP_VN_0:
          vx = map.applyFriction(vx - Value_jump_distance);
          break;
        default:
          System.err.println("State_jump: `flyingFlag' default");
      }
      vy += Value_jump_height;
      if (ctrl.do_U)  vz = -Value_jump_distancez;
      if (ctrl.do_D)  vz =  Value_jump_distancez;
    } else if ((flyingFlag & 1) == 0) {
      if (ctrl.do_F)
        faceRight = ctrl.do_R;
      if (ctrl.do_a) switch (weapon.type) {
        case NULL:
          nextAct = Act_jumpatk;
          break;
        case LIGHT:
          nextAct = ctrl.do_F ? Act_skywpthw : Act_jumpwpatk;
          break;
        case SMALL:
        case DRINK:
          nextAct = Act_skywpthw;
          break;
      }
    }
    return;
  }
  private int moveDash() {
    if ((flyingFlag & 1) == 1) {
      --flyingFlag;
      switch (flyingFlag) {
        case DASH_RP_0:
        case DASH_LP_0:
          vx = map.applyFriction( Value_dash_distance);
          break;
        case DASH_LN_0:
        case DASH_RN_0:
          vx = map.applyFriction(-Value_dash_distance);
          break;
        default:
          System.err.println("State_dash: `flyingFlag' default");
      }
      vy += Value_dash_height;
      if (ctrl.do_U) vz = -Value_dash_distancez;
      if (ctrl.do_D) vz =  Value_dash_distancez;
    } else if (ctrl.do_a && ((flyingFlag & 1) == 0)) {
      if ((flyingFlag == DASH_RP_0) || (flyingFlag == DASH_LN_0)) switch (weapon.type) {
        case NULL:
          nextAct = Act_dashatk;
          break;
        case LIGHT:
          nextAct = Act_dashwpatk;
          break;
        case SMALL:
        case DRINK:
          nextAct = Act_skywpthw;
          break;
      }
    } else if ((flyingFlag & 1) == 0) {
      switch (flyingFlag) {
        case DASH_RP_0:
          if (ctrl.do_L) {
            faceRight = false;
            nextAct = Act_dash2;
            flyingFlag = DASH_LP_0;
          }
          break;
        case DASH_LN_0:
          if (ctrl.do_R) {
            faceRight = true;
            nextAct = Act_dash2;
            flyingFlag = DASH_RN_0;
          }
          break;
        case DASH_LP_0:
          if (ctrl.do_R) {
            faceRight = true;
            nextAct = Act_dash1;
            flyingFlag = DASH_RP_0;
          }
          break;
        case DASH_RN_0:
          if (ctrl.do_L) {
            faceRight = false;
            nextAct = Act_dash1;
            flyingFlag = DASH_LN_0;
          }
          break;
        default:
          System.err.println("State_dash: `flyingFlag' default");
      }
    }
    return;
  }
  private int moveLand() {
    if (ctrl.do_d) {
      nextAct = Act_rolling;
      flyingFlag = NO_FLYING;
    } else if (ctrl.do_j) {
      if (ctrl.do_F) {
        nextAct = Act_dash1;
        if (ctrl.do_R) {
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
              nextAct = Act_dash1;
              flyingFlag = DASH_RP_1;
            } else {
              nextAct = Act_dash2;
              flyingFlag = DASH_LP_1;
            }
            break;
          case JUMP_VN_0:
          case ROW_VN_0:
            if (faceRight) {
              nextAct = Act_dash2;
              flyingFlag = DASH_RN_1;
            } else {
              nextAct = Act_dash1;
              flyingFlag = DASH_LN_1;
            }
            break;
          case JUMP_V0_0:
            /* this situation may happen because of the friction */
            nextAct = Act_jump;
            flyingFlag = JUMP_V0_1;
            break;
          default:
            System.err.printf("State_land: `flyingFlag' %d\n", flyingFlag);
        }
      } else if (waitTU < 2) {
        nextAct = Act_jump;
        flyingFlag = JUMP_V0_1;
      }
    } else if (ctrl.do_Z) {
      flyingFlag = NO_FLYING;
      vz = ctrl.do_U ? (-Value_walking_speedz) : Value_walking_speedz;
      if (waitTU < 2)
        nextAct = Act_walking + walkingCycle[walkingIndex = 0];
    } else if (ctrl.do_F) {
      flyingFlag = NO_FLYING;
      if (waitTU < 2)
        nextAct = Act_walking + walkingCycle[walkingIndex = 0];
    } else {
      flyingFlag = NO_FLYING;
    }
    return;
  }
  private int moveFall() {
    if (hitLag > 0 || extra.containsKey(LFextra.Kind.SONATA)) {
      /* NOP */
    } else if ((Act_bwfallR >= frame.curr && frame.curr >= Act_bwfall1)) {
      if (frame.curr == Act_bwfallR) {
        /* NOP */
      } else if (vy < -10.0) {
        if (frame.curr != Act_bwfall1)
          nextAct = Act_bwfall1;
      } else if (vy < 0.0) {
        if (frame.curr != Act_bwfall2)
          nextAct = Act_bwfall2;
      } else if (vy < 6.0) {
        if (ctrl.do_j) {
          nextAct = Act_rowing2;
          flyingFlag = faceRight ? ROW_VP_1 : ROW_VN_1;
        } else
        if (frame.curr != Act_bwfall3)
          nextAct = Act_bwfall3;
      } else {
        if (frame.curr != Act_bwfall4)
          nextAct = Act_bwfall4;
      }
    } else {
      if (frame.curr == Act_fwfallR) {
        /* NOP */
      } else if (vy < -10.0) {
        if (frame.curr != Act_fwfall1)
          nextAct = Act_fwfall1;
      } else if (vy < 0.0) {
        if (frame.curr != Act_fwfall2)
          nextAct = Act_fwfall2;
      } else if (vy < 6.0) {
        if (ctrl.do_j) {
          nextAct = Act_rowing1;
          flyingFlag = faceRight ? ROW_VN_1 : ROW_VP_1;
        } else
        if (frame.curr != Act_fwfall3)
          nextAct = Act_fwfall3;
      } else {
        if (frame.curr != Act_fwfall4)
          nextAct = Act_fwfall4;
      }
    }
    return;
  }
  private int moveDrink() {
    flyingFlag = NO_FLYING;
    /* in LFX drinking action will be cancalled if the weapon is not drink type */
    if (weapon.type == LFtype.DRINK) {
      double[] reg = weapon.drink();
      mp += reg[0];
      hp += reg[1];
      hp2nd += reg[2];
      if (hp2nd < hp)
        hp2nd = hp;
    } else
      nextAct = Act_standing;
    return;
  }
  private int moveFire() {
    flyingFlag = NO_FLYING;
    if (vy < 0.0 && ((frame.curr == Act_fireD) || (frame.curr == Act_fireD + 1)))
      nextAct = Act_fireU;
    if (vy > 0.0 && ((frame.curr == Act_fireU) || (frame.curr == Act_fireU + 1)))
      nextAct = Act_fireD;
    return;
  }
  private int moveRow() {
    extra.remove(LFextra.Kind.THROWINJURY);
    if ((flyingFlag & 1) == 1) {
      --flyingFlag;
      vy = Value_rowing_height;
      vx = (flyingFlag == ROW_VP_0) ? Value_rowing_distance : (-Value_rowing_distance);
    }
    return;
  }
  private int moveLying() {
    if (0.0 >= hp)
      nextAct = frame.curr;
    flyingFlag = NO_FLYING;
    return;
  }

  private int landing(boolean reboundable, boolean forward, double damage) {
    if (reboundable && py < 0.0 && (vx > 10.0 || vx < -10.0 || vy > 1.0)) {
      vy = FALLING_BOUNCE_VY;
      nextAct = forward ? Act_fwfallR : Act_bwfallR;
      hpLost(damage, false);
    } else {
      vy = 0.0;
      nextAct = forward ? Act_lying1 : Act_lying2;
    }
    if (extra.containsKey(LFextra.Kind.THROWINJURY))
      hpLost(extra.remove(LFextra.Kind.THROWINJURY).intValue, true);
    return nextAct;
  }

  @Override
  protected int updateKinetic(int nextAct) {
    if (hitLag != 0)
      return ACT_TBA;

    vx = frame.calcVX(vx, faceRight);
    /** In LF2 you can step a small distance even if blocked by stone.
        Besides, you will not walk faster while keeping pressing key.
        Therefore, the effect of keys is a force move, nothing to do with velocity. */
    px = extra.containsKey(LFextra.Kind.MOVEBLOCK) ? px : (px + vx);
    /** In LF2 even the frame with dvy = -1 causes the character flying for a while,
        so dvy takes effect before the calculation of gravity. */
    vy = frame.calcVY(vy);
    /** dvz is not directly added to vz */
    if (extra.containsKey(LFextra.Kind.MOVEBLOCK) || (frame.dvz == LFframe.DV_550))
      vz = 0.0;
    else
      pz += vz + (ctrl.do_Z ? (ctrl.do_U ? (-frame.dvz) : frame.dvz) : 0.0);

    if (py + vy < 0.0) {
      py += vy;
      vy += map.gravity;
      return ACT_TBA;
    }

    vx = map.applyFriction(vx * LANDING_VELOCITY_REMAIN);
    vz = map.applyFriction(vz * LANDING_VELOCITY_REMAIN);
    if (frame.state == State.FALL) {
      boolean reboundable = frame.curr != ACT_FORWARD_FALLR &&
                            frame.curr != ACT_FORWARD_FALL1;
      boolean forward = ACT_FORWARD_FALLR >= frame.curr && frame.curr >= ACT_FORWARD_FALL1;
      nextAct = landing(reboundable, forward, 0.0);
    } else if (frame.state == State.FIRE) {
      nextAct = landing(true, false, 0.0);
    } else if (frame.state == LFstate.ICE) {
      nextAct = landing(true, false, ICED_FALLDOWN_DAMAGE);
    } else if (extra.containsKey(LFextra.Kind.LANDING)) {
      nextAct = extra.remove(LFextra.Kind.LANDING).intValue;
      vy = 0.0;
      flyingFlag = NO_FLYING;
    } else if ((frame.state == LFstate.JUMP || frame.state == LFstate.ROW) && (flyingFlag & 1) == 0) {
      nextAct = Act_crouch1;
      vy = 0.0;
    } else if ((py < 0.0) || ((frame.state == LFstate.DASH) && ((flyingFlag & 1) == 0))) {
      nextAct = Act_crouch2;
      vy = 0.0;
      flyingFlag = NO_FLYING;
    } else {
      vy = 0.0;
    }
    if (extra.remove(LFextra.Kind.SONATA) != null)
      hpLost(SONATA_FALLDOWN_DAMAGE, false);
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
      if (dp > 0)
        --dp;
      if (fp > 0)
        --fp;
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
    Integer spare = frame.combo.get(hit_d);
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
    double[] xzBound = env.getXzBound();
    px = Global.clamp(px, xzBound[0], xzBound[1]);
    pz = Global.clamp(pz, xzBound[2], xzBound[3]);
    return true;
  }

  @Override
  protected void updateItrs() {
    super().updateItrs();
    if (weapon != null) {
      itrList.addAll(weapon.getStrengthItrs(frame.wpoint.strength));
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
  public void setController(Controller controller) {
    this.controller = controller;
    return;
  }

  @Override
  public void updateViewer(Viewport viewer) {
    viewer.faceRight = faceRight;
    viewer.px = px;
    viewer.py = py;
    viewer.pz = pz;
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

}
/*

      case CATCH:
        if (grasp != null) switch (grasp.state) {
          case CATCHING:
            if ((ctrl.do_F & ctrl.do_a) && (frame.cpoint.taction != NOP)) {
              nextAct = frame.cpoint.taction;
            } else if (ctrl.do_a && (frame.cpoint.aaction != NOP)) {
              nextAct = frame.cpoint.aaction;
            } else if (ctrl.do_j && (frame.cpoint.jaction != NOP)) {
              nextAct = frame.cpoint.jaction;
            }
            if (frame.cpoint.dircontrol != 0) {
              if (ctrl.do_R)  faceRight = (frame.cpoint.dircontrol > 0);
              if (ctrl.do_L)  faceRight = (frame.cpoint.dircontrol < 0);
            }
            break;
          case TIMEUP:
          case DROP:
            nextAct = Act_999;
          case THROW_START:
          case THROW_END:
            grasp = null;
            break;
        }
        break;
      case CAUGHT:
        if (grasp != null) switch (grasp.state) {
          case CATCHING:
          case THROW_END:
            break;
          case TIMEUP:
            grasp = null;
            nextAct = Act_fwfall2;
            vx = faceRight ? -CAUGHT_TIMEUP_DVX : CAUGHT_TIMEUP_DVX;
            vy = CAUGHT_TIMEUP_DVY;
            break;
          case DROP:
            grasp = null;
            nextAct = Act_jumpair;
            vx = 0.0;
            vy = CAUGHT_DROP_DVY;
            break;
        }
        break;

*/