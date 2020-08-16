package lfx.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import lfx.base.Type;

abstract class Extractor {
  static final Pattern keyValuePattern = Pattern.compile("([^:\\s]+) *: *(\\S+)");
  static final String BLOCK_FORMAT = "%1$s:\\s+(.*?)\\s+%1$s_end:";
  static Pattern buildBlockPattern(String blockName) {
    return Pattern.compile(String.format(BLOCK_FORMAT, blockName), Pattern.DOTALL);
  }

  protected final Type type;
  protected final int frameNumber;
  protected final String content;

  protected Extractor(Type type, int frameNumber, String content) {
    this.type = type;
    this.frameNumber = frameNumber;
    this.content = content;
  }

  protected abstract Pattern getPattern();
  protected abstract Extractor build(Type type, int frameNumber, String content);
  protected abstract String parseLines(int state);

  @Override
  public String toString() {
    return String.format("%s.%s.%d", this.getClass().getName(), type.toString(), frameNumber);
  }

  /**
   * Extracts every colon-separating key-value pair in given content.
   *
   * @param   content
   *          the searching space
   * @param   validKeys
   *          raise warning if key not in this Set
   * @param   blockName
   *          used in debug message
   * @return  a Map containing key-value pairs
   */
  static Map<String, String> extract(Set<String> validKeys, String content, String debugInfo) {
    Map<String, String> result = new HashMap<>(validKeys.size());
    Matcher matcher = keyValuePattern.matcher(content);
    while (matcher.find()) {
      String key = matcher.group(1);
      String value = matcher.group(2);
      if (validKeys.contains(key)) {
        result.put(key, value);
      } else {
        System.out.printf("[%s] Unknown KeyValue (%s, %s)%n", debugInfo, key, value);
      }
    }
    return result;
      }

  static Map<String, Integer> wrapInt(Map<String, String> stringMap) {
    Map<String, Integer> result = new HashMap<>(stringMap.size());
    for (Map.Entry<String, String> entry : stringMap.entrySet()) {
      result.put(entry.getKey(), Integer.valueOf(entry.getValue()));
    }
    return result;
  }

  /**
   * Makes argument list String for Box.
   * String contains x, y, w, h, and optional zwidth.
   *
   * @param   data
   *          a map containing key-value pairs in Bdy or Itr block
   * @return  comma separating arguments String (parenthesis excluded)
   */
  static String getBoxString(Map<String, Integer> data) {
    List<String> argList = new ArrayList<>(5);
    argList.add(data.remove("x").toString());
    argList.add(data.remove("y").toString());
    argList.add(data.remove("w").toString());
    argList.add(data.remove("h").toString());
    if (data.containsKey("zwidth")) {
      argList.add(data.remove("zwidth").toString());
    }
    return String.format("new Box(%s)", String.join(", ", argList));
  }

  static final Extractor bpointExtractor = new Extractor(null, 0, null) {
      final Pattern blockPattern = buildBlockPattern("bpoint");
      @Override protected Pattern getPattern() { return blockPattern; }
      @Override protected Extractor build(Type type, int fn, String content) { return null; }
      @Override protected String parseLines(int state) { return null; }
  };

}
