package lfx.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.scene.layout.Pane;
import javafx.scene.Node;
import lfx.map.Field;
import lfx.object.Hero;
import lfx.object.Observable;
import lfx.util.Const;
import lfx.util.Point;
import lfx.util.Util;

public class BaseMap implements Field {
  public final double boundWidth;
  public final double boundTop;
  public final double boundBottom;
  private final List<Observable>    heroList = new ArrayList<>(64);
  private final List<Observable>  weaponList = new ArrayList<>(128);
  private final List<Observable>  energyList = new ArrayList<>(512);
  private final List<Observable>   heroQueue = new ArrayList<>(8);
  private final List<Observable> weaponQueue = new ArrayList<>(16);
  private final List<Observable> energyQueue = new ArrayList<>(64);
  private final List<Observable>    heroView = Collections.unmodifiableList(heroList);
  private final List<Observable>  weaponView = Collections.unmodifiableList(weaponList);
  private final List<Observable>  energyView = Collections.unmodifiableList(energyList);
  private int independentTeamId = -Const.TEAM_NUM;  // reserve for manual selection
  protected boolean unlimitedMode = false;
  protected double friction = 1.0;
  protected double gravity = 1.7;
  protected int timestamp = 0;
  protected final List<Double> zBound;
  protected final List<Double> heroXBound;
  protected final List<Double> itemXBound;
  protected final List<Node> fxNodeList;
  protected final Pane screenPane = new Pane();

  public BaseMap(double boundWidth, double boundTop, double boundBottom) {
    this.boundWidth = boundWidth;
    this.boundTop = boundTop;
    this.boundBottom = boundBottom;
    zBound = List.of(boundBottom, boundTop);
    heroXBound = List.of(boundWidth, 0.0);
    itemXBound = List.of(boundWidth + ITEM_ADDITIONAL_WIDTH, -ITEM_ADDITIONAL_WIDTH);

    Pane fxNodeLayer = new Pane();
    fxNodeList = fxNodeLayer.getChildren();
    screenPane.setMaxSize(Const.FIELD_WIDTH, Const.FIELD_HEIGHT);
    screenPane.setMinSize(Const.FIELD_WIDTH, Const.FIELD_HEIGHT);
    screenPane.getChildren().add(fxNodeLayer);
  }

  @Override
  public Pane getScreenPane() {
    return screenPane;
  }

  @Override
  public List<Node> getFxNodeList() {
    return fxNodeList;
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
    return --independentTeamId;
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

  public List<Observable> getHeroView() {
    return heroView;
  }

  public void spawnHero(List<Observable> objectList) {
    synchronized (heroQueue) {
      heroQueue.addAll(objectList);
    }
    return;
  }

  public void spawnWeapon(List<Observable> objectList) {
    synchronized (weaponQueue) {
      weaponQueue.addAll(objectList);
    }
    return;
  }

  public void spawnEnergy(List<Observable> objectList) {
    synchronized (energyQueue) {
      energyQueue.addAll(objectList);
    }
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
    targetQueue.forEach(o -> fxNodeList.add(o.getVisualNode().getFxNode()));
    targetQueue.clear();
    return;
  }

  @Override
  public void stepOneFrame() {
    ++timestamp;
    if (Util.randomBounds(0.0, 1.0) >= DROP_PROBABILITY) {
      System.out.println("Drop a random weapon.");
    }
    List<Observable> everything = Stream.of(heroList, weaponList, energyList)
                                        .flatMap(Collection::stream)
                                        .collect(Collectors.toUnmodifiableList());
    /** Hero has higher privilege to spread itrs, or you cannot rebound energys
        without getting hurt, since energys can hit you at the same time. */
    heroList.parallelStream().forEach(o -> o.spreadItrs(everything));
    weaponList.parallelStream().forEach(o -> o.spreadItrs(everything));
    energyList.parallelStream().forEach(o -> o.spreadItrs(everything));
    everything.parallelStream().forEach(o -> o.react());

    updateObservableList(heroList, heroQueue);
    updateObservableList(weaponList, weaponQueue);
    updateObservableList(energyList, energyQueue);
    fxNodeList.removeIf(o -> !o.isVisible());
    return;
  }

  @Override
  public double calculateViewpoint(List<Hero> tracingList, double origin) {
    // Camera movement policy is modified from F.LF.
    double position = 0.0;
    int weight = 0;
    for (Hero hero : tracingList) {
      Point point = hero.getViewpoint();
      position += point.x;
      weight += point.y;
    }
    position = weight * Const.WIDTH_DIV24 + position / tracingList.size() - Const.WIDTH_DIV2;
    position = Util.clamp(position, boundWidth - Const.FIELD_WIDTH, 0.0);
    double speed = (position - origin) * Const.CAMERA_SPEED_FACTOR;
    return Math.abs(speed) < Const.CAMERA_SPEED_THRESHOLD ? position : (origin + speed);
  }

}
