package lfx.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lfx.component.Frame;
import lfx.component.Itr;
import lfx.object.AbstractObject;
import lfx.object.Weapon;

public class BaseWeapon extends AbstractObject implements Weapon {

  private static enum Subtype {
    SMALL(0.500, List.of("Baseball")),
    DRINK(0.667, List.of("Beer", "Milk")),
    HEAVY(1.000, List.of("Stone", "WoodenBox", "LouisArmour1", "LouisArmour2")),
    LIGHT(1.000, List.of(/* default */));

    public final double gravityRatio;
    public final List<String> samples;

    private Subtype(double gravityRatio, List<String> samples) {
      this.gravityRatio = gravityRatio;
      this.samples = samples;
    }

    public static Subtype getSubtype(String identifier) {
      for (Subtype subtype : Subtype.values()) {
        if (subtype.samples.contains(identifier)) {
          return subtype;
        }
      }
      return LIGHT;
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
  private Hero owner = null;

  protected BaseWeapon(List<Frame> frameList, Map<Wpoint.Usage, Itr> strengthMap,
                       Map<String, String> stamina) {
    super(identifier, frameArray);
    subtype = Subtype.getSubtype(identifier);
    isHeavy = subtype == Subtype.HEAVY;
    gravityRatio = subtype.gravityRatio;

    mp = SPECIAL_MP.getOrDefault(identifier, INITIAL_MP);
    hp = Double.valueOf(stamina.get(Key_hp));

    dropHurt = stamina.get(Key_drop_hurt);
    soundHit = stamina.get(Key_hit_sound);
    soundDrop = stamina.get(Key_drop_sound);
    soundBroken = stamina.get(Key_broken_sound);
    this.strengthMap = strengthMap;
  }

  protected BaseWeapon(BaseWeapon base) {
    super(this);
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
  public Weapon makeClone() {
    return new BaseWeapon(weaponMapping.get(identifier));
  }

  @Override
  protected void registerObjectMap() {
    weaponMapping.putIfAbsent(identifier, this);
    return;
  }

  @Override
  protected int getDefaultActNumber() {
    if (isHeavy) {
      return py < 0.0 ? Global.randomBounds(0, HEAVY_RANGE) : HEAVY_STABLE_ON_GROUND;
    } else {
      return py < 0.0 ? Global.randomBounds(0, ACT_RANGE) : ACT_STABLE_ON_GROUND;
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
  protected int getScopeView(int targetTeamId) {
    return Global.getSideView(Global.SCOPE_VIEW_WEAPON, targetTeamId == this.teamId);
  }

  @Override
  protected void itrCallback() {
    System.out.println("itrCallback NotImplemented");
    return;
  }

  @Override
  public void react() {
    int bdefend = 0;
    int injury = 0;
    int fall = 0;
    int dvx = 0;
    int dvy = 0;
    int lag = 0;

    RESULT_LOOP:
    for (Tuple<AbstractObject, Itr> tuple: resultItrList) {
      final AbstractObject that = tuple.first;
      final Itr itr = tuple.second;
      switch (itr.effect) {
      }
      bdefend = Math.max(bdefend, itr.bdefend);
      injury += itr.injury;
      fall = Math.max(fall, itr.fall);
      dvx += itr.calcDvx(vx);
      dvy += itr.dvy;
    }
    hp -= injury;
    if (bdefend == 100) {
      destroy();
    } else if (fall >= 0) {
      vx = dvx;
      nextAct = type.hitAct(fall, vx);
      if (nextAct != ACT_DEF)
        setCurr(act);
      if (type != Type.HEAVY) {
        faceRight ^= true;
        vx *= -type.vxLast;
        vz *=  type.vxLast;
      }
    }
    resultItrList.clear();
    return;
  }

  @Override
  protected int updateAction(int nextAct) {
    return owner == null ? moveFree(nextAct) : moveHeld(nextAct);
  }

  private int moveFree(int nextAct) {
    /** It seems that if WeaponA has applied itr on WeaponB in State_throw, then
        WeaponA will immune to WeaponB until WeaponA goes into a frame with state onground. */
    if (frame.state == LFstate.ONGROUND)
      immune.clear();
    if (hitLag == 0) {
      vx = frame.calcVX(vx, faceRight);
      px = extra.containsKey(LFextra.Kind.MOVEBLOCK) ? px : (px + vx);
      vy = frame.calcVY(vy);
      if (extra.containsKey(LFextra.Kind.MOVEBLOCK) || (frame.dvz == LFframe.DV_550))
        vz = 0.0;
      else
        pz += vz;
      if (py + vy >= 0.0) {
        if (py < 0.0) {
          hp -= drophurt;
          if (hp < 0.0)
            broken();
          else {
            if (vy < type.threshold) {
              nextAct = type.landingAct;
              vy = 0.0;
            } else {
              nextAct = type.bounceAct;
              vy *= type.vyLast;
            }
            vx = map.applyFriction(vx * type.vxLast);
            vz = map.applyFriction(vz * type.vxLast);
          }
        } else {
          vx = map.applyFriction(vx);
          vz = map.applyFriction(vz);
          vy = 0.0;
        }
        py = 0.0;
      } else {
        py += vy;
        vy += map.gravity * type.gRatio;
      }
    }
    return;
  }

  private int moveHeld(int nextAct) {
    teamID = wpunion.teamID;
    final LFwpoint wpoint = wpunion.frame.wpoint;
    if (wpoint != null) {// there is no wpoint in the first frame of picking weapon (Act_punch)
      setCurr(wpoint.waction);
      if (wpunion.faceRight) {
        faceRight = true;
        px = (wpunion.px - wpunion.frame.centerR + wpoint.x) - (frame.wpoint.x - frame.centerR);
        py = (wpunion.py - wpunion.frame.centerY + wpoint.y) - (frame.wpoint.y - frame.centerY);
        pz = wpunion.pz + PICKINGOFFSET;
      } else {
        faceRight = false;
        px = (wpunion.px + wpunion.frame.centerR - wpoint.x) + (frame.wpoint.x - frame.centerR);
        py = (wpunion.py - wpunion.frame.centerY + wpoint.y) - (frame.wpoint.y - frame.centerY);
        pz = wpunion.pz + PICKINGOFFSET;
      }
      if (wpoint.dvx != 0 || wpoint.dvy != 0 || wpoint.attacking < 0) {
        vx = faceRight ? wpoint.dvx : (-wpoint.dvx);
        vy = wpoint.dvy;
        vz = wpunion.getControlZ() * wpoint.dvz;
        setCurr(wpoint.waction + type.throwOffset);
        wpunion.weapon = dummy;
        wpunion = null;
      }
    }

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
    double[] xzBound = env.getNonHeroXzBound();
    if (frame.state != State.ONGROUND || (xzBound[0] >= px && px >= xzBound[1])) {
      pz = Global.clamp(pz, xzBound[2], xzBound[3]);
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
  public Tuple<Itr, Area> getStrengthItrs(Wpoint.Usage wusage) {
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
