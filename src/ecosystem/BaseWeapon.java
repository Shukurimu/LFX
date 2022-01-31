package ecosystem;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import base.Region;
import base.Scope;
import base.Type;
import base.Vector;
import component.Action;
import component.Effect;
import component.Frame;
import component.Itr;
import component.State;
import component.Wpoint;
import util.Tuple;

public class BaseWeapon extends AbstractObject implements Weapon {
  private static final System.Logger logger = System.getLogger("");
  protected static final int DEFAULT_ITR_SCOPE =
      Scope.ENEMY_HERO | Scope.ALL_WEAPON | Scope.ALL_ENERGY;

  private final Action actionOnHand;
  private final Action actionThrowing;
  private final Action actionInTheSky;
  private final Action actionOnGround;
  private final Map<Wpoint.Usage, Itr> strength;
  private final List<Observable> mutuallyExcludedList = new ArrayList<>();

  protected double dropHurt = 0.0;
  protected String soundHit = "";
  protected String soundDrop = "";
  protected String soundBroken = "";

  protected BaseWeapon(Frame.Collector collector, Type type, Map<Wpoint.Usage, Itr> strength) {
    super(type, collector);
    if (!type.isWeapon) {
      throw new IllegalArgumentException("Not a weapon type.");
    }
    if (type == Type.HEAVY) {
      actionOnHand = Action.HEAVY_ON_HAND;
      actionThrowing = Action.HEAVY_THROWING;
      actionInTheSky = Action.HEAVY_IN_THE_SKY;
      actionOnGround = Action.HEAVY_ON_GROUND;
    } else {
      actionOnHand = Action.LIGHT_ON_HAND;
      actionThrowing = Action.LIGHT_THROWING;
      actionInTheSky = Action.LIGHT_IN_THE_SKY;
      actionOnGround = Action.LIGHT_ON_GROUND;
    }
    this.strength = Collections.unmodifiableMap(new EnumMap<>(strength));
  }

  protected BaseWeapon(BaseWeapon base) {
    super(base);
    actionOnHand = base.actionOnHand;
    actionThrowing = base.actionThrowing;
    actionInTheSky = base.actionInTheSky;
    actionOnGround = base.actionOnGround;
    strength = base.strength;
    hpMax = base.hpMax;
    dropHurt = base.dropHurt;
    soundHit = base.soundHit;
    soundDrop = base.soundDrop;
    soundBroken = base.soundBroken;
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

  protected void release(Vector velocity) {
    vx = velocity.x();
    vy = velocity.y();
    vz = velocity.z();
    return;
  }

  @Override
  public void destroy() {
    hp = 0.0;
    // TODO: create fabrics
    return;
  }

  @Override
  protected int checkInteraction(List<Tuple<Itr, Region>> itrList, Observable that) {
    if (mutuallyExcludedList.contains(that)) {
      return 0;
    } else {
      return super.checkInteraction(itrList, that);
    }
  }

  @Override
  protected final Action updateByState() {
    return switch (frame.state) {
      case ON_HAND -> moveOnHand();
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

  private Action moveOnHand() {
    Optional<SyncPick> osp = SyncPick.getVictim(this);
    if (osp.isEmpty()) {
      return Action.DEFAULT;
    }
    SyncPick sync = osp.get();
    transitGoto(sync.getVictimAction());
    faceRight = sync.updateVictim(frame.wpoint);
    Vector velocity = sync.getThrowVelocity();
    if (velocity == Vector.ZERO) {
      return Action.CONTROLLED;
    } else {
      vx = velocity.x();
      vy = velocity.y();
      vz = velocity.z();
      if (sync.getUsage() == Wpoint.Usage.RELEASE) {
        return actionInTheSky.shift(frame.curr - actionOnHand.index);
      } else {
        return actionThrowing.shift(frame.curr - actionOnHand.index);
      }
    }
  }

  private Action landing() {
    hp -= dropHurt;
    if (vy < type.threshold) {
      vy = 0.0;
      return actionOnGround;
    } else {
      vy *= -0.333;
      return actionInTheSky.shift(random);
    }
  }

  @Override
  protected List<Tuple<Itr, Region>> computeItrList() {
    Optional<SyncPick> osp = SyncPick.getVictim(this);
    if (osp.isEmpty()) {
      return super.computeItrList();
    }
    if (type == Type.HEAVY) {
      return List.of();
    }
    Itr strengthItr = strength.get(osp.get().getUsage());
    if (strengthItr == null) {
      return List.of();
    }
    return List.of(); // TODO
  }

  @Override
  public void sendItr(Observable target, Itr itr) {
    if (target instanceof Weapon w) {
      mutuallyExcludedList.add(w);
    }
    switch (itr.kind) {
      case PUNCH:
      case STAB:
      case FIRE:
      case WEAK_FIRE:
      case ICE:
      case WEAK_ICE:
        if (itr.param instanceof Itr.Damage x) {
          actionPause = Math.max(actionPause, terrain.getTimestamp() + x.actPause());
          return;
        }
        break;
      case WEAPON_STRENGTH:
        logger.log(Level.WARNING, "not implemented %s", itr);
        return;
      case FORCE_ACTION:
      case BLOCK:
      case SONATA:
      case VORTEX:
        return;
      case SHIELD:
      case THROWN_DAMAGE:
      case PICK:
      case ROLL_PICK:
      case HEAL:
      case GRAB_DOP:
      case GRAB_BDY:
        break;
    }
    logger.log(Level.WARNING, "%s sent unexpected: %s", this, itr);
  }

  @Override
  public boolean receiveItr(Observable source, Itr itr, Region absoluteRegion) {
    if (source instanceof Weapon w) {
      mutuallyExcludedList.add(w);
    }
    switch (itr.kind) {
      case PUNCH:
      case STAB:
      case FIRE:
      case WEAK_FIRE:
      case ICE:
      case WEAK_ICE:
      case SHIELD:
      case THROWN_DAMAGE:
        if (itr.param instanceof Itr.Damage x) {
          if (x.bdefend() >= 100) {
            destroy();
            return true;
          }
          actionPause = Math.max(actionPause, terrain.getTimestamp() + x.actPause());
          hp -= x.injury();
          vx += x.calcDvx(source.isFaceRight());
          vy += x.dvy();
          if (x.fall() >= 0) {
            // nextAct = type.hitAct(fall, vx);
            // if (nextAct != Action.TBA) {
            // transitFrame(nextAct);
            // }
            if (isHeavy()) {
              faceRight ^= true;
              vx *= -type.vxLast;
              vz *= type.vxLast;
            }
          }
        }
        return true;
      case ROLL_PICK:
      case PICK:
        if (source instanceof Hero x && SyncPick.tryRegister(x, this, terrain.getTimestamp())) {
          teamId = source.getTeamId();
          transitGoto(Action.LIGHT_ON_HAND);
          return true;
        } else {
          return false;
        }
      case BLOCK:
        buff.put(Effect.MOVE_BLOCKING, 0);
        return true;
      case SONATA:
      case VORTEX:
        logger.log(Level.INFO, "Unimplemented: %s", itr);
        return true;
      case FORCE_ACTION:
      case WEAPON_STRENGTH:
      case HEAL:
      case GRAB_DOP:
      case GRAB_BDY:
        break;
    }
    logger.log(Level.WARNING, "%s received unexpected: %s", this, itr);
    return false;
  }

  @Override
  protected Action updateKinetic() {
    if (SyncPick.getVictim(this).isPresent()) {
      return Action.UNASSIGNED;
    }
    vx = frame.calcVx(vx, faceRight);
    vy = frame.calcVy(vy);
    vz = frame.calcVz(vz, 0.0);

    if (buff.getOrDefault(Effect.MOVE_BLOCKING, 0) < terrain.getTimestamp()) {
      px += vx;
      pz += vz;
    }
    boolean beforeOnGround = py >= 0.0;
    py += vy;
    boolean afterOnGround = py >= 0.0;
    if (beforeOnGround && afterOnGround) {
      py = 0.0;
      vx = terrain.applyFriction(vx);
      vz = terrain.applyFriction(vz);
      return Action.UNASSIGNED;
    }

    if (!afterOnGround) {
      vy += terrain.getGravity() * type.gravityRatio;
      return Action.UNASSIGNED;
    }

    py = 0.0;
    vx = terrain.applyLandingFriction(vx);
    vz = terrain.applyLandingFriction(vz);

    return landing();
  }

  @Override
  protected Action updateStamina() {
    return hp >= 0.0 && mp >= 0.0 ? Action.UNASSIGNED : Action.REMOVAL;
  }

  @Override
  protected boolean fitBoundary() {
    Region boundary = terrain.getItemBoundary();
    // System.out.printf("%d %.2f %.2f %.2f %n", frame.curr, px, py, pz);
    if (frame.state != State.ON_GROUND || (boundary.x1() < px && px < boundary.x2())) {
      pz = Math.min(Math.max(pz, boundary.z1()), boundary.z2());
      return true;
    } else {
      return false;
    }
  }

}
