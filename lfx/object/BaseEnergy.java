package lfx.object;

import java.util.List;
import lfx.component.Frame;
import lfx.object.AbstractObject;
import lfx.object.BaseEnergy;
import lfx.object.Hero;
import lfx.object.Observable;
import lfx.util.Global;

class BaseEnergy extends AbstractObject implements Energy {
  public final String soundHit;
  private int lifetime = DESTROY_TIME;
  private Hero focus = null;
  private Observable creator = null;

  protected BaseEnergy(String identifier, List<Frame> frameList, String soundHit) {
    super(identifier, frameArray);
    this.soundHit = soundHit;
  }

  protected BaseEnergy(BaseEnergy base) {
    super(this);
    soundHit = base.soundHit;
  }

  @Override
  public Energy makeClone() {
    return new BaseEnergy(energyMapping.get(identifier));
  }

  @Override
  protected void registerObjectMap() {
    energyMapping.putIfAbsent(identifier, this);
    return;
  }

  @Override
  protected int getDefaultActNumber() {
    return ACT_FLYING;
  }

  @Override
  public void rebound() {
    System.out.println("rebound NotImplemented");
    return;
  }

  @Override
  public void disperse() {
    System.out.println("disperse NotImplemented");
    return;
  }

  @Override
  protected int getScopeView(int targetTeamId) {
    return Global.getSideView(Global.SCOPE_VIEW_ENERGY, targetTeamId == this.teamId);
  }

  @Override
  protected void itrCallback() {
    System.out.println("itrCallback NotImplemented");
    return;
  }

  @Override
  public void react() {
    System.out.println("react NotImplemented");
    return;
  }

  @Override
  protected int updateAction(int nextAct) {
    switch (frame.combo.get(Combo.hit_Fa, 0)) {
      case FA_DENNIS_CHASE:
        nextAct = moveDennisChase();
        break;
      case FA_JOHN_DISK_CHASE:
        nextAct = moveJohnChase();
        break;
      case FA_JOHN_DISK_FAST:
        nextAct = moveJohn2();
        break;
    }
    return nextAct;
  }

  private Hero selectRandomTarget() {
    if (focus == null || !focus.isAlive()) {
      System.out.println("selectRandomTarget NotImplemented");
    }
    return null;
  }

  private int moveDennisChase() {
    if (hp > 0.0) {
      return moveJohnChase();
    }

    if (frame.curr != 5 && frame.curr != 6) {
      nextAct = 5;
    }
    vx = vx >= 0.0 ? Math.min( CHASE_VXOUT, vx + CHASE_AX)
                   : Math.max(-CHASE_VXOUT, vx - CHASE_AX);
    vy = 0.0;
    return nextAct;
  }

  private int moveJohnChase() {
    if (((focus.px - px) >= 0.0) == (vx >= 0.0)) {
      // straight
      if (frame.curr != 3 && frame.curr != 4) {
        nextAct = 3;
      }
    } else {
      // changedir
      if (frame.curr == 3 || frame.curr == 4) {
        nextAct = 1;
      }
    }
    py += Math.copySign(CHASE_VY, focus.py - focus.frame.centerY / 2.0 - py);
    vx = focus.px >= px ? Math.min( CHASE_VXMAX, vx + CHASE_AX)
                        : Math.max(-CHASE_VXMAX, vx - CHASE_AX);
    vz = focus.pz >= pz ? Math.min( CHASE_VZMAX, vz + CHASE_AZ)
                        : Math.max(-CHASE_VZMAX, vz - CHASE_AZ);
    faceRight = vx >= 0.0;

    return nextAct;
  }

  private int moveJohn2() {
    vx = vx >= 0.0 ? CHASE_VXOUT : -CHASE_VXOUT;
    vz = 0.0;
    return;
  }

  @Override
  protected int updateKinetic(int nextAct) {
    if (hitLag == 0) {
      vx = frame.calcVX(vx, faceRight);
      px = buff.containsKey(Effect.MOVEBLOCK) ? px : (px + vx);
      vy = frame.calcVY(vy);
      if (buff.containsKey(Effect.MOVEBLOCK) || frame.dvz == Frame.DV_550) {
        vz = 0.0;
      } else {
        pz += vz + frame.combo.get(Combo.hit_j, 0);
      }
    }
    return nextAct;
  }

  @Override
  protected int updateHealth(int nextAct) {
    hp -= frame.combo.get(Combo.hit_a, 0);
    return nextAct;
  }

  @Override
  protected int getNextActNumber() {
    return hp > 0.0 ? frame.next
                    : frame.combo.getOrDefault(Combo.hit_d, frame.next);
  }

  @Override
  protected boolean adjustBoundary() {
    double[] xzBound = env.getNonHeroXzBound();
    if (xzBound[0] >= px && px >= xzBound[1]) {
      /** Refresh countdown timer if in bound. */
      lifetime = DESTROY_TIME;
    } else if ((!frame.combo.containsKey(Combo.hit_Fa) || hp < 0.0) && (--lifetime < 0)) {
      /** Even the blast flies out of bound and is not in a functional frame (hit_Fa == NONE),
          it still can live a short time. (e.g., dennis_chase first 4 frames) */
      return false;
    }
    pz = Global.clamp(pz, xzBound[2], xzBound[3]);
    return true;
  }

}

// https://www.lf-empire.de/en/lf2-empire/data-changing/reference-pages/183-effect-3-hitfa
