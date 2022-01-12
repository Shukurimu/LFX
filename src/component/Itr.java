package component;

import base.Region;
import base.Scope;
import base.Type;
import util.IntMap;

/**
 * Define the frame element -- itr.
 * https://lf-empire.de/lf2-empire/param-changing/frame-elements/174-itr-interaction?showall=1
 * https://lf-empire.de/lf2-empire/param-changing/reference-pages/181-effects
 */
public class Itr {
  public static final Itr NULL_ITR = new Itr(Kind.PUNCH, Region.EMPTY, 0, 0, null);
  // https://lf-empire.de/forum/showthread.php?tid=8740
  public static final int DEFAULT_DAMAGE_PAUSE = 3;
  public static final int DEFAULT_DEFEND_PAUSE = 5;
  public static final int DEFAULT_DVY = -7;
  public static final Grab COMMON_WALKING_GRAB = new Grab(Action.HERO_CATCH, Action.HERO_CAUGHT);

  public enum Kind {
    PUNCH           (Damage.class),  // kind: 0  effect: 0
    STAB            (Damage.class),  // kind: 0  effect: 1
    FIRE            (Damage.class),  // kind: 0  effect: 2
    WEAK_FIRE       (Damage.class),  // kind: 0  effect: 20.21
    ICE             (Damage.class),  // kind: 0  effect: 3
    WEAK_ICE        (Damage.class),  // kind: 0  effect: 30
    THROWN_DAMAGE   (Damage.class),   // kind: 4
    WEAPON_STRENGTH (Damage.class),   // kind: 5
    SHIELD          (Damage.class),   // kind: 9
    GRAB_DOP        (Grab.class),     // kind: 1
    GRAB_BDY        (Grab.class),     // kind: 3
    FORCE_ACTION    (Integer.class),  // kind: 6
    PICK            (Integer.class),  // kind: 2
    ROLL_PICK       (Integer.class),  // kind: 7
    HEAL            (Integer.class),  // kind: 8
    SONATA          (Integer.class),  // kind: 10
    BLOCK           (Integer.class),  // kind: 14
    VORTEX          (Integer.class);  // kind: 15

    /**
     * The data type this {@code Kind} expecting.
     */
    public final Class<?> paramClass;

    private Kind(Class<?> paramClass) {
      this.paramClass = paramClass;
    }

    @Override
    public String toString() {
      return String.join(
          ".", getDeclaringClass().getEnclosingClass().getSimpleName(),
          getDeclaringClass().getSimpleName(), name());
    }

  }

  public static record Damage(
      int dvx, int dvy,
      int fall, int bdefend, int injury,
      boolean explosive, int actPause) {

    public double calcDvx(boolean sourceFaceRight) {
      return sourceFaceRight ? dvx : -dvx;
    }

  }

  public static Damage attack(
      int dvx, int dvy, int fall, int bdefend, int injury) {
    return new Damage(dvx, dvy, fall, bdefend, injury, false, DEFAULT_DAMAGE_PAUSE);
  }

  public static Damage explosion(
      int dvx, int dvy, int fall, int bdefend, int injury) {
    return new Damage(dvx, dvy, fall, bdefend, injury, true, DEFAULT_DAMAGE_PAUSE);
  }

  public static Damage smoothAttack(
      int dvx, int dvy, int fall, int bdefend, int injury) {
    return new Damage(dvx, dvy, fall, bdefend, injury, false, 0);
  }

  public static Damage smoothExplosion(
      int dvx, int dvy, int fall, int bdefend, int injury) {
    return new Damage(dvx, dvy, fall, bdefend, injury, true, 0);
  }

  public static record Grab(Action caughtingact, Action caughtact) {}

  public static Grab grab(int caughtingact, int caughtact) {
    if (Action.HERO_CATCH.index == caughtingact &&
        Action.HERO_CAUGHT.index == caughtact) {
      return COMMON_WALKING_GRAB;
    } else {
      return new Grab(Action.of(caughtingact), Action.of(caughtact));
    }
  }

  /**
   * The {@code Kind} of this {@code Itr}.
   */
  public final Kind kind;

  /**
   * A relative {@code Region} representing x, y, w, h, zwidth.
   */
  public final Region relative;

  /**
   * The scope of this {@code Itr} that can interact with some {@code Bdy}.
   */
  public final int scope;

  /**
   * A negative value means arest.
   */
  public final int vrest;

  /**
   * Due to the variety of {@code Itr}, an {@code Object} is used.
   * The underlying logics vary by {@code Kind}.
   */
  public final Object param;

  private Itr(Kind kind, Region relative, int scope, int vrest, Object param) {
    this.kind = kind;
    this.relative = relative;
    this.scope = scope;
    this.vrest = vrest;
    this.param = param;
  }

  public static Itr of(Kind kind, Region relative, int scope, int vrest, Object param) {
    if (vrest == 0) {
      throw new IllegalArgumentException("vrest cannot be zero.");
    }
    if (!kind.paramClass.isInstance(param)) {
      throw new IllegalArgumentException(String.format(
          "Kind %s expects param of class %s, but %s is found.",
          kind, kind.paramClass, param.getClass()
      ));
    }
    return new Itr(kind, relative, scope, vrest, param);
  }

  public static Itr of(Kind kind, Region relative, int scope, int vrest) {
    return of(kind, relative, scope, vrest, Integer.valueOf(0));
  }

  public boolean interactsWith(Bdy bdy, int bdyScopeView) {
    if ((bdy.attributes & Bdy.FRIENDLY_FIREABLE) != 0) {
      bdyScopeView |= Scope.getTeammateView(bdyScopeView);
    }
    return (bdyScopeView & scope) != 0;
  }

  @Override
  public String toString() {
    return String.format("Itr[%s, %s, scope=%x, vrest=%d]", kind, relative, scope, vrest);
  }

  // ==================== Parser Utility ====================

  private static String grabParam(IntMap data) {
    int caughtingact = data.pop("catchingact");
    int caughtact = data.pop("caughtact");
    if (Action.HERO_CATCH.index == caughtingact && Action.HERO_CAUGHT.index == caughtact) {
      return "Itr.COMMON_WALKING_GRAB";
    } else {
      return "Itr.grab(%d, %d)".formatted(caughtingact, caughtact);
    }
  }

  private static String damageParam(IntMap data, boolean explosive, boolean noPause) {
    String method = switch ((explosive ? 2 : 0) | (noPause ? 1 : 0)) {
      case 0b00 -> "Itr.attack";
      case 0b01 -> "Itr.smoothAttack";
      case 0b10 -> "Itr.explosion";
      case 0b11 -> "Itr.smoothExplosion";
      default -> "impossible";
    };
    return "%s(%d, %d, %d, %d, %d)".formatted(
        method, data.pop("dvx", 0), data.pop("dvy", -7),
        data.pop("fall", 20), data.pop("bdefend", 0), data.pop("injury", 0));
  }

  private static String extractGivenRegion(IntMap data, int rawState, Type type, String region) {
    String scope = "DEFAULT_ITR_SCOPE";  // defined in BaseXXX
    String param = null;
    Kind kind = switch (data.pop("kind")) {
      case 0 -> {
        int effect = data.pop("effect", 0);
        param = damageParam(data, effect == 22 || effect == 23, false);
        yield switch (effect) {
          case 0 -> Kind.PUNCH;
          case 1 -> Kind.STAB;
          case 2 -> rawState == 19 ? Kind.WEAK_FIRE : Kind.FIRE;
          case 3 -> Kind.ICE;
          case 4 -> {
            scope = "Scope.ALL_NON_HERO";
            yield Kind.PUNCH;
          }
          case 20 -> {
            scope = "Scope.ALL_HERO";
            yield Kind.WEAK_FIRE;
          }
          case 21 -> Kind.WEAK_FIRE;
          case 22 -> Kind.FIRE;
          case 23 -> Kind.PUNCH;
          case 30 -> Kind.WEAK_ICE;
          default -> throw new IllegalArgumentException("effect");
        };
      }
      case 4 -> {
        scope = "Scope.TEAMMATE_HERO";
        param = damageParam(data, false, false);
        yield Kind.THROWN_DAMAGE;
      }
      case 5 -> {
        param = damageParam(data, false, false);
        yield Kind.WEAPON_STRENGTH;
      }
      case 9 -> {
        param = damageParam(data, false, false);
        yield Kind.SHIELD;
      }
      case 16 -> {
        scope = "Scope.ENEMY_HERO";
        param = damageParam(data, false, false);
        yield Kind.ICE;
      }
      case 1 -> {
        scope = "Scope.ENEMY_HERO";
        param = grabParam(data);
        yield Kind.GRAB_DOP;
      }
      case 3 -> {
        scope = "Scope.ENEMY_HERO";
        param = grabParam(data);
        yield Kind.GRAB_BDY;
      }
      case 2 -> {
        scope = "Scope.ALL_WEAPON";
        yield Kind.PICK;
      }
      case 7 -> {
        scope = "Scope.ALL_WEAPON";
        yield Kind.ROLL_PICK;
      }
      case 6 -> {
        scope = "Scope.ENEMY_HERO";
        yield Kind.FORCE_ACTION;
      }
      case 8 -> {
        scope = "Scope.ALL_HERO";
        param = Integer.toString(data.pop("dvx"));
        yield Kind.HEAL;
      }
      case 10, 11 -> {
        scope = "Scope.ENEMY_HERO | Scope.ALL_WEAPON";
        yield Kind.SONATA;
      }
      case 14 -> {
        scope = "Scope.EVERYTHING";
        yield Kind.BLOCK;
      }
      case 15 -> {
        scope = "Scope.COMMON_HITTABLE";
        yield Kind.VORTEX;
      }
      default -> throw new IllegalArgumentException("kind");
    };

    int vrest = type.isHero ? -7 : 9;
    int rawVrest = data.pop("vrest", 0);
    if (rawVrest != 0) {
      vrest = rawVrest;
    }
    int rawArest = data.pop("arest", 0);
    if (rawArest != 0) {
      vrest = -rawArest;
    }

    if (param == null) {
      return "Itr.of(%s, %s, %s, %d)".formatted(kind, region, scope, vrest);
    } else {
      return "Itr.of(%s, %s, %s, %d, %s)".formatted(kind, region, scope, vrest, param);
    }
  }

  /**
   * Extracts and prepares {@code Itr} setting.
   *
   * @param data     a map containing key-value pairs
   * @param rawState original state of the enclosing frame
   * @param type     the {@code Type} of the owner of enclosing frame
   * @return a statement to create an {@code Itr}
   * @throws IllegalArgumentException for invalid kind
   */
  public static String extract(IntMap data, int rawState, Type type) {
    return extractGivenRegion(data, rawState, type, Region.extract(data));
  }

  /**
   * Extracts and prepares {@code Itr} setting for Weapon's strength.
   *
   * @param data a map containing key-value pairs
   * @return a statement to create an {@code Itr}
   * @throws IllegalArgumentException for invalid kind
   */
  public static String extractStrength(IntMap data) {
    return extractGivenRegion(data, 0, Type.LIGHT, "Region.EMPTY");
  }

}
