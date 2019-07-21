package lfx.object;

import java.util.ArrayList;

abstract class LFweapon extends LFobject {
  public static final LFweapon dummy = new LFweapon("noWeapon", LFtype.NULL) {};
  /* this small value is added to pz while being held, so that the weapon image is rendered above the character */
  public static final double PICKINGOFFSET = 0.001;
  public static final int NONHEAVY_HITGROUND = 7;

  public String soundHit;
  public String soundDrop;
  public String soundBroken;
  public double drophurt = 35.0;
  public LFitr[] strength = null;
  public LFhero picker = null;

  protected LFweapon(String id, LFtype t) {
    super(id, t, 160);
    strength = new LFitr[5];
    mp = identifier.equals("Milk") ? 166 : 750;
  }

  @Override
  public int checkItrScope(LFobject o) {
    return (o.teamID == teamID) ? 0b000100 : 0b001000;
  }

  @Override
  public void revive() {
    hp = hpMax;
    mp = identifier.equals("Milk") ? 166 : 750;
    return;
  }

  @Override
  public void damageCallback(LFitr i, LFobject o) {
    if (picker == null) {
      hitLag = HITLAG_SPAN;
      if (o instanceof LFweapon)
        immune.add(o);
      int act = type.hittingAct();
      if (act != DEFAULT_ACT)
        setCurr(act);
      if (type != LFtype.HEAVY) {
        faceRight ^= true;
        vx *= -type.vxLast;
        vz *=  type.vxLast;
      }
    } else {
      picker.damageCallback(i, o);
    }
    return;
  }

  @Override
  public void damageReceived(LFitrarea ia, LFbdyarea ba) {
    recvDmg.add(ia, ba);
    teamID = ia.owner.teamID;
    return;
  }

  @Override
  public final void setCurr(int index) {
    super.setCurr(index);
    registerItr();
    registerBdy();
    return;
  }

  @Override
  protected final int resolveAct(int index) {
    if (index < 0)
      index = -index;
    return (index == Act_999) ? type.hittingAct() : index;
  }

  public final void broken() {
    hp = 0.0;
    System.out.printf("%s is broken.\n", this);
    return;
  }

  public final void setState(int h, int d, String sh, String sd, String sb) {
    hp = h;
    drophurt = d;
    soundHit = sh;
    soundDrop = sd;
    soundBroken = sb;
    return;
  }

  public final void setStrength(int index, LFitr i) {
    strength[index] = i;
    return;
  }

  @Override
  public boolean reactAndMove(LFmap map) {
    if (picker != null && !recvItr.isEmpty()) {
      for (LFitrarea r: recvItr) {
        switch (r.itr.effect) {
          case FENCE:
            extra.put(LFextra.Kind.MOVEBLOCK, LFextra.oneTime());
            break;
          case VORTEX:
          case REFLECT:
          case SONATA:
            System.out.printf("\nImplementing ItrKind: %d", r.itr.effect);
            break;
          default:
            System.out.printf("\n%s got unexpected ItrKind: %d", this, r.itr.effect);
        }
      }
      recvItr.clear();

      if (recvDmg.effect != LFeffect.NONE) {
        hp -= recvDmg.injury;
        if (recvDmg.lag)
          hitLag = HITLAG_SPAN;
        if (recvDmg.bdefend == 100)
          broken();
        else if (recvDmg.fall >= 0) {
          int act = type.hitAct(recvDmg.fall, vx);
          if (act != DEFAULT_ACT)
            setCurr(act);
        }
      }
      recvDmg.reset();
    }

    if (currFrame.state == LFstate.BROKENWEAPON) {
      broken();
      return false;
    }

    int nextAct = DEFAULT_ACT;
    if (picker == null) {
        /* It seems that if WeaponA has applied itr on WeaponB in State_throw, then
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
      teamID = picker.teamID;
      final LFwpoint wpoint = picker.currFrame.wpoint;
      if (wpoint != null) {// there is no wpoint in the first frame of picking weapon (Act_punch)
        setCurr(wpoint.waction);
        if (picker.faceRight) {
          faceRight = true;
          px = (picker.px - picker.currFrame.centerR + wpoint.x) - (currFrame.wpoint.x - currFrame.centerR);
          py = (picker.py - picker.currFrame.centerY + wpoint.y) - (currFrame.wpoint.y - currFrame.centerY);
          pz = picker.pz + PICKINGOFFSET;
        } else {
          faceRight = false;
          px = (picker.px + picker.currFrame.centerR - wpoint.x) + (currFrame.wpoint.x - currFrame.centerR);
          py = (picker.py - picker.currFrame.centerY + wpoint.y) - (currFrame.wpoint.y - currFrame.centerY);
          pz = picker.pz + PICKINGOFFSET;
        }
        if (wpoint.dvx != 0 || wpoint.dvy != 0 || wpoint.attacking < 0) {
          vx = faceRight ? wpoint.dvx : (-wpoint.dvx);
          vy = wpoint.dvy;
          vz = picker.getControlZ() * wpoint.dvz;
          setCurr(wpoint.waction + type.throwOffset);
          picker.weapon = dummy;
          picker = null;
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
  protected boolean checkBoundary(LFmap map) {
    if ((currFrame.state != LFstate.ONGROUND) || (px > map.xwidthl && px < map.xwidthr)) {
      pz = (pz > map.zboundB) ? map.zboundB : ((pz < map.zboundT) ? map.zboundT : pz);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void registerItr() {
    currItr.clear();
    if (picker == null) {
      for (LFitr i: currFrame.itr) {
        currItr.add(new LFitrarea(this, i));
      }
    } else {
      for (LFitr i: currFrame.itr) {
        if (i.effect == LFeffect.WPSTREN) {
          if (picker.currFrame.wpoint.attacking > 0)
          currItr.add(new LFitrarea(this, i, strength[picker.currFrame.wpoint.attacking]));
        } else
          currItr.add(new LFitrarea(this, i));
      }
    }
    return;
  }

  @Override
  public void registerBdy() {
    currBdy.clear();
    if (picker == null) {
      for (LFbdy b: currFrame.bdy) {
        currBdy.add(new LFbdyarea(this, b));
      }
    }
    return;
  }


  @Override
  public void statusOverwrite(final LFhero target) {
    super.statusOverwrite(target);
    target.hp2nd = hp;
    return;
  }

  public double[] drink() {
    double[] regen = null;
    switch (identifier) {
      case "Milk":
        mp -= 1.67;
        regen = new double[] { 1.67, 1.6, 0.8 };
        break;
      case "Beer":
        mp -= 6.00;
        regen = new double[] { 6.00, 0.0, 0.0 };
        break;
      default:
        regen = new double[] { 0.00, 0.0, 0.0 };
        System.out.printf("drinking unexpected weapon: %s", identifier);
    }
    if (mp < 0.0) {
      picker.setCurr(Act_999);
      picker.weapon = dummy;
      picker = null;
      hp = 0.0;
      broken();
    }
    return regen;
  }

  @Override
  protected final LFweapon clone() {
    System.out.printf("%s.clone()\n", identifier);
    return (LFweapon)super.clone();
  }

}
