package lfx.object;

import java.util.ArrayList;
import java.util.List;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import lfx.component.Bdy;
import lfx.component.Effect;
import lfx.component.Frame;
import lfx.component.Grasp;
import lfx.component.Itr;
import lfx.util.Area;
import lfx.util.Global;
import lfx.util.Point;
import lfx.util.Tuple;
import lfx.util.Type;

// http://gjp4860sev.myweb.hinet.net/lf2/page10.htm
// https://lf-empire.de/lf2-empire/data-changing/frame-elements/177-cpoint-catch-point?showall=1

abstract class AbstractObject {
  public static final AbstractObject dummy = new AbstractObject() {};
  public static final int ACT_DEF = 999;
  public static final int ACT_TBA = 1236987450;  // arbitrary
  public static final int GRASP_TIME = 305;  // test-value
  public static final int GRASP_FLAG_WAITING = -1;
  public static final int GRASP_FLAG_UPDATED = -2;
  public static final int GRASP_FLAG_FREE = -3;
  public static final int GRASP_FLAG_DROP = -4;
  public static final int GRASP_FLAG_THROW = -5;
  public static final double GRASP_DROP_DVX = +8.0;
  public static final double GRASP_DROP_DVY = -2.5;
  private static int teamIdCounter = Math.max(Global.MAX_TEAMS, 16);  // count for `independent`
  private static final Set<AbstractObject> competitionGuard = new HashSet<>();

  public final Type type;
  public final String identifier;
  public final List<Frame> frameList;
  protected final List<Tuple<AbstractObject, Itr>> resultItrList = new ArrayList<>();
  protected final Map<Effect.Kind, Effect> status = new EnumMap<>();
  protected final Map<AbstractObject, Integer> vrest = new WeakHashMap<>();
  protected int arest = 0;
  protected Frame currFrame = null;
  protected boolean faceRight = true;
  protected double px = 0.0;
  protected double py = 0.0;
  protected double pz = 0.0;
  protected double vx = 0.0;
  protected double vy = 0.0;
  protected double vz = 0.0;
  protected double hp = 500.0;
  protected double mp = 200.0;
  protected double hp2nd = 500.0;
  protected double hpMax = 500.0;
  protected double mpMax = 500.0;
  protected int actLag = 0;
  protected int teamId = 0;
  protected int graspField = 0;
  protected AbstractObject grasper = dummy;
  protected AbstractObject graspee = dummy;
  protected AbstractObject wpunion = dummy;  // weapon or picker
  private int transition = 0;
  private double anchorX = 0.0;  // picture right(left) x-coordinate
  private double anchorY = 0.0;  // picture top y-coordinate

  protected static class CanonicalAct {
    public final boolean changeFacing;
    public final int index;

    public CanonicalAct(int action, int defaultIndex) {
      if (action < 0) {
        action = -action;
        changeFacing = true;
      } else {
        changeFacing = false;
      }
      index = action == ACT_DEF ? defaultIndex : action;
    }

  }

  protected AbstractObject(Type type, String identifier, List<Frame> frameList) {
    this.type = type;
    this.identifier = identifier;
    this.frameList = List.copyOf(frameList);
  }

  protected AbstractObject(AbstractObject baseObject) {
    /** Copy Constructor */
  }

  public static final int requestIndependentTeamId() {
    return ++teamIdCounter;
  }

  public static final void periodicalTask() {
    competitionGuard.clear();
    return;
  }

  /** Only the first consumer can get the resource (hero-grasp and weapon-pick). */
  public static final boolean compete(AbstractObject resource) {
    synchronized (competitionGuard) {
      return competitionGuard.add(resource);
    }
  }

  public final void updateAnchor() {
    anchorX = faceRight ? (px - currFrame.centerx) : (px + currFrame.centerx);
    anchorY = py - currFrame.centery;
    return;
  }

  public final double[] getAnchor() {
    return new double[] {anchorX, anchorY, pz};
  }

  /** Get based position of Xpoint. */
  public final double[] getPointPosition(Point point) {
    double[] position = getAnchor();
    position[0] += faceRight ? point.x : -point.x;
    position[1] += point.y;
    return position;
  }

  /** Set position of Xpoint relative to another position. */
  public final void setPointPosition(double[] basePosition, Point point) {
    anchorX = basePosition[0] - faceRight ? point.x : -point.x;
    anchorY = basePosition[1] - point.y;
    px = anchorX + faceRight ? currFrame.centerx : -currFrame.centerx;
    py = anchorY + currFrame.centery;
    pz = basePosition[2];
    return;
  }

  public final void transitFrame(CanonicalAct act) {
    faceRight ^= act.changeFacing;
    newFrame = frame.get(act.index);
    currFrame.effect.forEach((kind, effect) -> status.compute(kind, effect::stack));
    return;
  }

  /** Called when F7 is pressed. */
  public abstract void revive();

  /** Returns canonical action number.
      For example, hero's 999 can be ACT_STANDING or ACT_JUMPAIR based on py. */
  public abstract CanonicalAct getCanonicalAct(int action);

  public abstract List<Tuple<Bdy, Area>> registerBdyArea();
  public abstract List<Tuple<Itr, Area>> registerItrArea();

  public final void spreadItrArea(Map<AbstractObject, List<Tuple<Bdy, Area>>> map, int mapTime) {
    if (arest > mapTime)
      return;
    final List<Tuple<Itr, Area>> itrArea = registerItrArea();
    map.forEach((that, bdyAreaList) -> {
      if (vrest.getOrDefault(that, 0) > mapTime)
        return;
      final int scopeView = teamId == that.teamId ? that.type.teamView() : that.type.enemyView();
      for (Tuple<Bdy, Area> bdyArea: bdyAreaList) {
        final Bdy bdy = bdyArea.first;
        final Area area = bdyArea.second;
        for (Tuple<Itr, Area> itrArea: itrArea) {
          if (!area.collidesWith(itrArea.second))
            continue;
          final Itr itr = itrArea.first;
          if (!bdy.interactsWith(itr, scopeView))
            continue;
          if (itr.Effect == Effect.GRASP_BDY || itr.Effect == Effect.GRASP_DOP) {
            if (!compete(that))
              continue;
            that.grasper = this.grasper = this;
            that.graspee = this.graspee = that;
          } else if (itr.Effect == Effect.PICK || itr.Effect == Effect.ROLL_PICK) {
            if (!compete(that))
              continue;
            that.wpunion = this;
            this.wpunion = that;
          }
          this.resultItrList.add(new Tuple<>(that, itr));
          that.resultItrList.add(new Tuple<>(this, itr));
          return;
        }
      }
    });
    return;
  }

  protected final int updateGrasp() {
    if (grasper.currFrame.state != State.GRASP || grasper.currFrame.cpoint == null ||
        graspee.currFrame.state != State.GRASP || graspee.currFrame.cpoint == null) {
      return ACT_DEF;
    }
    if (grasper == this) {
      if (graspee.grasper != this) {
        // re-grasped by other
        grasper = graspee = dummy;
        return ACT_DEF;
      }
      return updateGrasper();
    } else {
      return updateGraspee();
    }
  }

  private int updateGrasper() {
    int graspeeFlag = GRASP_FLAG_WAITING;
    graspField -= Math.abs(cpoint.decrease);
    if (currFrame.cpoint == null) {
      // does a combo and goes to a frame without cpoint
      graspeeFlag = GRASP_FLAG_FREE;
    } else if (graspField < 0 && currFrame.cpoint.decrease < 0) {
      // will not drop graspee in cpoint with positive decrease even if timeup
      graspeeFlag = GRASP_FLAG_DROP;
    } else if (transition == currFrame.wait) {
      // these functions only take effect once
        if (currFrame.cpoint.injury > 0) {
          actLag = Math.max(actLag, Itr.LAG);
        }
        if (cpoint.throwing) {
          graspeeFlag = GRASP_FLAG_THROW;
        }
        if (cpoint.transform) {
          graspeeFlag = GRASP_FLAG_THROW;
          status.put(Extension.Kind.TRANSFORM_TO, new Extension(1, graspee.identifier));
        }
      }
    }
    graspee.setPointPosition(getPointPosition(currFrame.cpoint), graspee.currFrame.cpoint);
    synchronized (graspee) {
      graspee.graspField = graspeeFlag;
      graspee.notify();
    }
    return;
  }

  protected synchronized int updateGraspee() {
    while (graspField == GRASP_FLAG_WAITING) {
      try {
        this.wait(1000);
      } catch (InterruptedException expected) {
      }
    }
    if (graspField == GRASP_FLAG_FREE)
      return ACT_JUMPAIR;
    if (graspField == GRASP_FLAG_DROP)
      return ACT_FORWARD_FALL2;
    final Cpoint cpoint = grasper.currFrame.cpoint;
    if (graspField == GRASP_FLAG_THROW) {
      vx = grasper.faceRight ? cpoint.throwvx : -cpoint.throwvx;
      vy = cpoint.throwvy;
      vz = catcher.getControlZ() * cpoint.throwvz;
      status.put(Extension.Kind.THROWINJURY, new Extension(-1, cpoint.throwinjury));
      return cpoint.vaction;
    }
    if (cpoint.injury > 0) {
      hpLost(cpoint.injury, false);
      actLag = Math.max(actLag, Itr.LAG);
    } else {
      hpLost(-cpoint.injury, false);
    }
    faceRight = grasper.faceRight ^ cpoint.changedir;
    px = grasper.faceRight ?
         (grasper.anchorX + cpoint.x) + (currFrame.cpoint.x - currFrame.centerx):
         (grasper.anchorX - cpoint.x) - (currFrame.cpoint.x - currFrame.centerx);
    py = (grasper.anchorY + cpoint.y) - (currFrame.cpoint.y - currFrame.centery);
    pz = grasper.pz;
    graspField = GRASP_FLAG_WAITING;
    return actLag == 0 ? cpoint.vaction : ACT_TBA;
  }

  /** These methods are called every mapTime. */
  protected abstract boolean react();
  protected abstract boolean move2();
  protected abstract boolean updateMovement();
  protected abstract boolean updateStatus();
  protected abstract boolean updateFrame();

  public final List<AbstractObject> move(int nextAct) {
    List<AbstractObject> spawnList = new ArrayList<>();
    // Opoint is triggered only at the first timeunit.
    if (transition != currFrame.wait || currFrame.opoint.isEmpty()) {
      for (Opoint opoint: currFrame.opoint) {
        spawnList.addAll(opoint.launch(this, getControlZ() * OPOINT_DVZ));
      }
    }
    return move2() ? spawnList : List.of();
  }

  public final void applyStatus() {
    Iterator<Map.Entry<Extension, Extension.Value>> iterator = status.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<Extension, Extension.Value> entry = iterator.next();
      Extension.Value value = entry.getValue();
      switch (entry.getKey()) {
        case HEALING:
          // TODO: visual effect
          hp = Math.min(hp + value.doubleValue, hp2nd);
          break;
        case TELEPORT_ENEMY: {
          AbstractObject target = objectList.stream()
                                            .filter(object -> object.type == Type.HERO)
                                            .mapToDouble(object -> Math.abs(this.px - object.px))
                                            .min(Math::min).orElse(this);
          if (target != this) {
            px = target.px + faceRight ? -value.doubleValue : value.doubleValue;
            pz = target.pz;
          }
          py = 0.0;
          break;
        }
        case TELEPORT_TEAM: {
          AbstractObject target = objectList.stream()
                                            .filter(object -> object.type == Type.HERO)
                                            .mapToDouble(object -> Math.abs(this.px - object.px))
                                            .max(Math::max).orElse(this);
          if (target != this) {
            px = target.px + faceRight ? -value.doubleValue : value.doubleValue;
            pz = target.pz;
          }
          py = 0.0;
          break;
        }
        case ARMOUR:
          map.spawnObject(LFopoint.createArmour(this));
          break;
        case TRANSFORM_TO:
        case TRANSFORM_BACK:
          break;
      }
      if (value.lapse())
        iterator.remove();
    }
    return;
  }

  protected abstract boolean adjustBoundary(int[] xzBound);

  @Override
  public String toString() {
    return String.format("%s@%d", identifier, this.hashCode());
  }

  /** Hero's action numbers */
  public static final int ACT_TRANSFORM_INVALID = ACT_DEF;
  public static final int ACT_TRANSFORM_BACK = 245;  // default
  public static final int ACT_STANDING = 0;
  public static final int ACT_WALKING = 5;
  public static final int ACT_RUNNING = 9;
  public static final int ACT_HEAVY_WALK = 12;
  public static final int ACT_HEAVY_RUN = 16;
  public static final int ACT_HEAVY_STOP_RUN = 19;
  public static final int ACT_WEAPON_ATK1 = 20;
  public static final int ACT_WEAPON_ATK2 = 25;
  public static final int ACT_JUMP_WEAPON_ATK = 30;
  public static final int ACT_RUN_WEAPON_ATK = 35;
  public static final int ACT_DASH_WEAPON_ATK = 40;
  public static final int ACT_LIGHT_WEAPON_THROW = 45;
  public static final int ACT_HEAVY_WEAPON_THROW = 50;
  public static final int ACT_SKY_WEAPON_THROW = 52;
  public static final int ACT_DRINK = 55;
  public static final int ACT_PUNCH1 = 60;
  public static final int ACT_PUNCH2 = 65;
  public static final int ACT_SUPER_PUNCH = 70;
  public static final int ACT_JUMP_ATK = 80;
  public static final int ACT_RUN_ATK = 85;
  public static final int ACT_DASH_ATK = 90;
  public static final int ACT_DASH_DEF = 95;
  public static final int ACT_ROLLING = 102;
  public static final int ACT_ROWING1 = 100;
  public static final int ACT_ROWING2 = 108;
  public static final int ACT_DEFEND = 110;
  public static final int ACT_DEFEND_HIT = 111;
  public static final int ACT_BROKEN_DEF = 112;
  public static final int ACT_PICK_LIGHT = 115;
  public static final int ACT_PICK_HEAVY = 116;
  public static final int ACT_CATCH = 120;
  public static final int ACT_CAUGHT = 130;
  public static final int ACT_FORWARD_FALL1 = 180;
  public static final int ACT_FORWARD_FALL2 = 181;
  public static final int ACT_FORWARD_FALL3 = 182;
  public static final int ACT_FORWARD_FALL4 = 183;
  public static final int ACT_FORWARD_FALL5 = 184;
  public static final int ACT_FORWARD_FALLR = 185;
  public static final int ACT_BACKWARD_FALL1 = 186;
  public static final int ACT_BACKWARD_FALL2 = 187;
  public static final int ACT_BACKWARD_FALL3 = 188;
  public static final int ACT_BACKWARD_FALL4 = 189;
  public static final int ACT_BACKWARD_FALL5 = 190;
  public static final int ACT_BACKWARD_FALLR = 191;
  public static final int ACT_ICE = 200;
  public static final int ACT_UPWARD_FIRE = 203;
  public static final int ACT_DOWNWARD_FIRE = 205;
  public static final int ACT_TIRED = 207;
  public static final int ACT_JUMP = 210;
  public static final int ACT_JUMPAIR = 212;
  public static final int ACT_DASH1 = 213;
  public static final int ACT_DASH2 = 214;
  public static final int ACT_CROUCH1 = 215;
  public static final int ACT_STOPRUN = 218;
  public static final int ACT_CROUCH2 = 219;
  public static final int ACT_INJURE1 = 220;
  public static final int ACT_FRONTHURT = 221;
  public static final int ACT_INJURE2 = 222;
  public static final int ACT_BACKHURT = 223;
  public static final int ACT_INJURE3 = 224;
  public static final int ACT_DOP = 226;
  public static final int ACT_LYING1 = 230;
  public static final int ACT_LYING2 = 231;
  public static final int ACT_THROW_LYING_MAN = 232;
  public static final int ACT_DUMMY = 399;

  /** Blast's action numbers */
  public static final int ACT_FLYING = 0;
  public static final int ACT_HITTING = 10;
  public static final int ACT_HIT = 20;
  public static final int ACT_REBOUND = 30;
  public static final int ACT_DISAPPEAR = 40;

  /** Weapon's action map */

  public int landingAct() {
    throw IllegalCallerException(String.format("Unexpected type %s.", type));
  }

  public int bouncingAct() {
    throw IllegalCallerException(String.format("Unexpected type %s.", type));
  }

  public int hittingAct() {
    throw IllegalCallerException(String.format("Unexpected type %s.", type));
  }

  public int hitAct(int fall) {
    throw IllegalCallerException(String.format("Unexpected type %s.", type));
  }

}
