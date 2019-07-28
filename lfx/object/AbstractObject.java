package lfx.object;

import java.util.ArrayList;
import java.util.List;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import lfx.component.Bdy;
import lfx.component.Effect;
import lfx.component.Frame;
import lfx.component.Grasp;
import lfx.component.Itr;
import lfx.util.Area;
import lfx.util.Pair;
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
  private static int teamIdCounter = 16;  // count for `independent`

  public final Type type;
  public final String identifier;
  public final List<Frame> frame;
  protected final List<Pair<AbstractObject, Itr>> resultItrList = new ArrayList<>();
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
  protected double hpMax = 500.0;
  protected double mpMax = 500.0;
  protected int actLag = 0;
  protected int teamId = 0;
  protected int graspField = 0;
  protected AbstractObject grasper = dummy;
  protected AbstractObject graspee = dummy;
  protected AbstractObject picker = dummy;
  protected AbstractObject weapon = dummy;
  private int transition = 0;
  private double anchorX = 0.0;
  private double anchorY = 0.0;

  private AbstractObject() {
    this.type = Type.OTHER;
    this.identifier = null;
    this.frame = List.of();
  }

  public static int getIndependentTeamId() {
    return ++teamIdCounter;
  }

  public final void updateAnchor() {
    anchorX = faceRight ? (px - currFrame.centerx) : (px + currFrame.centerx);
    anchorY = py - currFrame.centery;
    return;
  }

  public final void transitFrame(int actNumber) {
    faceRight ^= (actNumber < 0);
    newFrame = frame.get(Math.abs(actNumber));
    currFrame.effect.forEach((kind, effect) -> status.compute(kind, effect::stack));
    return;
  }

  public final void initialize(double px, double py, double pz, int actNumber) {
    this.px = px;
    this.py = py;
    this.pz = pz;
    transitFrame(actNumber);
    return;
  }

  /** Called when F7 is pressed. */
  public abstract void revive();

  public abstract Pair<Integer, Boolean> resolveAct(int index);
  public abstract List<Pair<Bdy, Area>> registerBdyArea();
  public abstract List<Pair<Itr, Area>> registerItrArea();

  public final void processItrArea(Map<AbstractObject, List<Pair<Bdy, Area>>> map, int mapTime) {
    if (arest > mapTime)
      return;
    final List<Pair<Itr, Area>> itrArea = registerItrArea();
    map.forEach((that, bdyAreaList) -> {
      if (vrest.getOrDefault(that, 0) > mapTime)
        return;
      final int scopeView = type.getScopeView(teamId == that.teamId);
      for (Pair<Bdy, Area> bdyArea: bdyAreaList) {
        final Bdy bdy = bdyArea.first;
        final Area area = bdyArea.second;
        for (Pair<Itr, Area> itrArea: itrArea) {
          if (!area.collidesWith(itrArea.second))
            continue;
          final Itr itr = itrArea.first;
          if (!bdy.interactsWith(itr, scopeView))
            continue;
          this.resultItrList.add(new Pair<>(that, itr));
          that.resultItrList.add(new Pair<>(this, itr));
          return;
        }
      }
    });
    return;
  }

  public final boolean assertRaceCondition(AbstractObject consumer, AbstractObject producer) {
    return true;
  }

  public int updateGrasp() {
    if (graspee.grasper != this) {
      graspee = dummy;
      return;
    }
    return updateGrasper();
    return updateGraspee();
  }

  protected void updateGrasper() {
    int graspeeFlag = GRASP_FLAG_WAITING;
    graspField -= Math.abs(cpoint.decrease);
    if (currFrame.cpoint == null) {
      // grasper does a combo whose frame has no cpoint
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

  /* these methods are invoked every TimeUnit */
  protected abstract boolean reactAndMove(LFmap map);
  protected abstract boolean adjustBoundary(int[] xzBound);

  /* called by LFmap, return false if this object is no longer used in current map */
  public final void updateStatus(LFmap map) {
    objectExist = reactAndMove(map) && checkBoundary(map);
    return;
  }

  @Override
  public String toString() {
    return String.format("%s@%d", identifier, this.hashCode());
  }

}
