import java.util.HashMap;

enum LFstate {
  NORM   (false),
  UNIMPLEMENTED(false),
  /* states for hero */
  STAND  (false),
  WALK   (false),
  RUN    (false),
  HWALK  (false),
  HRUN   (false),
  JUMP   (false),
  DASH   (false),
  ROW    (false),
  DRINK  (false),
  DEFEND (false),
  CATCH  (false),
  CAUGHT (false),
  FALL   (false),
  ICE    (false),
  FIRE   (false),
  INJURED(false),
  DOP    (false),
  LYING  (false),
  LAND   (false),
  TRY_TRANSFORM(false),
  /* states for weapon */
  INSKY   (true),
  ONHAND  (false),
  THROW   (true),
  ONGROUND(false),
  JUST_ONGROUND(false),
  BROKENWEAPON (false),
  /* states for blast */
  NORMAL (true),
  HITSUCC(false),
  HITFAIL(false),
  REBOUND(false),
  ENERGY (false),
  PIERCE (true);

  public final boolean createVz;

  private LFstate(boolean z) {
    createVz = z;
  }

  public String parserText() {
    return "LFstate." + this.toString();
  }

  public static HashMap<String, LFstate> buildParserMap() {
    HashMap<String, LFstate> map = new HashMap<>();
    map.put("0", STAND);
    map.put("1", WALK);
    map.put("2", RUN);
    map.put("3", NORM);// (attack) use State_noact
    map.put("4", JUMP);
    map.put("5", DASH);
    map.put("6", ROW);
    map.put("7", DEFEND);
    map.put("8", NORM);// (broken_defend) no use
    map.put("9", CATCH);
    map.put("10", CAUGHT);
    map.put("11", INJURED);
    map.put("12", FALL);
    map.put("13", ICE);
    map.put("14", LYING);
    map.put("15", NORM);
    map.put("16", DOP);
    map.put("17", DRINK);
    map.put("18", FIRE);// only used in hero on fire actions
    map.put("19", NORM);// (firerun) use State_noact with dvz and visual effect
    map.put("100", NORM);// (louis landing) use LFextra
    map.put("301", NORM);// (Deep_Strafe) use State_noact with dvz
    map.put("400", NORM);// (teleport) use LFextra
    map.put("401", NORM);// (teleport) use LFextra
    map.put("500", TRY_TRANSFORM);
    map.put("501", NORM);// (transformback) use LFextra
    map.put("1000", INSKY);
    map.put("1001", ONHAND);
    map.put("1002", THROW);
    map.put("1003", JUST_ONGROUND);
    map.put("1004", ONGROUND);
    map.put("2000", INSKY);
    map.put("2001", ONHAND);
    map.put("2004", ONGROUND);// unknown
    map.put("3000", NORMAL);
    map.put("3001", HITSUCC);
    map.put("3002", HITFAIL);
    map.put("3003", REBOUND);
    map.put("3004", HITFAIL);// unknown real effect
    map.put("3005", ENERGY);
    map.put("3006", PIERCE);
    map.put("1700", NORM);// (healing) use LFextra
    map.put("9995", UNIMPLEMENTED);
    map.put("9996", NORM);// use opoint kind==ARMOUR
    map.put("9998", BROKENWEAPON);
    return map;
  }

}
