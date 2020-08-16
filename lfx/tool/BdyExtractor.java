package lfx.tool;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.Set;
import lfx.base.Type;
import lfx.tool.Extractor;

class BdyExtractor extends Extractor {
  static final Set<String> validKeys = Set.of(
      "kind", "x", "y", "w", "h"
  );
  private static final Pattern blockPattern = buildBlockPattern("bdy");

  BdyExtractor(Type type, int frameNumber, String content) {
    super(type, frameNumber, content);
  }

  @Override
  protected Pattern getPattern() {
    return blockPattern;
  }

  @Override
  protected Extractor build(Type type, int frameNumber, String content) {
    return new BdyExtractor(type, frameNumber, content);
  }

  @Override
  protected String parseLines(int state) {
    Map<String, Integer> data = wrapInt(extract(validKeys, content, toString()));
    String boxString = getBoxString(data);
    String attribute = "";
    if (type == Type.LIGHT || type == Type.SMALL || type == Type.DRINK) {
      attribute = "Bdy.ROLLING_PICKABLE";
    } else if (state == 12) {
      attribute = "Bdy.IMMUNE_FALL_40";
    } else if (state == 13) {
      attribute = "Bdy.FRIENDLY_FIRE | Bdy.IMMUNE_WEAK_ICE";
    } else if (state == 16) {
      attribute = "Bdy.DANCE_OF_PAIN";
    } else if (state == 18) {
      attribute = "Bdy.IMMUNE_WEAK_FIRE";
    } else {
      return String.format("new Bdy(%s)", boxString);
    }
    return String.format("new Bdy(%s, %s)", boxString, attribute);
  }

}
