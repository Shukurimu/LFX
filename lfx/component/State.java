package lfx.component;

import java.util.EnumSet;

// https://lf-empire.de/lf2-empire/data-changing/reference-pages/182-states?showall=1
public enum State {
  NORMAL(true),
  /** Hero */
  STAND (true),  // direction keys
  WALK  (true),  // direction keys
  RUN   (true),  // direction keys
  HEAVY_WALK(true),  // direction keys
  HEAVY_RUN (true),  // direction keys
  JUMP    (true),  // with hidden flying state; direction keys
  DASH    (true),  // with hidden flying state; direction keys
  ROW     (true),  // with hidden flying state
  GRASP   (true),  // complicated cpoint
  DEFEND  (true),  // goes to act 111 if being hit
  FALL    (true),  // changes action accroding to vy
  FIRE    (true),  // changes action accroding to vy
  LYING   (true),  // loops in same act if no hp
  /** Weapon */
  INSKY        (true),
  ONHAND       (true),
  THROW        (true),
  ONGROUND     (true),
  JUST_ONGROUND(true),
  /** Blast */
  HITTING (true),
  HIT     (true),
  REBOUND (true),
  ENERGY  (true),
  PIERCE  (true),
  UNIMPLEMENTED(true),

  /** There can be only one State of basicMove. */
  public static final EnumSet<State> BASIC_MOVES = EnumSet.range(NORMAL, UNIMPLEMENTED);
  public final boolean basicMove;

  private State(boolean basicMove) {
    this.basicMove = basicMove;
  }

  @Override
  public String toString() {
    return String.format("%s.%s", this.getDeclaringClass().getSimpleName(), super.toString());
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

  public static HashMap<String, State> buildParserMap() {
    HashMap<String, State> map = new HashMap<>();
    map.put("0", STAND);
    map.put("1", WALK);
    map.put("2", RUN);
    map.put("3", NORM);  // (attack) use State_noact
    map.put("4", JUMP);
    map.put("5", DASH);
    map.put("6", ROW);
    map.put("7", DEFEND);
    map.put("8", NORM);  // (broken_defend) no use
    map.put("9", CATCH);
    map.put("10", CAUGHT);
    map.put("11", INJURED);
    map.put("12", FALL);
    map.put("13", ICE);
    map.put("14", LYING);
    map.put("15", NORM);
    map.put("16", DOP);
    map.put("17", DRINK);
    map.put("18", FIRE);  // only used in hero on fire actions
    map.put("19", NORM);  // (firerun) use State_noact with dvz and visual effect
    map.put("100", NORM);  // (louis landing) use Effect
    map.put("301", NORM);  // (Deep_Strafe) use State_noact with dvz
    map.put("400", NORM);  // (teleport) use Effect
    map.put("401", NORM);  // (teleport) use Effect
    map.put("500", TRY_TRANSFORM);
    map.put("501", NORM);  // (transformback) use Effect
    map.put("1000", INSKY);
    map.put("1001", ONHAND);
    map.put("1002", THROW);
    map.put("1003", JUST_ONGROUND);
    map.put("1004", ONGROUND);
    map.put("2000", INSKY);
    map.put("2001", ONHAND);
    map.put("2004", ONGROUND);  // unknown
    map.put("3000", NORMAL);
    map.put("3001", HITSUCC);
    map.put("3002", HITFAIL);
    map.put("3003", REBOUND);
    map.put("3004", HITFAIL);  // unknown real effect
    map.put("3005", ENERGY);
    map.put("3006", PIERCE);
    map.put("1700", NORM);  // (healing) use Effect
    map.put("9995", UNIMPLEMENTED);
    map.put("9996", NORM);  // use opoint kind==ARMOUR
    map.put("9998", BROKENWEAPON);
    return map;
  }

}
