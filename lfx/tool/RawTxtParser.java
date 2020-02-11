package lfx.tool;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.util.Set;
import lfx.component.Bdy;
import lfx.component.State;
import lfx.object.Hero;
import lfx.util.Tuple;

public class RawTxtParser {
  static final Pattern bmpTagPattern = Pattern.compile("<bmp_begin>(.*?)<bmp_end>", Pattern.DOTALL);
  static final Pattern frameTagPattern =
      Pattern.compile("<frame>\\s*(\\d+)(.*?)\n(.*?)<frame_end>", Pattern.DOTALL);
  static final Pattern strengthTagPattern =
      Pattern.compile("<weapon_strength_list>(.*?)<weapon_strength_list_end>", Pattern.DOTALL);
  static final String blockFormat = "(%1$s):\\s*(.*?)\\s*%1$s_end:";
  static final List<Pattern> blockPatternList = List.of(
      Pattern.compile(String.format(blockFormat, "bpoint"), Pattern.DOTALL),
      Pattern.compile(String.format(blockFormat, "cpoint"), Pattern.DOTALL),
      Pattern.compile(String.format(blockFormat, "opoint"), Pattern.DOTALL),
      Pattern.compile(String.format(blockFormat, "wpoint"), Pattern.DOTALL),
      Pattern.compile(String.format(blockFormat, "itr"), Pattern.DOTALL),
      Pattern.compile(String.format(blockFormat, "bdy"), Pattern.DOTALL)
  );
  static final Pattern strengthBlockPattern =
      Pattern.compile("entry *: *(\\d+) +\\S*(.*?)((?=entry)|(?=<))", Pattern.DOTALL);
  static final Pattern imageFilePattern = Pattern.compile("file.*?: *(\\S+) +(.*)");
  static final Pattern keyValuePattern = Pattern.compile("([^:\\s]+) *: *(\\S+)");
  static final Pattern staminaPattern = Pattern.compile("([_\\w]+) +(\\S+)");

  static final Set<String> imageKeySet = Set.of("w", "h", "row", "col");
  static final Set<String> bdyKeySet = Set.of("kind", "x", "y", "w", "h");
  static final Set<String> itrKeySet = Set.of(
    "kind", "x", "y", "w", "h", "zwidth", "dvx", "dvy",
    "fall", "bdefend", "injury", "vrest", "arest", "catchingact", "caughtact", "effect"
  );
  static final Set<String> wpointKeySet = Set.of(
    "kind", "x", "y", "weaponact", "attacking", "dvx", "dvy", "dvz", "cover"
  );
  static final Set<String> opointKeySet = Set.of(
    "kind", "x", "y", "action", "dvx", "dvy", "oid", "facing"
  );
  static final Set<String> cpointKeySet = Set.of(
    "kind", "x", "y", "injury", "vaction", "aaction", "jaction", "taction",
    "throwvx", "throwvy", "throwvz", "throwinjury", "fronthurtact", "backhurtact",
    "hurtable", "decrease", "dircontrol", "cover"
  );
  static final Set<String> frameKeySet = Set.of(
      "pic", "state", "wait", "next", "centerx", "centery",  // required fields
      "dvx", "dvy", "dvz", "mp", "sound", "hit_a", "hit_d", "hit_j",
      "hit_Fa", "hit_Fj", "hit_Ua", "hit_Uj", "hit_Da", "hit_Dj", "hit_ja"
  );

  static final Map<String, Tuple<Integer, Integer>> fileMap = new HashMap<>(96);
  static final Map<Integer, String> oidMap = new HashMap<>(96);
  static final Map<String, String> variableNaming = Map.of(
      "ObjectImage", "imageList",
      "ItemInfo", "info",
      "HeroStamina", "stamina",
      "HeroPortrait", "portrait",
      "WeaponStrength", "strength"
  );

  final String className;
  final String baseClassName;
  final boolean isHero;
  final boolean isEnergy;
  final List<String> lineList = new ArrayList<>(4096);
  int frameNumber = 0;
  String frameNote = "";

  RawTxtParser(Tuple<Integer, Integer> tuple) {
    className = oidMap.get(tuple.first);
    String typeName = null;
    switch (tuple.second) {
      case 0:  typeName = "BaseHero";    break;
      case 1:  typeName = "BaseWeapon";  break;
      case 2:  typeName = "BaseWeapon";  break;
      case 3:  typeName = "BaseEnergy";  break;
      case 4:  typeName = "BaseWeapon";  break;
      case 6:  typeName = "BaseWeapon";  break;
      default: typeName = "";
    }
    baseClassName = "lfx.object." + typeName;
    isHero = tuple.second == 0;
    isEnergy = tuple.second == 3;
  }

  void message(String formatter, Object... args) {
    System.err.printf("\t%s@%s(%d): %s%n",
                      className, frameNote, frameNumber,
                      String.format(formatter, args)
    );
    return;
  }

  static void buildMaps(int oid, int rawType, String fileName, String note) {
    if (note.isEmpty()) {
      note = fileName;
    }
    if (note.equals("<")) {
      note = "boomerang";
    }
    StringBuilder builder = new StringBuilder();
    for (String token : note.split("_")) {
      builder.append(token.substring(0, 1).toUpperCase() + token.substring(1));
    }
    oidMap.put(oid, builder.toString());
    fileMap.put(fileName, new Tuple<>(oid, rawType));
    return;
  }

  static void processDataTxt() {
    // Regular Expressions
    // id: +(\d+) +type: +(\d) .*?data\\(.*?)\.dat *#?(.*?)$
    // buildMaps\((\1), (\2), "(\3)", "(\4)"\);
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

  static String indent(int level) {
    return "  ".repeat(level);
  }

  Map<String, String> extract(String content, Set<String> validKeys, String blockName) {
    Map<String, String> result = new HashMap<>(validKeys.size());
    Matcher matcher = keyValuePattern.matcher(content);
    while (matcher.find()) {
      String key = matcher.group(1);
      String value = matcher.group(2);
      if (validKeys.contains(key)) {
        result.put(key, value);
      } else {
        message("Unknown (%s, %s) in %s block.", key, value, blockName);
      }
    }
    return result;
  }

  List<String> getItrProperty(Map<String, String> data) {
    List<String> propertyList = new ArrayList<>(8);
    propertyList.add(data.get("kind"));

    for (String key : List.of("dvx", "dvy", "fall", "bdefend", "injury")) {
      String value = data.get(key);
      if (value == null) {
        message("Use default %s.", key);
        value = "Const.TBA";
      } else {  // invalid check
        value = Integer.toString(Integer.valueOf(value));
      }
      propertyList.add(value);
    }

    String vrest = null;
    if (data.containsKey("arest")) {
      int value = Integer.valueOf(data.get("arest"));
      vrest = Integer.toString(-value);
    } else if (data.containsKey("vrest")) {
      int value = Integer.valueOf(data.get("vrest"));
      vrest = Integer.toString(+value);
    } else {
      message("Use default %s.", "vrest");
      vrest = isHero ? "Itr.AREST" : "Itr.VREST";
    }
    propertyList.add(vrest);

    return propertyList;
  }

  State getState(int rawState, int curr) {
    if (isHero) {
      if (curr == Hero.ACT_CROUCH1) {
        return State.LAND;
      }
      if (Hero.ACT_HEAVY_RUN > curr && curr >= Hero.ACT_HEAVY_WALK) {
        return State.HEAVY_WALK;
      }
      if (Hero.ACT_HEAVY_STOP_RUN > curr && curr >= Hero.ACT_HEAVY_RUN) {
        return State.HEAVY_RUN;
      }
      if (Hero.ACT_ROWING2 > curr && curr >= Hero.ACT_ROLLING) {
        return State.NORMAL;
      }
      if (rawState == 18 && (curr < Hero.ACT_UPWARD_FIRE || curr >= Hero.ACT_TIRED)) {
        return State.NORMAL;
      }
    }
    switch (rawState) {
      case 0: return State.STAND;
      case 1: return State.WALK;
      case 2: return State.RUN;
      case 3: return State.NORMAL;  // attack action
      case 4: return State.JUMP;
      case 5: return State.DASH;
      case 6: return State.ROW;
      case 7: return State.DEFEND;
      case 8: return State.NORMAL;  // (broken_defend) no use
      case 9: return State.GRASP;
      case 10: return State.GRASP;
      case 11: return State.NORMAL;
      case 12: return State.FALL;
      case 13: return State.NORMAL;
      case 14: return State.LYING;
      case 15: return State.NORMAL;
      case 16: return State.NORMAL;
      case 17: return State.DRINK;
      case 18: return State.FIRE;  // only used in hero on fire actions
      case 19: return State.NORMAL;  // (firerun) use return State_noact with dvz and visual effect
      case 100: return State.NORMAL;  // (louis landing) use Effect
      case 301: return State.NORMAL;  // (Deep_Strafe) use return State_noact with dvz
      case 400: return State.NORMAL;  // (teleport) use Effect
      case 401: return State.NORMAL;  // (teleport) use Effect
      case 500: return State.TRY_TRANSFORM;
      case 501: return State.NORMAL;  // (transformback) use Effect
      case 1000: return State.IN_THE_SKY;
      case 1001: return State.ON_HAND;
      case 1002: return State.THROWING;
      case 1003: return State.JUST_ON_GROUND;
      case 1004: return State.ON_GROUND;
      case 2000: return State.IN_THE_SKY;
      case 2001: return State.ON_HAND;
      case 2004: return State.ON_GROUND;  // unknown
      case 3000: return State.NORMAL;
      case 3001: return State.HITTING;
      case 3002: return State.HIT;
      case 3003: return State.REBOUND;
      case 3004: return State.HIT;  // unknown real effect
      case 3005: return State.ENERGY;
      case 3006: return State.PIERCE;
      case 1700: return State.NORMAL;  // (healing) use Effect
      case 9995: return State.UNIMPLEMENTED;
      case 9996: return State.NORMAL;  // use opoint kind==ARMOUR
      case 9998: return State.NORMAL;
      default: return State.NORMAL;
    }
  }

  String processImageLine(String tagContent) {
    final String varName = variableNaming.get("ObjectImage");
    lineList.add(String.format("%sList<Tuple<Image, Image>> %s = new ArrayList<>(256);",
                               indent(2), varName));
    StringBuilder builder = new StringBuilder(1024);
    Matcher matcher = imageFilePattern.matcher(tagContent);
    while (matcher.find()) {
      Map<String, String> format = extract(matcher.group(2), imageKeySet, "image");
      lineList.add(String.format("%s%s.addAll(loadImageCells(\"%s\", %d, %d, %d, %d));",
                                 indent(2), varName,
                                 matcher.group(1),
                                 Integer.valueOf(format.get("w")),
                                 Integer.valueOf(format.get("h")),
                                 Integer.valueOf(format.get("row")),
                                 Integer.valueOf(format.get("col"))
      ));
      matcher.appendReplacement(builder, "");
    }
    matcher.appendTail(builder);
    return builder.toString();
  }

  void itemBmpTag(String bmpContent) {
    final String mapName = variableNaming.get("ItemInfo");
    bmpContent = processImageLine(bmpContent);
    lineList.add(String.format("%sMap<String, String> %s = new HashMap<>();", indent(2), mapName));
    Matcher matcher = keyValuePattern.matcher(bmpContent);
    while (matcher.find()) {
      lineList.add(String.format("%s%s.add(\"%s\", \"%s\");",
                                 indent(4), mapName,
                                 matcher.group(1),
                                 matcher.group(2)
      ));
    }
    return;
  }

  void heroBmpTag(String bmpContent) {
    bmpContent = processImageLine(bmpContent);
    Matcher keyValueMatcher = keyValuePattern.matcher(bmpContent);
    while (keyValueMatcher.find()) {
      if (keyValueMatcher.group(1).equals("head")) {
        lineList.add(String.format("%sImage %s = loadImage(\"%s\");",
                                   indent(2),
                                   variableNaming.get("HeroPortrait"),
                                   keyValueMatcher.group(2)
        ));
        break;
      }
    }
    
    lineList.add("");
    final String mapName = variableNaming.get("HeroStamina");
    lineList.add(String.format("%sMap<String, Double> %s = new HashMap<>();", indent(2), mapName));
    Matcher matcher = staminaPattern.matcher(bmpContent);
    while (matcher.find()) {
      if (matcher.group(1).endsWith("_rate")) {
        continue;
      }
      lineList.add(String.format("%s%s.add(\"%s\", %s);",
                                 indent(2), mapName,
                                 matcher.group(1),
                                 Float.toString(Float.valueOf(matcher.group(2)))
      ));
    }
    return;
  }

  void strengthTag(String strengthContent) {
    final String mapName = variableNaming.get("WeaponStrength");
    lineList.add(String.format("%sMap<Integer, Itr> %s = new HashMap<>(4);", indent(2), mapName));
    Matcher matcher = strengthBlockPattern.matcher(strengthContent);
    while (matcher.find()) {
      int attacking = Integer.valueOf(matcher.group(1));
      Map<String, String> data = extract(matcher.group(2), itrKeySet, "strength");
      String itr = String.format("Itr.strength(%s)", String.join(", ", getItrProperty(data)));
      lineList.add(String.format("%s%s.add(%d, %s);", indent(2), mapName, attacking, itr));
    }
    return;
  }

  Tuple<String, Integer> parseAction(String rawAction) {
    int intValue = Math.abs(Integer.valueOf(rawAction));
    String rawResult = null;
    Integer invisibility = null;
    if (intValue == 1000) {
      rawResult = "ACT_REMOVAL";
    } else if (intValue == 999) {
      rawResult = "ACT_DEF";
    } else if (intValue > 1000) {
      rawResult = "ACT_DEF";
      invisibility = Integer.valueOf(intValue - 1100);
    } else if (intValue == 0) {
      rawResult = Integer.toString(frameNumber);
    } else {
      rawResult = Integer.toString(intValue);
    }
    return new Tuple<>(rawAction.startsWith("-") ? ("-" + rawResult) : rawResult, invisibility);
  }

  void frameTag(String frameContext) {
    StringBuilder builder = new StringBuilder(1024);
    List<Tuple<String, String>> blockList = new ArrayList<>(8);
    for (Pattern pattern : blockPatternList) {
      Matcher matcher = pattern.matcher(frameContext);
      while (matcher.find()) {
        blockList.add(new Tuple<>(matcher.group(1), matcher.group(2)));
        // System.err.printf("<%s>%n", blockList.get(blockList.size() - 1));
        matcher.appendReplacement(builder, "");
        // System.err.printf("[%s]%n", blockList.get(blockList.size() - 1));
      }
      matcher.appendTail(builder);
      frameContext = builder.toString();
      builder.setLength(0);
    }

    Map<String, String> data = extract(frameContext, frameKeySet, "frame");
    int picIndex = Integer.valueOf(data.remove("pic"));
    int rawState = Integer.valueOf(data.remove("state"));
    int wait = Integer.valueOf(data.remove("wait"));
    int centerx = Integer.valueOf(data.remove("centerx"));
    int centery = Integer.valueOf(data.remove("centery"));
    State state = getState(rawState, frameNumber);

    Tuple<String, Integer> nextTuple = parseAction(data.remove("next"));
    
    int dvx = Integer.valueOf(data.getOrDefault("dvx", "0"));
    int dvy = Integer.valueOf(data.getOrDefault("dvy", "0"));
    int dvz = Integer.valueOf(data.getOrDefault("dvz", "0"));
    if (rawState == 19 || rawState == 301) {
      dvz = 3;  // uncertain value
    }

    List<String> optionalList = new ArrayList<>(8);
    for (Tuple<String, Integer> tuple :
         List.of(Tuple.of("dvx", dvx), Tuple.of("dvy", dvy), Tuple.of("dvz", dvz))) {
      if (tuple.second != 0) {
        optionalList.add(String.format("\"%s\"", tuple.first));
        optionalList.add(tuple.second == 550 ? "Const.DV_550" : Integer.toString(tuple.second));
      }
    }
    int mp = Integer.valueOf(data.getOrDefault("mp", "0"));
    if (mp != 0) {
      optionalList.add("\"mp\"");
      optionalList.add(Integer.toString(mp));
    }

    for (Map.Entry<String, String> entry : data.entrySet()) {
      String key = entry.getKey();
      if (!key.startsWith("hit_")) {
        continue;
      }
      int value = Integer.valueOf(entry.getValue());
      if (value == 0) {
        continue;
      }
      if (isEnergy && key.equals("hit_j")) {
        if (value != 50) {
          optionalList.add("\"hit_j\"");
          optionalList.add(Integer.toString(value - 50));
        }
      } else {
        optionalList.add(String.format("\"%s\"", key));
        optionalList.add(parseAction(entry.getValue()).first);
      }
    }

    List<String> remaining = new ArrayList<>();
    String sound = data.containsKey("sound") ? String.format(", \"%s\"", data.get("sound")) : "";
    remaining.add(String.format("%d, %d, %s, %s, %d, %d, %d%s",
                                frameNumber,
                                wait,
                                nextTuple.first,
                                state.toString(),
                                picIndex, centerx, centery,
                                sound
    ));
    if (!optionalList.isEmpty()) {
      remaining.add(String.format("Map.of(%s)", String.join(", ", optionalList)));
    }
    for (Tuple<String, String> blockTuple : blockList) {
      String result = null;
      switch (blockTuple.first) {
        case "bdy":  result = bdyBlock(rawState, blockTuple.second);  break;
        case "itr":  result = itrBlock(rawState, blockTuple.second);  break;
        case "wpoint":  result = wpointBlock(blockTuple.second);  break;
        case "opoint":  result = opointBlock(blockTuple.second);  break;
        case "cpoint":  result = cpointBlock(blockTuple.second);  break;
        default: message("Unknown block %s: %s", blockTuple.first, blockTuple.second);
      }
      if (result != null) {
        remaining.add(result);
      }
    }

    lineList.add(String.format("%scollector.add(  // %s", indent(2), frameNote));

    int count = remaining.size();
    for (String line : remaining) {
      lineList.add(String.format("%s%s%s", indent(4), line, --count > 0 ? "," : ""));
    }
    lineList.add(indent(2) + ");");
    return;
  }

  List<String> getBox(Map<String, String> data) {
    List<String> paramList = new ArrayList<>(6);
    for (String key : List.of("x", "y", "w", "h")) {
      paramList.add(data.get(key));
    }
    String zwidth = data.get("zwidth");
    if (zwidth != null) {
      paramList.add(zwidth);
    }
    return paramList;
  }

  String bdyBlock(int state, String content) {
    Map<String, String> data = extract(content, bdyKeySet, "bdy");
    String box = String.join(", ", getBox(data));
    List<String> attriList = new ArrayList<>(4);
    if (state == 13) {  // frozen
      attriList.add(Bdy.Attribute.FRIENDLY_FIRE.toString());
      attriList.add(Bdy.Attribute.IMMUNE_WEAKICE.toString());
    }
    String attribute = String.join(" ", attriList);
    return attribute.isEmpty() ? String.format("new Bdy(%s)", box)
                               : String.format("new Bdy(%s, \"%s\")", box, attribute);
  }

  String itrBlock(int state, String content) {
    Map<String, String> data = extract(content, itrKeySet, "itr");
    String box = String.format("new Box(%s)", String.join(", ",  getBox(data)));

    String kind = data.get("kind");
    if (kind.equals("1") || kind.equals("3")) {
      return String.format("Itr.grab(%s, %s, %s, %s)",
                           box, Boolean.toString(kind.equals("3")),
                           data.get("catchingact"), data.get("caughtact"));
    }

    if (kind.equals("5")) {
      return String.format("Itr.hand(%s)", box);
    }

    if (kind.equals("8")) {
      data.put("dvy", "100");
    }

    List<String> propertyList = getItrProperty(data);
    String scope = null;
    propertyList.add(scope);
    return String.format("new Itr(%s, %s)", box, String.join(", ", propertyList));
  }

  String cpointBlock(String content) {
    Map<String, String> data = extract(content, cpointKeySet, "cpoint");
    List<String> values = new ArrayList<>(List.of(data.remove("x"), data.remove("y")));
    if (data.get("kind").equals("1")) {  // grabber
      values.add(data.remove("vaction"));
      values.add(data.remove("decrease"));
      for (Map.Entry<String, String> entry : data.entrySet()) {
        String val = entry.getValue();
        if (val.equals("-842150451")) {
          continue;
        }
        String key = entry.getKey();
        if (key.equals("hurtable") && val.equals("1")) {
          continue;
        }
        values.add(String.format("\"%s %s\"", key, val));
      }
    } else {  // grabbee
      values.add(data.get("fronthurtact"));
      values.add(data.get("backhurtact"));
    }
    return String.format("new Cpoint(%s)", String.join(", ", values));
  }

  String wpointBlock(String content) {
    Map<String, String> data = extract(content, wpointKeySet, "wpoint");
    List<String> values = new ArrayList<>(List.of(
        data.get("x"), data.get("y"), data.get("weaponact"), data.get("cover")
    ));
    int dvx = Integer.valueOf(data.getOrDefault("dvx", "0"));
    int dvy = Integer.valueOf(data.getOrDefault("dvy", "0"));
    int dvz = Integer.valueOf(data.getOrDefault("dvz", "0"));
    if (dvx != 0 || dvy != 0 || dvz != 0) {
      return String.format("new Wpoint(%s, %d, %d, %d)", String.join(", ", values), dvx, dvy, dvz);
    }

    String attacking = data.get("kind").equals("3") ? "-1" : data.getOrDefault("attacking", "0");
    if (attacking != "0") {
      values.add(attacking);
    }
    return String.format("new Wpoint(%s)", String.join(", ", values));
  }

  String opointBlock(String content) {
    Map<String, String> data = extract(content, opointKeySet, "opoint");
    String oid = oidMap.get(data.get("oid"));
    String method = null;
    List<String> values = new ArrayList<>(List.of(data.get("x"), data.get("y"), oid));
    if (data.get("kind").equals("2")) {
      method = "hold";
    } else {
      String facing = data.get("facing");
      method = facing.endsWith("0") ? "front" : "back";
      int amount = Math.min(1, Integer.valueOf(facing) / 10);
      values.add(data.get("dvx"));
      values.add(data.get("dvy"));
      values.add(data.get("action"));
      values.add(Integer.toString(amount));
    }
    return String.format("Opoint.%s(%s)", method, String.join(", ", values));
  }

  public static void main(String[] args) {
    if (args.length != 1 || !args[0].endsWith(".txt")) {
      System.err.println("Input error.");
      return;
    }

    processDataTxt();
    File file = new File(args[0]);
    String fileName = file.getName();
    Tuple<Integer, Integer> tuple = fileMap.get(fileName.substring(0, fileName.lastIndexOf(".")));
    if (tuple == null) {
      System.err.println("File not supported: " + args[0]);
      return;
    }

    String content = null;
    try (Scanner scanner = new Scanner(file)) {
      content = scanner.useDelimiter("\\Z").next();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    if (content == null) {
      System.err.println("Reading file error: " + args[0]);
      return;
    }

    RawTxtParser parser = new RawTxtParser(tuple);
    parser.doTask(content);
    try (PrintWriter writer = new PrintWriter(parser.className + ".java", "utf-8")) {
      parser.lineList.forEach(writer::println);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return;
  }

  void doTask(String content) {
    lineList.add("package lfx.data;");
    lineList.add("");
    lineList.add("import java.util.ArrayList;");
    lineList.add("import java.util.HashMap;");
    lineList.add("import java.util.List;");
    lineList.add("import java.util.Map;");
    lineList.add("import lfx.component.*;");
    lineList.add("import lfx.util.*;");
    lineList.add("");
    lineList.add(String.format("public final class %s extends %s {", className, baseClassName));
    lineList.add(String.format("%sprivate static %s singleton = null;", indent(1), className));
    lineList.add("");
    lineList.add(String.format("%spublic static synchronized %s of() {", indent(1), className));
    lineList.add(String.format("%sif (singleton != null) {", indent(2)));
    lineList.add(String.format("%sreturn singleton;", indent(3)));
    lineList.add(String.format("%s}", indent(2)));

    StringBuilder builder = new StringBuilder(128);
    Matcher matcher = bmpTagPattern.matcher(content);
    matcher.find();
    matcher.appendReplacement(builder, "");
    if (isHero) {
      heroBmpTag(matcher.group(1));
    } else if (isEnergy) {
      itemBmpTag(matcher.group(1));
    } else {
      itemBmpTag(matcher.group(1));
      matcher.usePattern(strengthTagPattern);
      if (matcher.find()) {
        strengthTag(matcher.group(1));
        matcher.appendReplacement(builder, "");
      }
    }
    lineList.add("");
    lineList.add(String.format("%sFrame.Collector collector = new Collector(400, %s);",
                               indent(2), variableNaming.get("ObjectImage")
    ));

    int frameCount = 0;
    matcher.usePattern(frameTagPattern);
    while (matcher.find()) {
      ++frameCount;
      frameNumber = Integer.valueOf(matcher.group(1));
      frameNote = matcher.group(2).trim();
      frameTag(matcher.group(3));
      matcher.appendReplacement(builder, "");
    }
    matcher.appendTail(builder);
    content = builder.toString().trim();
    lineList.add("");
    lineList.add(String.format("%sreturn singleton = new %s();", indent(2), className));
    lineList.add(String.format("%s}", indent(1)));
    lineList.add("");
    lineList.add("}");
    System.err.printf("Find %d frames in %s. Remain: %s.%n", frameCount, className, content);
    return;
  }

}
