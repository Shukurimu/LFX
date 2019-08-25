package lfx.object;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import lfx.component.Type;
import lfx.object.AbstractObject;
import lfx.util.Act;
import lfx.util.Looper;

// https://lf-empire.de/lf2-empire/data-changing/types/167-effect-0-characters
// http://lf2.wikia.com/wiki/Health_and_mana

abstract class AbstractHero extends AbstractObject {
  /** The defend point is set to NODEF_DP if got hit not in defend state. */
  public static final int NODEF_DP = 45;
  public static final double DEFEND_INJURY_REDUCTION = 0.10;
  public static final double DEFEND_DVX_REDUCTION = 0.10;
  public static final double FALLING_BOUNCE_VY = -4.25;  // guess-value
  public static final double LANDING_VELOCITY_REMAIN = 0.5;  // guess-value
  public static final double CONTROL_VZ = 2.5;  // press U or D; test-value
  public static final double DIAGONAL_VX_RATIO = 1.0 / 1.4;  // test-value

  /** dp and fp will not be recovered if hit in 2 TimeUnit. */
  private int lastHitTime = 0;
  /** Hidden action frame counter */
  private final Looper walkingIndexer = Looper.generator(new int[] {2, 3, 2, 1, 0, 1});
  private final Looper runningIndexer = Looper.generator(new int[] {0, 1, 2, 1});
  protected final Set<Combo> = new EnumSet<>();
  protected int dp = 0;  // defend point
  protected int fp = 0;  // fall point
  protected int dpReg = 1;
  protected int fpReg = 1;
  protected double hpReg = 1.0 / 12.0;
  protected double mpReg = 1.0 / 3.00;
  protected double Value_walking_speed  = 0.0;
  protected double Value_walking_speedz = 0.0;
  protected double Value_walking_speedx = 0.0;
  protected double Value_running_speed  = 0.0;
  protected double Value_running_speedz = 0.0;
  protected double Value_running_speedx = 0.0;
  protected double Value_heavy_walking_speed  = 0.0;
  protected double Value_heavy_walking_speedz = 0.0;
  protected double Value_heavy_walking_speedx = 0.0;
  protected double Value_heavy_running_speed  = 0.0;
  protected double Value_heavy_running_speedz = 0.0;
  protected double Value_heavy_running_speedx = 0.0;
  protected double Value_jump_height     = 0.0;
  protected double Value_jump_distance   = 0.0;
  protected double Value_jump_distancez  = 0.0;
  protected double Value_dash_height     = 0.0;
  protected double Value_dash_distance   = 0.0;
  protected double Value_dash_distancez  = 0.0;
  protected double Value_rowing_height   = 0.0;
  protected double Value_rowing_distance = 0.0;
  /** Hidden flying-velocity-related status.
      lsb indicates whether the velocity will be applied. */
  private int flyState = NO_FLYING;
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

  protected AbstractHero(String identifier, List<Frame> frameList) {
    super(Type.HERO, identifier, frameList);
  }

  protected AbstractHero(AbstractHero baseHero) {
    super(this);
  }

  /** used in StatusBar */
  public final void fillStatus(double[] values) {
    values[0] = hp;
    values[1] = hp2nd;
    values[2] = hpMax;
    values[3] = mp;
    values[4] = mp2nd;
    return;
  }

  @Override
  public final void revive() {
    hp = hp2nd = hpMax;
    mp = mpMax;
    return;
  }

  @Override
  protected final CanonicalAct getCanonicalAct(int action) {
    return new CanonicalAct(action, wpunion.type == Type.HEAVY ? ACT_HEAVY_WALK :
                                    py >= 0.0 ? ACT_STANDING : ACT_JUMPAIR);
  }

  public final boolean checkComboCost(Combo combo, Frame comboFrame) {
    // TODO: Louis transform hp limit
    if (comboFrame.cost == 0 || GlobalVariable.isUnlimitedMode)
      return true;
    if (comboFrame.cost < 1000)
      return mp >= comboFrame.cost;
    return mp >= comboFrame.cost % 1000 && hp > comboFrame.cost / 1000 * 10;
  }

  /** Most of the time, potential HP is reduced by one-third of the received damage.
      Use `sync=true` to the situations not following this rule (e.g., throwinjury).
      Note that the lower bound is zero in LFX, which is different from LF2. */
  public final void hpLost(double injury, boolean sync) {
    hp -= injury;
    hp2nd -= sync ? injury : Math.floor(injury / 3.0);
    if (hp < 0.0) {
      hp = 0.0;
      hp2nd = Math.max(hp2nd, 0.0);
    }
    return;
  }

  /** Return false if consumable goes empty. */
  public final boolean consume() {
    switch (wpunion.identifier) {
      case "Milk":
        hp2nd = Math.max(hpMax, hp2nd + 0.8);
        hp = Math.max(hp2nd, hp + 1.60);
        mp = Math.max(mpMax, mp + 1.67);
        wpunion.mp -= 1.67;
        break;
      case "Beer":
        mp = Math.max(mpMax, mp + 6.00);
        wpunion.mp -= 6.00;
        break;
      default:
        // You can drink anything in LF2.
        System.err.printf("%s consumes %s.\n", this, wpunion);
    }
    if (wpunion.mp < 0.0) {
      wpunoin.hp = 0.0;
      return false;
    }
    return true;
  }

  @Override
  public final void react() {
    int nextAct = ACT_TBA;
    int bdefend = 0;
    int injury = 0;
    int fall = 0;
    int dvx = 0;
    int dvy = 0;
    int lag = 0;

    RESULT_LOOP:
    for (Tuple<AbstractObject, Itr> tuple: resultItrList) {
      final AbstractObject that = tuple.first;
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

    lastHitTime = map.mapTime;
    if (currFrame.state == State.ICED) {
    }
    // TODO: defend same direction
    if (currFrame.state == State.DEFEND) {
      hpLost(injury * DEFEND_INJURY_REDUCTION, false);
      vx = dvx * DEFEND_DVX_REDUCTION;
      if (hp == 0.0) {
        fp = dp = 0;
        nextAct = ACT_FORWARD_FALL1;
      } else if (dp > 30) {
        setCurr(Act_broken);
      } else if (currFrame.curr == Act_defend) {
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
        currFrame.cpoint.throwvx : currFrame.cpoint.throwvy);
    }
    if (currFrame.state != LFstate.DEFEND) {
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
  public boolean move2(int nextAct) {
    int nextAct = ACT_TBA;
    switch (currFrame.state) {
      case STAND:
        flyState = NO_FLYING;
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
            System.err.printf("%s hold %s in %s\n", identifier, weapon.type, currFrame.state);
            weapon = LFweapon.dummy;
        } else if (ctrl.do_j) {
          nextAct = Act_jump;
          if (ctrl.do_R) {
            flyState = JUMP_VP_1;
            faceRight = true;
          } else if (ctrl.do_L) {
            flyState = JUMP_VN_1;
            faceRight = false;
          } else {
            flyState = JUMP_V0_1;
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
        break;
      case WALK:
        flyState = NO_FLYING;
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
            System.err.printf("%s hold %s in %s\n", identifier, weapon.type, currFrame.state);
            weapon = LFweapon.dummy;
        } else if (ctrl.do_j) {
          nextAct = Act_jump;
          if (ctrl.do_R) {
            flyState = JUMP_VP_1;
            faceRight = true;
          } else if (ctrl.do_L) {
            flyState = JUMP_VN_1;
            faceRight = false;
          } else {
            flyState = JUMP_V0_1;
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
        break;
      case HWALK:
        flyState = NO_FLYING;
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
        break;
      case RUN:
        flyState = NO_FLYING;
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
          flyState = faceRight ? DASH_RP_1 : DASH_LN_1;
        } else if (ctrl.do_d) {
          nextAct = Act_rolling;
        } else if ((faceRight && ctrl.do_L) || (!faceRight && ctrl.do_R)) {
          nextAct = Act_stoprun;
        } else if (waitTU < 1) {
          nextAct = Act_running + runningIndexer.next();
        }
        break;
      case HRUN:
        flyState = NO_FLYING;
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
        break;
      case JUMP:
        if (((flyState & 1) == 1) && (currFrame.curr == Act_jumpair)) {
          --flyState;
          /* dvx is applied after friction reduction */
          switch (flyState) {
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
              System.err.println("State_jump: `flyState' default");
          }
          vy += Value_jump_height;
          if (ctrl.do_U)  vz = -Value_jump_distancez;
          if (ctrl.do_D)  vz =  Value_jump_distancez;
        } else if ((flyState & 1) == 0) {
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
        break;
      case DASH:
        if ((flyState & 1) == 1) {
          --flyState;
          switch (flyState) {
            case DASH_RP_0:
            case DASH_LP_0:
              vx = map.applyFriction( Value_dash_distance);
              break;
            case DASH_LN_0:
            case DASH_RN_0:
              vx = map.applyFriction(-Value_dash_distance);
              break;
            default:
              System.err.println("State_dash: `flyState' default");
          }
          vy += Value_dash_height;
          if (ctrl.do_U) vz = -Value_dash_distancez;
          if (ctrl.do_D) vz =  Value_dash_distancez;
        } else if (ctrl.do_a && ((flyState & 1) == 0)) {
          if ((flyState == DASH_RP_0) || (flyState == DASH_LN_0)) switch (weapon.type) {
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
        } else if ((flyState & 1) == 0) {
          switch (flyState) {
            case DASH_RP_0:
              if (ctrl.do_L) {
                faceRight = false;
                nextAct = Act_dash2;
                flyState = DASH_LP_0;
              }
              break;
            case DASH_LN_0:
              if (ctrl.do_R) {
                faceRight = true;
                nextAct = Act_dash2;
                flyState = DASH_RN_0;
              }
              break;
            case DASH_LP_0:
              if (ctrl.do_R) {
                faceRight = true;
                nextAct = Act_dash1;
                flyState = DASH_RP_0;
              }
              break;
            case DASH_RN_0:
              if (ctrl.do_L) {
                faceRight = false;
                nextAct = Act_dash1;
                flyState = DASH_LN_0;
              }
              break;
            default:
              System.err.println("State_dash: `flyState' default");
          }
        }
        break;
      case LAND:
        if (ctrl.do_d) {
          nextAct = Act_rolling;
          flyState = NO_FLYING;
        } else if (ctrl.do_j) {
          if (ctrl.do_F) {
            nextAct = Act_dash1;
            if (ctrl.do_R) {
              faceRight = true;
              flyState = DASH_RP_1;
            } else {
              faceRight = false;
              flyState = DASH_LN_1;
            }
          } else if (vx != 0.0) {
            switch (flyState) {
              case JUMP_VP_0:
              case ROW_VP_0:
                if (faceRight) {
                  nextAct = Act_dash1;
                  flyState = DASH_RP_1;
                } else {
                  nextAct = Act_dash2;
                  flyState = DASH_LP_1;
                }
                break;
              case JUMP_VN_0:
              case ROW_VN_0:
                if (faceRight) {
                  nextAct = Act_dash2;
                  flyState = DASH_RN_1;
                } else {
                  nextAct = Act_dash1;
                  flyState = DASH_LN_1;
                }
                break;
              case JUMP_V0_0:
                /* this situation may happen because of the friction */
                nextAct = Act_jump;
                flyState = JUMP_V0_1;
                break;
              default:
                System.err.printf("State_land: `flyState' %d\n", flyState);
            }
          } else if (waitTU < 2) {
            nextAct = Act_jump;
            flyState = JUMP_V0_1;
          }
        } else if (ctrl.do_Z) {
          flyState = NO_FLYING;
          vz = ctrl.do_U ? (-Value_walking_speedz) : Value_walking_speedz;
          if (waitTU < 2)
            nextAct = Act_walking + walkingCycle[walkingIndex = 0];
        } else if (ctrl.do_F) {
          flyState = NO_FLYING;
          if (waitTU < 2)
            nextAct = Act_walking + walkingCycle[walkingIndex = 0];
        } else {
          flyState = NO_FLYING;
        }
        break;
      case FIRE:
        flyState = NO_FLYING;
        if (vy < 0.0 && ((currFrame.curr == Act_fireD) || (currFrame.curr == Act_fireD + 1)))
          nextAct = Act_fireU;
        if (vy > 0.0 && ((currFrame.curr == Act_fireU) || (currFrame.curr == Act_fireU + 1)))
          nextAct = Act_fireD;
        break;
      case DRINK:
        flyState = NO_FLYING;
        /* in LFX drinking action will be cancalled if the weapon is not drink type */
        if (weapon.type == LFtype.DRINK) {
          double[] reg = weapon.drink();
          mp  += reg[0];
          hp  += reg[1];
          hp2nd += reg[2];
          if (hp2nd < hp)
            hp2nd = hp;
        } else
          nextAct = Act_standing;
        break;
      case ROW:
        extra.remove(LFextra.Kind.THROWINJURY);
        if ((flyState & 1) == 1) {
          --flyState;
          vy = Value_rowing_height;
          vx = (flyState == ROW_VP_0) ? Value_rowing_distance : (-Value_rowing_distance);
        }
        break;
      case CATCH:
        if (grasp != null) switch (grasp.state) {
          case CATCHING:
            if ((ctrl.do_F & ctrl.do_a) && (currFrame.cpoint.taction != NOP)) {
              nextAct = currFrame.cpoint.taction;
            } else if (ctrl.do_a && (currFrame.cpoint.aaction != NOP)) {
              nextAct = currFrame.cpoint.aaction;
            } else if (ctrl.do_j && (currFrame.cpoint.jaction != NOP)) {
              nextAct = currFrame.cpoint.jaction;
            }
            if (currFrame.cpoint.dircontrol != 0) {
              if (ctrl.do_R)  faceRight = (currFrame.cpoint.dircontrol > 0);
              if (ctrl.do_L)  faceRight = (currFrame.cpoint.dircontrol < 0);
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
      case FALL:
        if (hitLag > 0 || extra.containsKey(LFextra.Kind.SONATA)) {
          /* NOP */
        } else if ((Act_bwfallR >= currFrame.curr && currFrame.curr >= Act_bwfall1)) {
          if (currFrame.curr == Act_bwfallR) {
            /* NOP */
          } else if (vy < -10.0) {
            if (currFrame.curr != Act_bwfall1)
              nextAct = Act_bwfall1;
          } else if (vy < 0.0) {
            if (currFrame.curr != Act_bwfall2)
              nextAct = Act_bwfall2;
          } else if (vy < 6.0) {
            if (ctrl.do_j) {
              nextAct = Act_rowing2;
              flyState = faceRight ? ROW_VP_1 : ROW_VN_1;
            } else
            if (currFrame.curr != Act_bwfall3)
              nextAct = Act_bwfall3;
          } else {
            if (currFrame.curr != Act_bwfall4)
              nextAct = Act_bwfall4;
          }
        } else {
          if (currFrame.curr == Act_fwfallR) {
            /* NOP */
          } else if (vy < -10.0) {
            if (currFrame.curr != Act_fwfall1)
              nextAct = Act_fwfall1;
          } else if (vy < 0.0) {
            if (currFrame.curr != Act_fwfall2)
              nextAct = Act_fwfall2;
          } else if (vy < 6.0) {
            if (ctrl.do_j) {
              nextAct = Act_rowing1;
              flyState = faceRight ? ROW_VN_1 : ROW_VP_1;
            } else
            if (currFrame.curr != Act_fwfall3)
              nextAct = Act_fwfall3;
          } else {
            if (currFrame.curr != Act_fwfall4)
              nextAct = Act_fwfall4;
          }
        }
        break;
      case LYING:
        if (0.0 >= hp)
          nextAct = currFrame.curr;
        flyState = NO_FLYING;
        break;
      case TRY_TRANSFORM:
        if (transf == null)
          nextAct = Act_notransform;
        break;
      default:
        flyState = NO_FLYING;
    }

    /** You can change facing in this frame. */
    if (currFrame.curr == ACT_DEFEND && key.F)
      faceRight = key.R;

    return true;
  }

  @Override
  public boolean updateMovement() {
    /* block all velocity and position changes if the chatacter in hitlag duration */
    if ((grasp == null || grasp.isNotCaught(this)) && hitLag == 0) {
      vx = currFrame.calcVX(vx, faceRight);
      px = extra.containsKey(LFextra.Kind.MOVEBLOCK) ? px : (px + vx);
      /* in original LF2 even the frame with dvy==-1 causes the character flying for a while
         so dvy takes effect before the calculation of gravity */
      vy = currFrame.calcVY(vy);
      /* dvz is not directly added to vz */
      if (extra.containsKey(LFextra.Kind.MOVEBLOCK) || (currFrame.dvz == LFframe.DV_550))
        vz = 0.0;
      else
        pz += vz + (ctrl.do_Z ? (ctrl.do_U ? (-currFrame.dvz) : currFrame.dvz) : 0.0);
      if (py + vy >= 0.0) {
        if (currFrame.state == LFstate.FALL || currFrame.state == LFstate.FIRE) {
          if ((py < 0.0) && (vx > 10.0 || vx < -10.0 || vy > 1.0) &&
            (currFrame.curr != Act_fwfallR && currFrame.curr != Act_bwfallR)) {
            /* rebounce */
            vy = FALLING_BOUNCE_VY;
            nextAct = (Act_bwfallR >= currFrame.curr && currFrame.curr >= Act_bwfall1) ? Act_bwfallR : Act_fwfallR;
          } else {
            vy = 0.0;
            nextAct = (Act_bwfallR >= currFrame.curr && currFrame.curr >= Act_bwfall1) ? Act_lying2 : Act_lying1;
          }
          vx = map.applyFriction(vx * LANDING_VELOCITY_REMAIN);
          vz = map.applyFriction(vz * LANDING_VELOCITY_REMAIN);
          if (extra.containsKey(LFextra.Kind.THROWINJURY))
            hpLost(extra.remove(LFextra.Kind.THROWINJURY).intValue, true);
        } else if (currFrame.state == LFstate.ICE) {
          if ((py < 0.0) && (vx > 10.0 || vx < -10.0 || vy > 1.0)) {
            /* broke ice and rebounce */
            hpLost(ICED_FALLDOWN_DAMAGE, false);
            vy = FALLING_BOUNCE_VY;
            nextAct = Act_fwfallR;
          } else {
            vx = map.applyFriction((py < 0.0) ? (vx * LANDING_VELOCITY_REMAIN) : vx);
            vz = map.applyFriction((py < 0.0) ? (vz * LANDING_VELOCITY_REMAIN) : vz);
            vy = 0.0;
          }
        } else if (extra.containsKey(LFextra.Kind.LANDING)) {
          nextAct = extra.remove(LFextra.Kind.LANDING).intValue;
          vx = map.applyFriction(vx * LANDING_VELOCITY_REMAIN);
          vz = map.applyFriction(vz * LANDING_VELOCITY_REMAIN);
          vy = 0.0;
          flyState = NO_FLYING;
        } else if ((currFrame.state == LFstate.JUMP || currFrame.state == LFstate.ROW) && ((flyState & 1) == 0)) {
          nextAct = Act_crouch1;
          vx = map.applyFriction(vx * LANDING_VELOCITY_REMAIN);
          vz = map.applyFriction(vz * LANDING_VELOCITY_REMAIN);
          vy = 0.0;
        } else if ((py < 0.0) || ((currFrame.state == LFstate.DASH) && ((flyState & 1) == 0))) {
          nextAct = Act_crouch2;
          vx = map.applyFriction(vx * LANDING_VELOCITY_REMAIN);
          vz = map.applyFriction(vz * LANDING_VELOCITY_REMAIN);
          vy = 0.0;
          flyState = NO_FLYING;
        } else {
          vx = map.applyFriction(vx);
          vz = map.applyFriction(vz);
          vy = 0.0;
        }
        if (extra.remove(LFextra.Kind.SONATA) != null)
          hpLost(SONATA_FALLDOWN_DAMAGE, false);
        py = 0.0;
      } else {
        py += vy;
        vy += map.gravity;
      }
    }
    return true;
  }

  @Override
  public boolean updateHealth() {
    if (hp > 0.0) {
      if (hp < hp2nd)
        hp = Math.min(hp2nd, hp + hpReg);
      if (mp < mpMax)
        mp = Math.min(mpMax, mp + mpReg + Math.max(0.0, (hpMax - hp) / 300.0));
      if (map.mapTime > lastHitTime + 2) {
        if (dp > 0)
          dp = Math.max(dp - dpDec, 0);
        if (fp > 0)
          fp = Math.max(fp - fpDec, 0);
      }
    }
    return true;
  }

  @Override
  public boolean updateFrame() {
    if (actLag > 0) {
      --actLag;
    } else if (nextAct != ACT_TBA) {
      setCurr(nextAct);
    } else if (--waitTU < 0) {
      LFframe nextFrame = getFrame(currFrame.next);
      if (nextFrame == null)
        return false;
      if (nextFrame.mpCost < 0 && !LFX.currMap.isUnlimitedMode()) {
        if (mp < -nextFrame.mpCost)/* out of mana */
          setCurr(currFrame.comboList[LFact.hit_d.index]);
        else {
          mp += nextFrame.mpCost;
          setNext(nextFrame);
        }
      } else {
        setNext(nextFrame);
      }
    }
    return true;
  }

  @Override
  protected boolean adjustBoundary(int[] xzBound) {
    px = Global.clamp(px, xzBound[0], xzBound[1]);
    pz = Global.clamp(pz, xzBound[2], xzBound[3]);
    return true;
  }

  @Override
  public List<Tuple<Itr, Area>> registerItrArea() {
    List<Tuple<Itr, Area>> result = new ArrayList<>();
    for (Itr itr: currFrame.itr) {
      if (itr.effect != Effect.THROW_ATK || extra.containsKey(LFextra.Kind.THROWINJURY))
        currItr.add(new LFitrarea(this, i));
    }
    if (wpunion != dummy && currFrame.wpoint.attacking) {
    }
    return;
  }

  @Override
  protected final void preprocess() {
    super.preprocess();
    Value_walking_speedx = Value_walking_speed * DIAGONAL_VX_RATIO;
    Value_running_speedx = Value_running_speed * DIAGONAL_VX_RATIO;
    Value_heavy_walking_speedx = Value_heavy_walking_speed * DIAGONAL_VX_RATIO * 0.5;
    Value_heavy_running_speedx = Value_heavy_running_speed * DIAGONAL_VX_RATIO * 0.5;
    return;
  }

}
