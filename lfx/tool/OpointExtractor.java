package lfx.tool;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Set;
import lfx.base.Type;
import lfx.util.Tuple;

class OpointExtractor extends Extractor {
  static final Set<String> validKeys = Set.of(
      "kind", "x", "y", "action", "dvx", "dvy", "oid", "facing"
  );
  private static final Pattern blockPattern = buildBlockPattern("opoint");
  private static final Map<Integer, String> oid2identifier = new HashMap<>(72);
  private static final Map<String, Tuple<Type, String>> fileName2info = new HashMap<>(72);

  OpointExtractor(Type type, int frameNumber, String content) {
    super(type, frameNumber, content);
  }

  @Override
  protected Pattern getPattern() {
    return blockPattern;
  }

  @Override
  protected Extractor build(Type type, int frameNumber, String content) {
    return new OpointExtractor(type, frameNumber, content);
  }

  @Override
  protected String parseLines(int state) {
    Map<String, Integer> data = wrapInt(extract(validKeys, content, toString()));
    String identifier = oid2identifier.get(data.get("oid"));
    int x = data.get("x").intValue();
    int y = data.get("y").intValue();
    if (data.get("kind").intValue() == 2) {
      return String.format("Opoint.hold(%d, %d, \"%s\")", x, y, identifier);
    }
    int facing = data.get("facing").intValue();
    return String.format(
        "Opoint.%s(%d, %d, \"%s\", %d, %d, new Action(%d), %d)",
        (facing & 1) == 0 ? "front": "back", x, y, identifier,
        data.get("dvx").intValue(), data.get("dvy").intValue(),
        data.get("action").intValue(), Math.max(1, facing / 10)
    );
  }

  static Tuple<Type, String> getInfo(String fileName) {
    return fileName2info.get(fileName);
  }

  private static void buildMaps(int oid, int rawType, String fileName, String alias) {
    String identifier = alias.isEmpty() ? fileName : alias.equals("<") ? "boomerang" : alias;
    StringBuilder builder = new StringBuilder(32);
    for (String token : identifier.split("_")) {
      builder.append(token.substring(0, 1).toUpperCase() + token.substring(1));
    }
    identifier = builder.toString();
    oid2identifier.put(oid, identifier);
    Type type = null;
    switch (rawType) {
      case 0: type = Type.HERO;   break;
      case 1: type = Type.LIGHT;  break;
      case 2: type = Type.HEAVY;  break;
      case 3: type = Type.ENERGY; break;
      case 4: type = Type.SMALL;  break;
      case 5: type = Type.OTHERS; break;
      case 6: type = Type.DRINK;  break;
    }
    fileName2info.put(fileName, new Tuple<>(type, identifier));
    return;
  }

  /**
   * Builds information map of data.txt.
   * Pattern in use:
   *   id: +(\d+) +type: +(\d) .*?data\\(.*?)\.dat *#?(.*?)$
   *   buildMaps\((\1), (\2), "(\3)", "(\4)"\);
   */
  static {
    buildMaps(0, 0, "template", "");
    buildMaps(52, 0, "julian", "");
    buildMaps(51, 0, "firzen", "");
    buildMaps(50, 0, "louisEX", "");
    buildMaps(38, 0, "bat", "");
    buildMaps(39, 0, "justin", "");
    buildMaps(37, 0, "knight", "");
    buildMaps(36, 0, "jan", "");
    buildMaps(35, 0, "monk", "");
    buildMaps(34, 0, "sorcerer", "");
    buildMaps(33, 0, "jack", "");
    buildMaps(32, 0, "mark", "");
    buildMaps(31, 0, "hunter", "");
    buildMaps(30, 0, "bandit", "");
    buildMaps(1, 0, "deep", "");
    buildMaps(2, 0, "john", "");
    buildMaps(4, 0, "henry", "");
    buildMaps(5, 0, "rudolf", "");
    buildMaps(6, 0, "louis", "");
    buildMaps(7, 0, "firen", "");
    buildMaps(8, 0, "freeze", "");
    buildMaps(9, 0, "dennis", "");
    buildMaps(10, 0, "woody", "");
    buildMaps(11, 0, "davis", "");

    buildMaps(100, 1, "weapon0", "stick");
    buildMaps(101, 1, "weapon2", "hoe");
    buildMaps(120, 1, "weapon4", "knife");
    buildMaps(121, 4, "weapon5", "baseball");
    buildMaps(122, 6, "weapon6", "milk");
    buildMaps(150, 2, "weapon1", "stone");
    buildMaps(151, 2, "weapon3", "wooden_box");
    buildMaps(123, 6, "weapon8", "beer");
    buildMaps(124, 1, "weapon9", "<");
    buildMaps(217, 2, "weapon10", "louis_armour");
    buildMaps(218, 2, "weapon11", "louis_armour");
    buildMaps(300, 5, "criminal", "criminal");

    buildMaps(200, 3, "john_ball", "");
    buildMaps(201, 1, "henry_arrow1", "");
    buildMaps(202, 1, "rudolf_weapon", "");
    buildMaps(203, 3, "deep_ball", "");
    buildMaps(204, 3, "henry_wind", "");
    buildMaps(205, 3, "dennis_ball", "");
    buildMaps(206, 3, "woody_ball", "");
    buildMaps(207, 3, "davis_ball", "");
    buildMaps(208, 3, "henry_arrow2", "");
    buildMaps(209, 3, "freeze_ball", "");
    buildMaps(210, 3, "firen_ball", "");
    buildMaps(211, 3, "firen_flame", "");
    buildMaps(212, 3, "freeze_column", "");
    buildMaps(213, 1, "weapon7", "ice_sword");
    buildMaps(214, 3, "john_biscuit", "");
    buildMaps(215, 3, "dennis_chase", "");
    buildMaps(216, 3, "jack_ball", "");
    buildMaps(219, 3, "jan_chaseh", "");
    buildMaps(220, 3, "jan_chase", "");
    buildMaps(221, 3, "firzen_chasef", "");
    buildMaps(222, 3, "firzen_chasei", "");
    buildMaps(223, 3, "firzen_ball", "");
    buildMaps(224, 3, "bat_ball", "");
    buildMaps(225, 3, "bat_chase", "");
    buildMaps(226, 3, "justin_ball", "");
    buildMaps(228, 3, "julian_ball", "");
    buildMaps(229, 3, "julian_ball2", "");

    buildMaps(998, 5, "etc", "");
    buildMaps(999, 5, "broken_weapon", "");
  }

}
