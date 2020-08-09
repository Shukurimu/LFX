package lfx.object;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import lfx.base.Action;
import lfx.base.Box;
import lfx.base.Scope;
import lfx.base.Viewer;
import lfx.component.Bdy;
import lfx.component.Effect;
import lfx.component.Frame;
import lfx.component.Itr;
import lfx.component.Opoint;
import lfx.map.Environment;
import lfx.object.Library;
import lfx.object.Observable;
import lfx.util.Area;
import lfx.util.Point;
import lfx.util.Tuple;
import lfx.util.Util;

public abstract class AbstractObject implements Observable {
  public final String identifier;
  protected final List<Frame> frameList;  // shared between same objects
  protected final List<Observable> spawnedObjectList = new ArrayList<>(16);
  protected final List<Tuple<Observable, Itr>> sendItrList = new ArrayList<>(16);
  protected final List<Tuple<Observable, Itr>> recvItrList = new ArrayList<>(16);
  protected final List<Tuple<Bdy, Area>> bdyList = new ArrayList<>(8);
  protected final List<Tuple<Itr, Area>> itrList = new ArrayList<>(8);
  protected final Map<Effect, Effect.Value> buff = new EnumMap<>(Effect.class);
  protected final Map<Observable, Integer> vrest = new WeakHashMap<>(128);
  protected int arest = 0;
  protected int actPause = 0;
  protected int teamId = 0;
  protected int transition = 0;
  protected Environment env = null;
  protected Frame frame = Frame.DUMMY;
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
  protected double hp2nd = 500.0;
  protected double mpMax = 500.0;
  private final Viewer viewer = new Viewer();
  private final int baseScope;
  private double anchorX = 0.0;  // picture x-coordinate (left if faceRight else right)
  private double anchorY = 0.0;  // picture y-coordinate (top)
  private boolean newAction = true;
  private boolean existence = true;

  protected AbstractObject(String identifier, List<Frame> frameList, int baseScope) {
    this.identifier = identifier;
    this.frameList = frameList;
    this.baseScope = baseScope;
  }

  protected AbstractObject(AbstractObject base) {
    identifier = base.identifier;
    frameList = base.frameList;
    baseScope = base.baseScope;
  }

  @Override
  public abstract AbstractObject makeClone();

  /**
   * Call once after base object is constructed.
   */
  protected abstract void registerLibrary();

  @Override
  public String getIdentifier() {
    return identifier;
  }

  @Override
  public int getTeamId() {
    return teamId;
  }

  @Override
  public double getPosX() {
    return px;
  }

  @Override
  public boolean getFacing() {
    return faceRight;
  }

  @Override
  public boolean isFirstTimeunit() {
    return transition == frame.wait;
  }

  @Override
  public boolean isActionFirstTimeunit() {
    return newAction && isFirstTimeunit();
  }

  @Override
  public Frame getCurrentFrame() {
    return frame;
  }

  @Override
  public double getInputZ() {
    return 0.0;
  }

  @Override
  public void revive() {
    hp = hp2nd = hpMax;
    mp = mpMax;
    return;
  }

  /**
   * For the cases of next:999 in LF2.
   */
  protected abstract Action getDefaultAct();

  protected void transitFrame(Action action) {
    if (action == Action.REMOVAL) {
      existence = false;
      return;
    }
    if (action == Action.REPEAT) {
      newAction = false;
    } else {
      newAction = true;
      if (action == Action.DEFAULT) {
        action = getDefaultAct();
      }
      frame = frameList.get(action.index);
      faceRight ^= action.changeFacing;
    }
    transition = frame.wait;
    frame.effect.forEach((key, value) -> buff.compute(key, value::stack));
    return;
  }

  protected void transitNextFrame() {
    transitFrame(frame.next);
    return;
  }

  protected void hpLost(double injury, boolean sync) {
    hp -= injury;
    return;
  }

  @Override
  public List<Double> getBasePosition() {
    return List.of(px, py, pz);
  }

  @Override
  public List<Double> getBasePosition(Point point) {
    return List.of(anchorX + (faceRight ? point.x : -point.x),
                   anchorY + point.y,
                   pz
    );
  }

  @Override
  public void setPosition(List<Double> basePosition, Point point, double zOffect) {
    anchorX = basePosition.get(0) - (faceRight ? point.x : -point.x);
    anchorY = basePosition.get(1) - point.y;
    px = anchorX + (faceRight ? frame.centerx : -frame.centerx);
    py = anchorY + frame.centery;
    pz = basePosition.get(2) + zOffect;
    return;
  }

  @Override
  public void setPosition(double px, double py, double pz) {
    this.px = px;
    this.py = py;
    this.pz = pz;
    return;
  }

  @Override
  public void setVelocity(double vx, double vy, double vz) {
    this.vx = vx;
    this.vy = vy;
    this.vz = vz;
    return;
  }

  @Override
  public void setProperty(Environment env, int teamId, boolean faceRight) {
    this.env = env;
    this.teamId = teamId;
    this.faceRight = faceRight;
    return;
  }

  @Override
  public List<Tuple<Bdy, Area>> getBdys() {
    return bdyList;
  }

  @Override
  public List<Tuple<Itr, Area>> getItrs() {
    return itrList;
  }

  @Override
  public int getScopeView(int targetTeamId) {
    return Scope.getSideView(baseScope, targetTeamId >= 0 && targetTeamId == teamId);
  }

  private Itr getSuccessfulItr(Observable that) {
    int scopeView = that.getScopeView(teamId);
    for (Tuple<Bdy, Area> bdyArea : that.getBdys()) {
      Bdy bdy = bdyArea.first;
      Area area = bdyArea.second;
      for (Tuple<Itr, Area> itrArea : itrList) {
        if (!area.collidesWith(itrArea.second)) {
          continue;
        }
        Itr itr = itrArea.first;
        if (!bdy.interactsWith(itr, scopeView)) {
          continue;
        }
        return itr;
      }
    }
    return null;
  }

  /**
   * Register object attemping to require this object.
   * For instance, Hero picks weapon or grabbing Hero.
   * The competitor should be read-only.
   */
  protected abstract void addRaceCondition(Observable competitor);

  @Override
  public void spreadItrs(Iterable<Observable> everything) {
    int timestamp = env.getTimestamp();
    if (arest > timestamp) {
      return;
    }
    for (Observable that : everything) {
      if (vrest.getOrDefault(that, 0) > timestamp) {
        continue;
      }
      Itr itr = getSuccessfulItr(that);
      if (itr != null) {
        sendItrList.add(new Tuple<>(that, itr));
        that.receiveItr(this, itr);
        // if (itr.kind.raceCondition) {  TODO: public interface
        //   that.addRaceCondition(this);
        // }
        if (itr.vrest > 0) {
          vrest.put(that, timestamp + itr.vrest);
        } else {
          arest = timestamp - itr.vrest;
        }
        return;
      }
    }
    return;
  }

  @Override
  public void receiveItr(Observable source, Itr itr) {
    recvItrList.add(new Tuple<>(source, itr));
    return;
  }

  @Override
  public abstract void react();

  protected Action applyStatus() {
    Iterator<Map.Entry<Effect, Effect.Value>> iterator = buff.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<Effect, Effect.Value> entry = iterator.next();
      Effect.Value value = entry.getValue();
      switch (entry.getKey()) {
        case HEALING:  // TODO: visual effect
          hp = Math.min(hp + value.doubleValue, hp2nd);
          break;
        case TELEPORT_ENEMY:
        case TELEPORT_TEAM:
          System.out.println("Teleport");
          break;
        case TRANSFORM_INTO:
        case TRANSFORM_BACK:
          System.out.println("Transform");
          break;
        default:
          System.out.println("Other");
      }
      if (value.elapse()) {
        iterator.remove();
      }
    }
    return Action.UNASSIGNED;
  }

  // State-related & User-input
  protected abstract Action updateAction(Action nextAct);
  // Velocity & Position
  protected abstract Action updateKinetic(Action nextAct);
  // HP & MP
  protected abstract Action updateStamina(Action nextAct);
  /**
   * If the object still alive, fit the boundary to the env and returns true.
   *
   * @return false if the object should be deleted
   */
  protected abstract boolean fitBoundary();

  protected void updateAnchor() {
    anchorX = faceRight ? (px - frame.centerx) : (px + frame.centerx);
    anchorY = py - frame.centery;
    return;
  }

  protected Area makeArea(Box box) {
    double startPosX = anchorX + (faceRight ? box.x : (box.w - box.x));
    double startPosY = anchorY + box.y;
    return new Area(startPosX, startPosX + box.w,
                    startPosY, startPosY + box.h,
                    pz - box.zu, pz + box.zd
    );
  }

  protected List<Tuple<Bdy, Area>> getCurrentBdys() {
    List<Tuple<Bdy, Area>> result = new ArrayList<>(4);
    for (Bdy bdy : frame.bdyList) {
      result.add(new Tuple<>(bdy, makeArea(bdy.box)));
    }
    return result;
  }

  protected List<Tuple<Itr, Area>> getCurrentItrs() {
    List<Tuple<Itr, Area>> result = new ArrayList<>(4);
    for (Itr itr : frame.itrList) {
      result.add(new Tuple<>(itr, makeArea(itr.box)));
    }
    return result;
  }

  public void updateViewer() {
    viewer.update(faceRight, anchorX, anchorY, pz, frame.pic);
    return;
  }

  @Override
  public void act() {
    spawnedObjectList.clear();
    // Opoint is triggered only at the first timeunit.
    if (isFirstTimeunit()) {
      frame.opointList.forEach(opoint -> opointify(opoint));
    }
    // TODO: check taking effect is at frame begining or ending.
    Action nextAct = applyStatus();
    nextAct = updateAction(nextAct);
    nextAct = updateKinetic(nextAct);
    nextAct = updateStamina(nextAct);

    if (actPause > 0) {
      --actPause;
    } else if (nextAct != Action.UNASSIGNED) {
      transitFrame(nextAct);
    } else if (--transition < 0) {
      transitNextFrame();
    }

    if (fitBoundary()) {
      updateAnchor();
      bdyList.clear();
      bdyList.addAll(getCurrentBdys());
      itrList.clear();
      itrList.addAll(getCurrentItrs());
      updateViewer();
    } else {
      existence = false;
    }
    return;
  }

  @Override
  public List<Observable> getSpawnedObjectList() {
    return spawnedObjectList;
  }

  @Override
  public boolean exists() {
    return existence;
  }

  @Override
  public Viewer getViewer() {
    return viewer;
  }

  @Override
  public double[] getStamina() {
    return {hp2nd / hpMax, hp / hpMax, mp / mpMax};
  }

  protected void opointify(Opoint opoint) {
    if (!opoint.release) {
      System.out.println("NotImplemented: Holding Opoint");
      return;
    }
    // TODO: slightly different px in multiple shots
    boolean singleShot = opoint.amount == 1;
    double zStep = singleShot ? 0.0 : (2.0 * Opoint.Z_RANGE / (opoint.amount - 1));
    double orderVz = Opoint.Z_RANGE * getInputZ();
    List<Double> basePosition = getBasePosition(opoint);
    List<Observable> cloneList = Library.instance().getCloneList(opoint.oid, opoint.amount);
    int index = 0;
    for (Observable clone : cloneList) {
      double cloneVz = singleShot ? 0.0 : (index * zStep - Opoint.Z_RANGE);
      boolean facing = opoint.direction.getFacing(faceRight);
      clone.setProperty(env, teamId, facing);
      clone.setVelocity(facing ? opoint.dvx : -opoint.dvx,
                        opoint.dvy,
                        orderVz + cloneVz
      );
      clone.setPosition(basePosition, Point.ORIGIN, Point.Z_OFFSET);
      ++index;
    }
    spawnedObjectList.addAll(cloneList);
    // TODO: Several weapons should immune to each other (shurikens, arrows)
    return;
  }

  protected void createArmours() {
    List<Double> basePosition = getBasePosition();
    double[] rvx = {1.0, 1.0, -1.0, -1.0, Util.randomBounds(-0.4, 0.4)};
    double[] rvz = {1.0, -1.0, 1.0, -1.0, Util.randomBounds(-0.4, 0.4)};
    List<Observable> cloneList = Library.instance().getArmourSetList();
    int index = 0;
    for (Observable clone : cloneList) {
      clone.setProperty(env, teamId, Util.randomBool());
      clone.setVelocity((Util.randomBounds(0.0, 9.0) + 6.0) * rvx[index],
                        (Util.randomBounds(0.0, 3.0) - 8.0),
                        (Util.randomBounds(0.0, 4.0) + 3.0) * rvz[index]
      );
      clone.setPosition(basePosition, Point.ORIGIN, Point.Z_OFFSET);
      ++index;
    }
    spawnedObjectList.addAll(cloneList);
    return;
  }

  @Override
  public String toString() {
    return String.format("%s.%d", identifier, this.hashCode());
  }

}
