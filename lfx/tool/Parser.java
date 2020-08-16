package lfx.tool;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import lfx.base.Type;
import lfx.tool.Extractor;
import lfx.tool.FrameExtractor;
import lfx.tool.ItrExtractor;
import lfx.tool.OpointExtractor;
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
  static final Pattern strengthBlockPattern =
      Pattern.compile("entry *: *\\d+ +(\\S+)(.*?)((?=entry)|$)", Pattern.DOTALL);
  static final Pattern imageFilePattern = Pattern.compile("file.*?: *(\\S+) +(.*)");
  static final Pattern staminaPattern = Pattern.compile("([_\\w]+) +(\\S+)");
  static final Set<String> imageKeySet = Set.of("w", "h", "row", "col");
  static final Set<String> heroStaminaKeys = Set.of("name", "head", "small");
  static final Set<String> itemStaminaKeys = Set.of(
      "weapon_hp", "weapon_drop_hurt",
      "weapon_hit_sound", "weapon_drop_sound", "weapon_broken_sound"
  );

  final Type type;
  final String identifier;
  final List<String> lineList = new ArrayList<>(2400);

  Parser(Type type, String identifier) {
    this.type = type;
    this.identifier = identifier;
  }

  static String indent(int level) {
    return "  ".repeat(level);
  }

  String processImageLine(String tagContent) {
    // file(0-99): sprite\sys\weapon7.bmp  w: 69  h: 69  row: 10  col: 4
    StringBuilder builder = new StringBuilder(1024);
    Matcher matcher = imageFilePattern.matcher(tagContent);
    while (matcher.find()) {
      Map<String, Integer> data =
          Extractor.wrapInt(Extractor.extract(imageKeySet, matcher.group(2), "Image"));
      lineList.add(indent(2) +
          String.format("imageList.addAll(ImageCell.loadImageCells(%s, %s, %s, %s, %s));",
                        FrameExtractor.asPath(matcher.group(1)),
                        data.get("w").toString(),
                        data.get("h").toString(),
                        data.get("row").toString(),
                        data.get("col").toString()
      ));
      matcher.appendReplacement(builder, "");
    }
    lineList.add("");
    matcher.appendTail(builder);
    return builder.toString();
  }

  void itemBmpTag(String bmpContent) {
    bmpContent = processImageLine(bmpContent);
    Map<String, String> data = Extractor.extract(itemStaminaKeys, bmpContent, "stamina");
    lineList.add(indent(2) + "Map<String, String> stamina = new HashMap<>();");
    for (Map.Entry<String, String> entry : data.entrySet()) {
      lineList.add(indent(2) +
          String.format("stamina.put(%s, %s);",
                        FrameExtractor.asPath(entry.getKey()),
                        FrameExtractor.asPath(entry.getValue()))
      );
    }
    return;
  }

  void heroBmpTag(String bmpContent) {
    bmpContent = processImageLine(bmpContent);
    Map<String, String> data = Extractor.extract(heroStaminaKeys, bmpContent, "stamina");
    lineList.add(indent(2) +
        String.format("ImageCell portrait = ImageCell.loadPortrait(%s);",
                      FrameExtractor.asPath(data.get("head")))
    );
    // walking_frame_rate 3
    // walking_speed 5.000000
    // walking_speedz 2.500000
    // ...
    lineList.add("");
    lineList.add(indent(2) + "Map<String, Double> stamina = new HashMap<>();");
    Matcher matcher = staminaPattern.matcher(bmpContent);
    while (matcher.find()) {
      if (!matcher.group(1).endsWith("_rate")) {
        lineList.add(indent(2) +
            String.format("stamina.add(%s, %.2f);",
                          FrameExtractor.asPath(matcher.group(1)),
                          Double.valueOf(matcher.group(2))
        ));
      }
    }
    return;
  }

  void strengthTag(String strengthContent) {
    //  entry: 1 normal
    //    dvx: 2  fall: 40  vrest: 10 bdefend: 16  injury: 30  effect: 3
    //  entry: 2 jump
    //    dvx: 7  fall: 70  vrest: 10  bdefend: 16   injury: 30  effect: 3
    //  ...
    Matcher matcher = strengthBlockPattern.matcher(strengthContent);
    while (matcher.find()) {
      Map<String, Integer> data =
          Extractor.wrapInt(Extractor.extract(ItrExtractor.validKeys, matcher.group(2), "wpstr"));
      String itr = String.format("Itr.strength(Itr.Kind.%s, %s)",
                                 ItrExtractor.getKind(0, data.getOrDefault("effect", 0)).toString(),
                                 String.join(", ", ItrExtractor.getCommonArgs(data, false))
      );
      lineList.add(indent(2) +
          String.format("strength.put(Wpoint.Usage.%s, %s);", matcher.group(1).toUpperCase(), itr)
      );
    }
    return;
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Input error.");
      return;
    }
    Path sourcePath = Path.of(args[0]);
    String fileName = sourcePath.getFileName().toString().split("\\.")[0];
    Tuple<Type, String> info = OpointExtractor.getInfo(fileName);
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
    parser.doTask(info.first.isHero ? "BaseHero" :
                  info.first.isWeapon ? "BaseWeapon": "BaseEnergy", content);

    Path targetPath = Path.of("lfx", "data", fileName = info.second + ".java");
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

  void doTask(String baseClass, String content) {
    lineList.add("package lfx.data;");
    lineList.add("");
    lineList.add("import java.util.ArrayList;");
    lineList.add("import java.util.HashMap;");
    lineList.add("import java.util.List;");
    lineList.add("import java.util.Map;");
    lineList.add("import lfx.component.*;");
    lineList.add(String.format("import lfx.object.%s;", baseClass));
    lineList.add("");
    lineList.add(String.format("public class %s extends %s {", identifier, baseClass));
    lineList.add(indent(1) + String.format("private static %s singleton = null;", identifier));
    lineList.add("");
    lineList.add(indent(1) + String.format("public static synchronized %s of() {", identifier));
    lineList.add(indent(2) + "if (singleton != null) { return singleton; }");
    lineList.add("");

    StringBuilder builder = new StringBuilder(128);
    Matcher matcher = bmpTagPattern.matcher(content);
    matcher.find();
    matcher.appendReplacement(builder, "");
    lineList.add(indent(2) + "List<ImageCell> imageList = new ArrayList<>(280);");
    if (type.isHero) {
      heroBmpTag(matcher.group(1));
    } else if (type.isEnergy) {
      itemBmpTag(matcher.group(1));
    } else {
      itemBmpTag(matcher.group(1));
      lineList.add("");
      lineList.add(indent(2) + "Map<Wpoint.Usage, Itr> strength = new HashMap<>(4);");
      matcher.usePattern(strengthTagPattern);
      if (matcher.find()) {
        strengthTag(matcher.group(1));
        matcher.appendReplacement(builder, "");
      }
    }
    lineList.add("");
    lineList.add(indent(2) + "DataCollector collector = new DataCollector(400, imageList);");

    int frameCount = 0;
    matcher.usePattern(frameTagPattern);
    while (matcher.find()) {
      matcher.appendReplacement(builder, "");
      int frameNumber = Integer.parseInt(matcher.group(1));
      if (frameNumber == 399) {  // dummy
        continue;
      }
      ++frameCount;
      lineList.add("");
      lineList.add(indent(2) + String.format("collector.add(  // %s", matcher.group(2).trim()));
      List<String> frameArgumentLines = FrameExtractor.getFrameArgumentLines(
          type, frameNumber, matcher.group(3));
      for (Iterator<String> it = frameArgumentLines.iterator(); it.hasNext(); ) {
        lineList.add(indent(3) + it.next() + (it.hasNext() ? "," : ""));
      }
      lineList.add(indent(2) + ");");
    }
    matcher.appendTail(builder);
    content = builder.toString().trim();

    List<String> argList = new ArrayList<>(6);
    argList.add(FrameExtractor.asPath(identifier));
    argList.add("collector.getFrameList()");
    argList.add("stamina");
    if (type.isHero) {
      argList.add("portrait");
    } else if (type.isWeapon) {
      argList.add("Type." + type.toString());
      argList.add("strength");
    }
    String args = String.join(", ", argList);

    lineList.add("");
    lineList.add(indent(2) + String.format("singleton = new %s(%s);", baseClass, args));
    lineList.add(indent(2) + "return singleton;");
    lineList.add(indent(1) + "}");
    lineList.add("");
    lineList.add("}");
    System.err.printf("Collect %d frames in %s. Remaining: %s%n",
                      frameCount, identifier, content.isEmpty() ? "(empty)" : content);
    return;
  }

}
