package component;

import base.Point;
import util.IntMap;
import util.Vector;

/**
 * Define the frame element -- wpoint.
 */
public class Wpoint extends Point {

  public enum Usage {
    RELEASE (false),
    HOLD    (false),
    NORMAL   (true),
    JUMP    (true),
    RUN     (true),
    DASH    (true);

    /**
     * Whether corresponding {@code Weapon} is in a attacking state.
     */
    public boolean attacking;

    private Usage(boolean attacking) {
      this.attacking = attacking;
    }

    @Override
    public String toString() {
      return String.join(
          ".", getDeclaringClass().getEnclosingClass().getSimpleName(),
          getDeclaringClass().getSimpleName(), name());
    }

  }

  public final Usage usage;
  public final Action weaponact;
  public final Vector velocity;
  public final boolean cover;

  private Wpoint(int x, int y, Usage usage, Action weaponact, Vector velocity, int cover) {
    super(x, y);
    this.usage = usage;
    this.weaponact = weaponact;
    this.velocity = velocity;
    this.cover = cover == 0;
  }

  /**
   * Creates a {@code Wpoint} to hold a {@code Weapon}.
   *
   * @param x         the x coordinate
   * @param y         the y coordinate
   * @param weaponact {@code Action} of the item
   * @param cover     {@code true} if the {@code Weapon} should be rendered behind
   * @return a {@code Wpoint} instance
   */
  public static Wpoint hold(int x, int y, Action weaponact, int cover) {
    return new Wpoint(x, y, Usage.HOLD, weaponact, Vector.ZERO, cover);
  }

  /**
   * Creates a {@code Wpoint} to attack with a {@code Weapon}.
   *
   * @param x         the x coordinate
   * @param y         the y coordinate
   * @param weaponact {@code Action} of the item
   * @param cover     {@code true} if the {@code Weapon} should be rendered behind
   * @param usage     {@code Usage} type
   * @return a {@code Wpoint} instance
   */
  public static Wpoint attack(int x, int y, Action weaponact, int cover, Usage usage) {
    if (!usage.attacking) {
      throw new IllegalArgumentException(usage.toString());
    }
    return new Wpoint(x, y, usage, weaponact, Vector.ZERO, cover);
  }

  /**
   * Creates a {@code Wpoint} to release a {@code Weapon}.
   * If the given {@code velocity} is not {@code Vector.ZERO},
   * the {@code Weapon} will be in throwing state.
   *
   * @param x         the x coordinate
   * @param y         the y coordinate
   * @param weaponact {@code Action} of the item
   * @param velocity  initial released velocity
   * @param cover     {@code true} if the {@code Weapon} should be rendered behind
   * @return a {@code Wpoint} instance
   */
  public static Wpoint release(int x, int y, Action weaponact, Vector velocity, int cover) {
    return new Wpoint(x, y, Usage.RELEASE, weaponact, velocity, cover);
  }

  /**
   * Creates a {@code Wpoint} to define position from {@code Weapon} side.
   *
   * @param x the x coordinate
   * @param y the y coordinate
   * @return a {@code Wpoint} instance
   */
  public static Wpoint onHand(int x, int y) {
    return new Wpoint(x, y, Usage.HOLD, Action.UNASSIGNED, Vector.ZERO, 0);
  }

  // ==================== Parser Utility ====================

  /**
   * Extracts and prepares {@code Wpoint} setting.
   *
   * @param data a map containing key-value pairs
   * @return a statement to create a {@code Wpoint}
   * @throws IllegalArgumentException for invalid kind
   */
  public static String extract(IntMap data) {
    int x = data.pop("x");
    int y = data.pop("y");
    int dvx = data.pop("dvx", 0);
    int dvy = data.pop("dvy", 0);
    int dvz = data.pop("dvz", 0);
    int cover = data.pop("cover");
    int attacking = data.pop("attacking", 0);
    String action = Action.processGoto(data.pop("weaponact"));
    switch (data.pop("kind")) {
      case 1:
        break;
      case 2:
        return "Wpoint.onHand(%d, %d)".formatted(x, y);
      case 3:
        return "Wpoint.release(%d, %d, %s, Vector.ZERO, %d)".formatted(x, y, action, cover);
      default:
        throw new IllegalArgumentException("kind");
    }
    if ((dvx | dvy | dvz) != 0) {
      String velocity = "Vector.of(%d, %d, %d)".formatted(dvx, dvy, dvz);
      return "Wpoint.release(%d, %d, %s, %s, %d)".formatted(x, y, action, velocity, cover);
    }
    if (attacking == 0) {
      return "Wpoint.hold(%d, %d, %s, %d)".formatted(x, y, action, cover);
    }
    Usage usage = convertUsage(attacking);
    return "Wpoint.attack(%d, %d, %s, %d, %s)".formatted(x, y, action, cover, usage);
  }

  /**
   * Gets the corresponding {@code Wpoint.Usage} of given attacking value.
   *
   * @param attackingValue used in original LF2
   * @return a {@code Wpoint.Usage} enum
   */
  public static Usage convertUsage(int attackingValue) {
    return switch (attackingValue) {
      case 1 -> Usage.NORMAL;
      case 2 -> Usage.JUMP;
      case 3 -> Usage.RUN;
      case 4 -> Usage.DASH;
      default -> Usage.HOLD;
    };
  }

}
