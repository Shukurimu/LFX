package field;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import base.Region;
import object.Observable;

public class BaseField implements Field {
  private static final System.Logger logger = System.getLogger("");

  /**
   * The boundary of camera moving.
   */
  protected final double cameraWindow;

  /**
   * Boundary for {@code Hero}.
   * y1 and y2 have no use.
   */
  protected final Region heroBoundary;

  /**
   * Boundary for non-{@code Hero}.
   * y1 and y2 have no use.
   */
  protected final Region itemBoundary;

  /**
   * Temporary placeholder for newly created objects.
   */
  protected final List<Observable> pendingList = new ArrayList<>(64);

  /**
   * All objects on this {@code Field}.
   */
  protected final List<Observable> objectList = new ArrayList<>(512);

  /**
   * Unmodifiable view to pass to objects.
   */
  protected final List<Observable> objectView = Collections.unmodifiableList(objectList);

  /**
   * Counter for independent team id.
   * Starting value reserves for pre-defined teams.
   */
  private int independentTeamId = 16;

  /**
   * Records of functional keys (F6 ~ F10) pressed times.
   */
  private int[] keyPressedTimes = new int[5];

  /**
   * Factor of friction from ground.
   */
  protected double friction = 1.0;

  /**
   * Factor of gravity.
   */
  protected double gravity = 1.7;

  /**
   * Current timestamp.
   */
  protected int timestamp = 0;

  protected BaseField(double boundWidth, double boundTop, double boundBottom) {
    cameraWindow = boundWidth - FIELD_WIDTH;
    heroBoundary = new Region(0, boundWidth, 0, 0, boundTop, boundBottom);
    itemBoundary = new Region(
        -ITEM_ADDITIONAL_WIDTH, boundWidth + ITEM_ADDITIONAL_WIDTH, 0, 0, boundTop, boundBottom);
  }

  @Override
  public boolean isUnlimitedMode() {
    return (keyPressedTimes[0] & 1) == 1;
  }

  @Override
  public double applyFriction(double vx) {
    return vx >= 0.0 ? Math.max(vx - friction, 0.0) : Math.min(vx + friction, 0.0);
  }

  @Override
  public double applyGravity(double vy) {
    return vy + gravity;
  }

  @Override
  public int requestIndependentTeamId() {
    return ++independentTeamId;
  }

  @Override
  public int getTimestamp() {
    return timestamp;
  }

  @Override
  public Region getHeroBoundary() {
    return heroBoundary;
  }

  @Override
  public Region getItemBoundary() {
    return itemBoundary;
  }

  @Override
  public void switchUnlimitedMode() {
    ++keyPressedTimes[0];
    return;
  }

  @Override
  public void reviveAll() {
    logger.log(Level.INFO, "reviveAll()");
    ++keyPressedTimes[1];
    return;
  }

  @Override
  public void dropNeutralWeapons() {
    logger.log(Level.INFO, "dropNeutralWeapons()");
    ++keyPressedTimes[2];
    return;
  }

  @Override
  public void destroyWeapons() {
    objectList.removeIf(o -> o.getType().isWeapon);
    ++keyPressedTimes[3];
    return;
  }

  @Override
  public void disperseEnergies() {
    objectList.removeIf(o -> o.getType().isEnergy);
    ++keyPressedTimes[4];
    return;
  }

  @Override
  public void stepOneFrame() {
    ++timestamp;
    objectList.sort(Field::processOrder);
    objectList.forEach(o -> o.spreadItrs(objectView));
    objectList.forEach(o -> {
      o.run(objectView);
      pendingList.addAll(o.getSpawnedObjectList());
    });
    objectList.addAll(pendingList);
    pendingList.clear();
    return;
  }

  /**
   * Calculates camera position.
   * Moving policy is modified from F.LF.
   *
   * @param tracingList the objects that will be focused on
   * @param currentPos  current camera position
   * @return new camera position
   */
  protected double calcCameraPos(List<Observable> tracingList, double currentPos) {
    if (tracingList.isEmpty()) {
      return cameraWindow * 0.5;
    }
    double position = 0.0;
    int facingWeight = 0;
    for (Observable o : tracingList) {
      position += o.getPosX();
      facingWeight += o.isFaceRight() ? 1 : -1;
    }
    position /= tracingList.size();
    position = facingWeight * WIDTH_DIV24 + position - WIDTH_DIV2;
    position = position < 0.0 ? 0.0 : Math.min(position, cameraWindow);
    return position < currentPos
        ? Math.max(currentPos - CAMERA_SPEED_THRESHOLD, position)
        : Math.min(currentPos + CAMERA_SPEED_THRESHOLD, position);
  }

}
