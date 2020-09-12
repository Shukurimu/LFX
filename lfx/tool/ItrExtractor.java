package lfx.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Set;
import lfx.base.Type;
import lfx.component.Action;
import lfx.component.Itr;

class ItrExtractor extends Extractor {
  static final Set<String> validKeys = Set.of(
      "kind", "x", "y", "w", "h", "zwidth", "dvx", "dvy",
      "fall", "bdefend", "injury", "vrest", "arest", "catchingact", "caughtact", "effect"
  );
  private static final Pattern blockPattern = buildBlockPattern("itr");
  private static final Integer AREST = -7;  // hero default
  private static final Integer VREST = 9;  // weapon default
  private static final Integer DVX = 0;
  private static final Integer DVY = -7;
  private static final Integer FALL = 20;
  private static final Integer BDEFEND = 0;
  private static final Integer INJURY = 0;

  ItrExtractor(Type type, int frameNumber, String content) {
    super(type, frameNumber, content);
  }

  @Override
  protected Pattern getPattern() {
    return blockPattern;
  }

  @Override
  protected Extractor build(Type type, int frameNumber, String content) {
    return new ItrExtractor(type, frameNumber, content);
  }

  @Override
  protected String parseLines(int state) {
    Map<String, Integer> data = wrapInt(extract(validKeys, content, toString()));
    String boxString = getBoxString(data);
    switch (data.get("kind").intValue()) {
      case 1:
        return String.format("Itr.grab(%s, false, %s, %s)",
                             boxString, data.get("catchingact"), data.get("caughtact"));
      case 2:
        return String.format("Itr.kind(Itr.Kind.%s, %s, %s)",
                             boxString, Itr.Kind.PICK, "Scope.ITR_ALL_WEAPON");
      case 3:
        return String.format("Itr.grab(%s, true, %s, %s)",
                             boxString, data.get("catchingact"), data.get("caughtact"));
      case 5:
        return String.format("Itr.onHand(%s)", boxString);
      case 7:
        return String.format("Itr.kind(Itr.Kind.%s, %s, %s)",
                             boxString, Itr.Kind.ROLL_PICK, "Scope.ITR_ALL_WEAPON");
      case 14:
        return String.format("Itr.kind(Itr.Kind.%s, %s, %s)",
                             boxString, Itr.Kind.BLOCK, "Scope.ITR_EVERYTHING");
      case 15:
        return String.format("Itr.kind(Itr.Kind.%s, %s, %s)",
                             boxString, Itr.Kind.VORTEX, "Scope.ITR_NON_ENERGY");
      default:
        return String.format("new Itr(%s, %s)", boxString, getItrArguments(state, data));
    }
  }

  /**
   * Makes argument list String for Itr properties.
   * String contains kind, dvx, dvy, fall, bdefend, injury, vrest, scope, and optional attribute.
   *
   * @param   state
   *          original state value
   * @param   data
   *          a map containing key-value pairs in Itr block
   * @return  remaining formatted argument String
   */
  private String getItrArguments(int state, Map<String, Integer> data) {
    Itr.Kind kind = null;
    String scope = type.isHero ? "Scope.ITR_HERO" :
                   type.isWeapon ? "Scope.ITR_WEAPON": "Scope.ITR_ENERGY";
    int effect = data.getOrDefault("effect", 0);
    boolean state18Warning = state == 18;
    boolean twoSides = false;
    boolean actPause = true;

    switch (data.get("kind").intValue()) {
      case 0:
        kind = getKind(state, effect);
        if (effect == 22 || effect == 23) {
          twoSides = true;
        } else if (effect == 20) {
          scope = "Scope.ITR_ALL_HERO";
          state18Warning = false;
        } else if (effect == 4) {
          scope = "Scope.ITR_NON_HERO";
        }
        break;
      case 4:
        kind = Itr.Kind.THROW_DAMAGE;
        scope = "Scope.ITR_TEAMMATE_HERO";
        break;
      case 6:
        kind = Itr.Kind.FORCE_ACT;
        data.put("dvx", Action.HERO_SUPER_PUNCH.index);
        scope = "Scope.ITR_ENEMY_HERO";
      case 8:
        kind = Itr.Kind.HEAL;
        data.put("dvy", 100);
        actPause = false;
        scope = "Scope.ITR_ALL_HERO";
        break;
      case 9:
        kind = Itr.Kind.SHIELD;
        break;
      case 10:
      case 11:
        kind = Itr.Kind.SONATA;
        actPause = false;
        scope = "Scope.ITR_ENEMY_HERO | Scope.ITR_ALL_WEAPON";
        break;
      case 16:
        kind = Itr.Kind.ICE;
        actPause = false;
        scope = "Scope.ITR_ENEMY_HERO";
        break;
      default:
        System.out.printf("[%s] Unexpected kind %s%n", toString(), data.get("kind"));
    }
    if (state18Warning) {
      System.out.printf("[%s] Unexpected itr state 18%n", toString());
    }

    if (data.containsKey("arest")) {
      data.put("vrest", -data.get("arest").intValue());
    }

    List<String> argList = new ArrayList<>(12);
    argList.add("Itr.Kind." + kind.toString());
    argList.add(scope);
    argList.addAll(getCommonArgs(data, type.isHero));
    if (twoSides) {
      argList.add("true");
    }
    if (!actPause) {
      argList.add("0");
    }
    return String.join(", ", argList);
  }

  static Itr.Kind getKind(int rawState, int rawEffect) {
    switch (rawEffect) {
      case 0: return Itr.Kind.PUNCH;
      case 1: return Itr.Kind.STAB;
      case 2: return rawState == 19 ? Itr.Kind.WEAK_FIRE : Itr.Kind.FIRE;
      case 3: return Itr.Kind.ICE;
      case 4: return Itr.Kind.PUNCH;
      case 20: return Itr.Kind.WEAK_FIRE;
      case 21: return Itr.Kind.WEAK_FIRE;
      case 22: return Itr.Kind.FIRE;
      case 23: return Itr.Kind.PUNCH;
      case 30: return Itr.Kind.WEAK_ICE;
      default:
        System.out.printf("\tUnknown itr effect %d%n", rawEffect);
        return null;
    }
  }

  static List<String> getCommonArgs(Map<String, Integer> data, boolean isHero) {
    return List.of(
        data.getOrDefault("vrest", isHero ? AREST : VREST).toString(),
        data.getOrDefault("dvx", DVX).toString(),
        data.getOrDefault("dvy", DVY).toString(),
        data.getOrDefault("fall", FALL).toString(),
        data.getOrDefault("bdefend", BDEFEND).toString(),
        data.getOrDefault("injury", INJURY).toString()
    );
  }

}
