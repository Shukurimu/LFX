package lfx.object;

import java.util.List;
import lfx.base.Order;
import lfx.base.Scope;
import lfx.component.Effect;
import lfx.component.Frame;
import lfx.object.AbstractObject;
import lfx.object.Hero;
import lfx.object.Observable;
import lfx.util.Point;
import lfx.util.Util;

class BaseEnergy extends AbstractObject implements Energy {
  public final String soundHit;
  private int lifetime = DESTROY_TIME;
  private Hero focus = null;
  private Observable creator = null;

  protected BaseEnergy(String identifier, List<Frame> frameList, String soundHit) {
    super(identifier, frameList, Scope.ENERGY);
    this.soundHit = soundHit;
  }

  private BaseEnergy(BaseEnergy base) {
    super(base);
    soundHit = base.soundHit;
  }

  @Override
  public BaseEnergy makeClone() {
    return new BaseEnergy(this);
  }

  @Override
  protected void registerLibrary() {
    Library.instance().register(this);
    return;
  }

  @Override
  protected Action getDefaultAct() {
    return Action.ENERGY_FLYING;
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
  public void react() {
    System.out.println("react NotImplemented");
    return;
  }

  @Override
  protected void addRaceCondition(Observable competitor) {
    throw new RuntimeException();
  }

  @Override
  protected Action updateAction(Action nextAct) {
    switch (frame.combo.getOrDefault(Order.hit_Ra, Action.UNASSIGNED)) {
      case Action.JOHN_CHASE:
        nextAct = moveJohnChase(nextAct);
        break;
      case Action.JOHN_CHASE_FAST:
        nextAct = moveJohnChaseFast(nextAct);
        break;
      case Action.DENNIS_CHASE:
        nextAct = moveDennisChase(nextAct);
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

  private void updateChasingVelocity(List<Double> pos) {
    vx = pos.get(0) >= px ? Math.min( CHASE_VXMAX, vx + CHASE_AX)
                          : Math.max(-CHASE_VXMAX, vx - CHASE_AX);
    vy = pos.get(1) >= py ? CHASE_VY : -CHASE_VY;
    vz = pos.get(2) >= pz ? Math.min( CHASE_VZMAX, vz + CHASE_AZ)
                          : Math.max(-CHASE_VZMAX, vz - CHASE_AZ);
    faceRight = vx >= 0.0;
    return;
  }

  private Action moveJohnChase(Action nextAct) {
    updateChasingVelocity(focus.getBasePosition());
    return nextAct;
  }

  private Action moveJohnFast(Action nextAct) {
    vx = vx >= 0.0 ? CHASE_VXOUT : -CHASE_VXOUT;
    return nextAct;
  }

  private Action moveDennisChase(Action nextAct) {
    if (hp <= 0.0) {
      if (!Action.DENNIS_CHASE_AWAY.includes(frame.curr)) {
        nextAct = Action.DENNIS_CHASE_AWAY;
      }
      vx = vx >= 0.0 ? Math.min( CHASE_VXOUT, vx + CHASE_AX)
                     : Math.max(-CHASE_VXOUT, vx - CHASE_AX);
      return nextAct;
    }

    List<Double> pos = focus.getBasePosition();
    if (((pos.get(0) - px) >= 0.0) == (vx >= 0.0)) {
      // TODO: set every timeunit or condition
      // straight
      if (!Action.DENNIS_CHASE_CHANGEDIR.includes(frame.curr)) {
        nextAct = Action.DENNIS_CHASE_CHANGEDIR;
      }
    } else {
      // changedir
      if (Action.DENNIS_CHASE_CHANGEDIR.includes(frame.curr)) {
        nextAct = Action.DENNIS_CHASE_STRAIGHT;
      }
    }
    updateChasingVelocity(pos);
    return nextAct;
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
      vy = frame.dvy;
    }
    if (frame.dvz == Frame.RESET_VELOCITY) {
      vz = 0.0;
    }
    if (!buff.containsKey(Effect.MOVE_BLOCKING)) {
      px += vx;
      py += vy;
      pz += vz;
    }
    return nextAct;
  }

  @Override
  protected Action updateStamina(Action nextAct) {
    hp -= frame.cost.hp;
    return nextAct;
  }

  @Override
  protected void transitNextFrame() {
    transitFrame(hp > 0.0 ? frame.next
                          : frame.combo.getOrDefault(Action.hit_d, frame.next)
    );
    return;
  }

  @Override
  protected boolean fitBoundary() {
    List<Double> xBound = env.getItemXBound();
    if (xBound.get(0) >= px && px >= xBound.get(1)) {
      /** Refresh countdown timer if in bound. */
      lifetime = DESTROY_TIME;
    } else if ((!frame.combo.containsKey(Input.Combo.hit_Ra) || hp < 0.0) && (--lifetime < 0)) {
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
