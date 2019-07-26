package lfx.object;

import java.util.Iterator;
import java.util.Map;
import lfx.util.Act;
import lfx.util.Numeric;

abstract class AbstractHero extends AbstractObject {
  /** The defend point is set to NODEF_DP if got hit not in defend state. */
  public static final int NODEF_DP = 45;
  public static final double DEFEND_INJURY_REDUCTION = 0.10;
  public static final double DEFEND_DVX_REDUCTION = 0.10;
  public static final double FALLING_BOUNCE_VY = -4.25;  // guess-value
  public static final double LANDING_VELOCITY_REMAIN = 0.5;  // guess-value
  public static final double CONTROL_VZ = 2.5;  // press U or D; test-value
  public static final double DIAGONAL_MOVE_VX_RATIO = 1.0 / 1.4;  // test-value

  private LFcontrol ctrl = LFcontrol.noControl;
  private int lastHitTime = 0;
  /** Hidden action frame counter */
  private final Numeric walkingIndexer = Numeric.generator(new int[] {2, 3, 2, 1, 0, 1});
  private final Numeric runningIndexer = Numeric.generator(new int[] {0, 1, 2, 1});
  protected Weapon weapon = Weapon.dummy;
  protected int dp = 0;  // defend point
  protected int fp = 0;  // fall point
  protected int dpReg = 1;
  protected int fpReg = 1;
  protected double hp2nd = 500.0;
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
  private int flyState = NOTFLYING;
  private static final int NOTFLYING = 0;
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
  /** Action number lookup */
  public static final int ACT_TRANSFORM_INVALID = ACT_DEF;
  public static final int ACT_TRANSFORM_BACK = 245;  // default
  public static final int ACT_STANDING = 0;
  public static final int ACT_WALKING = 5;
  public static final int ACT_RUNNING = 9;
  public static final int ACT_HEAVY_WALK = 12;
  public static final int ACT_HEAVY_RUN = 16;
  public static final int ACT_HEAVY_STOP_RUN = 19;
  public static final int ACT_WEAPON_ATK1 = 20;
  public static final int ACT_WEAPON_ATK2 = 25;
  public static final int ACT_JUMP_WEAPON_ATK = 30;
  public static final int ACT_RUN_WEAPON_ATK = 35;
  public static final int ACT_DASH_WEAPON_ATK = 40;
  public static final int ACT_LIGHT_WEAPON_THROW = 45;
  public static final int ACT_HEAVY_WEAPON_THROW = 50;
  public static final int ACT_SKY_WEAPON_THROW = 52;
  public static final int ACT_DRINK = 55;
  public static final int ACT_PUNCH1 = 60;
  public static final int ACT_PUNCH2 = 65;
  public static final int ACT_SUPER_PUNCH = 70;
  public static final int ACT_JUMP_ATK = 80;
  public static final int ACT_RUN_ATK = 85;
  public static final int ACT_DASH_ATK = 90;
  public static final int ACT_DASH_DEF = 95;
  public static final int ACT_ROLLING = 102;
  public static final int ACT_ROWING1 = 100;
  public static final int ACT_ROWING2 = 108;
  public static final int ACT_DEFEND = 110;
  public static final int ACT_DEFEND_HIT = 111;
  public static final int ACT_BROKEN_DEF = 112;
  public static final int ACT_PICK_LIGHT = 115;
  public static final int ACT_PICK_HEAVY = 116;
  public static final int ACT_CATCH = 120;
  public static final int ACT_CAUGHT = 130;
  public static final int ACT_FORWARD_FALL1 = 180;
  public static final int ACT_FORWARD_FALL2 = 181;
  public static final int ACT_FORWARD_FALL3 = 182;
  public static final int ACT_FORWARD_FALL4 = 183;
  public static final int ACT_FORWARD_FALL5 = 184;
  public static final int ACT_FORWARD_FALLR = 185;
  public static final int ACT_BACKWARD_FALL1 = 186;
  public static final int ACT_BACKWARD_FALL2 = 187;
  public static final int ACT_BACKWARD_FALL3 = 188;
  public static final int ACT_BACKWARD_FALL4 = 189;
  public static final int ACT_BACKWARD_FALL5 = 190;
  public static final int ACT_BACKWARD_FALLR = 191;
  public static final int ACT_ICE = 200;
  public static final int ACT_UPWARD_FIRE = 203;
  public static final int ACT_DOWNWARD_FIRE = 205;
  public static final int ACT_TIRED = 207;
  public static final int ACT_JUMP = 210;
  public static final int ACT_JUMPAIR = 212;
  public static final int ACT_DASH1 = 213;
  public static final int ACT_DASH2 = 214;
  public static final int ACT_CROUCH1 = 215;
  public static final int ACT_STOPRUN = 218;
  public static final int ACT_CROUCH2 = 219;
  public static final int ACT_INJURE1 = 220;
  public static final int ACT_INJURE2 = 222;
  public static final int ACT_INJURE3 = 224;
  public static final int ACT_DOP = 226;
  public static final int ACT_LYING1 = 230;
  public static final int ACT_LYING2 = 231;
  public static final int ACT_THROW_LYING_MAN = 232;
  public static final int ACT_DUMMY = 399;

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
  protected final Pair<Integer, Boolean> resolveAct(int index) {
    if (index < 0)
      index = -index;
    return index != ACT_DEF ? index :
           (weapon.type == Type.HEAVY ? ACT_HEAVY_WALK : (py >= 0 ? ACT_STANDING : ACT_JUMPAIR));
  }

  public final boolean castCombo(Combo combo, Frame comboFrame) {
    // TODO: Louis transform hp limit
    if (comboFrame.cost == 0 || GlobalVariable.isUnlimitedMode)
      return true;
    if (comboFrame.cost < 1000)
      return mp >= comboFrame.cost;
    return mp >= comboFrame.cost % 1000 && hp > comboFrame.cost / 1000 * 10;
  }

  /** Most of the time, potential HP is reduced by one-third of the received damage.
      Use `sync=true` for the situations not following this rule (e.g., throwinjury).
      Note: the lower bound is zero in LFX, which is different from LF2. */
  public final void hpLost(double injury, boolean sync) {
    hp -= injury;
    hp2nd -= sync ? injury : Math.floor(injury / 3.0);
    if (hp < 0.0) {
      hp = 0.0;
      hp2nd = Math.max(hp2nd, 0.0);
    }
    return;
  }

  @Override
  public boolean reactAndMove(LFmap map) {
    int nextAct = ACT_TBA;
    int bdefend = 0;
    int injury = 0;
    int fall = 0;
    int dvx = 0;
    int dvy = 0;
    for (Pair<AbstractObject, Itr> pair: resultItrList) {
      final AbstractObject that = pair.first;
      final Itr itr = pair.second;
      switch (itr.effect) {
        case LET_SPUNCH:
          status.put(Extension.Kind.ATTACK_SPUNCH, Extension.oneshot());
          break;
        case PICK:
          if (assertRaceCondition(this, that)) {
            weapon = what;
            nextAct = weapon.type == Type.HEAVY ? ACT_PICK_HEAVY : ACT_PICK_LIGHT;
          }
          break;
        case ROLL_PICK:
          if (assertRaceCondition(this, that))
            weapon = what;
          break;
        case GRASP_DOP:
        case GRASP_BDY:
          if (assertRaceCondition(this, that)) {
            // TODO: distinguish this & that
          }
          break;
        case BLOCK:
          status.put(Extension.Kind.MOVE_BLOCKING, Extension.oneshot());
          break;
        case SONATA:
          vx = itr.sonataVxz(vx);
          vz = itr.sonataVxz(vz);
          vy = itr.sonataVy(py, vy);
          hpLost(itr.injury, false);
          status.put(Extension.Kind.SONATA, new Extension(Integer.MAX_VALUE, 1.0));
          nextAct = vy < 0.0 ? ACT_FORWARD_FALL3 : ACT_FORWARD_FALL4;
          break;
        case HEAL:
          status.put(Extension.Kind.HEALING, new Extension(itr.dvy, (double)(itr.injury / itr.dvy)));
          break;
        case VORTEX:
          vx += itr.vortexAx(px);
          vz += itr.vortexAz(pz);
          vy += itr.vortexAy(py, vy);
          break;
        default:
          System.out.printf("%s received unexpected Itr %s", this, itr);
      }
    }
    resultItrList.clear();

    /* react to the damage */
    if (recvDmg.effect != LFeffect.NONE) {
      final boolean originalIced = (currFrame.state == LFstate.ICE);
      if (recvDmg.fall < 0) {
        hpLost(recvDmg.injury, false);
      } else if (grasp == null || grasp.isNotCaught(this)) {
        if (recvDmg.lag)
          hitLag = HITLAG_SPAN;
        lastHitTime = map.mapTime;
        /* defend front damage */
        if ((currFrame.state == LFstate.DEFEND) && ((recvDmg.center >= px) == faceRight)) {
          hpLost(recvDmg.injury * DEFEND_DMG_REDUCTION, false);
          vx = recvDmg.dvx * DEFEND_DVX_REDUCTION;
          dp += recvDmg.bdefend;
          if (hp == 0.0) {
            fp = dp = 0;
            setCurr(Act_fwfall1);
          } else if (dp > 30) {
            setCurr(Act_broken);
          } else if (currFrame.curr == Act_defend) {
            setCurr(Act_defendhit);
          }
        } else {
          hpLost(recvDmg.injury, false);
          vx = recvDmg.dvx;
          fp = (fp + recvDmg.fall + 19) / 20 * 20;
          dp = NODEF_DP;
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
        }
        /* the caught hero is drop if the you get hurt */
        if (grasp != null) {
          grasp.state  = LFgrasp.State.DROP;
          grasp  = null;
        }
      } else {/* get hurt while caught */
        if (recvDmg.lag)
          hitLag = HITLAG_SPAN;
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
    }
    recvDmg.reset();

    /* opoint is triggered only at the first timeunit */
    if (waitTU == currFrame.wait && !currFrame.opoint.isEmpty()) {
      for (LFopoint x: currFrame.opoint)
        map.spawnObject(x.launch(this, getControlZ() * OPOINT_DVZ));
    }

    /* get user input */
    if (ctrl.activated)
      ctrl.updateInput();

    currFrame.inputCombo(this, ctrl);


    /* We do not set the `new' currFrame directly, since there might be
       several reasons leading to other results. e.g., landing on ground. */
    int nextAct = ACT.UNASSIGNED;

    if (grasp != null)
      grasp.update(this);
    switch (currFrame.state) {
      case STAND:
        flyState = NOTFLYING;
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
        flyState = NOTFLYING;
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
        flyState = NOTFLYING;
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
        flyState = NOTFLYING;
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
        flyState = NOTFLYING;
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
          flyState = NOTFLYING;
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
          flyState = NOTFLYING;
          vz = ctrl.do_U ? (-Value_walking_speedz) : Value_walking_speedz;
          if (waitTU < 2)
            nextAct = Act_walking + walkingCycle[walkingIndex = 0];
        } else if (ctrl.do_F) {
          flyState = NOTFLYING;
          if (waitTU < 2)
            nextAct = Act_walking + walkingCycle[walkingIndex = 0];
        } else {
          flyState = NOTFLYING;
        }
        break;
      case FIRE:
        flyState = NOTFLYING;
        if (vy < 0.0 && ((currFrame.curr == Act_fireD) || (currFrame.curr == Act_fireD + 1)))
          nextAct = Act_fireU;
        if (vy > 0.0 && ((currFrame.curr == Act_fireU) || (currFrame.curr == Act_fireU + 1)))
          nextAct = Act_fireD;
        break;
      case DRINK:
        flyState = NOTFLYING;
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
        flyState = NOTFLYING;
        break;
      case TRY_TRANSFORM:
        if (transf == null)
          nextAct = Act_notransform;
        break;
      default:
        flyState = NOTFLYING;
    }

    /* you can change facing direction in `defend' frame */
    if ((currFrame.curr == Act_defend) && ctrl.do_F)
      faceRight = ctrl.do_R;

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
          flyState = NOTFLYING;
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
          flyState = NOTFLYING;
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

    /* recovery */
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

    boolean transformed = false;
    applyExtra();
    for (Iterator<Map.Entry<LFextra.Kind, LFextra>> eit = extra.entrySet().iterator(); eit.hasNext(); ) {
      Map.Entry<LFextra.Kind, LFextra> me = (Map.Entry<LFextra.Kind, LFextra>)eit.next();
      LFextra e = me.getValue();
      switch (me.getKey()) {
        case HEALING:
          // TODO: healing effect
          hp = Math.min(hp + e.doubleValue, hp2nd);
          break;
        case TELEPORT_ENEMY:
          {
            LFobject telTarget = map.chooseHero(this, false, true);
            if (telTarget != null) {
              px = telTarget.px + (faceRight ? -e.doubleValue : e.doubleValue);
              pz = telTarget.pz;
            }
            py = 0.0;
          }
          break;
        case TELEPORT_TEAM:
          {
            LFobject telTarget = map.chooseHero(this, true, false);
            if (telTarget != null) {
              px = telTarget.px + (faceRight ? -e.doubleValue : e.doubleValue);
              pz = telTarget.pz;
            }
            py = 0.0;
          }
          break;
        case TRANSFORM_TO:
          {
            transf = LFX.objPool.get(e.stringValue).makeCopy(faceRight, teamID);
            transf.initialization(px, py, pz, e.intValue);
            transf.origin = this;
            transformed = true;
            map.transform(this, transf);
          }
          break;
        case TRANSFORM_BACK:
          if (transf != null) {
            // TODO: should not display 1st picture (comboed frame processed in same timeunit?)
            // TODO: key consumed (rudolf should not goto punch action)
            transf.initialization(px, py, pz, e.intValue);
            transformed = true;
            map.transform(this, transf);
          }
          break;
        case ARMOUR:
          map.spawnObject(LFopoint.createArmour(this));
          break;
      }
      if (e.lapse())
        eit.remove();
    }
    if (transformed)
      return false;

    /* process next frame */
    if (hitLag > 0) {
      --hitLag;
    } else if (grasp != null && !grasp.isNotCaught(this)) {
      /* NOP */
    } else if (nextAct != DEFAULT_ACT) {
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
    registerItr();
    registerBdy();
    return true;
  }

  @Override
  protected boolean checkBoundary(LFmap map) {
    px = (px > map.xwidth)  ? map.xwidth  : ((px < 0.0) ? 0.0 : px);
    pz = (pz > map.zboundB) ? map.zboundB : ((pz < map.zboundT) ? map.zboundT : pz);
    return true;
  }

  @Override
  public List<Pair<Itr, Area>> registerItrArea() {
    currItr.clear();
    for (LFitr i: currFrame.itr) {
      // weapon press a
      if (i.effect != LFeffect.FALLING || extra.containsKey(LFextra.Kind.THROWINJURY))
        currItr.add(new LFitrarea(this, i));
    }
    return;
  }

  @Override
  protected final void preprocess() {
    super.preprocess();
    Value_walking_speedx = Value_walking_speed * DIAGONAL_MOVE_VX_RATIO;
    Value_running_speedx = Value_running_speed * DIAGONAL_MOVE_VX_RATIO;
    Value_heavy_walking_speedx = Value_heavy_walking_speed * DIAGONAL_MOVE_VX_RATIO * .5;
    Value_heavy_running_speedx = Value_heavy_running_speed * DIAGONAL_MOVE_VX_RATIO * .5;
    return;
  }

}
