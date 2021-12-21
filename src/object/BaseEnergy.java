package object;

import java.lang.System.Logger.Level;
import java.util.List;

import base.KeyOrder;
import base.Region;
import base.Type;
import component.Action;
import component.Effect;
import component.Frame;
import component.Itr;
import util.Util;
import util.Vector;

public class BaseEnergy extends AbstractObject implements Energy {
  private static final System.Logger logger = System.getLogger("");
  private static final List<Action> CHASEABLES = List.of(Action.JOHN_CHASE, Action.DENNIS_CHASE);

  protected String soundHit;
  protected int destroyCountdown = DESTROY_TIME;
  protected Action chasingFunction = Action.UNASSIGNED;
  protected Hero chasingFocus = null;

  protected BaseEnergy(String identifier, List<Frame> frameList) {
    super(identifier, Type.ENERGY, frameList);
  }

  private BaseEnergy(BaseEnergy base) {
    super(base);
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
    if (chasingFunction == Action.JOHN_CHASE) {
      return moveJohnChase(null);
    } else if (chasingFunction == Action.JOHN_CHASE_FAST) {
      return moveJohnFast(null);
    } else if (chasingFunction == Action.DENNIS_CHASE) {
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
      if (!Action.DENNIS_CHASE_AWAY.includes(frame.curr)) {
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
  protected Action updateKinetic() {
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
  protected void transitNextFrame() {
    transitFrame(hp > 0.0 ? frame.next
        : frame.combo.getOrDefault(KeyOrder.hit_d, frame.next));
    return;
  }

  @Override
  public void run(List<Observable> allObjects) {
    chasingFunction = frame.combo.getOrDefault(KeyOrder.hit_Ra, Action.UNASSIGNED);
    if (CHASEABLES.contains(chasingFunction) && chasingFocus == null) {
      Observable result = allObjects.stream()
          .filter(o -> o.getType() == Type.HERO && o.exists() && o.getTeamId() != teamId)
          .min((a, b) -> Double.compare(Math.abs(a.getPosX() - px), Math.abs(b.getPosX() - px)))
          .orElse(null);
      if (result instanceof Hero x) {
        chasingFocus = x;
      }
    }
    super.run(allObjects);
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
    pz = Util.clamp(pz, boundary.z1(), boundary.z2());
    return true;
  }

  @Override
  public void receiveItr(Observable source, Itr itr, Region absoluteRegion) {
    // TODO Auto-generated method stub

  }

  @Override
  public void sendItr(Observable target, Itr itr) {
    // TODO Auto-generated method stub

  }

}

// https://www.lf-empire.de/en/lf2-empire/data-changing/reference-pages/183-effect-3-hitfa
