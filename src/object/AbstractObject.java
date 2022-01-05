package object;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import base.Point;
import base.Region;
import base.Scope;
import base.Type;
import component.Action;
import component.Bdy;
import component.Cpoint;
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

  protected final Random random = new Random(666L);

  /**
   * The type of this object.
   */
  public final Type type;

  /**
   * The team this object belongs to.
   */
  protected int teamId = 0;

  /**
   * The base {@code Scope} of this object.
   */
  protected final int baseScope;

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
  protected Frame frame = Frame.NULL_FRAME;

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
   * Victim rest (from this object's aspect).
   * This object is able to interact with others
   * only if corresponding value greater than current timestamp.
   */
  protected final Map<Observable, Integer> vrest = new WeakHashMap<>(128);

  /**
   * Attacker rest.
   * This value has higher priority than vrest.
   */
  protected int arest = 0;

  /**
   * Before this timestamp, the object cannot move or change action.
   * This is the freeze mechanism after hitting or being hit.
   */
  protected int actionPause = 0;

  /**
   * Indicates the remaining timeunit to transit to next frame.
   * This value will get decrement every timestamp.
   */
  private int transition = 0;
  private boolean newAction = true;
  protected Environment env = null;
  protected Hero grabbingHero = null;
  protected int grabbingTimer = 0;

  protected AbstractObject(Type type, Frame.Collector collector) {
    this.type = type;
    this.frameList = collector.toFrameList();
    baseScope = switch (type) {
      case HERO -> Scope.HERO;
      case SMALL, DRINK, HEAVY, LIGHT -> Scope.WEAPON;
      case ENERGY -> Scope.ENERGY;
      default -> throw new IllegalArgumentException(type.toString());
    };
  }

  protected AbstractObject(AbstractObject base) {
    type = base.type;
    frameList = base.frameList;
    baseScope = base.baseScope;
  }

  @Override
  public abstract AbstractObject makeClone();

  @Override
  public String getIdentifier() {
    return getClass().getSimpleName();
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
    double rx = faceRight ? (anchorX + point.x) : (anchorX - point.x);
    return new Vector(rx, anchorY + point.y, pz);
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
    pz = cover ? base.z() - Point.Z_EPSILON : base.z() + Point.Z_EPSILON;
    return;
  }

  @Override
  public Vector getAbsoluteVelocity(Vector relativeVelocity) {
    return new Vector(
        faceRight ? relativeVelocity.x() : -relativeVelocity.x(),
        relativeVelocity.y(),
        getInputZ() * relativeVelocity.z()
    );
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
    return targetTeamId == teamId ? Scope.getTeammateView(baseScope)
                                  : Scope.getEnemyView(baseScope);
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
    opointify(frame.opoint);
    return;
  }

  @Override
  public void setVelocity(Vector velocity) {
    vx = velocity.x();
    vy = velocity.y();
    vz = velocity.z();
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
        if (ri.collidesWith(rb) &&
            itr.interactsWith(bdy, scopeView) &&
            that.receiveItr(this, itr, ri)) {
          sendItr(that, itr);
          return itr.vrest;
        }
      }
    }
    return 0;
  }

  @Override
  public void spreadItrs(int timestamp, List<Observable> allObjects) {
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
  public abstract boolean receiveItr(Observable source, Itr itr, Region absoluteRegion);

  /**
   * Default implmentation for grabbing state.
   * This method only changes self status.
   *
   * @return new {@code Action} after this timestamp
   */
  protected Action moveGrabbing() {
    Cpoint cpoint = frame.cpoint;
    if (cpoint.decrease > 0) {
      grabbingTimer -= cpoint.decrease;
      // Does not drop on positive decrease.
    } else {
      grabbingTimer += cpoint.decrease;
      if (grabbingTimer < 0) {
        grabbingHero.setCpoint(Cpoint.DROP);
        return Action.DEFAULT;
      }
    }
    if (cpoint.injury > 0) {
      applyActionPause(Itr.DEFAULT_DAMAGE_PAUSE);
    }
    grabbingHero.setCpoint(cpoint);
    return Action.UNASSIGNED;
  }

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
  public void run(int timestamp, List<Observable> allObjects) {
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

  protected void applyActionPause(int value) {
    actionPause = Math.max(actionPause, env.getTimestamp() + value);
    return;
  }

  /**
   * Performs an {@code Opoint} instruction.
   *
   * @param opoint the {@code Opoint} instance
   */
  protected void opointify(Opoint opoint) {
    if (opoint == null) {
      return;
    }

    Vector baseVelocity = getAbsoluteVelocity(opoint.velocity);
    Vector basePosition = getRelativePosition(opoint);
    Observable origin = library.get(opoint.oid).makeClone();
    List<Observable> cloneList = new ArrayList<>();
    for (Vector velocity : opoint.getInitialVelocities(baseVelocity)) {
      Observable clone = origin.makeClone();
      clone.setProperty(teamId, faceRight ^ opoint.opposideDirection);
      clone.setVelocity(velocity);
      clone.setRelativePosition(basePosition, Point.ORIGIN, true);
      cloneList.add(clone);
    }

    if (cloneList.size() > 1 && origin.getType().isWeapon) {
      Weapon.setMutualExcluding(cloneList);
    }
    logger.log(Level.INFO, cloneList);
    spawnedObjectList.addAll(cloneList);
    return;
  }

  @Override
  public List<? extends Observable> getSpawnedObjectList() {
    return spawnedObjectList;
  }

  @Override
  public String toString() {
    return String.format("%s %s@%x [%d]", type, getIdentifier(), hashCode(), teamId);
  }

}
