package lfx.object;

import java.util.List;
import lfx.component.Effect;
import lfx.component.Frame;
import lfx.object.AbstractObject;
import lfx.object.Hero;
import lfx.object.Observable;
import lfx.util.Combo;
import lfx.util.Const;
import lfx.util.Point;
import lfx.util.Scope;
import lfx.util.Util;

class BaseEnergy extends AbstractObject implements Energy {
  private final String soundHit;
  private int lifetime = DESTROY_TIME;
  private Hero focus = null;
  private Observable creator = null;

  protected BaseEnergy(String identifier, List<Frame> frameList, String soundHit) {
    super(identifier, frameList, Scope.ENERGY);
    this.soundHit = soundHit;
  }

  protected BaseEnergy(BaseEnergy base) {
    super(base);
    soundHit = base.soundHit;
  }

  @Override
  public BaseEnergy makeClone(int teamId, boolean faceRight) {
    BaseEnergy clone = new BaseEnergy((BaseEnergy) energyMapping.get(identifier));
    clone.teamId = teamId;
    clone.faceRight = faceRight;
    return clone;
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
    switch (frame.combo.getOrDefault(Combo.hit_Ra, 0)) {
      case FA_DENNIS_CHASE:
        nextAct = moveDennisChase(nextAct);
        break;
      case FA_JOHN_DISK_CHASE:
        nextAct = moveJohnChase(nextAct);
        break;
      case FA_JOHN_DISK_FAST:
        nextAct = moveJohn2(nextAct);
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

  private int moveDennisChase(int nextAct) {
    if (hp > 0.0) {
      return moveJohnChase(nextAct);
    }

    if (frame.curr != 5 && frame.curr != 6) {
      nextAct = 5;
    }
    vx = vx >= 0.0 ? Math.min( CHASE_VXOUT, vx + CHASE_AX)
                   : Math.max(-CHASE_VXOUT, vx - CHASE_AX);
    vy = 0.0;
    return nextAct;
  }

  private int moveJohnChase(int nextAct) {
    Point point = focus.getChasingPoint();
    if (((point.x - px) >= 0.0) == (vx >= 0.0)) {
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
    py += Math.copySign(CHASE_VY, point.y - py);
    vx = point.x >= px ? Math.min( CHASE_VXMAX, vx + CHASE_AX)
                       : Math.max(-CHASE_VXMAX, vx - CHASE_AX);
    vz = point.x >= pz ? Math.min( CHASE_VZMAX, vz + CHASE_AZ)
                       : Math.max(-CHASE_VZMAX, vz - CHASE_AZ);
    faceRight = vx >= 0.0;

    return nextAct;
  }

  private int moveJohn2(int nextAct) {
    vx = vx >= 0.0 ? CHASE_VXOUT : -CHASE_VXOUT;
    vz = 0.0;
    return nextAct;
  }

  @Override
  protected int updateKinetic(int nextAct) {
    if (actLag == 0) {
      vx = frame.calcVX(vx, faceRight);
      px = buff.containsKey(Effect.MOVE_BLOCKING) ? px : (px + vx);
      vy = frame.calcVY(vy);
      if (buff.containsKey(Effect.MOVE_BLOCKING) || frame.dvz == Const.DV_550) {
        vz = 0.0;
      } else {
        pz += vz + frame.combo.getOrDefault(Combo.hit_j, 0);
      }
    }
    return nextAct;
  }

  @Override
  protected int updateHealth(int nextAct) {
    hp -= frame.combo.getOrDefault(Combo.hit_a, 0);
    return nextAct;
  }

  @Override
  protected int getNextActNumber() {
    return hp > 0.0 ? frame.next
                    : frame.combo.getOrDefault(Combo.hit_d, frame.next);
  }

  @Override
  protected boolean adjustBoundary() {
    List<Double> xBound = env.getItemXBound();
    if (xBound.get(0) >= px && px >= xBound.get(1)) {
      /** Refresh countdown timer if in bound. */
      lifetime = DESTROY_TIME;
    } else if ((!frame.combo.containsKey(Combo.hit_Ra) || hp < 0.0) && (--lifetime < 0)) {
      /** Even the blast flies out of bound and is not in a functional frame (hit_Fa == NONE),
          it still can live a short time. (e.g., dennis_chase first 4 frames) */
      return false;
    }
    List<Double> zBound = env.getZBound();
    pz = Util.clamp(pz, zBound.get(0), zBound.get(1));
    return true;
  }

}

// https://www.lf-empire.de/en/lf2-empire/data-changing/reference-pages/183-effect-3-hitfa
