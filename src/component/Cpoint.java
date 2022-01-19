package component;

import java.util.ArrayList;
import java.util.Objects;

import base.Point;
import base.Vector;
import util.IntMap;

/**
 * Define the frame element -- cpoint.
 * Since {@code catch} and {@code throw} are keywords in Java,
 * the terms are substituted by "Grab" in this implementation instead.
 * https://lf-empire.de/lf2-empire/data-changing/frame-elements/177-cpoint-catch-point?showall=1
 */
public class Cpoint extends Point {

  /**
   * From where the timer starts to countdown.
   * This value reduces by decrease field every timestamp.
   * Reaching zero is a reason to drop victim in most cases.
   */
  public static final int GRABBING_TIME = 305;

  /**
   * Indicates the victim should face as same direction as grabber.
   */
  public static final int SAME_FACING = 0b1;

  /**
   * Indicates the victim will be rendered behind the grabber.
   */
  public static final int COVER       = 0b10;

  /**
   * Indicates grabber is able to change direction.
   */
  public static final int DIRCONTROL  = 0b100;

  /**
   * Indicates the victim will not be hurt by other damage source.
   */
  public static final int UNHURTABLE  = 0b1000;

  /**
   * Intentionally drop the target.
   * 1. Countdown timer reaches zero.
   * 2. Perform transformation.
   */
  public static final Cpoint DROP =
      new Cpoint(0, 0, Action.HERO_FACEUP_FALL2, Action.UNASSIGNED, 0, 0, Vector.of(8, -3), 0);

  /**
   * Unintentionally release the target.
   * It is usually caused by going to a frame having no {@code Cpoint}.
   * For example, (actively) do a combo and (passively) hit by others.
   */
  public static final Cpoint RELEASE =
      new Cpoint(0, 0, Action.DEFAULT, Action.UNASSIGNED, 0, 0, Vector.ZERO, 0);

  public final Action vAction;
  public final Action tAction;
  public final int decrease;
  public final int injury;  // also throwinjury
  public final Vector velocity;
  public final boolean opposideFacing;
  public final boolean cover;
  public final boolean dircontrol;
  public final boolean hurtable;
  public final Action frontHurtAction;
  public final Action backHurtAction;

  /** Constructor for grabber. */
  private Cpoint(int x, int y, Action vAction, Action tAction,
                 int decrease, int injury, Vector velocity, int properties) {
    super(x, y);
    this.vAction = vAction;
    this.tAction = tAction;
    this.decrease = decrease;
    this.injury = injury;
    this.velocity = velocity;
    opposideFacing = (properties & SAME_FACING) == 0;
    cover = (properties & COVER) != 0;
    dircontrol = (properties & DIRCONTROL) != 0;
    hurtable = (properties & UNHURTABLE) == 0;
    frontHurtAction = backHurtAction = Action.UNASSIGNED;
  }

  /**
   * Creates a common use {@code Cpoint} instance.
   *
   * @param x          the x coordinate
   * @param y          the y coordinate
   * @param vAction    the victim's {@code Action}
   * @param decrease   countdown timer decreasing value
   * @param injury     damage to the victim
   * @param properties additional properties
   * @return a {@code Cpoint} instance
   */
  public static Cpoint grab(int x, int y, Action vAction,
                            int decrease, int injury, int properties) {
    return new Cpoint(x, y, vAction, Action.UNASSIGNED, decrease, injury, Vector.ZERO, properties);
  }

  /**
   * Creates a {@code Cpoint} instance accepting Left/Right + Attack order.
   * It usually used for throwing the victim.
   *
   * @param x          the x coordinate
   * @param y          the y coordinate
   * @param vAction    the victim's {@code Action}
   * @param tAction    the goto {@code Action} when pressing L/R + A
   * @param decrease   countdown timer decreasing value
   * @param injury     damage to the victim
   * @param properties additional properties
   * @return a {@code Cpoint} instance
   */
  public static Cpoint grab(int x, int y, Action vAction, Action tAction,
                            int decrease, int injury, int properties) {
    return new Cpoint(x, y, vAction, tAction, decrease, injury, Vector.ZERO, properties);
  }

  /**
   * Creates a {@code Cpoint} instance to throw the victim.
   *
   * @param x           the x coordinate
   * @param y           the y coordinate
   * @param vAction     the victim's {@code Action}
   * @param throwInjury the throwing damage
   * @param velocity    the throwing velocity
   * @return a {@code Cpoint} instance
   * @throws NullPointerException if {@code velocity} is {@code null}
   */
  public static Cpoint doThrow(int x, int y, Action vAction, int throwInjury, Vector velocity) {
    Objects.requireNonNull(velocity);
    return new Cpoint(x, y, vAction, Action.UNASSIGNED, 0, throwInjury, velocity, 0);
  }

  /** Constructor for victim. */
  private Cpoint(int x, int y, Action frontHurtAction, Action backHurtAction, boolean hurtable) {
    super(x, y);
    this.frontHurtAction = frontHurtAction;
    this.backHurtAction = backHurtAction;
    vAction = tAction = Action.UNASSIGNED;
    decrease = injury = 0;
    velocity = Vector.ZERO;
    this.hurtable = hurtable;
    opposideFacing = dircontrol = cover = false;
  }

  /**
   * Creates a {@code Cpoint} instance for a victim.
   *
   * @param x               the x coordinate
   * @param y               the y coordinate
   * @param frontHurtAction reacting {@code Action} when hit from front
   * @param backHurtAction  reacting {@code Action} when hit from back
   * @return a {@code Cpoint} instance
   */
  public static Cpoint grabbed(int x, int y, Action frontHurtAction, Action backHurtAction) {
    return new Cpoint(x, y, frontHurtAction, backHurtAction, true);
  }

  /**
   * Creates a {@code Cpoint} instance for a victim only take damage from grabber.
   *
   * @param x the x coordinate
   * @param y the y coordinate
   * @return a {@code Cpoint} instance
   */
  public static Cpoint grabbed(int x, int y) {
    return new Cpoint(x, y, Action.UNASSIGNED, Action.UNASSIGNED, false);
  }

  // ==================== Parser Utility ====================

  /**
   * Replaces action number to noop if it is equal to -842150451,
   * a number whose meaning is unknown (hex=CDCDCDCD).
   */
  private static int replaceMagicNumber(int rawValue) {
    return rawValue == -842150451 ? 0 : rawValue;
  }

  /**
   * Extracts and prepares {@code Cpoint} setting.
   *
   * @param data a map containing key-value pairs
   * @return a statement to create a {@code Cpoint}
   * @throws IllegalArgumentException for invalid kind
   */
  public static String extract(IntMap data) {
    int x = data.pop("x");
    int y = data.pop("y");
    switch (data.pop("kind")) {
      case 1:
        break;
      case 2: {
        int fronthurtact = data.pop("fronthurtact", -1);
        int backhurtact = data.pop("backhurtact", -1);
        if (fronthurtact == -1 && backhurtact == -1) {
          return "Cpoint.grabbed(%d, %d)".formatted(x, y);
        } else {
          return "Cpoint.grabbed(%d, %d, %s, %s)".formatted(
              x, y, Action.processGoto(fronthurtact), Action.processGoto(backhurtact));
        }
      }
      default:
        throw new IllegalArgumentException("kind");
    }

    ArrayList<String> property = new ArrayList<>();
    int cover = data.pop("cover", 0);
    if (cover / 10 % 2 == 1) {
      property.add("Cpoint.SAME_FACING");
    }
    if (cover % 2 == 1) {
      property.add("Cpoint.COVER");
    }
    if (data.pop("dircontrol", 0) == 1) {
      property.add("Cpoint.DIRCONTROL");
    }
    if (data.pop("hurtable", 0) == 0) {
      property.add("Cpoint.UNHURTABLE");
    }
    String properties = property.isEmpty() ? "0" : String.join(" | ", property);

    String vaction = Action.processGoto(data.pop("vaction"));
    int taction = data.pop("taction", 0);
    int decrease = data.pop("decrease", 0);
    int injury = data.pop("injury", 0);
    int throwvx = replaceMagicNumber(data.pop("throwvx", 0));
    int throwvy = replaceMagicNumber(data.pop("throwvy", 0));
    int throwvz = replaceMagicNumber(data.pop("throwvz", 0));
    int throwinjury = replaceMagicNumber(data.pop("throwinjury", 0));

    if (throwinjury == -1) {
      throw new UnsupportedOperationException("throwinjury: -1");
    }
    if ((throwinjury | throwvx | throwvy | throwvz) != 0) {
      return "Cpoint.doThrow(%d, %d, %s, %d, Vector.of(%d, %d, %d))"
          .formatted(x, y, vaction, throwinjury, throwvx, throwvy, throwvz);
    } else if (taction == 0) {
      return "Cpoint.grab(%d, %d, %s, %d, %d, %s)"
          .formatted(x, y, vaction, decrease, injury, properties);
    } else {
      return "Cpoint.grab(%d, %d, %s, %s, %d, %d, %s)"
          .formatted(x, y, vaction, Action.processGoto(taction), decrease, injury, properties);
    }
  }

}
