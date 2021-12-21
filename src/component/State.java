package component;

// https://lf-empire.de/lf2-empire/data-changing/reference-pages/182-states?showall=1
public enum State {
  NORMAL,
  UNIMPLEMENTED,
  LANDING_ACT,
  TEST_TRANSFORM,
  DO_TRANSFORM,
  TELEPORT_ENEMY,
  TELEPORT_TEAM,
  // Hero
  STAND, // UDLFajd
  WALK, // UDLFajd
  RUN, // UDLFajd
  HEAVY_WALK, // UDLFa
  HEAVY_RUN, // UDLFa
  JUMP, // flyingFlag UDLF
  DASH, // flyingFlag UDLF
  FLIP, // flyingFlag UDLFj
  DRINK, // connected with weapon
  GRAB, // complicated cpoint
  GRABBED, // complicated cpoint
  DEFEND, // goes to act 111 if hit
  FALL, // changes action accroding to vy
  FIRE, // changes action accroding to vy
  ICE, // one hit to fall; breaking or not depends on vxvy
  LYING, // loops in same frame if no hp
  TRY_TRANSFORM, // goes to default action if failed
  // Weapon
  IN_THE_SKY,
  ON_HAND,
  THROWING,
  ON_GROUND,
  JUST_ON_GROUND,
  // Energy
  FLYING,
  HITTING,
  HIT,
  REBOUND,
  ENERGY,
  PIERCE;

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

}
