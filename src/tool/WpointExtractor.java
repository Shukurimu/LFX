package tool;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Set;
import base.Type;
import component.Wpoint;

class WpointExtractor extends Extractor {
  static final Set<String> validKeys = Set.of(
      "kind", "x", "y", "weaponact", "attacking", "dvx", "dvy", "dvz", "cover"
  );
  private static final Pattern blockPattern = buildBlockPattern("wpoint");
  private static final Integer ZERO = Integer.valueOf(0);

  WpointExtractor(Type type, int frameNumber, String content) {
    super(type, frameNumber, content);
  }

  @Override
  protected Pattern getPattern() {
    return blockPattern;
  }

  @Override
  protected Extractor build(Type type, int frameNumber, String content) {
    return new WpointExtractor(type, frameNumber, content);
  }

  @Override
  protected String parseLines(int state) {
    Map<String, Integer> data = wrapInt(extract(validKeys, content, toString()));
    String requiredArgs = String.join(", ", List.of(
        data.get("x").toString(),
        data.get("y").toString(),
        String.format("new Action(%d)", data.get("weaponact").intValue()),
        data.get("cover").toString()
    ));
    int dvx = data.getOrDefault("dvx", ZERO).intValue();
    int dvy = data.getOrDefault("dvy", ZERO).intValue();
    int dvz = data.getOrDefault("dvz", ZERO).intValue();
    if ((dvx | dvy | dvz) != 0) {
      return String.format("Wpoint.release(%s, true, %d, %d, %d)", requiredArgs, dvx, dvy, dvz);
    }
    if (data.get("kind").intValue() == 3) {
      return String.format("Wpoint.release(%s, false, %d, %d, %d)", requiredArgs, dvx, dvy, dvz);
    }
    switch (data.get("attacking").intValue()) {
      case 0:
        return String.format("Wpoint.hold(%s)", requiredArgs);
      case 1:
        return String.format("Wpoint.attack(%s, \"%s\")", requiredArgs, Wpoint.Usage.NORMAL);
      case 2:
        return String.format("Wpoint.attack(%s, \"%s\")", requiredArgs, Wpoint.Usage.JUMP);
      case 3:
        return String.format("Wpoint.attack(%s, \"%s\")", requiredArgs, Wpoint.Usage.RUN);
      case 4:
        return String.format("Wpoint.attack(%s, \"%s\")", requiredArgs, Wpoint.Usage.DASH);
    }
    System.out.printf("[%s] Unclassifiable %s%n", toString(), content);
    return null;
  }

}
