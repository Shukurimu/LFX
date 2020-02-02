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
    Map<String, State> map = new HashMap<>();
    map.put("0", STAND);
    map.put("1", WALK);
    map.put("2", RUN);
    map.put("3", NORMAL);  // (attack) use State_noact
    map.put("4", JUMP);
    map.put("5", DASH);
    map.put("6", ROW);
    map.put("7", DEFEND);
    map.put("8", NORMAL);  // (broken_defend) no use
    map.put("9", GRASP);
    map.put("10", GRASP);
    map.put("11", NORMAL);
    map.put("12", FALL);
    map.put("13", NORMAL);
    map.put("14", LYING);
    map.put("15", NORMAL);
    map.put("16", NORMAL);
    map.put("17", DRINK);
    map.put("18", FIRE);  // only used in hero on fire actions
    map.put("19", NORMAL);  // (firerun) use State_noact with dvz and visual effect
    map.put("100", NORMAL);  // (louis landing) use Effect
    map.put("301", NORMAL);  // (Deep_Strafe) use State_noact with dvz
    map.put("400", NORMAL);  // (teleport) use Effect
    map.put("401", NORMAL);  // (teleport) use Effect
    map.put("500", TRY_TRANSFORM);
    map.put("501", NORMAL);  // (transformback) use Effect
    map.put("1000", INSKY);
    map.put("1001", ONHAND);
    map.put("1002", THROW);
    map.put("1003", JUST_ONGROUND);
    map.put("1004", ONGROUND);
    map.put("2000", INSKY);
    map.put("2001", ONHAND);
    map.put("2004", ONGROUND);  // unknown
    map.put("3000", NORMAL);
    map.put("3001", HITTING);
    map.put("3002", HIT);
    map.put("3003", REBOUND);
    map.put("3004", HIT);  // unknown real effect
    map.put("3005", ENERGY);
    map.put("3006", PIERCE);
    map.put("1700", NORMAL);  // (healing) use Effect
    map.put("9995", UNIMPLEMENTED);
    map.put("9996", NORMAL);  // use opoint kind==ARMOUR
    map.put("9998", NORMAL);
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
