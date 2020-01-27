package lfx.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.List;
import lfx.map.Environment;
import lfx.object.Observable;
import lfx.util.Const;
import lfx.util.Util;

public class BaseMap implements Environment {
  public final double boundWidth;
  public final double boundTop;
  public final double boundBottom;
  protected boolean unlimitedMode = false;
  protected double friction = 1.0;
  protected double gravity = 1.7;
  protected int timestamp = 0;
  protected List<Double> zBound;
  protected List<Double> heroXBound;
  protected List<Double> itemXBound;
  private int independentTeamId = -Const.TEAM_NUM;  // reserve for manual selection
  private final List<Observable>    heroList = new ArrayList<>(64);
  private final List<Observable>  weaponList = new ArrayList<>(128);
  private final List<Observable>  energyList = new ArrayList<>(512);
  private final List<Observable>   heroQueue = new ArrayList<>(8);
  private final List<Observable> weaponQueue = new ArrayList<>(16);
  private final List<Observable> energyQueue = new ArrayList<>(64);
  private final List<Observable>    heroView = Collections.unmodifiableList(heroList);
  private final List<Observable>  weaponView = Collections.unmodifiableList(weaponList);
  private final List<Observable>  energyView = Collections.unmodifiableList(energyList);

  protected BaseMap(double boundWidth, double boundTop, double boundBottom) {
    this.boundWidth = boundWidth;
    this.boundTop = boundTop;
    this.boundBottom = boundBottom;
    zBound = List.of(boundBottom, boundTop);
    heroXBound = List.of(boundWidth, 0.0);
    itemXBound = List.of(boundWidth + ITEM_ADDITIONAL_WIDTH, -ITEM_ADDITIONAL_WIDTH);
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

  @Override
  public List<Observable> getHeroView() {
    return heroView;
  }

  @Override
  public void spawnHero(List<Observable> objectList) {
    synchronized (heroQueue) {
      heroQueue.addAll(objectList);
    }
    return;
  }

  @Override
  public void spawnWeapon(List<Observable> objectList) {
    synchronized (weaponQueue) {
      weaponQueue.addAll(objectList);
    }
    return;
  }

  @Override
  public void spawnEnergy(List<Observable> objectList) {
    synchronized (energyQueue) {
      energyQueue.addAll(objectList);
    }
    return;
  }

  protected boolean switchUnlimitedMode() {
    return unlimitedMode ^= true;
  }

  protected void reviveAll() {
    System.out.println("reviveAll()");
    return;
  }

  protected void dropNeutralWeapons() {
    System.out.println("dropNeutralWeapons()");
    return;
  }

  protected void destroyWeapons() {
    System.out.println("destroyWeapons()");
    return;
  }

  protected void disperseEnergies() {
    System.out.println("disperseEnergies()");
    return;
  }

  protected void updateObservableList(List<Observable> targetList,
                                      List<Observable> targetQueue) {
    targetList.forEach(o -> o.move());
    targetList.removeIf(o -> !o.exists());
    targetList.addAll(targetQueue);
    targetQueue.forEach(o -> fxNodeList.add(o.getVisualNode()));
    targetQueue.clear();
    return;
  }

  protected void stepTimeunit() {
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

}
