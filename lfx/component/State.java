package lfx.component;

import java.util.HashMap;
import java.util.Map;

public enum State {
  NORMAL(true),
  /** Hero */
  STAND (true),  // UDLFajd
  WALK  (true),  // UDLFajd
  RUN   (true),  // UDLFajd
  HEAVY_WALK(true),  // UDLFa
  HEAVY_RUN (true),  // UDLFa
  JUMP    (true),  // flyingFlag UDLF 
  DASH    (true),  // flyingFlag UDLF
  LAND    (true),  // flyingFlag ajd (crouch2)
  ROW     (true),  // flyingFlag UDLFj
  DRINK   (true),  // connected with weapon
  GRASP   (true),  // complicated cpoint
  DEFEND  (true),  // goes to act 111 if hit
  FALL    (true),  // changes action accroding to vy
  FIRE    (true),  // changes action accroding to vy
  ICE     (true),  // breaking or not depends on vxvy
  LYING   (true),  // loops in same frame if no hp
  TRY_TRANSFORM(true),  // goes to default action if failed
  /** Weapon */
  IN_THE_SKY    (true),
  ON_HAND       (true),
  THROWING      (true),
  ON_GROUND     (true),
  JUST_ON_GROUND(true),
  /** Energy */
  HITTING (true),
  HIT     (true),
  REBOUND (true),
  ENERGY  (true),
  PIERCE  (true),
  UNIMPLEMENTED(true);

  public final boolean basicMove;

  private State(boolean basicMove) {
    this.basicMove = basicMove;
  }

  @Override
  public String toString() {
    return String.format("%s.%s", this.getDeclaringClass().getSimpleName(), super.toString());
  }

}
/*
// https://lf-empire.de/lf2-empire/data-changing/reference-pages/182-states?showall=1
  @Override
  public String toString() {
    return String.format("%s.%s", this.getDeclaringClass().getSimpleName(), super.toString());
  }

  public static Map<String, State> buildParseMap() {
    return map;
  }

  public static String parserState(int originalState) {
    switch (originalState) {
      case 9996:
        return "LFextra.Kind.ARMOUR, LFextra.oneTime()";
      case 1700:
        return "LFextra.Kind.HEALING, new LFextra(100, 1.0)";
      case 400:
        return "LFextra.Kind.TELEPORT_ENEMY, new LFextra(1, 120.0)";
      case 401:
        return "LFextra.Kind.TELEPORT_TEAM, new LFextra(1, 60.0)";
      case 100:
        return "LFextra.Kind.LANDING, new LFextra(-1, 94)";
      case 501:
        return "LFextra.Kind.TRANSFORM_BACK, LFextra.oneTime()";
      default:
        return null;
    }
  }

  public static String parserInvisibility(int invisibility) {
      return String.format("LFextra.Kind.INVISIBLE, new LFextra(%d)", invisibility);
  }

  }*/
