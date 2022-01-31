package tool;

import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import base.Type;
import component.Bdy;
import component.Cpoint;
import component.Frame;
import component.Itr;
import component.Opoint;
import component.Wpoint;
import ecosystem.BaseEnergy;
import ecosystem.BaseHero;
import ecosystem.BaseWeapon;
import util.IntMap;
import util.Tuple;

/**
 * This parser is only reponsible for original LF2 version.
 */
public class Parser {
  private static final System.Logger logger = System.getLogger("");

  final Type type;
  final String identifier;
  final String baseClassName;
  final LinkedList<String> lineList = new LinkedList<>();
  final Map<String, Double> stamina = new LinkedHashMap<>();
  final Map<String, String> bmpInfo = new LinkedHashMap<>();
  final List<String> pictureInfo = new ArrayList<>();
  final Map<Wpoint.Usage, String> strengthMap = new EnumMap<>(Wpoint.Usage.class);
  int indentLevel = 0;

  Parser(Type type, String identifier) {
    this.type = type;
    this.identifier = identifier;
    baseClassName = (switch (type) {
      case HERO -> BaseHero.class;
      case DRINK, HEAVY, LIGHT, SMALL -> BaseWeapon.class;
      case ENERGY -> BaseEnergy.class;
      default -> throw new IllegalArgumentException();
    }).getSimpleName();
  }

  static String asPath(String rawString) {
    return String.format("\"%s\"", rawString.replace("\\", "/"));
  }

  static final Pattern INT_PATTERN = Pattern.compile("(\\w+) *: *([-0-9]+)");
  static final Pattern STRING_PATTERN = Pattern.compile("(\\w+) *: *(\\S+)");

  /**
   * Extracts every colon-separating key-value pair in given content.
   * Values are converted to {@code Integer}s.
   * Shows warning if there are contents cannot be parsed.
   *
   * @param content the searching space
   * @return a Map containing key-value pairs
   */
  static IntMap parseIntValue(String content) {
    HashMap<String, Integer> result = new HashMap<>(32);
    StringBuilder sentinel = new StringBuilder(64);
    Matcher matcher = INT_PATTERN.matcher(content);
    while (matcher.find()) {
      matcher.appendReplacement(sentinel, "");
      String key = matcher.group(1);
      String value = matcher.group(2);
      result.put(key, Integer.valueOf(value));
    }
    matcher.appendTail(sentinel);

    String remaining = sentinel.toString().trim();
    if (!remaining.isEmpty()) {
      logger.log(Level.WARNING, "NoMapping [[{0}]]\n{1}", remaining, content);
    }
    return IntMap.of(result);
  }

  /**
   * Extracts every colon-separating key-value pair in given content.
   *
   * @param content the searching space
   * @return a Map containing key-value pairs
   */
  static Map<String, String> parseStringValue(String content) {
    Map<String, String> result = new HashMap<>(32);
    Matcher matcher = STRING_PATTERN.matcher(content);
    while (matcher.find()) {
      String key = matcher.group(1);
      String value = matcher.group(2);
      result.put(key, value);
    }
    return result;
  }

  static final Pattern IMAGE_CELL_PATTERN = Pattern.compile("file[^:]*: *(\\S+) +(.*)");
  static final Pattern STAMINA_PATTERN = Pattern.compile("(\\w+) +(\\S+)");

  void processBmpTag(String bmpContent) {
    StringBuilder remaining = new StringBuilder(256);
    // file(0-69): sprite\template1\0.bmp  w: 79  h: 79  row: 10  col: 7
    Matcher matcher = IMAGE_CELL_PATTERN.matcher(bmpContent);
    while (matcher.find()) {
      matcher.appendReplacement(remaining, "");
      IntMap data = parseIntValue(matcher.group(2));
      pictureInfo.add(
          String.format("util.Tuple.of(%s, new int[] { %d, %d, %d, %d })",
          asPath(matcher.group(1)),
          data.pop("w"), data.pop("h"), data.pop("row"), data.pop("col"))
      );
    }
    // walking_frame_rate 3
    // walking_speed 4.000000
    // walking_speedz 2.000000
    matcher.usePattern(STAMINA_PATTERN);
    while (matcher.find()) {
      matcher.appendReplacement(remaining, "");
      if (!matcher.group(1).endsWith("_rate")) {
        stamina.put(matcher.group(1), Double.valueOf(matcher.group(2)));
      }
    }
    matcher.appendTail(remaining);
    // =============== HERO ===============
    // name: Template
    // head: sprite\template1\face.bmp
    // small: sprite\template1\s.bmp
    // ============== WEAPON ==============
    // weapon_hp: 200
    // weapon_drop_hurt: 35
    // ========== WEAPON & ENERGY =========
    // weapon_hit_sound: data\023.wav
    // weapon_drop_sound: data\023.wav
    // weapon_broken_sound: data\066.wav
    bmpInfo.putAll(parseStringValue(remaining.toString()));
    return;
  }

  static final Pattern STRENGTH_BLOCK_PATTERN =
      Pattern.compile("entry *: *(\\d+) +\\S+(.*?)((?=entry)|$)", Pattern.DOTALL);

  void processStrengthTag(String strengthContent) {
    //  entry: 1 normal
    //    dvx: 2  fall: 40  vrest: 10 bdefend: 16  injury: 30  effect: 3
    //  entry: 2 jump
    //    dvx: 7  fall: 70  vrest: 10  bdefend: 16   injury: 30  effect: 3
    //  ...
    Matcher matcher = STRENGTH_BLOCK_PATTERN.matcher(strengthContent);
    while (matcher.find()) {
      Wpoint.Usage usage = Wpoint.convertUsage(Integer.parseInt(matcher.group(1)));
      IntMap data = parseIntValue(matcher.group(2));
      data.put("kind", 0);
      strengthMap.put(usage, Itr.extractStrength(data));
    }
    return;
  }

  static final Pattern BLOCK_PATTERN =
      Pattern.compile("(?<=\\s)(\\w+):(.*?)\\1_end:", Pattern.DOTALL);
  static final Pattern SOUND_PATTERN = Pattern.compile("sound *: *(\\S+)");

  static List<String> processFrameTag(Type type, int frameNumber, String frameContent) {
    List<Tuple<String, String>> pendingBlocks = new ArrayList<>();
    StringBuilder builder = new StringBuilder(1024);
    Matcher matcher = BLOCK_PATTERN.matcher(frameContent);
    while (matcher.find()) {
      logger.log(Level.DEBUG, matcher.group(1));
      matcher.appendReplacement(builder, "");
      if (!matcher.group(1).equals("bpoint")) {
        pendingBlocks.add(new Tuple<>(matcher.group(1), matcher.group(2).trim()));
      }
    }
    matcher.appendTail(builder);

    frameContent = SOUND_PATTERN.matcher(builder.toString().trim()).replaceFirst("");
    IntMap frameData = parseIntValue(frameContent);
    int rawState = frameData.pop("state");
    List<String> result = Frame.extract(frameData, type, rawState, frameNumber);

    for (Tuple<String, String> e : pendingBlocks) {
      IntMap data = parseIntValue(e.second);
      String content = switch (e.first) {
        case "opoint" -> Opoint.extract(data);
        case "wpoint" -> Wpoint.extract(data);
        case "cpoint" -> Cpoint.extract(data);
        case "bdy" -> Bdy.extract(data, rawState);
        case "itr" -> Itr.extract(data, rawState, type);
        default -> throw new IllegalArgumentException(e.toString());
      };
      result.add(".add(%s)".formatted(content));
      for (Map.Entry<String, Integer> x : data.entrySet()) {
        logger.log(Level.WARNING, "{0} remains {1}\n{2}", e.first, x, e.second);
      }
    }

    String last = result.remove(result.size() - 1);
    result.add(last + ";");
    return result;
  }

  void emplaceFileContent() {
    String importLib = "";
    String constructor = null;
    String preparation = null;
    switch (type) {
      case HERO: {
        importLib = """
        import java.util.HashMap;
        """;
        constructor = """
        private %s(Frame.Collector collector, HashMap<String, Double> stamina) {
          super(collector, %s, stamina);
        }
        """.formatted(identifier, asPath(bmpInfo.get("head")));
        List<String> statement = new ArrayList<>(16);
        statement.add("var stamina = new HashMap<String, Double>();");
        for (Map.Entry<String, Double> e : stamina.entrySet()) {
          statement.add("stamina.put(\"%s\", %.2f);".formatted(e.getKey(), e.getValue()));
        }
        statement.add("singleton = new %s(collector, stamina);".formatted(identifier));
        preparation = String.join("\n", statement);
        break;
      }
      case LIGHT:
      case HEAVY:
      case SMALL:
      case DRINK: {
        importLib = """
        import java.util.EnumMap;
        """;
        constructor = """
        private %s(Frame.Collector collector, EnumMap<Wpoint.Usage, Itr> strength) {
          super(collector, %s, strength);
          hpMax = %d;
          dropHurt = %d;
          soundHit = %s;
          soundDrop = %s;
          soundBroken = %s;
        }
        """.formatted(
            identifier, type,
            Integer.valueOf(bmpInfo.get("weapon_hp")),
            Integer.valueOf(bmpInfo.get("weapon_drop_hurt")),
            asPath(bmpInfo.get("weapon_hit_sound")),
            asPath(bmpInfo.get("weapon_drop_sound")),
            asPath(bmpInfo.get("weapon_broken_sound")));

        List<String> statement = new ArrayList<>(16);
        statement.add("var strength = new EnumMap<Wpoint.Usage, Itr>(Wpoint.Usage.class);");
        for (Map.Entry<Wpoint.Usage, String> e : strengthMap.entrySet()) {
          statement.add("strength.put(%s, %s);".formatted(e.getKey(), e.getValue()));
        }
        statement.add("singleton = new %s(collector, strength);".formatted(identifier));
        preparation = String.join("\n", statement);
        break;
      }
      case ENERGY:
        constructor = """
        private %s(Frame.Collector collector) {
          super(collector);
          soundHit = %s;
          soundDrop = %s;
          soundBroken = %s;
        }
        """.formatted(
            identifier,
            asPath(bmpInfo.get("weapon_hit_sound")),
            asPath(bmpInfo.get("weapon_drop_sound")),
            asPath(bmpInfo.get("weapon_broken_sound")));
        preparation = "singleton = new %s(collector);".formatted(identifier);
        break;
      default:
        throw new IllegalArgumentException(type.toString());
    }

    lineList.addFirst("""
    package data;

    import java.util.List;
    %3$s
    import base.*;
    import component.*;

    public class %1$s extends ecosystem.%2$s {
      private static %1$s singleton = null;
      \n%4$s
      @Override public List<util.Tuple<String, int[]>> getPictureInfo() {
        return List.of(
          %5$s
        );
      }

      public static %1$s register() {
        if (singleton != null) { return singleton; }

        var collector = new Frame.Collector();
    """.formatted(identifier, baseClassName, importLib, constructor.indent(2),
                  String.join(",\n" + " ".repeat(6), pictureInfo)));

    lineList.addLast("""
        // bmp content\n%s
        return singleton;
      }

    }
    """.formatted(preparation.indent(4)));
    return;
  }

  void parse(String content) {
    Pattern TAG_PATTERN = Pattern.compile("<(\\w+)(?:_begin)?>\\s+(.*?)<\\1_end>", Pattern.DOTALL);
    Matcher matcher = TAG_PATTERN.matcher(content);
    StringBuilder builder = new StringBuilder(128);
    int frameCount = 0;
    while (matcher.find()) {
      logger.log(Level.DEBUG, "<{0}>", matcher.group(1));
      logger.log(Level.TRACE, "\n{0}", matcher.group(2));
      matcher.appendReplacement(builder, "");
      switch (matcher.group(1)) {
        case "bmp":
          processBmpTag(matcher.group(2));
          break;
        case "weapon_strength_list":
          processStrengthTag(matcher.group(2));
          break;
        case "frame": {
          // <frame> 207 tired
          // pic: 69  state: 15  wait: 2  next: 0
          // ...
          // <frame_end>
          String[] part = matcher.group(2).split("\n", 2);
          logger.log(Level.DEBUG, "[{0}]", part[0]);
          logger.log(Level.TRACE, "\n{0}", part[1]);
          int splitIndex = part[0].indexOf(" ");
          int frameNumber = Integer.parseInt(part[0].substring(0, splitIndex));

          lineList.add("    collector  //" + part[0].substring(splitIndex));
          for (String s : processFrameTag(type, frameNumber, part[1])) {
            lineList.add(" ".repeat(6) + s);
          }
          lineList.add("");
          ++frameCount;
          break;
        }
        default:
          throw new IllegalArgumentException(matcher.group(1));
      }
    }

    matcher.appendTail(builder);
    content = builder.toString().trim();
    System.err.printf("Collect %d frames in %s. Remaining: %s%n",
                      frameCount, identifier, content.isEmpty() ? "(empty)" : content);
    emplaceFileContent();
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Input error.");
      return;
    }
    Path sourcePath = Path.of(args[0]);
    String fileName = sourcePath.getFileName().toString().split("\\.")[0];
    Tuple<Type, String> info = Type.getInfo(fileName);
    if (info == null) {
      System.err.println("File not supported: " + args[0]);
      return;
    }

    String content = null;
    try {
      content = Files.readString(sourcePath);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println("Reading file error: " + args[0]);
      return;
    }

    Parser parser = new Parser(info.first, info.second);
    parser.parse(content);

    Path targetPath = Path.of("src", "data", fileName = info.second + ".java");
    try {
      Files.createDirectories(targetPath.getParent());
      Files.write(targetPath, parser.lineList,
                  StandardOpenOption.CREATE,
                  StandardOpenOption.TRUNCATE_EXISTING,
                  StandardOpenOption.WRITE
      );
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println("Writing file error: " + fileName);
    }
    return;
  }

}
