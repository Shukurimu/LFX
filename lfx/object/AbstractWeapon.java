package lfx.object;

import java.util.ArrayList;
import lfx.component.Type;
import lfx.object.AbstractObject;

abstract class Weapon extends AbstractObject {
  public static final double INITIAL_MP = 750.0;
  public static final Map<String, Double> SPECIAL_MP = Map.of(
      "Milk", 500.0 / 3.0
  );

  public final double dropHurt;
  public final String soundHit;
  public final String soundDrop;
  public final String soundBroken;

  protected AbstractWeapon(Type type, String identifier, List<Frame> frameList, double dropHurt,
                           String soundHit, String soundDrop, String soundBroken) {
    super(type, identifier, frameList);
    assert type.isWeapon;
    this.dropHurt = dropHurt;
    this.soundHit = soundHit;
    this.soundDrop = soundDrop;
    this.soundBroken = soundBroken;
    mp = INITIAL_MP.getOrDefault("Milk", INITIAL_MP);
  }

  protected AbstractWeapon(AbstractWeapon baseWeapon) {
    super(this);
  }

  @Override
  public void revive() {
    hp = hpMax;
    mp = INITIAL_MP.getOrDefault("Milk", INITIAL_MP);
    return;
  }

  @Override
  protected final CanonicalAct getCanonicalAct(int action) {
    return new CanonicalAct(action, hittingAct());
  }

  public final void broken() {
    hp = 0.0;
    // TODO: create fabrics
    return;
  }

  @Override
  public boolean react() {
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
      broken;
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
  public boolean update() {
    int nextAct = DEFAULT_ACT;
    if (wpunion == null) {
        /** It seems that if WeaponA has applied itr on WeaponB in State_throw, then
            WeaponA will immune to WeaponB until WeaponA goes into a frame with state onground. */
      if (currFrame.state == LFstate.ONGROUND)
        immune.clear();
      if (hitLag == 0) {
        vx = currFrame.calcVX(vx, faceRight);
        px = extra.containsKey(LFextra.Kind.MOVEBLOCK) ? px : (px + vx);
        vy = currFrame.calcVY(vy);
        if (extra.containsKey(LFextra.Kind.MOVEBLOCK) || (currFrame.dvz == LFframe.DV_550))
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
      /* trans to next frame */
      if (hitLag > 0) {
        --hitLag;
      } else if (nextAct != DEFAULT_ACT) {
        setCurr(nextAct);
      } else if (--waitTU < 0) {
        LFframe nextFrame = getFrame(currFrame.next);
        if (nextFrame == null)
          return false;
        setNext(nextFrame);
      }

    } else {
      teamID = wpunion.teamID;
      final LFwpoint wpoint = wpunion.currFrame.wpoint;
      if (wpoint != null) {// there is no wpoint in the first frame of picking weapon (Act_punch)
        setCurr(wpoint.waction);
        if (wpunion.faceRight) {
          faceRight = true;
          px = (wpunion.px - wpunion.currFrame.centerR + wpoint.x) - (currFrame.wpoint.x - currFrame.centerR);
          py = (wpunion.py - wpunion.currFrame.centerY + wpoint.y) - (currFrame.wpoint.y - currFrame.centerY);
          pz = wpunion.pz + PICKINGOFFSET;
        } else {
          faceRight = false;
          px = (wpunion.px + wpunion.currFrame.centerR - wpoint.x) + (currFrame.wpoint.x - currFrame.centerR);
          py = (wpunion.py - wpunion.currFrame.centerY + wpoint.y) - (currFrame.wpoint.y - currFrame.centerY);
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

    registerItr();
    registerBdy();

    if (hp > 0.0) {
      return true;
    } else {
      broken();
      return false;
    }
  }

  @Override
  public List<Tuple<Itr, Area>> registerItrArea();
    if (wpunion != dummy) {
      for (LFitr i: currFrame.itr) {
        if (i.effect == LFeffect.WPSTREN) {
          if (wpunion.currFrame.wpoint.attacking > 0)
          currItr.add(new LFitrarea(this, i, strength[wpunion.currFrame.wpoint.attacking]));
        } else
          currItr.add(new LFitrarea(this, i));
      }
    } else {
      for (LFitr i: currFrame.itr) {
        currItr.add(new LFitrarea(this, i));
      }
    }
    return;
  }

  @Override
  public List<Tuple<Bdy, Area>> registerBdyArea();
    if (wpunion != dummy)
      return List.of();
    for (LFbdy b: currFrame.bdy) {
      currBdy.add(new LFbdyarea(this, b));
    }
    return;
  }

  @Override
  protected boolean adjustBoundary(int[] xzBound) {
    if ((currFrame.state != State.ONGROUND) || (px > xzBound[0] && px < xzBound[1])) {
      pz = Function.clamp(pz, xzBound[2], xzBound[3]);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public int landingAct() {
    return type == Type.HEAVY ? 20 : 70;
  }

  @Override
  public int bouncingAct() {
    return type == Type.HEAVY ? ACT_TBA :
           type == Type.LIGHT ? 7 : 0;
  }

  @Override
  public int hittingAct() {
    return type == Type.HEAVY ? ACT_TBA : Global.randomBounds(0, 16);
  }

  @Override
  public int hitAct(int fall) {
    if (type == Type.HEAVY)
      return fall > 60 ? Global.randomBounds(0, 6) : ACT_TBA;
    else
      return type == Type.LIGHT ? Global.randomBounds(0, 16) :
                                  vx >= 10.0 ? 40 : ACT_TBA;
  }

}
