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
import javafx.scene.image.ImageView;
import javafx.scene.Node;
import lfx.base.Box;
import lfx.base.Scope;
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
  protected final List<Tuple<Observable, Itr>> sendItrList = new ArrayList<>(16);
  protected final List<Tuple<Observable, Itr>> recvItrList = new ArrayList<>(16);
  protected final List<Tuple<Bdy, Area>> bdyList = new ArrayList<>(8);
  protected final List<Tuple<Itr, Area>> itrList = new ArrayList<>(8);
  protected final Map<Effect, Effect.Value> buff = new EnumMap<>(Effect.class);
  protected final Map<Observable, Integer> vrest = new WeakHashMap<>(128);
  protected final int scope;
  protected int arest = 0;
  protected int actLag = 0;
  protected int teamId = 0;
  protected Environment env = null;
  protected Frame frame = null;
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
  protected int transition = 0;
  private double anchorX = 0.0;  // picture x-coordinate (left if faceRight else right)
  private double anchorY = 0.0;  // picture y-coordinate (top)
  private boolean existence = true;
  protected final ImageView visualNode = new ImageView();

  protected AbstractObject(String identifier, List<Frame> frameList, int scope) {
    this.identifier = identifier;
    this.frameList = frameList;
    this.scope = scope;
  }

  protected AbstractObject(AbstractObject base) {
    identifier = base.identifier;
    frameList = base.frameList;
    scope = base.scope;
    hp = base.hp;
    mp = base.mp;
    hpMax = base.hpMax;
    mpMax = base.mpMax;
    hp2nd = base.hp2nd;
  }

  @Override
  public abstract AbstractObject makeClone(int teamId, boolean faceRight);

  /** Call once after base object is constructed. */
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
  public double getInputZ() {
    return 0.0;
  }

  @Override
  public void revive() {
    hp = hp2nd = hpMax;
    mp = mpMax;
    return;
  }

  /** For the cases of next:999 in LF2. */
  protected abstract int getDefaultActNumber();

  protected Frame getFrame(int actNumber) {
    if (actNumber == frame.curr) {
      return frame;
    }
    actNumber = Math.abs(actNumber);
    if (actNumber == ACT_DEF) {
      actNumber = getDefaultActNumber();
    }
    try {
      return frameList.get(actNumber);
    } catch (Exception ex) {
      existence = false;
      return null;
    }
  }

  protected void hpLost(double injury, boolean sync) {
    hp -= injury;
    return;
  }

  protected void transitFrame(int actNumber) {
    faceRight ^= actNumber < 0;
    frame = getFrame(actNumber);
    frame.effect.forEach((key, value) -> buff.compute(key, value::stack));
    return;
  }

  protected void updateAnchor() {
    anchorX = faceRight ? (px - frame.centerx) : (px + frame.centerx);
    anchorY = py - frame.centery;
    return;
  }

  @Override
  public double[] getBasePosition(Point point) {
    return new double[] {
      anchorX + (faceRight ? point.x : -point.x),
      anchorY + point.y,
      pz,
    };
  }

  @Override
  public void setPosition(double[] basePosition, Point point, double zOffect) {
    anchorX = basePosition[0] - (faceRight ? point.x : -point.x);
    anchorY = basePosition[1] - point.y;
    px = anchorX + (faceRight ? frame.centerx : -frame.centerx);
    py = anchorY + frame.centery;
    pz = basePosition[2] + zOffect;
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

  public int getScopeView(int targetTeamId) {
    return Scope.getSideView(scope, targetTeamId == teamId);
  }

  protected void itrCallback() {
    System.out.println("NotImplemented");
  }

  @Override
  public void interact(Observable source, Observable target, Itr itr) {
    if (source == this) {
      sendItrList.add(new Tuple<>(target, itr));
    } else {
      recvItrList.add(new Tuple<>(source, itr));
    }
    return;
  }

  @Override
  public void spreadItrs(List<Observable> everything) {
    int timestamp = env.getTimestamp();
    ONE_MATCH_LOOP:
    for (Observable that : everything) {
      if (arest > timestamp) {
        return;
      }
      if (vrest.getOrDefault(that, 0) > timestamp) {
        continue;
      }

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
          // TODO: grasp & weapon-pick
          // TODO: distinguish active or passive
          this.interact(this, that, itr);
          that.interact(this, that, itr);
          if (itr.vrest < 0) {
            arest = timestamp - itr.vrest;
          } else {
            vrest.put(that, timestamp + itr.vrest);
          }
          break ONE_MATCH_LOOP;
        }
      }
    }
    return;
  }

  @Override
  public abstract void react();

  @Override
  public void move() {
    /** Opoint is triggered only at the first timeunit. */
    if (transition == frame.wait) {
      frame.opointList.forEach(opoint -> opointify(opoint));
    }
    int nextAct = ACT_TBA;
    nextAct = applyStatus(nextAct);
    nextAct = updateAction(nextAct);
    nextAct = updateKinetic(nextAct);
    nextAct = updateHealth(nextAct);

    if (actLag > 0) {
      --actLag;
    } else if (nextAct != ACT_TBA) {
      transitFrame(nextAct);
    } else if (--transition < 0) {
      transitFrame(getNextActNumber());
    }


    existence = existence && adjustBoundary();
    updateAnchor();
    updateBdys();
    updateItrs();
    updateVisualNode();
    return;
  }

  // TODO: check taking effect at frame begining or ending.
  protected int applyStatus(int nextAct) {
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
      }
      if (value.elapse()) {
        iterator.remove();
      }
    }
    return ACT_TBA;
  }

  /** State-related & User-input */
  protected abstract int updateAction(int nextAct);
  /** Velocity & Position */
  protected abstract int updateKinetic(int nextAct);
  /** HP & MP & Stamina */
  protected abstract int updateHealth(int nextAct);
  /** Get proper action number connected by next-tag. */
  protected abstract int getNextActNumber();
  /** Fit the boundary of map, or set to be removed. */
  protected abstract boolean adjustBoundary();

  protected Area makeArea(Box box) {
    double baseX = anchorX + (faceRight ? box.x : (box.w - box.x));
    double baseY = anchorY + box.y;
    return new Area(baseX, baseX + box.w,
                    baseY, baseY + box.h,
                    pz - box.zu, pz + box.zd);
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
  public boolean exists() {
    return existence;
  }

  @Override
  public Node getVisualNode() {
    return visualNode;
  }

  @Override
  public void updateVisualNode() {
    visualNode.setX(anchorX);
    visualNode.setY(anchorY + pz);
    visualNode.setViewOrder(pz);
    visualNode.setImage(faceRight ? frame.pic1 : frame.pic2);
    return;
  }

  private List<Observable> generateList(Opoint opoint, Observable origin) {
    if (!opoint.release) {
      System.out.println("NotImplemented: Holding Opoint");
      return List.of();
    }
    List<Observable> output = new ArrayList<>(opoint.amount);
    // TODO: slightly different vx in multiple shots
    double zStep = (opoint.amount == 1) ? 0.0 : (2.0 * Z_RANGE / (opoint.amount - 1));
    double ctrlVz = 2.5 * getInputZ();
    double[] basePosition = getBasePosition(opoint);
    for (int i = 0; i < opoint.amount; ++i) {
      boolean facing = opoint.direction.getFacing(faceRight);
      Observable clone = origin.makeClone(teamId, facing);
      clone.setPosition(basePosition, Point.ZERO, Const.Z_OFFSET);
      double thisVz = (opoint.amount == 1) ? 0.0 : (i * zStep - Z_RANGE);
      clone.setVelocity(facing ? opoint.dvx : -opoint.dvx,
                        opoint.dvy,
                        ctrlVz + thisVz);
      output.add(clone);
    }
    // TODO: Several weapon should immune to each other (shurikens, arrows)
    return output;
  }

  protected void opointify(Opoint opoint) {
    Observable energy = energyMapping.get(opoint.oid);
    if (energy != null) {
      env.spawnEnergy(generateList(opoint, energy));
      return;
    }
    Observable weapon = weaponMapping.get(opoint.oid);
    if (weapon != null) {
      env.spawnWeapon(generateList(opoint, weapon));
      return;
    }
    Observable hero = heroMapping.get(opoint.oid);
    if (hero != null) {
      env.spawnHero(generateList(opoint, hero));
      return;
    }
    System.err.println("Oid not found: " + opoint.oid);
    return;
  }

  /** Implementation is very likely different from LF2. */
  protected void opointCreateArmour() {
    List<Observable> output = new ArrayList<>(5);
    double[] basePosition = getBasePosition(new Point(0.0, 0.0));
    double[] dx = {1.0, 1.0, -1.0, -1.0};
    double[] dz = {1.0, -1.0, 1.0, -1.0};
    for (int i = 0; i < 4; ++i) {
      Observable origin = weaponMapping.get("LouisArmour1");
      if (origin == null) {
        System.err.println("LouisArmour1 not found.");
        break;
      }
      Observable clone = origin.makeClone(teamId, Util.randomBool());
      clone.setVelocity((Util.randomBounds(0.0, 9.0) + 6.0) * dx[i],
                        (Util.randomBounds(0.0, 3.0) - 8.0),
                        (Util.randomBounds(0.0, 4.0) + 3.0) * dz[i]);
      clone.setPosition(basePosition, Point.ZERO, Const.Z_OFFSET);
      output.add(clone);
    }

    Observable origin = weaponMapping.get("LouisArmour2");
    if (origin == null) {
      System.err.println("LouisArmour2 not found.");
    } else {
      Observable clone = origin.makeClone(teamId, Util.randomBool());
      clone.setVelocity(Util.randomBounds(0.0, 9.6) - 4.8,
                        Util.randomBounds(0.0, 2.5) - 6.0,
                        Util.randomBounds(0.0, 6.0) - 3.0);
      clone.setPosition(basePosition, Point.ZERO, Const.Z_OFFSET);
      output.add(clone);
    }

    env.spawnWeapon(output);
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
