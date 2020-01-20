package lfx.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lfx.map.Environment;
import lfx.util.Global;

public class AbstractMap implements Environment {
  public static final double WIDTH = 794;
  public static final double HEIGHT = 550 - 128;
  public static final double NONHERO_ADDITIONAL_WIDTH = 40.0;
  public final double boundWidth;
  public final double boundTop;
  public final double boundBottom;
  private int timestamp = 0;
  private double gravity = 1.7;
  private double friction = 1.0;
  private double dropRate = 1.0 / Global.FPS / 7.0;  // average x seconds per weapon
  private final List<AbstractHero> playerList = new ArrayList<>(8);
  private final List<AbstractObject> objectList = new ArrayList<>(256);
  private final List<AbstractObject> pendingObjectList = new ArrayList<>(64);
  protected final List<AbstractHero> playerView = Collections.unmodifiableList(playerList);
  protected final List<AbstractObject> objectView = Collections.unmodifiableList(objectList);

  protected AbstractField(double boundWidth, double boundTop, double boundBottom) {
    this.boundWidth = boundWidth;
    this.boundTop = boundTop;
    this.boundBottom = boundBottom;
    playerList.addAll(Collections.nCopies(8, AbstractObject.dummy));
  }

  /** Get a valid random z-position on this field. */
  public final double getRandomZ() {
    return Global.randomBounds(boundTop, boundBottom);
  }

  public final void addPlayerHero(AbstractHero hero, int act, int index) {
    hero.initialize(boundWidth * 0.3 + Global.randomBounds(0.0, WIDTH), 0, getRandomZ(), act);
    playerList.set(index, hero);
    objectList.add(hero);
    return;
  }

  /** Usually called from Opoint. The position of object should be set in advance. */
  public final void spawnObjects(List<AbstractObject> objectList) {
    // Temporarily add to a pending list to avoid ConcurrentModificationException.
    synchronized (pendingList) {
      pendingList.addAll(objectList);
    }
    return;
  }

  @Override
  public final double applyFriction(double vx) {
    return vx >= 0.0 ? Math.max(vx - friction, 0.0)
                     : Math.min(vx + friction, 0.0);
  }

  /** Teleportation use. */
  public final Stream<AbstractObject> getHeroStream() {
    return objectList.stream().filter(o -> o.type == Type.HERO);
  }

  /** This method should be called every TimeUnit.
      Hero has higher privilege to spread itrs, or you cannot rebound blasts
      without getting hurt. (Blasts can hit you at same time.)
  */
  public void process() {
    ++timestamp;
    objectList.parallelStream()
              .filter(o -> o.type == Type.HERO)
              .forEach(o -> o.spreadItrArea(timestamp));
    objectList.parallelStream()
              .filter(o -> o.type != Type.HERO)
              .forEach(o -> o.spreadItrArea(timestamp));
    // Respond to itrs and do actions.
    objectList.parallelStream().forEach(o -> o.updateStatus());
    objectList.removeIf(o -> !o.exists());
    objectList.addAll(pendingList);
    pendingList.clear();
    if (Global.randomBounds(0.0, 1.0) >= dropRate)
      pendingList.add(AbstractWeapon);
    return;
  }

}
