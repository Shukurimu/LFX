package lfx.map;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.Pane;
import javafx.scene.Node;
import lfx.object.Energy;
import lfx.object.Hero;
import lfx.object.Observable;
import lfx.object.Weapon;
import lfx.util.UnionReadOnlyIterable;

public class AbstractField implements Field {
  protected final double boundWidth;
  protected final double boundTop;
  protected final double boundBottom;
  private final Pane visualNodePane = new Pane();
  private final List<Node> visualNodeList = visualNodePane.getChildren();
  private final List<Observable>    heroList = new ArrayList<>(64);
  private final List<Observable>  weaponList = new ArrayList<>(128);
  private final List<Observable>  energyList = new ArrayList<>(512);
  private final List<Observable>   heroQueue = new ArrayList<>(8);
  private final List<Observable> weaponQueue = new ArrayList<>(16);
  private final List<Observable> energyQueue = new ArrayList<>(64);
  private final Iterable<Observable> heroView = new UnionReadOnlyIterable<>(heroList);
  private final Iterable<Observable> everything = new UnionReadOnlyIterable<>(
      heroList, weaponList, energyList
  );
  private int independentTeamId = 16;  // Reserve for pre-defined teams.
  private boolean unlimitedMode = false;
  protected double friction = 1.0;
  protected double gravity = 1.7;
  protected int timestamp = 0;
  protected List<Double> zBound;
  protected List<Double> heroXBound;
  protected List<Double> itemXBound;

  public AbstractField(double boundWidth, double boundTop, double boundBottom) {
    this.boundWidth = boundWidth;
    this.boundTop = boundTop;
    this.boundBottom = boundBottom;
    zBound = List.of(boundBottom, boundTop);
    heroXBound = List.of(boundWidth, 0.0);
    itemXBound = List.of(boundWidth + ITEM_ADDITIONAL_WIDTH, -ITEM_ADDITIONAL_WIDTH);
  }

  @Override
  public double getBoundWidth() {
    return boundWidth;
  }

  @Override
  public Pane getVisualNodePane() {
    return visualNodePane;
  }

  @Override
  public int getObjectCount() {
    return visualNodeList.size();
  }

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

  public void spawnObject(Observable object) {
    if (object instanceof Energy) {
      energyQueue.add(object);
      return;
    }
    if (object instanceof Weapon) {
      weaponQueue.add(object);
      return;
    }
    heroQueue.add(object);
    return;
  }

  @Override
  public boolean switchUnlimitedMode() {
    return unlimitedMode ^= true;
  }

  @Override
  public void reviveAll() {
    System.out.println("reviveAll()");
    return;
  }

  @Override
  public void dropNeutralWeapons() {
    System.out.println("dropNeutralWeapons()");
    return;
  }

  @Override
  public void destroyWeapons() {
    System.out.println("destroyWeapons()");
    return;
  }

  @Override
  public void disperseEnergies() {
    System.out.println("disperseEnergies()");
    return;
  }

  protected void updateObservableList(List<Observable> targetList,
                                      List<Observable> targetQueue) {
    targetList.forEach(o -> o.act());
    targetList.removeIf(o -> !o.exists());
    targetList.addAll(targetQueue);
    targetQueue.forEach(o -> visualNodeList.add(o.getViewer().getFxNode()));
    targetQueue.clear();
    return;
  }

  @Override
  public void stepOneFrame() {
    ++timestamp;
    /** Hero has higher privilege to spread itrs, or you cannot rebound energys
        without getting hurt, since energys can hit you at the same time. */
    heroList.forEach(o -> o.spreadItrs(everything));
    weaponList.forEach(o -> o.react());
    energyList.forEach(o -> o.react());

    weaponList.forEach(o -> o.spreadItrs(everything));
    energyList.forEach(o -> o.spreadItrs(everything));

    for (Observable o : everything) {
      o.react();
      o.act();
      for (Observable s : o.getSpawnedObjectList()) {
        spawnObject(s);
      }
    }

    updateObservableList(heroList, heroQueue);
    updateObservableList(weaponList, weaponQueue);
    updateObservableList(energyList, energyQueue);
    visualNodeList.removeIf(o -> !o.isVisible());
    return;
  }

}
