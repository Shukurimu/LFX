package lfx.tool;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Set;
import lfx.base.Type;

class CpointExtractor extends Extractor {
  static final Set<String> validKeys = Set.of(
      "kind", "x", "y", "injury", "vaction", "taction", "aaction", "jaction",
      "throwvx", "throwvy", "throwvz", "throwinjury", "fronthurtact", "backhurtact",
      "hurtable", "decrease", "dircontrol", "cover"
  );
  private static final Pattern blockPattern = buildBlockPattern("cpoint");
  private static final List<String> builderIntValueList = List.of(
      "injury", "taction", "aaction", "jaction", "cover"
  );
  private static final Integer DECREASE = Integer.valueOf(0);

  CpointExtractor(Type type, int frameNumber, String content) {
    super(type, frameNumber, content);
  }

  @Override
  protected Pattern getPattern() {
    return blockPattern;
  }

  @Override
  protected Extractor build(Type type, int frameNumber, String content) {
    return new CpointExtractor(type, frameNumber, content);
  }

  private static int getThrowValue(Integer value) {
    if (value == null) {
      return 0;
    }
    int intValue = value.intValue();
    return intValue == -842150451 ? 0 : intValue;
  }

  @Override
  protected String parseLines(int state) {
    Map<String, Integer> data = wrapInt(extract(validKeys, content, toString()));
    int x = data.get("x").intValue();
    int y = data.get("y").intValue();
    if (data.get("kind").intValue() == 2) {
      int fronthurtact = data.getOrDefault("fronthurtact", 221);
      int backhurtact = data.getOrDefault("backhurtact", 223);
      if (fronthurtact == 221 && backhurtact == 223) {
        return String.format("Cpoint.grabee(%d, %d)", x, y);
      } else {
        return String.format("Cpoint.grabee(%d, %d, %d, %d)", x, y, fronthurtact, backhurtact);
      }
    }

    StringBuilder builder = new StringBuilder(128);
    builder.append(String.format("Cpoint.graber(%d, %d, %d, %d)",
                                 x, y,
                                 data.get("vaction").intValue(),
                                 data.getOrDefault("decrease", DECREASE).intValue()
    ));
    for (String key : builderIntValueList) {
      Integer value = data.get(key);
      if (value != null) {
        builder.append(String.format(".%s(%d)", key, value.intValue()));
      }
    }
    if (data.getOrDefault("dircontrol", Integer.valueOf(0)).intValue() == 1) {
      builder.append(".dircontrol()");
    }
    if (data.getOrDefault("hurtable", Integer.valueOf(0)).intValue() == 1) {
      builder.append(".hurtable()");
    }
    int throwinjury = getThrowValue(data.get("throwinjury"));
    if (throwinjury == -1) {
      throwinjury = 0;
      builder.append(".transform()");
    }
    int throwvx = getThrowValue(data.get("throwvx"));
    int throwvy = getThrowValue(data.get("throwvy"));
    int throwvz = getThrowValue(data.get("throwvz"));
    if ((throwvx | throwvy | throwvz | throwinjury) != 0) {
      builder.append(
          String.format(".doThrow(%d, %d, %d, %d)", throwvx, throwvy, throwvz, throwinjury)
      );
    }
    return builder.toString();
  }

}
