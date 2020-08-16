package lfx.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import lfx.base.Action;
import lfx.base.Type;
import lfx.component.State;
import lfx.tool.BdyExtractor;
import lfx.tool.CpointExtractor;
import lfx.tool.Extractor;
import lfx.tool.ItrExtractor;
import lfx.tool.OpointExtractor;
import lfx.tool.WpointExtractor;

class FrameExtractor extends Extractor {
  private static final List<? extends Extractor> extractorBaseList = List.of(
      new WpointExtractor(null, 0, null),
      new ItrExtractor(null, 0, null),
      new BdyExtractor(null, 0, null),
      new OpointExtractor(null, 0, null),
      new CpointExtractor(null, 0, null),
      Extractor.bpointExtractor
  );
  private static final Set<String> frameKeySet = Set.of(
      "pic", "state", "wait", "next", "centerx", "centery", "sound",
      "dvx", "dvy", "dvz", "mp", "hit_a", "hit_d", "hit_j",
      "hit_Fa", "hit_Fj", "hit_Ua", "hit_Uj", "hit_Da", "hit_Dj", "hit_ja"
  );

  private List<String> argsLine = null;
  private int rawState = 0;

  FrameExtractor(Type type, int frameNumber, String content) {
    super(type, frameNumber, content);
  }

  @Override
  protected Pattern getPattern() {
    return null;
  }

  @Override
  protected Extractor build(Type type, int frameNumber, String content) {
    throw new UnsupportedOperationException();
  }

  static String asPath(String rawString) {
    return String.format("\"%s\"", rawString.replace("\\", "/"));
  }

  /**
   * Processes the value in `next` and `hit_x` tags.
   *
   * @param   intValue
   *          original action number
   * @return  target Action
   */
  private String getAction(int intValue) {
    int absValue = Math.abs(intValue);
    if (absValue == 1000) {
      return "Action.REMOVAL";
    }
    if (absValue == 0) {
      return "Action.REPEAT";
    }
    if (absValue == 999) {
      return intValue >= 0 ? "Action.DEFAULT" : "Action.DEFAULT_REVERSE";
    }
    if (absValue > 1000) {
      argsLine.add(String.format("Effect.INVISIBLE.of(%d)", absValue - 1100));
      return intValue >= 0 ? "Action.DEFAULT" : "Action.DEFAULT_REVERSE";
    }
    return String.format("new Action(%d)", intValue);
  }

  /**
   * Processes `state` tag, while taking frame number into account.
   * Also inserts Effect for special states.
   *
   * @return  resulting State
   */
  private State getState() {
    if (type.isHero) {
      if (frameNumber == Action.HERO_CROUCH1.index) {
        return State.LANDING;
      }
      if (Action.HERO_HEAVY_WALK.includes(frameNumber)) {
        return State.HEAVY_WALK;
      }
      if (Action.HERO_HEAVY_RUN.includes(frameNumber)) {
        return State.HEAVY_RUN;
      }
      if (Action.HERO_ROLLING.includes(frameNumber)) {
        return State.NORMAL;
      }
      if (rawState == 18) {  // only used in hero on fire actions
        boolean fire = Action.HERO_UPWARD_FIRE.includes(frameNumber) ||
                       Action.HERO_DOWNWARD_FIRE.includes(frameNumber);
        return fire ? State.FIRE : State.NORMAL;
      }
    }
    switch (rawState) {
      case 0:  return State.STAND;
      case 1:  return State.WALK;
      case 2:  return State.RUN;
      case 3:  return State.NORMAL;  // (attack action) no use
      case 4:  return State.JUMP;
      case 5:  return State.DASH;
      case 6:  return State.FLIP;
      case 7:  return State.DEFEND;
      case 8:  return State.NORMAL;  // (broken_defend) no use
      case 9:  return State.GRAB;
      case 10: return State.GRAB;
      case 11: return State.NORMAL;
      case 12: return State.FALL;
      case 13: return State.NORMAL;
      case 14: return State.LYING;
      case 15: return State.NORMAL;
      case 16: return State.NORMAL;
      case 17: return State.DRINK;
      case 18: return State.NORMAL;
      case 19: return State.NORMAL;  // (Firen firerun) use dvz and visual effect
      case 100:  // (Louis landing)
                argsLine.add("Effect.LANDING_ACT.of(94)");
                return State.NORMAL;
      case 301: return State.NORMAL;  // (Deep chop_series) use dvz
      case 400:  // (Woody teleport enemy)
                argsLine.add("Effect.TELEPORT_ENEMY.of()");
                return State.NORMAL;
      case 401:  // (Woody teleport teammate)
                argsLine.add("Effect.TELEPORT_TEAM.of()");
                return State.NORMAL;
      case 500: return State.TRY_TRANSFORM;
      case 501: return State.NORMAL;  // (transformback) use Effect
      case 1000: return State.IN_THE_SKY;
      case 1001: return State.ON_HAND;
      case 1002: return State.THROWING;
      case 1003: return State.JUST_ON_GROUND;
      case 1004: return State.ON_GROUND;
      case 1700:  // (John healing)
                 argsLine.add("Effect.HEALING.of(100)");
                 return State.NORMAL;
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
      case 9995:  // TODO: (transform into LouisEX)
                 return State.UNIMPLEMENTED;
      case 9996:  // (create Louis armours)
                 argsLine.add("Effect.CREATE_ARMOUR.of()");
                 return State.NORMAL;
      case 9997: return State.UNIMPLEMENTED;
      case 9998: return State.UNIMPLEMENTED;
      case 9999: return State.UNIMPLEMENTED;
    }
    System.out.printf("[%s] Unexpected raw state %d%n", toString(), rawState);
    return State.UNIMPLEMENTED;
  }

  private String parseFrameDvxyz(Integer rawValue) {
    return rawValue == null ? "0" :
           rawValue.intValue() == 550 ? "Frame.RESET_VELOCITY" : rawValue.toString();
  }

  @Override
  protected String parseLines(int nouse) {
    argsLine = new ArrayList<>(16);
    argsLine.add(null);  // reserve for basic arguments

    Map<String, String> allData = extract(frameKeySet, content, toString());
    if (allData.containsKey("sound")) {
      argsLine.add(asPath(allData.remove("sound")));
    }
    Map<String, Integer> data = wrapInt(allData);
    rawState = data.remove("state").intValue();
    if (rawState == 19 || rawState == 301) {
      data.put("dvz", 3);  // uncertain value
    }
    if (type.isEnergy && data.containsKey("hit_j")) {
      data.put("dvz", data.remove("hit_j") - 50);
    }

    argsLine.set(0, String.join(", ", List.of(
        Integer.toString(frameNumber),
        data.remove("wait").toString(),
        getState().toString(),
        data.remove("pic").toString(),
        data.remove("centerx").toString(),
        data.remove("centery").toString(),
        parseFrameDvxyz(data.remove("dvx")),
        parseFrameDvxyz(data.remove("dvy")),
        parseFrameDvxyz(data.remove("dvz")),
        getAction(data.remove("next").intValue())
    )));
    if (type.isHero && data.containsKey("mp")) {
      argsLine.add(String.format("Cost.of(%d)", data.remove("mp").intValue()));
    }
    if (type.isEnergy && data.containsKey("hit_a")) {
      argsLine.add(String.format("Cost.of(0, %d)", data.remove("hit_a").intValue()));
    }
    for (Map.Entry<String, Integer> entry : data.entrySet()) {  // hit_xxx
      String key = entry.getKey();
      if (!key.startsWith("hit_")) {
        System.out.printf("[%s] Unexpected hit_xxx %s%n", toString(), key);
        continue;
      }
      int intValue = entry.getValue().intValue();
      if (intValue == 0) {
        continue;
      }
      String value = null;
      if (type.isHero || key.equals("hit_d")) {
        value = getAction(intValue);
      } else if (key.equals("hit_Fa")) {
        switch (intValue) {
          case 1: value = "Action.JOHN_CHASE"; break;
          case 10: value = "Action.JOHN_CHASE_FAST"; break;
          case 2: value = "Action.DENNIS_CHASE"; break;
          default: value = "Action.UNASSIGNED"; break;
        }
      } else {
        System.out.printf("[%s] Unexpected hit_xxx %s %d%n", toString(), key, intValue);
        continue;
      }
      argsLine.add(String.format("new Tuple(Order.%s, %s)", key, value));
    }
    return null;
  }

  static List<String> getFrameArgumentLines(Type type, int frameNumber, String frameContent) {
    List<Extractor> pendingList = new ArrayList<>();
    for (Extractor extractorBase : extractorBaseList) {
      StringBuilder builder = new StringBuilder(1024);
      Matcher matcher = extractorBase.getPattern().matcher(frameContent);
      while (matcher.find()) {
        pendingList.add(extractorBase.build(type, frameNumber, matcher.group(1)));
        matcher.appendReplacement(builder, "");
      }
      matcher.appendTail(builder);
      frameContent = builder.toString();
    }

    FrameExtractor current = new FrameExtractor(type, frameNumber, frameContent);
    current.parseLines(0);
    for (Extractor extractor : pendingList) {
      if (extractor == null) {
        continue;  // no need
      }
      current.argsLine.add(extractor.parseLines(current.rawState));
    }
    return current.argsLine;
  }

}
