package lfx.game.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lfx.base.Action;
import lfx.base.Scope;
import lfx.base.Type;
import lfx.component.Effect;
import lfx.component.Frame;
import lfx.component.Itr;
import lfx.component.State;
import lfx.component.Wpoint;
import lfx.game.Hero;
import lfx.game.Library;
import lfx.game.Observable;
import lfx.game.Weapon;
import lfx.util.Area;
import lfx.util.Tuple;
import lfx.util.Util;

class BaseWeapon extends AbstractObject implements Weapon {
  public final Type type;
  public final double dropHurt;
  public final String soundHit;
  public final String soundDrop;
  public final String soundBroken;
  public final Map<Wpoint.Usage, Itr> strengthMap;
  private Hero holder = null;

  protected BaseWeapon(String identifier, List<Frame> frameList, Map<String, String> stamina,
                       Type type, Map<Wpoint.Usage, Itr> strengthMap) {
    super(identifier, frameList, Scope.WEAPON);
    this.type = type;
    mp = identifier.equals("Milk") ? INITIAL_MILK_MP : INITIAL_MP;
    hp = Double.valueOf(stamina.get(Key_hp));
    dropHurt = Double.valueOf(stamina.get(Key_drop_hurt));
    soundHit = stamina.get(Key_hit_sound);
    soundDrop = stamina.get(Key_drop_sound);
    soundBroken = stamina.get(Key_broken_sound);
    this.strengthMap = Map.copyOf(strengthMap);
  }

  private BaseWeapon(BaseWeapon base) {
    super(base);
    type = base.type;
    mp = base.mp;
    hp = base.hp;
    dropHurt = base.dropHurt;
    soundHit = base.soundHit;
    soundDrop = base.soundDrop;
    soundBroken = base.soundBroken;
    strengthMap = base.strengthMap;
  }

  @Override
  public BaseWeapon makeClone() {
    return new BaseWeapon(this);
  }

  @Override
  protected void registerLibrary() {
    Library.instance().register(this);
    return;
  }

  @Override
  protected Action getDefaultAct() {
    if (isHeavy()) {
      return py < 0.0 ? Action.HEAVY_IN_THE_SKY.shifts(0) : Action.HEAVY_STABLE_ON_GROUND;
    } else {
      return py < 0.0 ? Action.LIGHT_IN_THE_SKY.shifts(0) : Action.LIGHT_STABLE_ON_GROUND;
    }
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
  public Observable getHolder() {
    return holder;
  }

  @Override
  protected void addRaceCondition(Observable competitor) {
    holder = (Hero) competitor;
    return;
  }

  @Override
  public List<Double> consume() {
    if (identifier.equals("Milk")) {
      mp -= MILK_REGENERATION.get(0);
      return MILK_REGENERATION;
    }
    if (identifier.equals("Beer")) {
      mp -= BEER_REGENERATION.get(0);
      return BEER_REGENERATION;
    }
    System.out.println("Unknown consume: " + this.toString());
    return OTHERS_REGENERATION;
  }

  @Override
  public void react() {
    for (Tuple<Observable, Itr> tuple : recvItrList) {
      Itr itr = tuple.second;
      hp -= itr.injury;
      vx += itr.calcDvx(vx, faceRight);
      vy += itr.dvy;
      if (itr.fall >= 0) {
        // nextAct = type.hitAct(fall, vx);
        // if (nextAct != Const.TBA) {
          // transitFrame(nextAct);
        // }
        if (isHeavy()) {
          faceRight ^= true;
          vx *= -type.vxLast;
          vz *=  type.vxLast;
        }
      }
      if (itr.bdefend >= 100) {
        destroy();
      }
    }
    recvItrList.clear();
    return;
  }

  @Override
  protected Action updateAction(Action nextAct) {
    // case IN_THE_SKY:
    // case ON_HAND:
    // case THROWING:
    // case ON_GROUND:
    // case JUST_ON_GROUND:
    if (frame.state == State.ON_HAND) {
      teamId = holder.getTeamId();
      Wpoint wpoint = holder.getWpoint();
      // There is no wpoint in the first frame of picking weapon (Act_punch).
      if (wpoint != null) {
        setPosition(holder.getBasePosition(wpoint), frame.wpoint, wpoint.zOffset);
        if (wpoint.usage == Wpoint.Usage.THROW) {
          vx = faceRight ? wpoint.dvx : -wpoint.dvx;
          vy = wpoint.dvy;
          vz = holder.getInputZ() * wpoint.dvz;
          nextAct = isHeavy() ? Action.HEAVY_THROWING.shifts(wpoint.weaponact.index)
                              : Action.LIGHT_THROWING.shifts(wpoint.weaponact.index);
          release();
          // TODO: hero-side release weapon reference
        } else {
          vx = vy = vz = 0.0;
          nextAct = wpoint.weaponact;
        }
      } else {
        vx = vy = vz = 0.0;
        nextAct = isHeavy() ? Action.HEAVY_ON_HAND.shifts(frame.curr)
                            : Action.LIGHT_ON_HAND.shifts(frame.curr);
        release();
      }
    }
    return nextAct;
  }

  private Action landing() {
    hp -= dropHurt;
    if (vy < type.threshold) {
      vy = 0.0;
      return isHeavy() ? Action.HEAVY_ON_GROUND : Action.LIGHT_ON_HAND;
    } else {
      vy *= type.vyLast;
      return isHeavy() ? Action.HEAVY_IN_THE_SKY.shifts(0) : Action.LIGHT_IN_THE_SKY.shifts(0);
    }
    // TODO: It seems that if WeaponA has applied itr on WeaponB in State.THROWING, then
    // WeaponA will immune to WeaponB until WeaponA goes into a State.ON_GROUND frame.
  }

  @Override
  protected Action updateKinetic(Action nextAct) {
    if (actPause != 0) {
      return Action.UNASSIGNED;
    }
    if (holder == null) {
      return nextAct;
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
    } else {
      nextAct = landing();
      vy = py = 0.0;
      vx = env.applyFriction(vx * type.vxLast);
      vz = env.applyFriction(vz * type.vxLast);
    }
    return nextAct;
  }

  @Override
  protected Action updateStamina(Action nextAct) {
    return hp >= 0.0 && mp >= 0.0 ? nextAct : Action.REMOVAL;
  }

  @Override
  protected boolean fitBoundary() {
    List<Double> xBound = env.getItemXBound();
    if (frame.state != State.ON_GROUND || (xBound.get(0) >= px && px >= xBound.get(1))) {
      List<Double> zBound = env.getZBound();
      pz = Util.clamp(pz, zBound.get(0), zBound.get(1));
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected List<Tuple<Itr, Area>> getCurrentItrs() {
    if (holder == null) {
      return super.getCurrentItrs();
    }
    Wpoint wpoint = holder.getWpoint();
    Itr strengthItr = strengthMap.get(wpoint.usage);
    if (strengthItr == null) {
      return List.of();
    }
    List<Tuple<Itr, Area>> result = new ArrayList<>(4);
    for (Itr itr : frame.itrList) {
      result.add(new Tuple<>(strengthItr, makeArea(itr.box)));
    }
    return result;
  }

}
