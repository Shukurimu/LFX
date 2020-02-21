package lfx.tool;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.util.Set;
import lfx.component.Bdy;
import lfx.component.Effect;
import lfx.component.Itr;
import lfx.component.State;
import lfx.object.Hero;
import lfx.util.Tuple;

/**
 * This parser is only reponsible for original LF2 version.
 */

public class Parser {
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
      "pic", "state", "wait", "next", "centerx", "centery",  // required
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
      "WeaponStrength", "strength",
      "FrameCollector", "collector"
  );

  final String className;
  final String baseClassName;
  final boolean isHero;
  final boolean isEnergy;
  final List<String> lineList = new ArrayList<>(4096);
  int frameNumber = 0;
  String frameNote = "";

  Parser(Tuple<Integer, Integer> tuple) {
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
    isHero = tuple.second.intValue() == 0;
    isEnergy = tuple.second.intValue() == 3;
  }

  void message(String formatter, Object... args) {
    System.err.printf("\t%s@%s(%d): %s%n",
                      className, frameNote, frameNumber,
                      String.format(formatter, args)
    );
    return;
  }

  static String wrapPath(String rawString) {
    return String.format("\"%s\"", rawString.replace("\\", "/"));
  }

  static String pureInt(String rawString) {
    return Integer.valueOf(rawString).toString();
  }

  static String indent(int level) {
    return "  ".repeat(level);
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

  /**
   * Builds information map of data.txt.
   * Pattern in use:
   *   id: +(\d+) +type: +(\d) .*?data\\(.*?)\.dat *#?(.*?)$
   *   buildMaps\((\1), (\2), "(\3)", "(\4)"\);
   */
  static void processDataTxt() {
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

  /**
   * Extracts every colon-separating key-value pair in given content.
   *
   * @param content the searching space
   * @param validKeys raise warning for keys not in this Set
   * @param blockName for debug message
   * @return a Map containing key-value pairs
   */
  private Map<String, String> extract(String content, Set<String> validKeys, String blockName) {
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

  String processImageLine(String tagContent) {
    final String varName = variableNaming.get("ObjectImage");
    lineList.add(String.format("%sList<Tuple<Image, Image>> %s = new ArrayList<>(256);",
                               indent(2), varName));
    StringBuilder builder = new StringBuilder(1024);
    Matcher matcher = imageFilePattern.matcher(tagContent);
    while (matcher.find()) {
      Map<String, String> format = extract(matcher.group(2), imageKeySet, "image");
      lineList.add(String.format("%s%s.addAll(loadImageCells(%s, %s, %s, %s, %s));",
                                 indent(2), varName,
                                 wrapPath(matcher.group(1)),
                                 pureInt(format.get("w")),
                                 pureInt(format.get("h")),
                                 pureInt(format.get("row")),
                                 pureInt(format.get("col"))
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
        lineList.add(String.format("%sImage %s = loadImage(%s);",
                                   indent(2),
                                   variableNaming.get("HeroPortrait"),
                                   wrapPath(keyValueMatcher.group(2))
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

  /**
   * Makes argument list String for Itr properties.
   * String contains kind, dvx, dvy, fall, bdefend, injury, vrest, scope, and optional attribute.
   *
   * @param state original state value, may affect some fields
   * @param data a map containing key-value pairs in Itr block
   * @return List of properties in determined order
   */
  private String getItrArguments(int state, Map<String, String> data) {
    Itr.Kind kind = null;
    String scope = isHero ? "Scope.ITR_HERO" : isEnergy ? "Scope.ITR_ENERGY" : "Scope.ITR_WEAPON";
    String attribute = null;
    boolean state18Warning = state == 18;

    switch (Integer.valueOf(data.get("kind"))) {
      case 0:
        switch (Integer.valueOf(data.getOrDefault("effect", "0"))) {
          case 0:
            kind = Itr.Kind.PUNCH;
            break;
          case 1:
            kind = Itr.Kind.STAB;
            break;
          case 2:  // State19 Effect2 works like Effect20
            kind = state == 19 ? Itr.Kind.WEAKFIRE : Itr.Kind.FIRE;
            break;
          case 20:  // on fire
            kind = Itr.Kind.WEAKFIRE;
            scope = "Scope.ITR_ALL_HERO";
            state18Warning = false;
            break;
          case 21:
            kind = Itr.Kind.WEAKFIRE;
            break;
          case 22:
            kind = Itr.Kind.FIRE;
            attribute = "exp";
            break;
          case 23:
            kind = Itr.Kind.PUNCH;
            attribute = "exp";
            break;
          case 3:
            kind = Itr.Kind.ICE;
            break;
          case 30:
            kind = Itr.Kind.WEAKICE;
            break;
          case 4:
            kind = Itr.Kind.PUNCH;
            scope = "Scope.ITR_NON_HERO";
            break;
          default:
            message("Unknown effect %s", data.get("effect"));
        }
        break;
      case 4:
        kind = Itr.Kind.THROW_ATK;
        scope = "Scope.ITR_TEAMMATE_HERO";
        break;
      case 8:
        kind = Itr.Kind.HEAL;
        data.put("dvy", "100");
        attribute = "nolag";
        scope = "Scope.ITR_ALL_HERO";
        break;
      case 9:
        kind = Itr.Kind.SHIELD;
        break;
      case 10:
      case 11:
        kind = Itr.Kind.SONATA;
        attribute = "nolag";
        scope = "Scope.ITR_ENEMY_HERO | Scope.ITR_ALL_WEAPON";
        break;
      case 16:
        kind = Itr.Kind.ICE;
        attribute = "nolag";
        scope = "Scope.ITR_ENEMY_HERO";
        break;
      default:
        message("Unexpected kind %s", data.get("kind"));
    }
    if (state18Warning) {
      message("Unexpected state 18.");
    }
    if (kind == null) {
      kind = Itr.Kind.NONE;
      message("No kind specified.");
    }

    List<String> propertyList = new ArrayList<>(8);
    propertyList.add(kind.toString());
    for (String key : List.of("dvx", "dvy", "fall", "bdefend", "injury")) {
      String value = data.get(key);
      if (value == null) {
        message("Use default %s.", key);
        value = "null";
      } else {
        value = pureInt(value);
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
    propertyList.add(scope);
    if (attribute != null) {
      propertyList.add(attribute);
    }
    return String.join(", ", propertyList);
  }

  void strengthTag(String strengthContent) {
    final String mapName = variableNaming.get("WeaponStrength");
    lineList.add(String.format("%sMap<Integer, Itr> %s = new HashMap<>(4);", indent(2), mapName));
    Matcher matcher = strengthBlockPattern.matcher(strengthContent);
    while (matcher.find()) {
      Map<String, String> data = extract(matcher.group(2), itrKeySet, "strength");
      String itr = String.format("Itr.strength(%s)", getItrArguments(0, data));
      lineList.add(String.format("%s%s.add(%s, %s);",
                                 indent(2), mapName, pureInt(matcher.group(1)), itr));
    }
    return;
  }

  /**
   * Processes the value in `next` and `hit_x` fields.
   *
   * @param rawAction original action number
   * @return action number in first field, invisibility Effect or null in second field
   */
  private Tuple<String, String> parseAction(String rawAction) {
    int intValue = Math.abs(Integer.valueOf(rawAction));
    String rawResult = null;
    String invisibility = null;
    if (intValue == 1000) {
      rawResult = "ACT_REMOVAL";
    } else if (intValue == 999) {
      rawResult = "ACT_DEF";
    } else if (intValue > 1000) {
      rawResult = "ACT_DEF";
      invisibility = String.format("Tuple.of(%s, Effect.Value.last(%d))",
                                   Effect.INVISIBILITY, intValue - 1100);
    } else if (intValue == 0) {
      rawResult = "ACT_SELF";
    } else {
      rawResult = Integer.toString(intValue);
    }
    return new Tuple<>(rawAction.startsWith("-") ? ("-" + rawResult) : rawResult, invisibility);
  }

  Tuple<State, String> getState(int rawState, int curr) {
    Function<State, Tuple<State, String>> wrapper = state -> new Tuple<>(state, null);
    if (isHero) {
      if (curr == Hero.ACT_CROUCH1) {
        return wrapper.apply(State.LAND);
      }
      if (Hero.ACT_HEAVY_RUN > curr && curr >= Hero.ACT_HEAVY_WALK) {
        return wrapper.apply(State.HEAVY_WALK);
      }
      if (Hero.ACT_HEAVY_STOP_RUN > curr && curr >= Hero.ACT_HEAVY_RUN) {
        return wrapper.apply(State.HEAVY_RUN);
      }
      if (Hero.ACT_ROWING2 > curr && curr >= Hero.ACT_ROLLING) {
        return wrapper.apply(State.NORMAL);
      }
      if (rawState == 18 && (curr < Hero.ACT_UPWARD_FIRE || curr >= Hero.ACT_TIRED)) {
        return wrapper.apply(State.NORMAL);
      }
    }
    switch (rawState) {
      case 0: return wrapper.apply(State.STAND);
      case 1: return wrapper.apply(State.WALK);
      case 2: return wrapper.apply(State.RUN);
      case 3: return wrapper.apply(State.NORMAL);  // (attack action) no use
      case 4: return wrapper.apply(State.JUMP);
      case 5: return wrapper.apply(State.DASH);
      case 6: return wrapper.apply(State.ROW);
      case 7: return wrapper.apply(State.DEFEND);
      case 8: return wrapper.apply(State.NORMAL);  // (broken_defend) no use
      case 9: return wrapper.apply(State.GRASP);
      case 10: return wrapper.apply(State.GRASP);
      case 11: return wrapper.apply(State.NORMAL);
      case 12: return wrapper.apply(State.FALL);
      case 13: return wrapper.apply(State.NORMAL);
      case 14: return wrapper.apply(State.LYING);
      case 15: return wrapper.apply(State.NORMAL);
      case 16: return wrapper.apply(State.NORMAL);
      case 17: return wrapper.apply(State.DRINK);
      case 18: return wrapper.apply(State.FIRE);  // only used in hero on fire actions
      case 19: return wrapper.apply(State.NORMAL);  // (Firen firerun) use dvz and visual effect
      case 100:  // (Louis landing)
        return new Tuple<>(State.NORMAL, String.format(
            "Tuple.of(%s, Effect.Value.until(94))", Effect.LANDING_ACT));
      case 301: return wrapper.apply(State.NORMAL);  // (Deep chop_series) use dvz
      case 400:  // (Woody teleport enemy)
        return new Tuple<>(State.NORMAL, String.format(
            "Tuple.of(%s, Effect.Value.once(120.0))", Effect.TELEPORT_ENEMY));
      case 401:  // (Woody teleport teammate)
        return new Tuple<>(State.NORMAL, String.format(
            "Tuple.of(%s, Effect.Value.once(60.0))", Effect.TELEPORT_TEAM));
      case 500: return wrapper.apply(State.TRY_TRANSFORM);
      case 501: return wrapper.apply(State.NORMAL);  // (transformback) use Effect
      case 1000: return wrapper.apply(State.IN_THE_SKY);
      case 1001: return wrapper.apply(State.ON_HAND);
      case 1002: return wrapper.apply(State.THROWING);
      case 1003: return wrapper.apply(State.JUST_ON_GROUND);
      case 1004: return wrapper.apply(State.ON_GROUND);
      case 1700:  // (John healing)
        return new Tuple<>(State.NORMAL, String.format(
            "Tuple.of(%s, Effect.Value.last(100, 1.0))", Effect.HEALING));
      case 2000: return wrapper.apply(State.IN_THE_SKY);
      case 2001: return wrapper.apply(State.ON_HAND);
      case 2004: return wrapper.apply(State.ON_GROUND);  // unknown
      case 3000: return wrapper.apply(State.NORMAL);
      case 3001: return wrapper.apply(State.HITTING);
      case 3002: return wrapper.apply(State.HIT);
      case 3003: return wrapper.apply(State.REBOUND);
      case 3004: return wrapper.apply(State.HIT);  // unknown real effect
      case 3005: return wrapper.apply(State.ENERGY);
      case 3006: return wrapper.apply(State.PIERCE);
      case 9995:  // (transform into LouisEX)
        return new Tuple<>(State.NORMAL, String.format(
            "Tuple.of(%s, Effect.Value.once(\"LouisEX\"))", Effect.TRANSFORM_INTO));
      case 9996:  // (create Louis armours)
        return new Tuple<>(State.NORMAL, String.format(
            "Tuple.of(%s, Effect.Value.once())", Effect.CREATE_ARMOUR));
      case 9998: return wrapper.apply(State.UNIMPLEMENTED);
      default:
        message("Unexpected raw state %d", rawState);
        return wrapper.apply(State.UNIMPLEMENTED);
    }
  }

  void frameTag(String frameContext) {
    List<Tuple<String, String>> blockList = new ArrayList<>(8);
    for (Pattern pattern : blockPatternList) {
      StringBuilder builder = new StringBuilder(1024);
      Matcher matcher = pattern.matcher(frameContext);
      while (matcher.find()) {  // blockName + blockContent
        blockList.add(new Tuple<>(matcher.group(1), matcher.group(2)));
        matcher.appendReplacement(builder, "");
      }
      matcher.appendTail(builder);
      frameContext = builder.toString();
    }

    Map<String, String> data = extract(frameContext, frameKeySet, "frame");
    final int rawState = Integer.valueOf(data.remove("state"));
    Tuple<State, String> stateTuple = getState(rawState, frameNumber);
    Tuple<String, String> nextTuple = parseAction(data.remove("next"));
    List<String> requiredList = new ArrayList<>(8);
    requiredList.add(Integer.toString(frameNumber));
    requiredList.add(pureInt(data.remove("wait")));
    requiredList.add(nextTuple.first);
    requiredList.add(stateTuple.first.toString());
    requiredList.add(pureInt(data.remove("pic")));
    requiredList.add(pureInt(data.remove("centerx")));
    requiredList.add(pureInt(data.remove("centery")));
    if (data.containsKey("sound")) {
      requiredList.add(wrapPath(data.remove("sound")));
    }

    if (rawState == 19 || rawState == 301) {
      data.put("dvz", "3");  // uncertain value
    }
    List<String> optionalList = new ArrayList<>(8);
    for (String key : List.of("dvx", "dvy", "dvz", "mp")) {
      if (data.containsKey(key)) {
        String value = pureInt(data.remove(key));
        if (!value.equals("0")) {
          optionalList.add(String.format("\"%s\"", key));
          optionalList.add(key.startsWith("dv") && value.equals("550") ? "Const.DV_550" : value);
        }
      }
    }
    for (Map.Entry<String, String> entry : data.entrySet()) {  // hit_xxx
      int valueInt = Integer.valueOf(entry.getValue());
      String key = entry.getKey();
      String value = null;
      if (isHero) {
        if (valueInt == 0) {
          continue;
        }
        value = parseAction(entry.getValue()).first;
      } else {
        if (valueInt != 0 && key.equals("hit_j")) {
          valueInt -= 50;
        }
        value = Integer.toString(valueInt);
      }
      optionalList.add(String.format("\"%s\"", key));
      optionalList.add(value);
    }

    List<String> argsLine = new ArrayList<>();
    argsLine.add(String.join(", ", requiredList));
    if (!optionalList.isEmpty()) {
      argsLine.add(String.format("Map.of(%s)", String.join(", ", optionalList)));
    }
    if (stateTuple.second != null) {  // innate Effect
      argsLine.add(stateTuple.second);
    }
    if (nextTuple.second != null) {  // invisibility Effect
      argsLine.add(nextTuple.second);
    }
    for (Tuple<String, String> blockTuple : blockList) {
      switch (blockTuple.first) {
        case "bdy":
          argsLine.add(bdyBlock(rawState, blockTuple.second));
          break;
        case "itr":
          argsLine.add(itrBlock(rawState, blockTuple.second));
          break;
        case "wpoint":
          argsLine.add(wpointBlock(blockTuple.second));
          break;
        case "opoint":
          argsLine.add(opointBlock(blockTuple.second));
          break;
        case "cpoint":
          argsLine.add(cpointBlock(blockTuple.second));
          break;
        case "bpoint":
          break;
        default:
          message("Unknown block %s: %s", blockTuple.first, blockTuple.second);
      }
    }

    lineList.add(String.format("%scollector.add(  // %s", indent(2), frameNote));
    int count = argsLine.size();
    for (String line : argsLine) {
      lineList.add(String.format("%s%s%s", indent(4), line, --count > 0 ? "," : ""));
    }
    lineList.add(indent(2) + ");");
    return;
  }

  /**
   * Makes argument list String for Box.
   * String contains x, y, w, h, and optional zwidth.
   *
   * @param data a map containing key-value pairs in Bdy or Itr block
   * @return comma separating arguments String (parenthesis excluded)
   */
  private String getBoxArguments(Map<String, String> data) {
    List<String> argList = new ArrayList<>(6);
    for (String key : List.of("x", "y", "w", "h")) {
      argList.add(pureInt(data.get(key)));
    }
    String zwidth = data.get("zwidth");
    if (zwidth != null) {
      argList.add(pureInt(zwidth));
    }
    return String.join(", ", argList);
  }

  String bdyBlock(int state, String content) {
    Map<String, String> data = extract(content, bdyKeySet, "bdy");
    String boxArgs = getBoxArguments(data);
    List<String> attriList = new ArrayList<>(4);
    if (state == 13) {  // frozen
      attriList.add(Bdy.Attribute.FRIENDLY_FIRE.toString());
      attriList.add(Bdy.Attribute.IMMUNE_WEAKICE.toString());
    }
    String attribute = String.join(" ", attriList);
    return attribute.isEmpty() ? String.format("new Bdy(%s)", boxArgs)
                               : String.format("new Bdy(%s, \"%s\")", boxArgs, attribute);
  }

  String itrBlock(int state, String content) {
    Map<String, String> data = extract(content, itrKeySet, "itr");
    String boxArgs = String.format("new Box(%s)", getBoxArguments(data));
    switch (Integer.valueOf(data.get("kind"))) {
      case 1:
        return String.format("Itr.grab(%s, %s, %s, %s)", boxArgs, Boolean.FALSE.toString(),
                             pureInt(data.get("catchingact")), pureInt(data.get("caughtact")));
      case 2:
        return String.format("Itr.kind(%s, %s, %s)",
                             boxArgs, Itr.Kind.PICK, "Scope.ITR_ALL_WEAPON");
      case 3:
        return String.format("Itr.grab(%s, %s, %s, %s)", boxArgs, Boolean.TRUE.toString(),
                             pureInt(data.get("catchingact")), pureInt(data.get("caughtact")));
      case 5:
        return String.format("Itr.onHand(%s)", boxArgs);
      case 6:
        return String.format("Itr.kind(%s, %s, %s)",
                             boxArgs, Itr.Kind.LET_SPUNCH, "Scope.ITR_ENEMY_HERO");
      case 7:
        return String.format("Itr.kind(%s, %s, %s)",
                             boxArgs, Itr.Kind.ROLL_PICK, "Scope.ITR_ALL_WEAPON");
      case 14:
        return String.format("Itr.kind(%s, %s, %s)",
                             boxArgs, Itr.Kind.BLOCK, "Scope.ITR_EVERYTHING");
      case 15:
        return String.format("Itr.kind(%s, %s, %s)",
                             boxArgs, Itr.Kind.VORTEX, "Scope.ITR_NON_ENERGY");
      default:
        return String.format("new Itr(%s, %s)", boxArgs, getItrArguments(state, data));
    }
  }

  String cpointBlock(String content) {
    Map<String, String> data = extract(content, cpointKeySet, "cpoint");
    List<String> argList = new ArrayList<>(16);
    argList.add(pureInt(data.remove("x")));
    argList.add(pureInt(data.remove("y")));
    if (pureInt(data.remove("kind")).equals("1")) {  // grabber
      argList.add(data.remove("vaction"));
      argList.add(data.remove("decrease"));
      for (Map.Entry<String, String> entry : data.entrySet()) {
        String val = entry.getValue();
        if (val.equals("-842150451")) {
          continue;
        }
        String key = entry.getKey();
        if (key.equals("hurtable") && val.equals("1")) {
          continue;
        }
        argList.add(String.format("\"%s %s\"", key, val));
      }
    } else {  // grabbee
      argList.add(data.get("fronthurtact"));
      argList.add(data.get("backhurtact"));
    }
    return String.format("new Cpoint(%s)", String.join(", ", argList));
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

    Parser parser = new Parser(tuple);
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
    lineList.add(String.format("%sFrame.Collector %s = new Collector(400, %s);",
                               indent(2),
                               variableNaming.get("FrameCollector"),
                               variableNaming.get("ObjectImage")
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

    List<String> argList = new ArrayList<>(4);
    argList.add(String.format("\"%s\"", className));
    argList.add(String.format("%s.getFrameList()", variableNaming.get("FrameCollector")));
    if (isHero) {
      argList.add(variableNaming.get("HeroStamina"));
      argList.add(variableNaming.get("HeroPortrait"));
    } else if (isEnergy) {
      argList.add(variableNaming.get("ItemInfo"));
    } else {
      argList.add(variableNaming.get("Subtype"));
      argList.add(variableNaming.get("ItemInfo"));
      argList.add(variableNaming.get("WeaponStrength"));
    }

    lineList.add("");
    lineList.add(String.format("%sreturn singleton = new %s(%s);",
                               indent(2), className, String.join(", ", argList)));
    lineList.add(String.format("%s}", indent(1)));
    lineList.add("");
    lineList.add("}");
    System.err.printf("Find %d frames in %s. Remaining: %s%n",
                      frameCount, className, content.isEmpty() ? "(empty)" : content);
    return;
  }

}
