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
  // https://lf-empire.de/forum/showthread.php?tid=9172

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
   * Calculates absolute velocity based on this object.
   * The use cases include throwing {@code Hero} and {@code Weapon}.
   *
   * @param relativeVelocity defined in {@code Cpoint} or {@code Opoint}
   * @return absolute velocity
   */
  Vector getAbsoluteVelocity(Vector relativeVelocity);

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
   * It is mainly used in checking interaction.
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
   * Tells the object {@code Itr}s it received, and returns if the action successed.
   *
   * @param source         the source object
   * @param itr            the {@code Itr} information
   * @param absoluteRegion the effective region
   * @return {@code true} if the action is successed
   */
  boolean receiveItr(Observable source, Itr itr, Region absoluteRegion);

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
  void setVelocity(Vector velocity);

  void setProperty(int teamId, boolean faceRight);

  /**
   * Check all ItrArea happened in current timeunit.
   * Store the interactable ones into pending list.
   */
  void spreadItrs(int timestamp, List<Observable> allObjects);

  /**
   * Do actions & update status.
   */
  void run(int timestamp, List<Observable> allObjects);

}
