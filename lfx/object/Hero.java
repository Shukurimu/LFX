package lfx.object;

import javafx.scene.image.Image;
import java.util.Iterator;
import java.util.Map;
import java.util.EnumMap;

abstract class LFhero extends LFobject {
  /** the defend point is set to NODEF_DP if one got hit not in defend state */
  public static final int NODEF_DP = 45;
  public static final double ICED_FALLDOWN_DAMAGE = 10.0;
  public static final double SONATA_FALLDOWN_DAMAGE = 10.0;
  public static final double DEFEND_DMG_REDUCTION = 0.10;
  public static final double DEFEND_DVX_REDUCTION = 0.10;
  /** (uncertain value) the velocity of bouncing up after falling */
  public static final double FALLING_BOUNCE_VY = -4.25;
  /** (uncertain value) the waitTU non-y velocity ratio after character landing */
  public static final double LANDING_VELOCITY_REMAIN = 0.5;
  /** (self-test value) the z-velocity of opoint if press U or D */
  public static final double OPOINT_DVZ = 2.5;
  /** (self-test value) the x-velocity ratio if move diagonally */
  public static final double DIAMOVE_VX_RATIO = 1.0 / 1.4;
  /** (uncertain value) velocity of being caught and dropped by time out */
  public static final double CAUGHT_TIMEUP_DVX =  8.0;
  public static final double CAUGHT_TIMEUP_DVY = -3.0;
  public static final double CAUGHT_DROP_DVY = -2.0;

  public final Image faceImage;
  public LFweapon weapon = LFweapon.dummy;
  private LFcontrol ctrl = LFcontrol.noControl;
  private int lastHitTime = 0;

  protected int dp = 0, dpDec = 1;
  protected int fp = 0, fpDec = 1;
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
  protected double Value_jump_height   = 0.0;
  protected double Value_jump_distance   = 0.0;
  protected double Value_jump_distancez  = 0.0;
  protected double Value_dash_height   = 0.0;
  protected double Value_dash_distance   = 0.0;
  protected double Value_dash_distancez  = 0.0;
  protected double Value_rowing_height   = 0.0;
  protected double Value_rowing_distance = 0.0;

  protected LFhero(String id, LFtype t, String c) {
    super(id, t, 400);
    faceImage = LFobject.loadImage(c);
  }

  public final void ctrlRegister(EnumMap<javafx.scene.input.KeyCode, LFkeyrecord> keyStatus) {
    ctrl.register(keyStatus);
    return;
  }

  @Override
  public final double getControlZ() {
    return ctrl.do_Z ? (ctrl.do_U ? -1.0 : 1.0) : 0.0;
  }

  /* used in LFstatus */
  public final void getHealth(double[] values) {
    if (hp > 0.0) {
      values[0] = hp2nd / hpMax;
      values[1] = hp  / hpMax;
      values[2] = mp  / mpMax;
    } else
      values[0] = values[1] = values[2] = 0.0;
    return;
  }

  @Override
  public final int checkItrScope(LFobject o) {
    return (teamID == o.teamID) ? 0b000001 : 0b000010;
  }

  @Override
  public final void revive() {
    hp = hp2nd = hpMax;
    mp = mpMax;
    return;
  }

  @Override
  public final void damageCallback(LFitr i, LFobject o) {
    hitLag = HITLAG_SPAN;
    return;
  }

  @Override
  public void damageReceived(LFitrarea ia, LFbdyarea ba) {
    recvDmg.add(ia, ba);
    return;
  }

  @Override
  protected final int resolveAct(int index) {
    if (index < 0)
      index = -index;
    return (index != Act_999) ? index :
        ((weapon.type == LFtype.HEAVY) ? Act_hvwalking : ((py >= 0) ? Act_standing : Act_jumpair));
  }

  /* if the hero satisfies the limitation and has enough mana, then consume the mana and return true. */
  public final boolean tryCombo(LFact combo, LFframe comboFrame, boolean changeFacing) {
    if (LFX.currMap.isUnlimitedMode()) {
      /* you can do the combo anyway ! */
    } else if (comboFrame.limit > 0.0 && hp >= comboFrame.limit * hpMax) {
      return false;
    } else if (comboFrame.mpCost < 1000) {
      if (mp >= comboFrame.mpCost)
        mp -= comboFrame.mpCost;
      else
        return false;
    } else {
      if (mp >= comboFrame.mpCost % 1000 && hp > comboFrame.mpCost / 100) {
        mp -= comboFrame.mpCost % 1000;
        hp -= comboFrame.mpCost / 100;
      } else
        return false;
    }
    faceRight = (combo.facing == null) ? (faceRight ^ changeFacing) : combo.facing;
    currFrame = comboFrame;
    waitTU = currFrame.wait;
    ctrl.consumeKey(combo);
    return true;
  }

  /* most of the time, dark HP is reduced by one-third of received damage
     use `sync=true' for the situations not following this rule (e.g., throwinjury)
     note that the lower bound is zero in LFX, which is different from LF2 */
  public final void hpLost(double dmg, boolean sync) {
    hp -= dmg;
    hp2nd -= sync ? dmg : Math.floor(dmg / 3.0);
    if (hp < 0.0) {
      hp = 0.0;
      hp2nd = Math.max(hp2nd, 0.0);
    }
    return;
  }

  /* ActNumber lookup table */
  protected static final int Act_notransform = 0;// default
  protected static final int Act_transformback = 245;// default
  protected static final int Act_standing = 0;
  protected static final int Act_walking = 5;
  protected static final int Act_running = 9;
  protected static final int Act_hvwalking = 12;
  protected static final int Act_hvrunning = 16;
  protected static final int Act_hvstoprun = 19;
  protected static final int Act_nrmwpatk1 = 20;
  protected static final int Act_nrmwpatk2 = 25;
  protected static final int Act_jumpwpatk = 30;
  protected static final int Act_runwpatk = 35;
  protected static final int Act_dashwpatk = 40;
  protected static final int Act_lgwpthw = 45;
  protected static final int Act_hvwpthw = 50;
  protected static final int Act_skywpthw = 52;
  protected static final int Act_drink = 55;
  protected static final int Act_punch1 = 60;
  protected static final int Act_punch2 = 65;
  protected static final int Act_spunch = 70;
  protected static final int Act_jumpatk = 80;
  protected static final int Act_runatk = 85;
  protected static final int Act_dashatk = 90;
  // protected static final int Act_dashdef = 95;// nouse
  protected static final int Act_rolling = 102;
  protected static final int Act_rowing1 = 100;
  protected static final int Act_rowing2 = 108;
  protected static final int Act_defend = 110;
  protected static final int Act_defendhit = 111;
  protected static final int Act_broken = 112;
  protected static final int Act_picklight = 115;
  protected static final int Act_pickheavy = 116;
  protected static final int Act_catch = 120;
  protected static final int Act_caught = 130;
  protected static final int Act_fwfall1 = 180;
  protected static final int Act_fwfall2 = 181;
  protected static final int Act_fwfall3 = 182;
  protected static final int Act_fwfall4 = 183;
  protected static final int Act_fwfall5 = 184;
  protected static final int Act_fwfallR = 185;
  protected static final int Act_bwfall1 = 186;
  protected static final int Act_bwfall2 = 187;
  protected static final int Act_bwfall3 = 188;
  protected static final int Act_bwfall4 = 189;
  protected static final int Act_bwfall5 = 190;
  protected static final int Act_bwfallR = 191;
  protected static final int Act_ice = 200;
  protected static final int Act_fireU = 203;
  protected static final int Act_fireD = 205;
  // protected static final int Act_tired = 207;// nouse
  protected static final int Act_jump = 210;
  protected static final int Act_jumpair = 212;
  protected static final int Act_dash1 = 213;
  protected static final int Act_dash2 = 214;
  protected static final int Act_crouch1 = 215;
  protected static final int Act_stoprun = 218;
  protected static final int Act_crouch2 = 219;
  protected static final int Act_injure1 = 220;
  protected static final int Act_injure2 = 222;
  protected static final int Act_injure3 = 224;
  protected static final int Act_dop = 226;
  protected static final int Act_lying1 = 230;
  protected static final int Act_lying2 = 231;
  protected static final int Act_thwlyman = 232;
  // protected static final int Act_dummy = 399;// nouse

  /* hidden action frame counter */
  private static final int[] walkingCycle = { 2, 3, 2, 1, 0, 1 };
  private int walkingIndex = 0;
  private static final int[] runningCycle = { 0, 1, 2, 1 };
  private int runningIndex = 0;

  /* hidden flying-related status, lsb indicates whether the velocity need to be granted */
  private static final byte NOTFLYING = 0;
  private static final byte JUMP_V0_1 = (1 << 1) | 1, JUMP_V0_0 = (1 << 1) | 0;// jump vx=0
  private static final byte JUMP_VP_1 = (2 << 1) | 1, JUMP_VP_0 = (2 << 1) | 0;// jump vx>0
  private static final byte JUMP_VN_1 = (3 << 1) | 1, JUMP_VN_0 = (3 << 1) | 0;// jump vx<0
  private static final byte DASH_RP_1 = (4 << 1) | 1, DASH_RP_0 = (4 << 1) | 0;// dash vx>0 facing right
  private static final byte DASH_RN_1 = (5 << 1) | 1, DASH_RN_0 = (5 << 1) | 0;// dash vx<0 facing right
  private static final byte DASH_LP_1 = (6 << 1) | 1, DASH_LP_0 = (6 << 1) | 0;// dash vx>0 facing left
  private static final byte DASH_LN_1 = (7 << 1) | 1, DASH_LN_0 = (7 << 1) | 0;// dash vx<0 facing left
  private static final byte  ROW_VP_1 = (8 << 1) | 1,  ROW_VP_0 = (8 << 1) | 0;//  row vx>0
  private static final byte  ROW_VN_1 = (9 << 1) | 1,  ROW_VN_0 = (9 << 1) | 0;//  row vx<0
  private byte flyState = NOTFLYING;

  @Override
  public boolean reactAndMove(LFmap map) {
    for (LFitrarea r: recvItr) {
      switch (r.itr.effect) {
        case PICKSTAND:
          /* assert you finally get the weapon */
          if (weapon.picker != this)
            weapon = LFweapon.dummy;
          else
            setCurr((weapon.type == LFtype.HEAVY) ? Act_pickheavy : Act_picklight);
          break;
        case PICKROLL:
          /* assert you finally get the weapon */
          if (weapon.picker != this)
            weapon = LFweapon.dummy;
          break;
        case LETSP:
          extra.put(LFextra.Kind.LETSPUNCH, LFextra.oneTime());
          break;
        case FENCE:
          extra.put(LFextra.Kind.MOVEBLOCK, LFextra.oneTime());
          break;
        case SONATA:
          vx = r.sonataVxz(vx);
          vz = r.sonataVxz(vz);
          vy = r.sonataVy(py, vy);
          hpLost(r.itr.injury, false);
          extra.put(LFextra.Kind.SONATA, new LFextra(Integer.MAX_VALUE, 1.0));
          setCurr((vy < 0.0) ? 182 : 183);
          break;
        case VORTEX:
          vx += r.vortexAx(px);
          vz += r.vortexAz(pz);
          vy += r.vortexAy(py, vy);
          break;
        case HEAL:
          extra.put(LFextra.Kind.HEALING, new LFextra(r.itr.dvy, (double)(r.itr.injury / r.itr.dvy)));
          break;
        default:
          System.out.printf("\n%s should not receive effect: %d", this, r.itr.effect);
      }
    }
    recvItr.clear();

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
    int nextAct = DEFAULT_ACT;

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
          nextAct = Act_running + runningCycle[runningIndex = 0];
          faceRight = ctrl.do_RR;
        } else if (ctrl.do_F | ctrl.do_Z) {
          nextAct = Act_walking + walkingCycle[walkingIndex = 0];
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
          nextAct = Act_running + runningCycle[runningIndex = 0];
          faceRight = ctrl.do_RR;
        } else if (ctrl.do_F | ctrl.do_Z) {
          if    (ctrl.do_R) { vx =  Value_walking_speed; faceRight = true;  }
          else if (ctrl.do_L) { vx = -Value_walking_speed; faceRight = false; }
          if    (ctrl.do_U)   vz = -Value_walking_speedz;
          else if (ctrl.do_D)   vz =  Value_walking_speedz;
          if (waitTU < 1) {
            walkingIndex = (walkingIndex + 1) % 6;
            nextAct = Act_walking + walkingCycle[walkingIndex];
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
            walkingIndex = (walkingIndex + 1) % 6;
            nextAct = Act_hvwalking + walkingCycle[walkingIndex];
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
          runningIndex = (runningIndex + 1) & 3;
          nextAct = Act_running + runningCycle[runningIndex];
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
          runningIndex = (runningIndex + 1) & 3;
          nextAct = Act_hvrunning + runningCycle[runningIndex];
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
  public void registerItr() {
    currItr.clear();
    for (LFitr i: currFrame.itr) {
      // weapon press a
      if (i.effect != LFeffect.FALLING || extra.containsKey(LFextra.Kind.THROWINJURY))
        currItr.add(new LFitrarea(this, i));
    }
    return;
  }

  @Override
  public void registerBdy() {
    currBdy.clear();
    if (grasp == null || grasp.hasBdy(this)) {
      for (LFbdy b: currFrame.bdy)
        currBdy.add(new LFbdyarea(this, b));
    }
    return;
  }

  @Override
  public void statusOverwrite(final LFhero target) {
    super.statusOverwrite(target);
    if (weapon != LFweapon.dummy) {
      weapon.picker = target;
      target.weapon = weapon;
    }
    target.ctrl = ctrl;
    target.hp2nd = hp2nd;
    /* self cleaning */
    lastHitTime = 0;
    dp = fp = 0;
    flyState = NOTFLYING;
    return;
  }

  @Override
  protected final void preprocess() {
    super.preprocess();
    /* while z-velocity is always the same, x-velocity is reduced when you move diagonally */
    Value_walking_speedx = Value_walking_speed * DIAMOVE_VX_RATIO;
    Value_running_speedx = Value_running_speed * DIAMOVE_VX_RATIO;
    Value_heavy_walking_speedx = Value_heavy_walking_speed * DIAMOVE_VX_RATIO * .5;
    Value_heavy_running_speedx = Value_heavy_running_speed * DIAMOVE_VX_RATIO * .5;
    /* distance kind is not affacted by z-direction move */
    return;
  }

  @Override
  protected final LFhero clone() {
    System.out.printf("%s.clone()\n", identifier);
    return (LFhero)super.clone();
  }

  public final LFhero makeCopyPick(LFcontrol c, int t) {
    LFhero p = (LFhero)makeCopy(Math.random() >= 0.5, t);
    p.ctrl = c;
    return p;
  }

}
