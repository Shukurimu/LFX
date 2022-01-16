package object;

import java.lang.System.Logger.Level;
import java.util.List;

import base.KeyOrder;
import base.Region;
import base.Scope;
import base.Type;
import component.Action;
import component.Effect;
import component.Frame;
import component.Itr;
import util.Vector;

public class BaseEnergy extends AbstractObject implements Energy {
  private static final System.Logger logger = System.getLogger("");
  protected static final int DEFAULT_ITR_SCOPE =
      Scope.ENEMY_HERO | Scope.ALL_WEAPON | Scope.ENEMY_ENERGY;

  protected String soundHit = "";
  protected String soundDrop = "";
  protected String soundBroken = "";

  protected int destroyCountdown = DESTROY_TIME;
  protected Action chasingFunction = Action.UNASSIGNED;
  protected Hero chasingFocus = null;

  protected BaseEnergy(Frame.Collector collector) {
    super(Type.ENERGY, collector);
  }

  private BaseEnergy(BaseEnergy base) {
    super(base);
    soundHit = base.soundHit;
    soundDrop = base.soundDrop;
    soundBroken = base.soundBroken;
  }

  @Override
  public BaseEnergy makeClone() {
    return new BaseEnergy(this);
  }

  @Override
  protected Action getDefaultAction() {
    return Action.ENERGY_FLYING;
  }

  @Override
  public void rebound() {
    logger.log(Level.INFO, "rebound NotImplemented");
    return;
  }

  @Override
  public void disperse() {
    logger.log(Level.INFO, "disperse NotImplemented");
    return;
  }

  @Override
  protected Action updateByState() {
    if (chasingFunction.index == 1) {
      return moveJohnChase(null);
    } else if (chasingFunction.index == 10) {
      return moveJohnFast(null);
    } else if (chasingFunction.index == 2) {
      return moveDennisChase(null);
    }
    return Action.UNASSIGNED;
  }

  private void updateChasingVelocity(Vector pos) {
    vx = pos.x() >= px ? Math.min(CHASE_VXMAX, vx + CHASE_AX)
        : Math.max(-CHASE_VXMAX, vx - CHASE_AX);
    vy = pos.y() >= py ? CHASE_VY : -CHASE_VY;
    vz = pos.z() >= pz ? Math.min(CHASE_VZMAX, vz + CHASE_AZ)
        : Math.max(-CHASE_VZMAX, vz - CHASE_AZ);
    faceRight = vx >= 0.0;
    return;
  }

  private Action moveJohnChase(Action nextAct) {
    // updateChasingVelocity(focus.getRelativePosition());
    return nextAct;
  }

  private Action moveJohnFast(Action nextAct) {
    vx = vx >= 0.0 ? CHASE_VXOUT : -CHASE_VXOUT;
    return nextAct;
  }

  private Action moveDennisChase(Action nextAct) {
    if (hp <= 0.0) {
      if (!Action.DENNIS_CHASE_AWAY.contains(frame.curr)) {
        nextAct = Action.DENNIS_CHASE_AWAY;
      }
      vx = vx >= 0.0 ? Math.min(CHASE_VXOUT, vx + CHASE_AX)
          : Math.max(-CHASE_VXOUT, vx - CHASE_AX);
      return nextAct;
    }

    Vector pos = chasingFocus.getAbsolutePosition();
    if (((pos.x() - px) >= 0.0) == (vx >= 0.0)) {
      // TODO: set every timeunit or condition
      // straight
      if (!Action.DENNIS_CHASE_CHANGEDIR.contains(frame.curr)) {
        nextAct = Action.DENNIS_CHASE_CHANGEDIR;
      }
    } else {
      // changedir
      if (Action.DENNIS_CHASE_CHANGEDIR.contains(frame.curr)) {
        nextAct = Action.DENNIS_CHASE_STRAIGHT;
      }
    }
    updateChasingVelocity(pos);
    return nextAct;
  }

  @Override
  protected Action updateKinetic() {
    vx = frame.calcVx(vx, faceRight);
    vy = frame.calcVy(vy);
    vz = frame.calcVz(vz, 0.0);
    if (buff.getOrDefault(Effect.MOVE_BLOCKING, 0) < env.getTimestamp()) {
      px += vx;
      pz += vz;
    }
    return Action.UNASSIGNED;
  }

  @Override
  protected Action updateStamina() {
    hp -= frame.cost.hp();
    if (hp <= 0.0) {
      chasingFunction = Action.UNASSIGNED;
    }
    return Action.UNASSIGNED;
  }

  @Override
  protected void transitNextFrame(Frame targetFrame, boolean changeFacing) {
    Action fallbackAction = frame.combo.getOrDefault(KeyOrder.hit_d, Action.UNASSIGNED);
    if (hp <= 0.0 && fallbackAction != Action.UNASSIGNED) {
      super.transitGoto(fallbackAction);
    } else {
      super.transitNextFrame(targetFrame, changeFacing);
    }
    return;
  }

  @Override
  public void run(int timestamp, List<Observable> allObjects) {
    chasingFunction = frame.combo.getOrDefault(KeyOrder.hit_Ra, Action.UNASSIGNED);
    if (chasingFocus == null) {
      Observable result = allObjects.stream()
          .filter(o -> o.getType() == Type.HERO && o.exists() && o.getTeamId() != teamId)
          .min((a, b) -> Double.compare(Math.abs(a.getPosX() - px), Math.abs(b.getPosX() - px)))
          .orElse(null);
      if (result instanceof Hero x) {
        chasingFocus = x;
      }
    }
    super.run(timestamp, allObjects);
  }

  @Override
  protected boolean fitBoundary() {
    Region boundary = env.getItemBoundary();
    if (boundary.x1() >= px && px >= boundary.x2()) {
      // Refresh countdown timer if in boundary.
      destroyCountdown = DESTROY_TIME;
    } else if (chasingFunction != Action.UNASSIGNED && (--destroyCountdown < 0)) {
      // Even a blast flies out of boundary and is not chaseable, it still lives a short
      // time. (e.g., DennisChase's first 4 frames)
      return false;
    }
    pz = Math.min(Math.max(pz, boundary.z1()), boundary.z2());
    return true;
  }

  @Override
  public void sendItr(Observable target, Itr itr) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean receiveItr(Observable source, Itr itr, Region absoluteRegion) {
    // TODO Auto-generated method stub
    return false;
  }

}

// https://www.lf-empire.de/en/lf2-empire/data-changing/reference-pages/183-effect-3-hitfa
