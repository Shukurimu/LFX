package component;

import java.util.ArrayList;
import java.util.List;

import base.Point;
import base.Type;
import util.IntMap;
import util.Vector;

/**
 * Define the frame element -- opoint.
 */
public class Opoint extends Point {

  /**
   * The Upper most item's z velocity when created simultaneously with others.
   */
  public static final double Z_RANGE = 5.0;

  /**
   * The relative velocity of newly spawned objects.
   */
  public final Vector velocity;

  /**
   * The initial {@code Action} newly spawned objects will go to.
   */
  public final Action action;

  /**
   * Corresponding object's identifier.
   */
  public final String oid;

  /**
   * Whether new objects should face as same direction as caller or not.
   */
  public final boolean opposideDirection;

  private Opoint(int x, int y, Vector velocity,
                 Action action, String oid, boolean opposideDirection) {
    super(x, y);
    this.velocity = new Vector(velocity.x(), velocity.y(), Z_RANGE);
    this.action = action;
    this.oid = oid;
    this.opposideDirection = opposideDirection;
  }

  /**
   * Returns a {@code List} of newly spawned objects' absolute velocity.
   * The size equals to the amount specified in {@code Opoint} declaration.
   *
   * @param baseVelocity the base velocity of object
   * @return a {@code List} of velocities
   */
  public List<Vector> getInitialVelocities(Vector baseVelocity) {
    return List.of(baseVelocity);
  }

  /**
   * Creates an {@code Opoint} which spawns object to the front.
   *
   * @param x        the x coordinate
   * @param y        the y coordinate
   * @param oid      target object identifier
   * @param velocity initial velocity (z value will be neglected)
   * @param action   initial {@code Action}
   * @param amount   the amount of objects to spawn
   * @return an {@code Opoint} instance
   * @throws IllegalArgumentException if amount is non positive
   */
  public static Opoint front(int x, int y, String oid, Vector velocity,
                             Action action, int amount) {
    if (amount < 1) {
      throw new IllegalArgumentException("amount " + amount);
    } else if (amount == 1) {
      return new Opoint(x, y, velocity, action, oid, false);
    } else {
      return new OpointN(x, y, velocity, amount, action, oid, false);
    }
  }

  /**
   * Creates an {@code Opoint} which spawns object to the back.
   *
   * @param x        the x coordinate
   * @param y        the y coordinate
   * @param oid      target object identifier
   * @param velocity initial velocity (z value will be neglected)
   * @param action   initial {@code Action}
   * @param amount   the amount of objects to spawn
   * @return an {@code Opoint} instance
   * @throws IllegalArgumentException if amount is non positive
   */
  public static Opoint back(int x, int y, String oid, Vector velocity,
                            Action action, int amount) {
    if (amount < 1) {
      throw new IllegalArgumentException("amount " + amount);
    } else if (amount == 1) {
      return new Opoint(x, y, velocity, action, oid, true);
    } else {
      return new OpointN(x, y, velocity, amount, action, oid, true);
    }
  }

  public static Opoint hold(int x, int y, String oid, Action action) {
    throw new UnsupportedOperationException();
  }

  /**
   * Multishot version {@code Opoint}.
   * The logic is slightly complicated when dealing with z velocity.
   */
  private static class OpointN extends Opoint {
    final int amount;

    OpointN(int x, int y, Vector velocity, int amount,
            Action action, String oid, boolean opposideDirection) {
      super(x, y, velocity, action, oid, opposideDirection);
      this.amount = amount;
    }

    @Override
    public List<Vector> getInitialVelocities(Vector baseVelocity) {
      List<Vector> result = new ArrayList<>(amount);
      for (int i = 0; i < amount; ++i) {
        double newVz = baseVelocity.z() + Z_RANGE * (2.0 / amount * i - 1.0);
        double newVx = Vector.findComponent(baseVelocity.z(), baseVelocity.x(), newVz);
        result.add(new Vector(newVx, baseVelocity.y(), newVz));
      }
      return result;
    }

  }

  // TODO: CREATE_ARMOUR
  // https://www.lf-empire.de/en/lf2-empire/data-changing/reference-pages/183-effect-3-hitfa
  // hit_Fa:  Effect:
  public static final String JanAngel = "JanAngel";
  //      5   Creates Jan's Healing Angels in a ratio 1:1 for teammates.
  public static final String JanDevil = "JanDevil";
  //      6   Creates Jan's Flying Devils in a ratio 1:1 for enemies. The upper limit is 7.
  public static final String BatBat = "BatBat";
  //      8   Creates three bats for up to four players. While playing against more
  //          enemies, it'll create one additional bat for every two enemies beyond the
  //          first four. Chases after a random target.
  public static final String FirzenDisaster = "FirzenDisaster";
  //      9   Creates four Ice-/Fire-balls if you are playing against four or less
  //          enemies. When playing against more enemies, it activate the balls in a ratio
  //          of 1:1 for each additional enemy. The upper limit is 10 balls (the number of
  //          fire- or ice-balls may be random, their sum is capped, however).
  public static final String FirzenVolcano = "FirzenVolcano";
  //     11   Creates Firzen's Explosion: id211 frame 109 explode & frame 50 groundfire,
  //          id212 frame 100 icicles, id 221 frame 81 overwhelming disaster.
  // no dedicated for this.
  //     13   Creates Skull-Blast that chases after a random target.


  // ==================== Parser Utility ====================

  /**
   * Extracts and prepares {@code Opoint} setting.
   *
   * @param data a map containing key-value pairs
   * @return a statement to create an {@code Opoint}
   * @throws IllegalArgumentException for invalid kind
   */
  public static String extract(IntMap data) {
    String oid = Type.getIdentifier(data.pop("oid"));
    int x = data.pop("x");
    int y = data.pop("y");
    String action = Action.processGoto(data.pop("action"));
    switch (data.pop("kind")) {
      case 1:
        break;
      case 2:
        return "Opoint.hold(%d, %d, \"%s\", %s)".formatted(x, y, oid, action);
      default:
        throw new IllegalArgumentException("kind");
    }
    int facing = data.pop("facing");
    String method = facing % 2 == 0 ? "Opoint.front" : "Opoint.back";
    return "%d(%d, %d, \"%s\", Vector.of(%d, %d), %s, %d)".formatted(
        method, x, y, oid, data.pop("dvx"), data.pop("dvy"),
        action, Math.max(1, facing / 10)
    );
  }

}
