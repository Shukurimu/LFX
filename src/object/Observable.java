package object;

import java.util.List;

import base.Point;
import base.Region;
import base.Type;
import component.Bdy;
import component.Itr;
import util.Tuple;
import util.Vector;

public interface Observable {
  // https://www.lf-empire.de/lf2-empire/data-changing/reference-pages

  /**
   * Gets the identifier, usually file name, of this object.
   *
   * @return object's identifier
   */
  String getIdentifier();

  /**
   * Gets the {@code Type} of this object.
   *
   * @return object's {@code Type}
   */
  Type getType();

  /**
   * Gets the team is this object belongs to.
   * A negative value means independent.
   *
   * @return team id
   */
  int getTeamId();

  /**
   * Gets the existence of this object.
   * The one does not exist will be removed from battle field.
   *
   * @return the existence
   */
  boolean exists();

  /**
   * Gets hp and mp ratio.
   */
  Vector getStamina();

  /**
   * A convenient method used in calculating scene camera position.
   *
   * @return the x coordinate of this object
   */
  double getPosX();

  /**
   * Object's facing also affects camera position.
   *
   * @return {@code true} if this object is facing rightward
   */
  boolean isFaceRight();

  /**
   * Returns the x(left), y(top) and z coordinate where the image starts.
   *
   * @return a {@code Vector} represents the coordinate of image
   */
  Vector getImageAnchor();

  /**
   * Returns the index of image this object showing now.
   * i.e. the pic value.
   *
   * @return image index
   */
  int getImageIndex();

  /**
   * Returns the coordinate that used in calculation.
   *
   * @return absulote position
   */
  Vector getAbsolutePosition();

  /**
   * Sets the absolute position of this object.
   *
   * @param px the x coordinate
   * @param py the y coordinate
   * @param pz the z coordinate
   */
  void setAbsolutePosition(double px, double py, double pz);

  /**
   * Translates the coordinate for a {@code Point}.
   * This is the position from self aspect.
   * The other side will based on this value and its anchor to set or update
   * position.
   *
   * @param point self {@code Cpoint}, {@code Opoint}, or {@code Wpoint}
   * @return relative position
   */
  Vector getRelativePosition(Point point);

  /**
   * Set position of a {@code Point} relative to base position.
   * This is the counter part of getRelativePosition().
   *
   * @param basePosition the relative position from other
   * @param point        self {@code Cpoint} or {@code Wpoint}
   * @param cover        adjustment for visual cover effect
   */
  void setRelativePosition(Vector basePosition, Point point, boolean cover);

  /**
   * Gets the {@code Bdy}s this object has in this timestamp.
   *
   * @return a {@code List} of {@code Bdy} with absolute {@code Region}
   */
  List<Tuple<Bdy, Region>> getBdys();

  /**
   * Gets the {@code Itr}s this object has in this timestamp.
   *
   * @return a {@code List} of {@code Itr} with absolute {@code Region}
   */
  List<Tuple<Itr, Region>> getItrs();

  /**
   * Returns this scope from another's perspective.
   * It is mainly used while checking interaction.
   */
  int getScopeView(int targetTeamId);

  /**
   * Define post action after successful interactions.
   *
   * @param target the {@code Bdy} owner
   * @param itr    the {@code Itr} successfully interacting with a {@code Bdy}
   */
  void sendItr(Observable target, Itr itr);

  /**
   * Tell the object {@code Itr}s it received.
   *
   * @param source         the source object
   * @param itr            the {@code Itr} information
   * @param absoluteRegion the effective region
   */
  void receiveItr(Observable source, Itr itr, Region absoluteRegion);

  /**
   * Collects new objects spawned in this timestamp.
   *
   * @return a {@code List} of new objects
   */
  List<? extends Observable> getSpawnedObjectList();

  /**
   * Clones this object.
   *
   * @return the cloned object
   */
  Observable makeClone();

  /**
   * Returns the sign of value representing Up/Down key press.
   * For opoint and throwing, they can have initial vz.
   *
   * @return +1, 0, or -1
   */
  default double getInputZ() {
    return 0.0;
  }

  /**
   * Resets the object to its initial stamina, basically called when F7 is
   * pressed.
   */
  void revive();

  /**
   * Mainly used in Opoint.
   */
  void setVelocity(double vx, double vy, double vz);

  void setProperty(int teamId, boolean faceRight);

  /**
   * Check all ItrArea happened in current timeunit.
   * Store the interactable ones into pending list.
   */
  void spreadItrs(List<Observable> allObjects);

  /**
   * Do actions & update status.
   */
  void run(List<Observable> allObjects);

}

// https://stackoverflow.com/questions/56867/interface-vs-base-class
// http://gjp4860sev.myweb.hinet.net/lf2/page10.htm
// https://lf-empire.de/lf2-empire/data-changing/frame-elements/177-cpoint-catch-point?showall=1

/*
 * protected int graspField = 0;
 * public static final int GRASP_TIME = 305; // test-value
 * public static final int GRASP_FLAG_WAITING = -1;
 * public static final int GRASP_FLAG_UPDATED = -2;
 * public static final int GRASP_FLAG_FREE = -3;
 * public static final int GRASP_FLAG_DROP = -4;
 * public static final int GRASP_FLAG_THROW = -5;
 * public static final double GRASP_DROP_DVX = +8.0;
 * public static final double GRASP_DROP_DVY = -2.5;
 * protected AbstractObject grasper = dummy;
 * protected AbstractObject graspee = dummy;
 * protected synchronized int updateGraspee() {
 * while (graspField == GRASP_FLAG_WAITING) {
 * try {
 * this.wait(1000);
 * } catch (InterruptedException expected) {
 * }
 * }
 * if (graspField == GRASP_FLAG_FREE)
 * return ACT_JUMPAIR;
 * if (graspField == GRASP_FLAG_DROP)
 * return ACT_FORWARD_FALL2;
 * final Cpoint cpoint = grasper.currFrame.cpoint;
 * if (graspField == GRASP_FLAG_THROW) {
 * vx = grasper.faceRight ? cpoint.throwvx : -cpoint.throwvx;
 * vy = cpoint.throwvy;
 * vz = catcher.getControlZ() * cpoint.throwvz;
 * status.put(Extension.Kind.THROWINJURY, new Extension(-1,
 * cpoint.throwinjury));
 * return cpoint.vaction;
 * }
 * if (cpoint.injury > 0) {
 * hpLost(cpoint.injury, false);
 * actLag = Math.max(actLag, Itr.LAG);
 * } else {
 * hpLost(-cpoint.injury, false);
 * }
 * faceRight = grasper.faceRight ^ cpoint.changedir;
 * px = grasper.faceRight ?
 * (grasper.anchorX + cpoint.x) + (currFrame.cpoint.x - currFrame.centerx):
 * (grasper.anchorX - cpoint.x) - (currFrame.cpoint.x - currFrame.centerx);
 * py = (grasper.anchorY + cpoint.y) - (currFrame.cpoint.y - currFrame.centery);
 * pz = grasper.pz;
 * graspField = GRASP_FLAG_WAITING;
 * return actLag == 0 ? cpoint.vaction : ACT_TBA;
 * }
 *
 *
 * protected final int updateGrasp() {
 * if (grasper.currFrame.state != State.GRASP || grasper.currFrame.cpoint ==
 * null ||
 * graspee.currFrame.state != State.GRASP || graspee.currFrame.cpoint == null) {
 * return ACT_DEF;
 * }
 * if (grasper == this) {
 * if (graspee.grasper != this) {
 * // re-grasped by other
 * grasper = graspee = dummy;
 * return ACT_DEF;
 * }
 * return updateGrasper();
 * } else {
 * return updateGraspee();
 * }
 * }
 *
 * private int updateGrasper() {
 * int graspeeFlag = GRASP_FLAG_WAITING;
 * graspField -= Math.abs(cpoint.decrease);
 * if (currFrame.cpoint == null) {
 * // does a combo and goes to a frame without cpoint
 * graspeeFlag = GRASP_FLAG_FREE;
 * } else if (graspField < 0 && currFrame.cpoint.decrease < 0) {
 * // will not drop graspee in cpoint with positive decrease even if timeup
 * graspeeFlag = GRASP_FLAG_DROP;
 * } else if (transition == currFrame.wait) {
 * // these functions only take effect once
 * if (currFrame.cpoint.injury > 0) {
 * actLag = Math.max(actLag, Itr.LAG);
 * }
 * if (cpoint.throwing) {
 * graspeeFlag = GRASP_FLAG_THROW;
 * }
 * if (cpoint.transform) {
 * graspeeFlag = GRASP_FLAG_THROW;
 * status.put(Extension.Kind.TRANSFORM_TO, new Extension(1,
 * graspee.identifier));
 * }
 * }
 * }
 * graspee.setPointPosition(getPointPosition(currFrame.cpoint),
 * graspee.currFrame.cpoint);
 * synchronized (graspee) {
 * graspee.graspField = graspeeFlag;
 * graspee.notify();
 * }
 * return;
 * }
 */
