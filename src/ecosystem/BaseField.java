package ecosystem;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import base.Region;
import component.Action;

public class BaseField implements Field {
  private static final System.Logger logger = System.getLogger("");

  protected final Random random = new Random(888L);

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
  private final List<Observable> pendingList = new ArrayList<>(64);

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
   * Current timestamp.
   */
  protected int timestamp = 0;

  protected BaseField(double boundWidth, double boundTop, double boundBottom) {
    cameraWindow = boundWidth - FIELD_WIDTH;
    heroBoundary = new Region(0, boundWidth, 0, 0, boundTop, boundBottom);
    itemBoundary = new Region(
        -ITEM_ADDITIONAL_WIDTH, boundWidth + ITEM_ADDITIONAL_WIDTH, 0, 0, boundTop, boundBottom);
  }

  // ==================== Terrain ====================

  @Override
  public boolean isUnlimitedMode() {
    return (keyPressedTimes[0] & 1) == 1;
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

  // ==================== Field ====================

  @Override
  public int requestIndependentTeamId() {
    return ++independentTeamId;
  }

  @Override
  public int getObjectCount() {
    return objectList.size();
  }

  protected void addObject(Observable o) {
    synchronized (pendingList) {
      pendingList.add(o);
    }
    o.initTerrain(this);
    return;
  }

  protected void addObjects(List<Observable> os) {
    if (!os.isEmpty()) {
      synchronized (pendingList) {
        pendingList.addAll(os);
      }
      os.forEach(o -> o.initTerrain(this));
    }
    return;
  }

  @Override
  public void emplace(Observable o) {
    Region boundary = getHeroBoundary();
    double width = boundary.x2();
    double px = random.nextDouble(width * 0.4, width * 0.6);
    double pz = random.nextDouble(boundary.z1(), boundary.z2());
    o.setAbsolutePosition(px, 0.0, pz);
    addObject(o);
    return;
  }

  protected void emplaceWeapon(Observable o) {
    logger.log(Level.INFO, o);
    Region boundary = getItemBoundary();
    double width = boundary.x2();
    double px = random.nextDouble(width * 0.05, width * 0.95);
    double pz = random.nextDouble(boundary.z1(), boundary.z2());
    o.setAbsolutePosition(px, -500.0, pz);
    addObject(o);
    return;
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
    for (Observable o : Library.getClonedWeapons()) {
      o.setAction(Action.DEFAULT);
      o.setProperty(requestIndependentTeamId(), random.nextBoolean());
      emplaceWeapon(o);
    }
    ++keyPressedTimes[2];
    return;
  }

  @Override
  public void destroyWeapons() {
    // objectList.removeIf(o -> o.getType().isWeapon);
    ++keyPressedTimes[3];
    return;
  }

  @Override
  public void disperseEnergies() {
    // objectList.removeIf(o -> o.getType().isEnergy);
    ++keyPressedTimes[4];
    return;
  }

  @Override
  public void stepOneFrame() {
    ++timestamp;
    objectList.forEach(o -> o.spreadItrs(timestamp, objectView));
    SyncGrab.update();
    SyncPick.update();
    objectList.forEach(o -> {
      o.run(timestamp, objectView);
      addObjects(o.getSpawnedObjectList());
    });
    objectList.removeIf(o -> !o.exists());
    objectList.addAll(pendingList);
    pendingList.clear();
    objectList.sort(Field::processOrder);
    return;
  }

  @Override
  public double calcCameraPos(List<Observable> tracingList, double currentPos) {
    if (tracingList.isEmpty()) {
      return cameraWindow * 0.5;
    }
    // Moving policy is modified from F.LF.
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
