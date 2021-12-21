package object;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import base.Point;
import base.Region;
import base.Scope;
import base.Type;
import component.Action;
import component.Bdy;
import component.Effect;
import component.Frame;
import component.Itr;
import component.Opoint;
import field.Environment;
import util.Tuple;
import util.Vector;

abstract class AbstractObject implements Observable {
  private static final System.Logger logger = System.getLogger("");
  private static final Map<String, Observable> library = new HashMap<>(128);

  /**
   * A human-readable text, which is usually the original file name.
   */
  public final String identifier;

  /**
   * The type of this object.
   */
  public final Type type;

  /**
   * The team this object belongs to.
   */
  protected int teamId = 0;

  /**
   * Stores itrs in a list to avoid repeated computation.
   */
  protected final List<Tuple<Bdy, Region>> bdyList = new ArrayList<>();

  /**
   * Stores itrs in a list to avoid repeated computation.
   */
  protected final List<Tuple<Itr, Region>> itrList = new ArrayList<>();

  /**
   * The x-coordinate of picture left.
   */
  protected double anchorX = 0.0;

  /**
   * The y-coordinate of picture top.
   */
  protected double anchorY = 0.0;

  /**
   * Indicates if this object will be removed later.
   */
  protected boolean existence = true;

  /**
   * Current frame.
   */
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
  protected double mpMax = 500.0;

  protected final List<Frame> frameList; // shared between same objects
  protected final List<Observable> spawnedObjectList = new ArrayList<>();

  /**
   * Effects that can last more than one timeunit, just like buff.
   * e.g., John's healing.
   */
  protected final Map<Effect, Integer> buff = new EnumMap<>(Effect.class);

  /**
   * This object is able to interact with others
   * only if the value greater than current timestamp.
   */
  protected final Map<Observable, Integer> vrest = new WeakHashMap<>(128);

  /**
   * vrest's global version, having higher priority than vrest.
   */
  protected int arest = 0;

  /**
   * Before this timestamp, the object cannot move or change action.
   * This is the freeze mechanism after hitting or being hit.
   */
  private int actionPause = 0;

  /**
   * Indicates the remaining timeunit to transit to next frame.
   * This value will get decrement every timestamp.
   */
  private int transition = 0;
  private boolean newAction = true;
  protected Environment env = null;

  protected AbstractObject(String identifier, Type type, List<Frame> frameList) {
    this.identifier = identifier;
    this.type = type;
    this.frameList = frameList;
    // library.put(identifier, this);
  }

  protected AbstractObject(AbstractObject base) {
    identifier = base.identifier;
    type = base.type;
    frameList = base.frameList;
  }

  @Override
  public abstract AbstractObject makeClone();

  @Override
  public String getIdentifier() {
    return identifier;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public int getTeamId() {
    return teamId;
  }

  @Override
  public boolean exists() {
    return existence;
  }

  @Override
  public Vector getStamina() {
    return new Vector(hp / hpMax, hp / hpMax, mp / mpMax);
  }

  @Override
  public double getPosX() {
    return px;
  }

  @Override
  public boolean isFaceRight() {
    return faceRight;
  }

  @Override
  public Vector getImageAnchor() {
    return new Vector(anchorX, anchorY, pz);
  }

  @Override
  public int getImageIndex() {
    return frame.pic;
  }

  /**
   * Updates the coordinate which many functionalities based on.
   */
  protected void updateImageAnchor() {
    anchorX = faceRight ? (px - frame.centerx) : (px + frame.centerx);
    anchorY = py - frame.centery;
    return;
  }

  @Override
  public Vector getAbsolutePosition() {
    return new Vector(px, py, pz);
  }

  @Override
  public void setAbsolutePosition(double px, double py, double pz) {
    this.px = px;
    this.py = py;
    this.pz = pz;
    updateImageAnchor();
    return;
  }

  @Override
  public Vector getRelativePosition(Point point) {
    return new Vector(
        anchorX + (faceRight ? point.x : -point.x),
        anchorY + point.y,
        pz);
  }

  @Override
  public void setRelativePosition(Vector base, Point point, boolean cover) {
    if (faceRight) {
      anchorX = base.x() - point.x;
      px = anchorX + frame.centerx;
    } else {
      anchorX = base.x() + point.x;
      px = anchorX - frame.centerx;
    }
    anchorY = base.y() - point.y;
    py = anchorY + frame.centery;
    pz = cover ? base.z() - Point.Z_OFFSET : base.z() + Point.Z_OFFSET;
    return;
  }

  /**
   * Default implementation of computing bdys.
   */
  protected List<Tuple<Bdy, Region>> computeBdyList() {
    List<Tuple<Bdy, Region>> result = new ArrayList<>();
    for (Bdy bdy : frame.bdyList) {
      result.add(new Tuple<>(bdy, bdy.relative.toAbsolute(anchorX, anchorY, pz, faceRight)));
    }
    return result;
  }

  @Override
  public List<Tuple<Bdy, Region>> getBdys() {
    return bdyList;
  }

  /**
   * Default implementation of computing itrs.
   */
  protected List<Tuple<Itr, Region>> computeItrList() {
    List<Tuple<Itr, Region>> result = new ArrayList<>();
    for (Itr itr : frame.itrList) {
      result.add(new Tuple<>(itr, itr.relative.toAbsolute(anchorX, anchorY, pz, faceRight)));
    }
    return result;
  }

  @Override
  public List<Tuple<Itr, Region>> getItrs() {
    return itrList;
  }

  @Override
  public int getScopeView(int targetTeamId) {
    return Scope.getSideView(type.baseScope, targetTeamId == teamId);
  }

  /**
   * Many functionalities only take effect at the first timeunit.
   * For example, opoint.
   */
  protected boolean isFirstTimeunit() {
    return transition == frame.wait;
  }

  /**
   * Mostly used in jump and dash kinetics.
   */
  protected boolean isActionFirstTimeunit() {
    return newAction && isFirstTimeunit();
  }

  /**
   * Mostly used in hidden frame counter.
   */
  protected boolean isLastTimeunit() {
    return transition == 0;
  }

  protected boolean isSameFacingVelocity() {
    return vx == 0.0 || faceRight == (vx > 0.0);
  }

  @Override
  public void revive() {
    hp = hpMax;
    mp = mpMax;
    return;
  }

  /**
   * For the cases of next:999 in LF2.
   */
  protected abstract Action getDefaultAction();

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
        action = getDefaultAction();
      } else if (action == Action.DEFAULT_REVERSE) {
        action = getDefaultAction();
        faceRight ^= true;
      }
      frame = frameList.get(action.index);
      faceRight ^= action.changeFacing;
    }

    transition = frame.wait;
    logger.log(Level.TRACE, "%s %d", action, transition);
    return;
  }

  protected void transitNextFrame() {
    transitFrame(frame.next);
    frame.opointList.forEach(this::opointify);
    return;
  }

  protected void hpLost(double injury) {
    hp -= injury;
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
  public void setProperty(int teamId, boolean faceRight) {
    this.teamId = teamId;
    this.faceRight = faceRight;
    return;
  }

  /**
   * Checks if this object can interact with another.
   *
   * @param that the target object is going to check
   * @return the non zero {@code vrest} value if there is a successful match
   */
  protected int checkInteraction(Observable that) {
    int scopeView = that.getScopeView(teamId);
    for (Tuple<Bdy, Region> bdyRegion : that.getBdys()) {
      Bdy bdy = bdyRegion.first;
      Region rb = bdyRegion.second;
      for (Tuple<Itr, Region> itrRegion : itrList) {
        Itr itr = itrRegion.first;
        Region ri = itrRegion.second;
        if (ri.collidesWith(rb) && itr.interactsWith(bdy, scopeView)) {
          sendItr(that, itr);
          that.receiveItr(this, itr, ri);
          return itr.vrest;
        }
      }
    }
    return 0;
  }

  @Override
  public void spreadItrs(List<Observable> allObjects) {
    int timestamp = env.getTimestamp();
    if (arest > timestamp) {
      return;
    }

    for (Observable that : allObjects) {
      if (that == this) {
        continue;
      }
      if (vrest.getOrDefault(that, 0) > timestamp) {
        continue;
      }
      int resultVrest = checkInteraction(that);
      if (resultVrest < 0) {
        arest = timestamp - resultVrest;
        break;
      }
      if (resultVrest > 0) {
        vrest.put(that, timestamp + resultVrest);
      }
    }
    return;
  }

  @Override
  public abstract void sendItr(Observable target, Itr itr);

  @Override
  public abstract void receiveItr(Observable source, Itr itr, Region absoluteRegion);

  /**
   * Updates hp, mp, and other inner states.
   *
   * @return an {@code Action} other than {@code ACTION.UNASSIGNED} if causing action change
   */
  protected abstract Action updateStamina();

  /**
   * Updates position & velocity.
   *
   * @return an {@code Action} other than {@code ACTION.UNASSIGNED} if causing action change
   */
  protected abstract Action updateKinetic();

  /**
   * Updates based on state and any other reasons.
   * Note that there are several state-specified logics must be handled.
   *
   * @return an {@code Action} other than {@code ACTION.UNASSIGNED} if causing action change
   */
  protected abstract Action updateByState();

  /**
   * If the object still alive, fits the boundary to env and returns true.
   *
   * @return false if the object should be deleted
   */
  protected abstract boolean fitBoundary();

  @Override
  public void run(List<Observable> allObjects) {
    final int timestamp = env.getTimestamp();
    Action nextAct = updateStamina();

    if (actionPause > timestamp) {
      return;
    }
    if (nextAct == Action.UNASSIGNED) {
      nextAct = updateKinetic();
    }
    if (nextAct == Action.UNASSIGNED) {
      nextAct = updateByState();
    }
    if (nextAct != Action.UNASSIGNED) {
      transitFrame(nextAct);
    } else if (--transition < 0) {
      transitNextFrame();
    }

    if (fitBoundary()) {
      updateImageAnchor();
      bdyList.clear();
      bdyList.addAll(computeBdyList());
      itrList.clear();
      itrList.addAll(computeItrList());
    } else {
      existence = false;
    }
    return;
  }

  /**
   * Performs an {@code Opoint} instruction.
   */
  protected void opointify(Opoint opoint) {
    if (!opoint.release) {
      logger.log(Level.INFO, "NotImplemented: Holding Opoint");
      return;
    }

    double vzStart = 0.0;
    double vzStep = 0.0;
    if (opoint.amount > 1) {
      vzStart = Opoint.Z_RANGE * (getInputZ() - 1.0);
      vzStep = 2.0 * Opoint.Z_RANGE / (opoint.amount - 1);
    }

    Vector basePosition = getRelativePosition(opoint);
    Observable origin = library.get(opoint.oid).makeClone();
    List<Observable> cloneList = new ArrayList<>();
    for (int i = 0; i < opoint.amount; ++i) {
      boolean facing = opoint.direction.getFacing(faceRight);
      Observable clone = origin.makeClone();
      clone.setProperty(teamId, facing);
      clone.setVelocity(
          facing ? opoint.dvx : -opoint.dvx,
          opoint.dvy,
          vzStart + vzStep * i);
      clone.setRelativePosition(basePosition, Point.ORIGIN, true);
    }

    // Weapons immune to each other if spawned simultaneously. (e.g., arrows, shurikens)
    if (opoint.amount > 1 && origin.getType().isWeapon) {
      Weapon.setMutualExcluding(cloneList);
    }
    logger.log(Level.INFO, cloneList);
    spawnedObjectList.addAll(cloneList);
    return;
  }

  /*
  protected void createArmours() {
    Vector basePosition = getAbsolutePosition();
    double[] rvx = {1.0, 1.0, -1.0, -1.0, Util.randomBounds(-0.4, 0.4)};
    double[] rvz = {1.0, -1.0, 1.0, -1.0, Util.randomBounds(-0.4, 0.4)};
    List<Observable> cloneList = Library.instance().getArmourSetList();
    int index = 0;
    for (Observable clone : cloneList) {
      clone.setProperty(teamId, Util.randomBool());
      clone.setVelocity((Util.randomBounds(0.0, 9.0) + 6.0) * rvx[index],
                        (Util.randomBounds(0.0, 3.0) - 8.0),
                        (Util.randomBounds(0.0, 4.0) + 3.0) * rvz[index]
      );
      clone.setPosition(basePosition, Point.ORIGIN, Point.Z_OFFSET);
      ++index;
    }
    spawnedObjectList.addAll(cloneList);
    return;
  }*/
  @Override
  public List<? extends Observable> getSpawnedObjectList() {
    return spawnedObjectList;
  }

  @Override
  public String toString() {
    return String.format("%s %s@%x [%d]", type, identifier, hashCode(), teamId);
  }

}
