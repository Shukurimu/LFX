package lfx.object;

import lfx.component.Type;
// https://www.lf-empire.de/en/lf2-empire/data-changing/reference-pages/183-effect-3-hitfa

abstract class AbstractBlast extends AbstractObject {
  public static final double CHASE_AX = 0.7;
  public static final double CHASE_VXMAX = 14.0;
  public static final double CHASE_VXOUT = 17.0;
  public static final double CHASE_VY = 1.0;
  public static final double CHASE_AZ = 0.4;
  public static final double CHASE_VZMAX = 2.2;
  public static final int DESTROY_TIME = 16;
  public static final int REFLECT_VREST = 8;

  public final String soundHit;
  private int destroyCountdown = DESTROY_TIME;
  private AbstractObject focusing = null;
  private AbstractObject reflector = null;

  /** Frame Attributes (TODO: use enum) */
  protected static final int FA_DENNIS_CHASE = 2;
  protected static final int FA_JOHN_DISK_CHASE = 1;
  protected static final int FA_JOHN_DISK_FAST = 10;

  protected AbstractBlast(String identifier, String soundHit) {
    super(Type.BLAST, identifier);
    this.soundHit = soundHit;
  }

  @Override
  public final void revive() {
    hp = hpMax;
    return;
  }

  @Override
  public final void damageCallback(LFitr i, LFobject o) {
    if (currFrame.state == LFstate.ENERGY) {
      if (o.currFrame.state == LFstate.ENERGY) {
        setCurr(Act_hitfail);
        vx = vy = vz = 0.0;
        return;
      }
    } else if (currFrame.state == LFstate.PIERCE) {
      if (o.currFrame.state == LFstate.ENERGY || o.currFrame.state == LFstate.PIERCE) {
        setCurr(Act_hitfail);
        vx = vy = vz = 0.0;
        return;
      }
    } else if (currFrame.state == LFstate.NORMAL) {
      setCurr((o instanceof LFblast) ? Act_hitfail : Act_hitsucc);
      vx = vy = vz = 0.0;
      return;
    }
    hitLag = HITLAG_SPAN;
    return;
  }

  @Override
  public void damageReceived(LFitrarea ia, LFbdyarea ba) {
    if (ia.itr.effect == LFeffect.REFLECT) {
      reflector = ia.owner;
      if (currFrame.state == LFstate.ENERGY)
        setCurr(Act_disappear);
      else {
        teamID = ia.owner.teamID;
        setCurr(Act_rebound);
      }
      vx = vy = vz = 0.0;
      return;
    } else if (currFrame.state == LFstate.ENERGY) {
      if (ia.owner.currFrame.state == LFstate.ENERGY) {
        setCurr(Act_hitfail);
        vx = vy = vz = 0.0;
        return;
      }
    } else if (currFrame.state == LFstate.PIERCE) {
      if (ia.owner.currFrame.state == LFstate.ENERGY) {
        teamID = ia.owner.teamID;
        setCurr((ia.owner instanceof LFhero) ? Act_rebound : Act_hitfail);
        vx = vy = vz = 0.0;
        return;
      }
      if (ia.owner.currFrame.state == LFstate.PIERCE) {
        setCurr(Act_hitfail);
        vx = vy = vz = 0.0;
        return;
      }
    } else if (currFrame.state == LFstate.NORMAL) {
      if (ia.owner instanceof LFhero) {
        teamID = ia.owner.teamID;
        setCurr(Act_rebound);
      } else
        setCurr(Act_hitfail);
      vx = vy = vz = 0.0;
      return;
    }
    hitLag = HITLAG_SPAN;
    return;
  }

  @Override
  protected final CanonicalAct getCanonicalAct(int action) {
    return new CanonicalAct(action, ACT_FLYING);
  }

  @Override
  public boolean react() {
    for (LFitrarea r: recvItr) {
      switch (r.itr.effect) {
        case FENCE:
          extra.put(LFextra.Kind.MOVEBLOCK, LFextra.oneTime());
          break;
        case REFLECT:
          System.out.printf("\nImplementing ItrKind: %s", r.itr.effect);
          break;
        default:
          System.out.printf("\n%s should not receive ItrKind: %s", this, r.itr.effect);
      }
    }
    return true;
  }

  @Override
  protected boolean move2() {
    int nextAct = DEFAULT_ACT;
    /** Reflect itr results in about 8 TimeUnit vrest between reflector and spawned objects.
        e.g., Bouncing ball between two John's shields. */
    if (reflector != null && currFrame.state != LFstate.REBOUND)
      reflector  = null;
    /* opoint is triggered only at the first timeunit */
    if (waitTU == currFrame.wait && !currFrame.opoint.isEmpty()) {
      for (LFopoint x: currFrame.opoint)
        map.spawnObject(x.launch(this, 0.0), reflector, REFLECT_VREST);
    }

    if (currFrame.comboList[LFact.hit_a.index] != 0) {
      if ((hp -= currFrame.comboList[LFact.hit_a.index]) > 0.0) {
        if (currFrame.comboList[LFact.hit_Fa.index] != 0) {
          /* randomly choose an alive enemy */
          if (focusing == null || focusing.hp == 0.0)
            focusing = map.chooseHero(this, false, null);
          if (focusing != null) {
            switch (currFrame.comboList[LFact.hit_Fa.index]) {
              case HitFa_johndiskchase:
              case HitFa_dennischase:
                if (((focusing.px - px) >= 0.0) == (vx >= 0.0)) {
                  /* straight34 */
                  if (currFrame.curr != 3 && currFrame.curr != 4)
                    nextAct = 3;
                } else {
                  /* changedir12 */
                  if (currFrame.curr == 3 || currFrame.curr == 4)
                    nextAct = 1;
                }
                py += Math.copySign(CHASE_VY, focusing.py - focusing.currFrame.centerY / 2.0 - py);
                vx = (focusing.px >= px) ?
                  Math.min(CHASE_VXMAX, vx + CHASE_AX) : Math.max(-CHASE_VXMAX, vx - CHASE_AX);
                vz = (focusing.pz >= pz) ?
                  Math.min(CHASE_VZMAX, vz + CHASE_AZ) : Math.max(-CHASE_VZMAX, vz - CHASE_AZ);
                faceRight = (vx >= 0.0);
                break;
              case HitFa_johndisk2:
                vx = (vx >= 0.0) ? CHASE_VXOUT : -CHASE_VXOUT;
                vz = 0.0;
                break;
            }
          }
        }
      } else {
        switch (currFrame.comboList[LFact.hit_Fa.index]) {
          case HitFa_dennischase:
            if (currFrame.curr != 5 && currFrame.curr != 6)
              nextAct = 5;
            vx = (vx >= 0.0) ? Math.min(CHASE_VXOUT, vx + CHASE_AX) : Math.max(-CHASE_VXOUT, vx - CHASE_AX);
            vy = 0.0;
            break;
          case HitFa_johndisk2:
            vx = (vx >= 0.0) ? CHASE_VXOUT : -CHASE_VXOUT;
            vz = 0.0;
            break;
          default:
            if (currFrame.comboList[LFact.hit_d.index] != 0) {
              if (currFrame.comboList[LFact.hit_d.index] > 0) {
                nextAct =  currFrame.comboList[LFact.hit_d.index];
              } else {
                faceRight = !faceRight;
                nextAct = -currFrame.comboList[LFact.hit_d.index];
              }
            }
        }
      }
    }
    return;
  }

  @Override
  protected boolean updateMovement() {
    if (hitLag == 0) {
      vx = currFrame.calcVX(vx, faceRight);
      px = extra.containsKey(LFextra.Kind.MOVEBLOCK) ? px : (px + vx);
      vy = currFrame.calcVY(vy);
      if (extra.containsKey(LFextra.Kind.MOVEBLOCK) || (currFrame.dvz == LFframe.DV_550))
        vz = 0.0;
      else
        pz += vz + currFrame.comboList[LFact.hit_j.index];
    }
    return;
  }

  @Override
  protected boolean updateStatus() {
    return true;
  }

  @Override
  protected boolean updateFrame() {
    if (hitLag > 0) {
      --hitLag;
    } else if (nextAct != DEFAULT_ACT) {
      setCurr(nextAct);
    } else if (--waitTU < 0) {
      LFframe nextFrame = getFrame(currFrame.next);
      if (nextFrame == null)
        return false;
      setNext(nextFrame);
    }
    return true;
  }

  @Override
  protected boolean adjustBoundary(double[] xzBound) {
    if (px > xzBound[0] && px < xzBound[1]) {
      /** Refresh countdown timer if in bound. */
      destroyCountdown = DESTROY_TIME;
    } else if ((currFrame.comboList[LFact.hit_Fa.index] == 0 || hp < 0.0) && (--destroyCountdown < 0)) {
      /** Even the blast flies out of bound and is not in a functional frame (hit_Fa == NONE),
          it still can live a short time. (e.g., dennis_chase first 4 frames) */
      return false;
    }
    pz = Function.clamp(pz, xzBound[2], xzBound[3]);
    return true;
  }

}
