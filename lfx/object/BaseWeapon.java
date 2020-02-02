package lfx.object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lfx.component.Effect;
import lfx.component.Frame;
import lfx.component.Itr;
import lfx.component.State;
import lfx.component.Wpoint;
import lfx.object.AbstractObject;
import lfx.object.Weapon;
import lfx.util.Area;
import lfx.util.Const;
import lfx.util.Tuple;
import lfx.util.Util;

class BaseWeapon extends AbstractObject implements Weapon {

  protected enum Subtype {
    SMALL(0.500,  9.0, 0.6, -0.4),
    DRINK(0.667,  9.0, 0.6, -0.4),
    HEAVY(1.000, 10.0, 0.3, -0.2),
    LIGHT(1.000, 10.0, 0.6, -0.4);

    public final double gravityRatio;
    public final double threshold;
    public final double vxLast;
    public final double vyLast;

    private Subtype(double gravityRatio, double threshold, double vxLast, double vyLast) {
      this.gravityRatio = gravityRatio;
      this.threshold = threshold;
      this.vxLast = vxLast;
      this.vyLast = vyLast;
    }

  }

  public final Subtype subtype;
  public final boolean isHeavy;
  public final double gravityRatio;
  public final double dropHurt;
  public final String soundHit;
  public final String soundDrop;
  public final String soundBroken;
  public final Map<Wpoint.Usage, Itr> strengthMap;
  public final Set<Weapon> immune = new HashSet<>();
  private Hero owner = null;

  protected BaseWeapon(String identifier, List<Frame> frameList, Subtype subtype,
                       Map<String, String> stamina, Map<Wpoint.Usage, Itr> strengthMap) {
    super(identifier, frameList);
    this.subtype = subtype;
    isHeavy = subtype == Subtype.HEAVY;
    gravityRatio = subtype.gravityRatio;

    mp = SPECIAL_MP.getOrDefault(identifier, INITIAL_MP);
    hp = Double.valueOf(stamina.get(Key_hp));

    dropHurt = Double.valueOf(stamina.get(Key_drop_hurt));
    soundHit = stamina.get(Key_hit_sound);
    soundDrop = stamina.get(Key_drop_sound);
    soundBroken = stamina.get(Key_broken_sound);
    this.strengthMap = strengthMap;
  }

  protected BaseWeapon(BaseWeapon base) {
    super(base);
    subtype = base.subtype;
    isHeavy = base.isHeavy;
    gravityRatio = base.gravityRatio;
    mp = base.mp;
    hp = base.hp;
    dropHurt = base.dropHurt;
    soundHit = base.soundHit;
    soundDrop = base.soundDrop;
    soundBroken = base.soundBroken;
    strengthMap = base.strengthMap;
  }

  @Override
  public BaseWeapon makeClone(int teamId, boolean faceRight) {
    BaseWeapon clone = new BaseWeapon((BaseWeapon) weaponMapping.get(identifier));
    clone.teamId = teamId;
    clone.faceRight = faceRight;
    return clone;
  }

  @Override
  protected void registerObjectMap() {
    weaponMapping.putIfAbsent(identifier, this);
    return;
  }

  @Override
  protected int getDefaultActNumber() {
    if (isHeavy) {
      return py < 0.0 ? Util.randomBounds(0, HEAVY_RANGE) : HEAVY_STABLE_ON_GROUND;
    } else {
      return py < 0.0 ? Util.randomBounds(0, ACT_RANGE) : ACT_STABLE_ON_GROUND;
    }
  }

  @Override
  public boolean isHeavy() {
    return isHeavy;
  }

  @Override
  public boolean isDrink() {
    return subtype == Subtype.DRINK;
  }

  @Override
  public boolean isLight() {
    return subtype == Subtype.LIGHT;
  }

  @Override
  public boolean isSmall() {
    return subtype == Subtype.SMALL;
  }

  /** Return null if cannot be consumed. */
  @Override
  public List<Double> consume() {
    if (0.0 >= mp) {
      hp = 0.0;
      return null;
    }
    if (identifier == "Milk") {
      mp -= MILK_REGENERATION.get(0);
      return MILK_REGENERATION;
    } else if (identifier == "Beer") {
      mp -= BEER_REGENERATION.get(0);
      return BEER_REGENERATION;
    }
    System.out.println("Unknown consume: " + identifier);
    return null;
  }

  @Override
  public void destroy() {
    hp = 0.0;
    // TODO: create fabrics
    return;
  }

  @Override
  public int getScopeView(int targetTeamId) {
    return Const.getSideView(Const.SCOPE_VIEW_WEAPON, targetTeamId == this.teamId);
  }

  @Override
  protected void itrCallback() {
    System.out.println("itrCallback NotImplemented");
    return;
  }

  @Override
  public void react() {
    RESULT_LOOP:
    for (Tuple<Observable, Itr> tuple: recvItrList) {
      final Observable that = tuple.first;
      final Itr itr = tuple.second;
      hp -= itr.injury;
      vx += itr.calcDvx(vx, faceRight);
      vy += itr.dvy;
      if (itr.fall >= 0) {
        // nextAct = subtype.hitAct(fall, vx);
        // if (nextAct != Const.TBA) {
          // transitFrame(nextAct);
        // }
        if (isHeavy) {
          faceRight ^= true;
          vx *= -subtype.vxLast;
          vz *=  subtype.vxLast;
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
  protected int updateAction(int nextAct) {
    return owner == null ? moveFree(nextAct) : moveHeld(nextAct);
  }

  private int landing() {
    int nextAct = Const.TBA;
    if (vy < subtype.threshold) {
      nextAct = 0;
      vy = 0.0;
    } else {
      nextAct = 0;
      vy *= subtype.vyLast;
    }
    hp -= dropHurt;
    // It seems that if WeaponA has applied itr on WeaponB in State.THROWING, then
    // WeaponA will immune to WeaponB until WeaponA goes into a State.ON_GROUND frame.
    immune.clear();
    return nextAct;
  }

  private int moveFree(int nextAct) {
    if (actLag == 0) {
      return nextAct;
    }
    vx = frame.calcVX(vx, faceRight);
    px = buff.containsKey(Effect.MOVE_BLOCKING) ? px : (px + vx);
    vy = frame.calcVY(vy);
    if (buff.containsKey(Effect.MOVE_BLOCKING) || frame.dvz == Const.DV_550) {
      vz = 0.0;
    } else {
      pz += vz;
    }
    if (py + vy >= 0.0) {
      nextAct = landing();
      vx = env.applyFriction(vx * subtype.vxLast);
      vz = env.applyFriction(vz * subtype.vxLast);
      py = 0.0;
    } else {
      py += vy;
      vy = env.applyGravity(vy) * gravityRatio;
    }
    return nextAct;
  }

  private int moveHeld(int nextAct) {
    teamId = owner.getTeamId();
    Wpoint wpoint = owner.getWpoint();
    // there is no wpoint in the first frame of picking weapon (Act_punch)
    if (wpoint != null) {
      setPosition(owner.getBasePosition(wpoint), frame.wpoint, wpoint.zOffset);
      if (wpoint.usage == Wpoint.Usage.DROP) {
        vx = faceRight ? wpoint.dvx : -wpoint.dvx;
        vy = wpoint.dvy;
        vz = owner.getInputZ() * wpoint.dvz;
        nextAct = wpoint.weaponact + (isHeavy ? HEAVY_RANGE : ACT_RANGE);
        owner = null;
        // TODO: hero-side release weapon reference
      } else {
        nextAct = wpoint.weaponact;
      }
    }
    return nextAct;
  }

  @Override
  protected int updateKinetic(int nextAct) {
    return ACT_TBA;
  }

  @Override
  protected int updateHealth(int nextAct) {
    return hp >= 0.0 && mp >= 0.0 ? ACT_TBA : ACT_TBA;
  }

  @Override
  protected int getNextActNumber() {
    return ACT_TBA;
  }

  @Override
  protected boolean adjustBoundary() {
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
  protected void updateBdys() {
    if (owner == null) {
      bdyList.clear();
    } else {
      super.updateBdys();
    }
    return;
  }

  @Override
  protected void updateItrs() {
    if (owner == null) {
      super.updateItrs();
    } else {
      bdyList.clear();
    }
    return;
  }

  @Override
  public List<Tuple<Itr, Area>> getStrengthItrs(Wpoint.Usage wusage) {
    Itr strengthItr = strengthMap.get(wusage);
    if (strengthItr == null) {
      return List.of();
    }
    List<Tuple<Itr, Area>> result = new ArrayList<>(4);
    for (Itr itr: frame.itrList) {
      result.add(new Tuple<>(strengthItr, makeArea(itr.box)));
    }
    return result;
  }

}
