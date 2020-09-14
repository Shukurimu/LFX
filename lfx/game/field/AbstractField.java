package lfx.game.field;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lfx.game.Field;
import lfx.game.Observable;
import lfx.util.UnionReadOnlyIterable;

public class AbstractField implements Field {
  protected final double boundWidth;
  protected final double boundTop;
  protected final double boundBottom;
  protected final List<Observable> heroList = new LinkedList<>();
  protected final List<Observable> itemList = new LinkedList<>();
  private final List<Observable> pendingQueue = new ArrayList<>(64);
  private final Iterable<Observable> heroView = new UnionReadOnlyIterable<>(heroList);
  private final Iterable<Observable> everything = new UnionReadOnlyIterable<>(heroList, itemList);
  private int independentTeamId = 16;  // Reserve for pre-defined teams.
  private boolean unlimitedMode = false;
  protected double friction = 1.0;
  protected double gravity = 1.7;
  protected int timestamp = 0;
  protected List<Double> zBound;
  protected List<Double> heroXBound;
  protected List<Double> itemXBound;

  protected AbstractField(double boundWidth, double boundTop, double boundBottom) {
    this.boundWidth = boundWidth;
    this.boundTop = boundTop;
    this.boundBottom = boundBottom;
    zBound = List.of(boundBottom, boundTop);
    heroXBound = List.of(boundWidth, 0.0);
    itemXBound = List.of(boundWidth + ITEM_ADDITIONAL_WIDTH, -ITEM_ADDITIONAL_WIDTH);
  }

  /** ========== Environment Interface ========== */

  @Override
  public boolean isUnlimitedMode() {
    return unlimitedMode;
  }

  @Override
  public double applyFriction(double vx) {
    return vx >= 0.0 ? Math.max(vx - friction, 0.0)
                     : Math.min(vx + friction, 0.0);
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
  public List<Double> getZBound() {
    return zBound;
  }

  @Override
  public List<Double> getHeroXBound() {
    return heroXBound;
  }

  @Override
  public List<Double> getItemXBound() {
    return itemXBound;
  }

  /** ========== Field Interface ========== */

  @Override
  public double getBoundWidth() {
    return boundWidth;
  }

  @Override
  public boolean switchUnlimitedMode() {
    return unlimitedMode ^= true;
  }

  @Override
  public boolean reviveAll() {
    System.out.println("reviveAll()");
    return true;
  }

  @Override
  public boolean dropNeutralWeapons() {
    System.out.println("dropNeutralWeapons()");
    return true;
  }

  @Override
  public boolean destroyWeapons() {
    System.out.println("destroyWeapons()");
    return true;
  }

  @Override
  public boolean disperseEnergies() {
    System.out.println("disperseEnergies()");
    return true;
  }

  @Override
  public void spawnObject(Observable anything) {
    pendingQueue.add(anything);
    return;
  }

  /**
   * Removes no longer existing objects from given List, and then returns a List of removed items.
   * Due to this operation, originalList is preferred to be a LinkedList.
   *
   * @param   originalList
   *          a List may contain objects should be removed
   * @return  a List of removed items from originalList, can be empty
   */
  protected List<Observable> retainExistingObjects(List<Observable> originalList) {
    List<Observable> removedItemList = new ArrayList<>(16);
    for (ListIterator<Observable> it = originalList.listIterator(); it.hasNext(); ) {
      Observable o = it.next();
      if (!o.exists()) {
        it.remove();
        removedItemList.add(o);
      }
    }
    return removedItemList;
  }

  /**
   * Partitions the given object List into Hero and Item groups.
   * Heroes are put in TRUE bucket, while Items are put in FALSE bucket.
   *
   * @param   mixingList
   *          a List containing Heroes or Items
   * @return  a Map grouping Heroes and Items
   */
  protected Map<Boolean, List<Observable>> partitionHeroItem(List<Observable> mixingList) {
    return mixingList.stream().collect(Collectors.partitioningBy(o -> o.isHero()));
  }

  @Override
  public void stepOneFrame() {
    ++timestamp;
    // Hero has higher privilege to spread itrs.
    // Otherwise you cannot rebound energys without getting hurt,
    // since energys can hit you at the same time.
    heroList.forEach(o -> o.spreadItrs(everything));
    itemList.forEach(o -> o.react());

    itemList.forEach(o -> o.spreadItrs(everything));
    Stream.concat(heroList.stream(), itemList.stream()).forEach(o -> {
      o.react();
      o.act();
      pendingQueue.addAll(o.getSpawnedObjectList());
    });

    retainExistingObjects(heroList);
    retainExistingObjects(itemList);
    pendingQueue.removeIf(o -> o == null);
    pendingQueue.forEach(o -> o.setProperty(this));
    Map<Boolean, List<Observable>> groupedMap = partitionHeroItem(pendingQueue);
    heroList.addAll(groupedMap.get(Boolean.TRUE));
    itemList.addAll(groupedMap.get(Boolean.FALSE));
    pendingQueue.clear();
    return;
  }

  protected double calcCameraPos(List<Observable> tracingList, double currentPos) {
    if (tracingList.isEmpty()) {  // default middle
      return (getBoundWidth() - FIELD_WIDTH) / 2.0;
    }
    // Camera moving policy is modified from F.LF.
    double position = tracingList.stream().mapToDouble(o -> o.getPosX()).average().getAsDouble();
    int weight = tracingList.stream().mapToInt(o -> o.getFacing() ? 1 : -1).sum();
    position = weight * WIDTH_DIV24 + position - WIDTH_DIV2;
    position = position < 0.0 ? 0.0 : Math.min(position, getBoundWidth() - FIELD_WIDTH);
    return position < currentPos ? Math.max(currentPos - CAMERA_SPEED_THRESHOLD, position)
                                 : Math.min(currentPos + CAMERA_SPEED_THRESHOLD, position);
  }

}
