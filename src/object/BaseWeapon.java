package object;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import base.Region;
import base.Type;
import component.Action;
import component.Effect;
import component.Frame;
import component.Itr;
import component.State;
import component.Wpoint;
import util.Tuple;
import util.Util;

public class BaseWeapon extends AbstractObject implements Weapon {
  private static final System.Logger logger = System.getLogger("");

  private final Action actionOnHand;
  private final Action actionThrowing;
  private final Action actionInTheSky;
  private final Map<Wpoint.Usage, Itr> strength;
  private final List<Observable> mutuallyExcludedList = new ArrayList<>();

  protected double dropHurt;
  protected String soundHit;
  protected String soundDrop;
  protected String soundBroken;
  protected Hero holder = null;

  protected BaseWeapon(
      String identifier, Type type, List<Frame> frameList,
      Map<Wpoint.Usage, Itr> strength) {
    super(identifier, type, frameList);
    if (!type.isWeapon) {
      throw new IllegalArgumentException("Not a weapon type.");
    }
    if (type == Type.HEAVY) {
      actionOnHand = Action.HEAVY_ON_HAND;
      actionThrowing = Action.HEAVY_THROWING;
      actionInTheSky = Action.HEAVY_IN_THE_SKY;
    } else {
      actionOnHand = Action.LIGHT_ON_HAND;
      actionThrowing = Action.LIGHT_THROWING;
      actionInTheSky = Action.LIGHT_IN_THE_SKY;
    }
    this.strength = Collections.unmodifiableMap(strength);
  }

  protected BaseWeapon(BaseWeapon base) {
    super(base);
    actionOnHand = base.actionOnHand;
    actionThrowing = base.actionThrowing;
    actionInTheSky = base.actionInTheSky;
    strength = base.strength;
  }

  public BaseWeapon makeClone() {
    return new BaseWeapon(this);
  }

  @Override
  protected Action getDefaultAction() {
    return actionInTheSky;
  }

  @Override
  public boolean isHeavy() {
    return type == Type.HEAVY;
  }

  @Override
  public boolean isDrink() {
    return type == Type.DRINK;
  }

  @Override
  public boolean isLight() {
    return type == Type.LIGHT;
  }

  @Override
  public boolean isSmall() {
    return type == Type.SMALL;
  }

  @Override
  public void release() {
    holder = null;
    return;
  }

  @Override
  public void destroy() {
    hp = 0.0;
    // TODO: create fabrics
    return;
  }

  @Override
  protected int checkInteraction(Observable that) {
    if (mutuallyExcludedList.contains(that)) {
      return 0;
    } else {
      return super.checkInteraction(that);
    }
  }

  @Override
  public boolean tryPick(Observable actor) {
    if (!(actor instanceof Hero o)) {
      logger.log(Level.WARNING, "NonHero %s", actor);
      return false;
    }
    synchronized (this) {
      if (holder == null) {
        holder = o;
        return true;
      }
    }
    return false;
  }

  @Override
  protected final Action updateByState() {
    return switch (frame.state) {
      case ON_HAND -> {
        if (holder == null) {
          yield actionOnHand.shifts(frame.curr);
        }
        Wpoint wpoint = holder.getWpoint();
        // TODO: There is no wpoint in the first frame of picking weapon (Act_punch).
        if (wpoint == null) {
          vx = vy = vz = 0.0;
          release();
          yield actionOnHand.shifts(frame.curr);
        } else {
          yield moveOnHand(wpoint);
        }
      }
      case IN_THE_SKY, THROWING, ON_GROUND -> Action.UNASSIGNED;
      case JUST_ON_GROUND -> {
        mutuallyExcludedList.clear();
        yield Action.UNASSIGNED;
      }
      default -> {
        logger.log(Level.WARNING, "Unexpected State.%s", frame.state);
        yield Action.UNASSIGNED;
      }
    };
  }

  private Action moveOnHand(Wpoint wpoint) {
    setRelativePosition(holder.getRelativePosition(wpoint), frame.wpoint, wpoint.cover);
    if (wpoint.usage == Wpoint.Usage.THROW) {
      vx = faceRight ? wpoint.dvx : -wpoint.dvx;
      vy = wpoint.dvy;
      vz = holder.getInputZ() * wpoint.dvz;
      release();
      return actionThrowing.shifts(wpoint.weaponact.index);
      // TODO: hero-side release weapon reference
    } else {
      vx = vy = vz = 0.0;
      return wpoint.weaponact;
    }
  }

  private Action landing() {
    hp -= dropHurt;
    if (vy < type.threshold) {
      vy = 0.0;
      return actionOnHand;
      // return Action.HEAVY_ON_GROUND;
    } else {
      vy *= type.vyLast;
      return actionInTheSky.shifts(0);
    }
    // TODO: It seems that if WeaponA has applied itr on WeaponB in State.THROWING,
    // then WeaponA will immune to WeaponB until WeaponA goes into State.ON_GROUND.
  }

  @Override
  protected List<Tuple<Itr, Region>> computeItrList() {
    if (holder == null) {
      return super.computeItrList();
    }
    if (type == Type.HEAVY) {
      return List.of();
    }
    Wpoint wpoint = holder.getWpoint();
    Itr strengthItr = strength.get(wpoint.usage);
    if (strengthItr == null) {
      return List.of();
    }
    return List.of(); // TODO
  }

  @Override
  public void receiveItr(Observable source, Itr itr, Region absoluteRegion) {
    if (source instanceof Weapon w) {
      mutuallyExcludedList.add(w);
    }
    /*
     * for (Tuple<Observable, Itr> tuple : recvItrList) {
     * Itr itr = tuple.second;
     * hp -= itr.injury;
     * vx += itr.calcDvx(vx, faceRight);
     * vy += itr.dvy;
     * if (itr.fall >= 0) {
     * // nextAct = type.hitAct(fall, vx);
     * // if (nextAct != Action.TBA) {
     * // transitFrame(nextAct);
     * // }
     * if (isHeavy()) {
     * faceRight ^= true;
     * vx *= -type.vxLast;
     * vz *= type.vxLast;
     * }
     * }
     * if (itr.bdefend >= 100) {
     * destroy();
     * }
     * }
     */
  }

  @Override
  public void sendItr(Observable target, Itr itr) {
    // TODO Auto-generated method stub
    if (target instanceof Weapon w) {
      mutuallyExcludedList.add(w);
    }
  }

  @Override
  protected Action updateKinetic() {
    if (holder == null) {
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
    if (py < 0.0) {
      vy = env.applyGravity(vy) * type.gravityRatio;
      return Action.UNASSIGNED;
    } else {
      py = 0.0;
      vx = env.applyFriction(vx * type.vxLast);
      vz = env.applyFriction(vz * type.vxLast);
      return landing();
    }
  }

  @Override
  protected Action updateStamina() {
    return hp >= 0.0 && mp >= 0.0 ? Action.UNASSIGNED : Action.REMOVAL;
  }

  @Override
  protected boolean fitBoundary() {
    Region boundary = env.getItemBoundary();
    if (frame.state != State.ON_GROUND || (boundary.x1() >= px && px >= boundary.x2())) {
      pz = Util.clamp(pz, boundary.z1(), boundary.z2());
      return true;
    } else {
      return false;
    }
  }

}
