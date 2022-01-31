package ecosystem;

import java.util.List;

import base.Controller;
import base.Point;
import base.Region;
import base.Type;
import base.Vector;
import component.Action;
import component.Bdy;
import component.Frame;
import component.Itr;
import util.Tuple;

public final class NullObject implements Hero, Weapon, Energy {
  public static final NullObject DUMMY = new NullObject();
  public static final Hero HERO = DUMMY;
  public static final Weapon WEAPON = DUMMY;
  public static final Energy ENERGY = DUMMY;

  /**
   * Don't let anyone instantiate this class.
   */
  private NullObject() {}

  // ==================== Observable ====================

  @Override
  public String getIdentifier() {
    return getClass().getSimpleName();
  }

  @Override
  public List<Tuple<String, int[]>> getPictureInfo() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Type getType() {
    return Type.OTHERS;
  }

  @Override
  public int getTeamId() {
    return -1;
  }

  @Override
  public boolean exists() {
    return false;
  }

  @Override
  public double getPosX() {
    return 0;
  }

  @Override
  public boolean isFaceRight() {
    return true;
  }

  @Override
  public Vector getSceneCoordinate() {
    return Vector.ZERO;
  }

  @Override
  public int getImageIndex() {
    return -1;
  }

  @Override
  public Vector getAbsolutePosition() {
    return Vector.ZERO;
  }

  @Override
  public void setAbsolutePosition(double px, double py, double pz) {
    throw new UnsupportedOperationException(getIdentifier());
  }

  @Override
  public Vector getRelativePosition(Point point) {
    return Vector.ZERO;
  }

  @Override
  public void setRelativePosition(Vector basePosition, Point point, boolean cover) {
    throw new UnsupportedOperationException(getIdentifier());
  }

  @Override
  public Vector getAbsoluteVelocity(Vector relativeVelocity) {
    return Vector.ZERO;
  }

  @Override
  public Frame getCurrentFrame() {
    return Frame.NULL_FRAME;
  }

  @Override
  public boolean isFirstTimeunit() {
    return false;
  }

  @Override
  public List<Tuple<Bdy, Region>> getBdys() {
    return List.of();
  }

  @Override
  public int getScopeView(int targetTeamId) {
    return 0;
  }

  @Override
  public void sendItr(Observable target, Itr itr) {
    throw new UnsupportedOperationException(getIdentifier());
  }

  @Override
  public boolean receiveItr(Observable source, Itr itr, Region absoluteRegion) {
    throw new UnsupportedOperationException(getIdentifier());
  }

  @Override
  public List<Observable> getSpawnedObjectList() {
    throw new UnsupportedOperationException(getIdentifier());
  }

  @Override
  public Observable makeClone() {
    throw new UnsupportedOperationException(getIdentifier());
  }

  @Override
  public void revive() {
    throw new UnsupportedOperationException(getIdentifier());
  }

  @Override
  public void setVelocity(Vector velocity) {
    throw new UnsupportedOperationException(getIdentifier());
  }

  @Override
  public void initTerrain(Terrain terrain) {
    throw new UnsupportedOperationException(getIdentifier());
  }

  @Override
  public void setAction(Action action) {
    throw new UnsupportedOperationException(getIdentifier());
  }

  @Override
  public void setFacing(boolean faceRight) {
    throw new UnsupportedOperationException(getIdentifier());
  }

  @Override
  public void setProperty(int teamId, boolean faceRight) {
    throw new UnsupportedOperationException(getIdentifier());
  }

  @Override
  public void spreadItrs(int timestamp, List<Observable> allObjects) {
    throw new UnsupportedOperationException(getIdentifier());
  }

  @Override
  public void run(int timestamp, List<Observable> allObjects) {
    throw new UnsupportedOperationException(getIdentifier());
  }

  // ==================== Playable ====================

  @Override
  public String getPortraitPath() {
    return "";
  }

  // ==================== Hero ====================

  @Override
  public void setController(Controller controller) {
    throw new UnsupportedOperationException(getIdentifier());
  }

  // ==================== Weapon ====================

  @Override
  public boolean isHeavy() {
    return false;
  }

  @Override
  public boolean isDrink() {
    return false;
  }

  @Override
  public boolean isLight() {
    return false;
  }

  @Override
  public boolean isSmall() {
    return false;
  }

  @Override
  public void destroy() {
    throw new UnsupportedOperationException(getIdentifier());
  }

  // ==================== Energy ====================

  @Override
  public void rebound() {
    throw new UnsupportedOperationException(getIdentifier());
  }

  @Override
  public void disperse() {
    throw new UnsupportedOperationException(getIdentifier());
  }

}