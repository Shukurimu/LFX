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

/** Reference webpages:
    https://github.com/Project-F/F.LF/tree/master/LF
    https://www.lf-empire.de/forum/showthread.php?tid=10733
    https://lf-empire.de/lf2-empire/data-changing/types/167-effect-0-characters
    https://lf-empire.de/lf2-empire/data-changing/reference-pages/182-states?showall=1&limitstart=
    http://lf2.wikia.com/wiki/Health_and_mana
    http://gjp4860sev.myweb.hinet.net/lf2/page10.htm */

abstract class AbstractObject {
  public static final int ACT_DEF = 999;
  public static final int ACT_TBA = 1236987450;  // arbitrary

  private static int teamIdCounter = 32;  // count for `independent`

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
  protected int lagCountdown = 0;
  protected int teamId = 0;
  protected Grasp grasp = null;
  protected AbstractObject weapon = null;
  protected AbstractObject picker = null;
  private double transition = 0.0;
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

  public final Pair<Double, Double> getAnchor() {
    return new Pair<>(anchorX, anchorY);
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

  public final boolean assertRaceCondition(AbstractObject demander, AbstractObject supplier) {
    return true;
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
