package component;

import java.util.Random;

/**
 * Define frame connection -- next & hit_xx.
 */
public class Action {

  /**
   * Maximum amount of {@code Action} allowed.
   */
  public static final int ACTION_LIMIT = 400;

  /**
   * Internal cache.
   */
  private static final Action[] cache = new Action[ACTION_LIMIT];

  /**
   * Follows the rule of {@code wait} - {@code next} flow.
   */
  public static final Action UNASSIGNED = new Action(Integer.MAX_VALUE, false);

  /**
   * The object will do the same {@code Action} in next timestamp.
   */
  public static final Action REPEAT = new Action(0, false);

  /**
   * Asks the object to perform a proper {@code Action}.
   * The result varies by {@code Type} and {@code State}.
   */
  public static final Action DEFAULT = new Action(999, false);

  /**
   * Same as {@code Action.DEFAULT} expect for changing direction.
   */
  public static final Action DEFAULT_REVERSE = new Action(-999, false);

  /**
   * Indicates the object will be removed from a {@code Field}.
   */
  public static final Action REMOVAL = new Action(1000, false);

  /**
   * The index of this {@code Action} in a {@code Frame} list.
   */
  public final int index;

  /**
   * Whether the object should change facing direction
   * after transiting to this {@code Action}.
   */
  public final boolean changeFacing;

  private Action(int index, boolean changeFacing) {
    this.index = index;
    this.changeFacing = changeFacing;
  }

  /**
   * Returns an {@code Action} instance representing the specified number.
   * Special action number cases are handled.
   *
   * @param value a raw action number
   * @return a proper {@code Action} instance
   * @throws IllegalArgumentException if the given value is invalid
   */
  public static Action of(int value) {
    if (value >= ACTION_LIMIT || value <= -ACTION_LIMIT) {
      throw new IllegalArgumentException("" + value);
    }
    if (value >= 0) {
      if (cache[value] == null) {
        return cache[value] = new Action(value, false);
      } else {
        return cache[value];
      }
    } else {
      return new Action(-value, true);
    }
  }

  /**
   * Checks if this {@code Action} contains the given action number.
   *
   * @param actionNumber to check
   * @return {@code true} if the action number is one of this {@code Action}
   * @throws UnsupportedOperationException on non pre-defined {@code Action}s
   * @see DefinedAction
   */
  public boolean contains(int actionNumber) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns an {@code Action} matching the query.
   * Use cases include hidden frame counter and weapon state change.
   *
   * @param delta change to this {@code Action}
   * @return the targeting {@code Action}
   * @throws UnsupportedOperationException on non pre-defined {@code Action}s
   * @see ReferenceAction
   */
  public Action shift(int delta) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns a randomly selected state of this {@code Action}.
   *
   * @param random the random instance to perform query
   * @return a random {@code Action}
   * @throws UnsupportedOperationException on non pre-defined {@code Action}s
   * @see ReferenceAction
   */
  public Action shift(Random random) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return String.format("Action[%d, changeFacing=%b]", index, changeFacing);
  }

  private static class DefinedAction extends Action {
    protected final int indexTo;

    DefinedAction(int indexFrom, int range) {
      super(indexFrom, false);
      indexTo = indexFrom + range;
    }

    @Override
    public boolean contains(int actionNumber) {
      return indexTo > actionNumber && actionNumber >= index;
    }

    @Override
    public String toString() {
      return String.format("DefinedAction[%d:%d]", index, indexTo);
    }

  }

  private static class ReferenceAction extends DefinedAction {
    protected final Action[] innerStates;

    ReferenceAction(int indexFrom, int range, int innerFrom) {
      super(indexFrom, range);
      innerStates = new Action[range];
      for (int i = 0; i < range; ++i) {
        innerStates[i] = new Action(innerFrom + i, false);
      }
    }

    ReferenceAction(int indexFrom, int range) {
      this(indexFrom, range, indexFrom);
    }

    @Override
    public Action shift(int delta) {
      return innerStates[delta];
    }

    @Override
    public Action shift(Random random) {
      return innerStates[random.nextInt(innerStates.length)];
    }

  }

  // ==================== Hero ====================
  public static final Action HERO_STANDING = new DefinedAction(0, 5);
  public static final Action HERO_WALKING = new ReferenceAction(5, 4);
  public static final Action HERO_RUNNING = new ReferenceAction(9, 3);
  public static final Action HERO_HEAVY_WALK = new ReferenceAction(12, 4);
  public static final Action HERO_HEAVY_RUN = new ReferenceAction(16, 3);
  public static final Action HERO_HEAVY_STOP_RUN = new DefinedAction(19, 1);
  public static final Action HERO_WEAPON_ATK1 = new DefinedAction(20, 5);
  public static final Action HERO_WEAPON_ATK2 = new DefinedAction(25, 5);
  public static final Action HERO_JUMP_WEAPON_ATK = new DefinedAction(30, 5);
  public static final Action HERO_RUN_WEAPON_ATK = new DefinedAction(35, 5);
  public static final Action HERO_DASH_WEAPON_ATK = new DefinedAction(40, 5);
  public static final Action HERO_LIGHT_WEAPON_THROW = new DefinedAction(45, 5);
  public static final Action HERO_HEAVY_WEAPON_THROW = new DefinedAction(50, 2);
  public static final Action HERO_SKY_WEAPON_THROW = new DefinedAction(52, 3);
  public static final Action HERO_DRINK = new DefinedAction(55, 5);
  public static final Action HERO_PUNCH1 = new DefinedAction(60, 5);
  public static final Action HERO_PUNCH2 = new DefinedAction(65, 5);
  public static final Action HERO_SUPER_PUNCH = new DefinedAction(70, 10);
  public static final Action HERO_JUMP_ATK = new DefinedAction(80, 5);
  public static final Action HERO_RUN_ATK = new DefinedAction(85, 5);
  public static final Action HERO_DASH_ATK = new DefinedAction(90, 5);
  public static final Action HERO_DASH_DEF = new DefinedAction(95, 0);
  public static final Action HERO_FLIP1 = new DefinedAction(100, 2);
  public static final Action HERO_ROLLING = new DefinedAction(102, 6);
  public static final Action HERO_FLIP2 = new DefinedAction(108, 2);
  public static final Action HERO_DEFEND = new DefinedAction(110, 1);
  public static final Action HERO_DEFEND_HIT = new DefinedAction(111, 1);
  public static final Action HERO_BROKEN_DEF = new DefinedAction(112, 3);
  public static final Action HERO_PICK_LIGHT = new DefinedAction(115, 1);
  public static final Action HERO_PICK_HEAVY = new DefinedAction(116, 4);
  public static final Action HERO_CATCH = new DefinedAction(120, 10);
  public static final Action HERO_CAUGHT = new DefinedAction(130, 20);
  public static final Action HERO_FORWARD_FALL = new DefinedAction(180, 6);
  public static final Action HERO_FORWARD_FALL1 = new DefinedAction(180, 1);
  public static final Action HERO_FORWARD_FALL2 = new DefinedAction(181, 1);
  public static final Action HERO_FORWARD_FALL3 = new DefinedAction(182, 1);
  public static final Action HERO_FORWARD_FALL4 = new DefinedAction(183, 1);
  public static final Action HERO_FORWARD_FALL5 = new DefinedAction(184, 1);
  public static final Action HERO_FORWARD_FALLR = new DefinedAction(185, 1);
  public static final Action HERO_BACKWARD_FALL = new DefinedAction(186, 6);
  public static final Action HERO_BACKWARD_FALL1 = new DefinedAction(186, 1);
  public static final Action HERO_BACKWARD_FALL2 = new DefinedAction(187, 1);
  public static final Action HERO_BACKWARD_FALL3 = new DefinedAction(188, 1);
  public static final Action HERO_BACKWARD_FALL4 = new DefinedAction(189, 1);
  public static final Action HERO_BACKWARD_FALL5 = new DefinedAction(190, 1);
  public static final Action HERO_BACKWARD_FALLR = new DefinedAction(191, 1);
  public static final Action HERO_ICE = new DefinedAction(200, 3);
  public static final Action HERO_UPWARD_FIRE = new DefinedAction(203, 2);
  public static final Action HERO_DOWNWARD_FIRE = new DefinedAction(205, 2);
  public static final Action HERO_TIRED = new DefinedAction(207, 0);
  public static final Action HERO_JUMP = new DefinedAction(210, 2);
  public static final Action HERO_JUMP_AIR = new DefinedAction(212, 1);
  public static final Action HERO_DASH = new DefinedAction(213, 1);
  public static final Action HERO_DASH_REVERSE = new DefinedAction(214, 1);
  public static final Action HERO_CROUCH1 = new DefinedAction(215, 3);
  public static final Action HERO_STOPRUN = new DefinedAction(218, 1);
  public static final Action HERO_CROUCH2 = new DefinedAction(219, 1);
  public static final Action HERO_INJURE1 = new DefinedAction(220, 1);
  public static final Action HERO_FRONTHURT = new DefinedAction(221, 1);
  public static final Action HERO_INJURE2 = new DefinedAction(222, 1);
  public static final Action HERO_BACKHURT = new DefinedAction(223, 1);
  public static final Action HERO_INJURE3 = new DefinedAction(224, 2);
  public static final Action HERO_DOP = new DefinedAction(226, 4);
  public static final Action HERO_LYING1 = new DefinedAction(230, 1);
  public static final Action HERO_LYING2 = new DefinedAction(231, 1);
  public static final Action HERO_THROW_LYING_MAN = new DefinedAction(232, 3);

  public static final Action LANDING_ACT = new DefinedAction(94, 0);
  public static final Action TRANSFORM_BACK = new DefinedAction(245, 0);

  // ==================== Weapon ====================
  public static final Action LIGHT_IN_THE_SKY = new ReferenceAction(0, 16);
  public static final Action LIGHT_ON_HAND = new ReferenceAction(20, 16, 0);
  public static final Action LIGHT_THROWING = new ReferenceAction(40, 16, 0);
  public static final Action LIGHT_ON_GROUND = new DefinedAction(60, 5);
  public static final Action LIGHT_STABLE_ON_GROUND = new DefinedAction(64, 1);
  public static final Action LIGHT_JUST_ON_GROUND = new DefinedAction(70, 3);

  public static final Action HEAVY_IN_THE_SKY = new ReferenceAction(0, 6);
  public static final Action HEAVY_ON_HAND = new ReferenceAction(10, 1, 0);
  public static final Action HEAVY_THROWING = new ReferenceAction(0, 6);
  public static final Action HEAVY_ON_GROUND = new DefinedAction(20, 1);
  public static final Action HEAVY_STABLE_ON_GROUND = new DefinedAction(20, 1);
  public static final Action HEAVY_JUST_ON_GROUND = new DefinedAction(21, 1);

  // ==================== Energy ====================
  public static final Action ENERGY_FLYING = new DefinedAction(0, 0);
  public static final Action ENERGY_HITTING = new DefinedAction(10, 0);
  public static final Action ENERGY_HIT = new DefinedAction(20, 0);
  public static final Action ENERGY_REBOUND = new DefinedAction(30, 0);
  public static final Action ENERGY_DISAPPEAR = new DefinedAction(40, 0);

  public static final Action DENNIS_CHASE_STRAIGHT = new DefinedAction(1, 2);
  public static final Action DENNIS_CHASE_CHANGEDIR = new DefinedAction(3, 2);
  public static final Action DENNIS_CHASE_AWAY = new DefinedAction(5, 2);

  // ==================== Parser Utility ====================

  /**
   * Prepares for a next {@code Action}.
   *
   * @param rawValue raw number specified in next field
   * @return a statement to create an {@code Action}
   */
  public static String processNext(int rawValue) {
    if (rawValue == 0) {
      return "Action.REPEAT";
    }
    if (rawValue == 999) {
      return "Action.DEFAULT";
    }
    if (rawValue == -999) {
      return "Action.DEFAULT_REVERSE";
    }
    if (rawValue == 1000) {
      return "Action.REMOVAL";
    }
    return "Action.of(%d)".formatted(rawValue);
  }

  /**
   * Prepares for a goto-like {@code Action}.
   * Goto-like actions are those directly assigning cases,
   * such as hit_xx, action, caughtact, etc.
   *
   * @param rawValue raw number specified in goto field
   * @return a statement to create an {@code Action}
   */
  public static String processGoto(int rawValue) {
    if (rawValue == 0) {
      return "Action.UNASSIGNED";
    }
    if (rawValue == 999) {
      return "Action.DEFAULT";
    }
    return "Action.of(%d)".formatted(rawValue);
  }

}
