package lfx.object;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import lfx.base.Box;
import lfx.base.Scope;
import lfx.base.VisualNode;
import lfx.component.Bdy;
import lfx.component.Effect;
import lfx.component.Frame;
import lfx.component.Itr;
import lfx.component.Opoint;
import lfx.map.Environment;
import lfx.object.Energy;
import lfx.object.Hero;
import lfx.object.Observable;
import lfx.object.Weapon;
import lfx.util.Area;
import lfx.util.Const;
import lfx.util.Point;
import lfx.util.Tuple;
import lfx.util.Util;

public abstract class AbstractObject implements Observable {
  protected static final Map<String, Hero> heroMapping = new LinkedHashMap<>(32);
  protected static final Map<String, Weapon> weaponMapping = new HashMap<>(16);
  protected static final Map<String, Energy> energyMapping = new HashMap<>(64);

  public final String identifier;
  protected final List<Frame> frameList;  // shared between same objects
  protected final List<Observable> opointObjectList = new ArrayList<>(16);
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
  private final VisualNode visualNode = new VisualNode();
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
  public abstract AbstractObject makeClone(int teamId, boolean faceRight);

  /**
   * Call once after base object is constructed.
   */
  protected abstract void registerObjectMap();

  // TODO: Change to factory method
  @Override
  public void initialize(Environment env, double px, double py, double pz,
                         double hp, double mp, int teamId, int actNumber) {
    this.env = env;
    this.px = px;
    this.py = py;
    this.pz = pz;
    this.hp = hp;
    this.mp = mp;
    this.teamId = teamId;
    transitFrame(actNumber);
    return;
  }

  @Override
  public int getTeamId() {
    return teamId;
  }

  @Override
  public Frame getCurrentFrame() {
    return frame;
  }

  @Override
  public boolean isFirstTimeunit() {
    return transition == frame.wait;
  }

  @Override
  public boolean isRealFirstTimeunit() {
    return newAction && isFirstTimeunit();
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
  protected abstract int getDefaultActNumber();

  /**
   * Returns the frame of target actNumber.
   * Sets existence to false if no expecting frame registered.
   *
   * @param actNumber any valid frame index; can positive or negative
   * @return target Frame
   */
  protected Frame getFrame(int actNumber) {
    actNumber = Math.abs(actNumber);
    if (actNumber == Const.DEF) {
      actNumber = getDefaultActNumber();
    }
    Frame targetFrame = frameList.get(actNumber);
    existence &= targetFrame != Frame.DUMMY;
    return targetFrame;
  }

  protected void transitFrame(int actNumber) {
    faceRight ^= actNumber < 0;
    frame = getFrame(actNumber);
    newAction = actNumber != Const.ACT_REPEAT;
    transition = frame.wait;
    frame.effect.forEach((key, value) -> buff.compute(key, value::stack));
    return;
  }

  protected void hpLost(double injury, boolean sync) {
    hp -= injury;
    return;
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
  public void setVelocity(double vx, double vy, double vz) {
    this.vx = vx;
    this.vy = vy;
    this.vz = vz;
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

  /**
   * Returns this scope from another object.
   * It is mainly used while checking interaction.
   */
  public int getScopeView(int targetTeamId) {
    return Scope.getSideView(baseScope, targetTeamId == teamId);
  }

  protected void itrCallback() {
    System.out.println("NotImplemented");
  }

  protected abstract void addRaceCondition(Observable competitor);

  private Itr checkInteraction(Observable that) {
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

  @Override
  public void receiveItr(Observable source, Itr itr) {
    recvItrList.add(new Tuple<>(source, itr));
    return;
  }

  @Override
  public void spreadItrs(List<Observable> everything) {
    int timestamp = env.getTimestamp();
    if (arest > timestamp) {
      return;
    }
    for (Observable that : everything) {
      if (vrest.getOrDefault(that, 0) > timestamp) {
        continue;
      }
      Itr itr = checkInteraction(that);
      if (itr != null) {
        sendItrList.add(new Tuple<>(that, itr));
        that.receiveItr(this, itr);
        if (itr.kind.raceCondition) {
          that.addRaceConditionObject(this);
        }
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
  public abstract void react();

  protected int applyStatus() {
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
    return Const.TBA;
  }

  // State-related & User-input
  protected abstract int updateAction(int nextAct);
  // Velocity & Position
  protected abstract int updateKinetic(int nextAct);
  // HP & MP
  protected abstract int updateStamina(int nextAct);
  // Get proper action number connected by next-tag.
  protected abstract int getNextActNumber();
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
    double baseX = anchorX + (faceRight ? box.x : (box.w - box.x));
    double baseY = anchorY + box.y;
    return new Area(baseX, baseX + box.w,
                    baseY, baseY + box.h,
                    pz - box.zu, pz + box.zd
    );
  }

  protected void updateBdys() {
    bdyList.clear();
    for (Bdy bdy: frame.bdyList) {
      bdyList.add(new Tuple<>(bdy, makeArea(bdy.box)));
    }
    return;
  }

  protected void updateItrs() {
    itrList.clear();
    for (Itr itr: frame.itrList) {
      itrList.add(new Tuple<>(itr, makeArea(itr.box)));
    }
    return;
  }

  @Override
  public void act() {
    opointObjectList.clear();
    // Opoint is triggered only at the first timeunit.
    if (isFirstTimeunit()) {
      frame.opointList.forEach(opoint -> opointify(opoint));
    }
    // TODO: check taking effect is at frame begining or ending.
    int nextAct = applyStatus();
    nextAct = updateAction(nextAct);
    nextAct = updateKinetic(nextAct);
    nextAct = updateStamina(nextAct);

    if (actPause > 0) {
      --actPause;
    } else if (nextAct != Const.TBA) {
      transitFrame(nextAct);
    } else if (--transition < 0) {
      transitFrame(getNextActNumber());
    }

    if (fitBoundary()) {
      updateAnchor();
      updateBdys();
      updateItrs();
      updateVisualNode();
    } else {
      existence = false;
    }
    return;
  }

  @Override
  public boolean exists() {
    return existence;
  }

  @Override
  public VisualNode getVisualNode() {
    return visualNode;
  }

  @Override
  public void updateVisualNode() {
    visualNode.updateImage(anchorX, anchorY, pz, frame.pic.get(faceRight));
    return;
  }

  protected void opointify(Opoint opoint) {
    Observable origin = energyMapping.get(opoint.oid);
    if (origin == null) {
      origin = weaponMapping.get(opoint.oid);
      if (origin == null) {
        origin = heroMapping.get(opoint.oid);
        if (origin == null) {
          System.err.println("Oid not found: " + opoint.oid);
          return;
        }
      }
    }
    if (!opoint.release) {
      System.out.println("NotImplemented: Holding Opoint");
      return;
    }
    // TODO: slightly different px in multiple shots
    double zStep = (opoint.amount == 1) ? 0.0 : (2.0 * Opoint.Z_RANGE / (opoint.amount - 1));
    double ctrlVz = 2.5 * getInputZ();
    List<Double> basePosition = getBasePosition(opoint);
    for (int i = 0; i < opoint.amount; ++i) {
      boolean facing = opoint.direction.getFacing(faceRight);
      Observable clone = origin.makeClone(teamId, facing);
      clone.setPosition(basePosition, Point.ORIGIN, Point.Z_OFFSET);
      double thisVz = (opoint.amount == 1) ? 0.0 : (i * zStep - Opoint.Z_RANGE);
      clone.setVelocity(facing ? opoint.dvx : -opoint.dvx,
                        opoint.dvy,
                        ctrlVz + thisVz
      );
      opointObjectList.add(clone);
    }
    // TODO: Several weapons should immune to each other (shurikens, arrows)
    return;
  }

  // Implementation is very likely different from LF2.
  protected void opointCreateArmour() {
    List<Double> basePosition = getBasePosition(new Point(0.0, 0.0));
    String[] oid = {"LouisArmour1", "LouisArmour1", "LouisArmour1", "LouisArmour1", "LouisArmour2"};
    double[] rvx = {1.0, 1.0, -1.0, -1.0, Util.randomBounds(-0.4, 0.4)};
    double[] rvz = {1.0, -1.0, 1.0, -1.0, Util.randomBounds(-0.4, 0.4)};
    for (int i = 0; i < 5; ++i) {
      Observable origin = weaponMapping.get(oid[i]);
      if (origin == null) {
        System.err.println(oid[i] + " not found.");
        break;
      }
      Observable clone = origin.makeClone(teamId, Util.randomBool());
      clone.setVelocity((Util.randomBounds(0.0, 9.0) + 6.0) * rvx[i],
                        (Util.randomBounds(0.0, 3.0) - 8.0),
                        (Util.randomBounds(0.0, 4.0) + 3.0) * rvz[i]
      );
      clone.setPosition(basePosition, Point.ORIGIN, Point.Z_OFFSET);
      opointObjectList.add(clone);
    }
    return;
  }

  @Override
  public String toString() {
    return String.format("%s.%d", identifier, this.hashCode());
  }

  public static Set<Map.Entry<String, Hero>> getHeroEntry() {
    return heroMapping.entrySet();
  }

}
