package component;

import base.Type;

/**
 * Define the frame tag -- state.
 * https://lf-empire.de/lf2-empire/data-changing/reference-pages/182-states?showall=1
 */
public enum State {
  UNIMPLEMENTED,
  // ==================== Shared ====================
  NORMAL,
  LANDING_ACT,
  TELEPORT_ENEMY,
  TELEPORT_TEAM,
  TEST_TRANSFORM,
  DO_TRANSFORM,
  // ==================== Hero ====================
  STAND,
  WALK,
  RUN,
  JUMP,
  DASH,
  FLIP,
  DEFEND,
  GRABBING,
  GRABBED,
  FALL,
  ICE,
  LYING,
  DANCE_OF_PAIN,
  DRINK,
  FIRE,
  // ==================== Weapon ====================
  IN_THE_SKY,
  ON_HAND,
  THROWING,
  ON_GROUND,
  JUST_ON_GROUND,
  // ==================== Energy ====================
  FLYING,
  HITTING,
  HIT,
  REBOUND,
  ENERGY,
  PIERCE;

  @Override
  public String toString() {
    return String.join(".", getDeclaringClass().getSimpleName(), name());
  }

  // If the ball hits other attacks with this state, it'll go to the hitting frame
  // (10). If it is hit by another ball or a character, it'll go to the the hit
  // frame (20) or rebounding frame (30).

  // If the ball hits a character while it has state 3001, then it won't go to the hitting frame (20).  It's the same for states 3002 through 3004.
  // If the ball hits a character while it has state 3002, then it won't go to the hitting frame (20).
  // If the ball hits a character while it has state 3003, then it won't go to the hitting frame (20).
  // If the ball hits a character while it has state 3004, then it won't go to the hitting frame (20).

  // It cannot be rebounded and state 3000 balls won't destroy it.  However, if a state 3006 ball is hit by a state 3005 attack or another state 3006 attack, it'll be destroyed.

  // Knight and Julian lose innate armor in state 8, 10, 11, 16,
  // Louis's armor is only effective in frame 0-19 or state: 4, 5 frames.

  // ==================== Parser Utility ====================

  /**
   * Gets the corresponding {@code State} of given information.
   *
   * @param type        the {@code Type} of the owner of enclosing frame
   * @param rawState    original state of the enclosing frame
   * @param frameNumber frame's index
   * @return a {@code State} enum
   * @throws IllegalArgumentException for invalid state
   */
  public static State process(Type type, int rawState, int frameNumber) {
    if (type.isHero) {
      if (Action.HERO_ROLLING.contains(frameNumber)) {
        return State.NORMAL;
      }
      if (rawState == 18) {  // only used in hero on fire actions
        boolean fire = Action.HERO_UPWARD_FIRE.contains(frameNumber) ||
                       Action.HERO_DOWNWARD_FIRE.contains(frameNumber);
        return fire ? State.FIRE : State.NORMAL;
      }
    }
    return switch (rawState) {
      case 0 -> State.STAND;
      case 1 -> State.WALK;
      case 2 -> State.RUN;
      case 3 -> State.NORMAL;  // (attack action) no use
      case 4 -> State.JUMP;
      case 5 -> State.DASH;
      case 6 -> State.FLIP;
      case 7 -> State.DEFEND;
      case 8 -> State.NORMAL;  // (broken_defend) no use
      case 9 -> State.GRABBING;
      case 10 -> State.GRABBED;
      case 11 -> State.NORMAL;
      case 12 -> State.FALL;
      case 13 -> State.NORMAL;
      case 14 -> State.LYING;
      case 15 -> State.NORMAL;
      case 16 -> State.DANCE_OF_PAIN;
      case 17 -> State.DRINK;
      case 18 -> State.NORMAL;
      case 19 -> State.NORMAL;  // (Firen firerun) use dvz and visual effect
      case 100 -> State.LANDING_ACT;
      case 301 -> State.NORMAL;  // (Deep chop_series) use dvz
      case 400 -> State.TELEPORT_ENEMY;
      case 401 -> State.TELEPORT_TEAM;
      case 500 -> State.DO_TRANSFORM;
      case 501 -> State.NORMAL;  // (transformback) use Effect
      case 1000 -> State.IN_THE_SKY;
      case 1001 -> State.ON_HAND;
      case 1002 -> State.THROWING;
      case 1003 -> State.JUST_ON_GROUND;
      case 1004 -> State.ON_GROUND;
      case 1700 -> State.UNIMPLEMENTED;
      case 2000 -> State.IN_THE_SKY;
      case 2001 -> State.ON_HAND;
      case 2004 -> State.ON_GROUND;  // unknown
      case 3000 -> State.NORMAL;
      case 3001 -> State.HITTING;
      case 3002 -> State.HIT;
      case 3003 -> State.REBOUND;
      case 3004 -> State.HIT;  // unknown real effect
      case 3005 -> State.ENERGY;
      case 3006 -> State.PIERCE;
      case 9995 -> State.UNIMPLEMENTED;
      case 9996 -> State.UNIMPLEMENTED;
      case 9997 -> State.UNIMPLEMENTED;
      case 9998 -> State.UNIMPLEMENTED;
      case 9999 -> State.UNIMPLEMENTED;
      default -> throw new IllegalArgumentException();
    };
  }

}
