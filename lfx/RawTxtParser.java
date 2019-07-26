import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Scanner;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.File;

public class Parser {
  static final Pattern bmpTag = Pattern.compile("<bmp_begin>(.*?)<bmp_end>", Pattern.DOTALL);
  static final Pattern wslTag = Pattern.compile("<weapon_strength_list>(.*?)<weapon_strength_list_end>", Pattern.DOTALL);
  static final Pattern frameTag = Pattern.compile("<frame> *([0-9]+) +(.*?)\n(.*?)<frame_end>", Pattern.DOTALL);
  static final Pattern icoPattern = Pattern.compile("([a-z]+) *: *([_A-Za-z0-9.\\\\]+)");
  static final Pattern picPattern = Pattern.compile("file.*?: ([_A-Za-z0-9.\\\\]+) +w: *([0-9]+) +h: *([0-9]+) +row: *([0-9]+) +col: *([0-9]+)");
  static final Pattern valPattern = Pattern.compile("([_A-Za-z0-9]+) *: *(-?[.0-9]+)");
  static final Pattern wavPattern = Pattern.compile("sound *: *([_A-Za-z0-9.\\\\]+)");
  static final Pattern bdyPattern = Pattern.compile("bdy:(.*?)bdy_end:", Pattern.DOTALL);
  static final Pattern itrPattern = Pattern.compile("itr:(.*?)itr_end:", Pattern.DOTALL);
  static final Pattern opointPattern = Pattern.compile("opoint:(.*?)opoint_end:", Pattern.DOTALL);
  static final Pattern wpointPattern = Pattern.compile("wpoint:(.*?)wpoint_end:", Pattern.DOTALL);
  static final Pattern cpointPattern = Pattern.compile("cpoint:(.*?)cpoint_end:", Pattern.DOTALL);
  static final Pattern bpointPattern = Pattern.compile("bpoint:(.*?)bpoint_end:", Pattern.DOTALL);
  
  static final HashMap<String, String> oidMap = new HashMap<>();
  
  static final IDinfo stagedummy = new IDinfo("stage");
  static class IDinfo {
    public final int originalType;
    public final String objectType;
    public final String identifier;
    public final String fileName;
    
    public IDinfo(String special) {
      originalType = -1;
      objectType = identifier = fileName = special;
    }
    
    public IDinfo(int id, int t, String x) {
      originalType = t;
      identifier = x.substring(0, 1).toUpperCase() + x.substring(1).replaceAll("_", "");
      objectType = LFtype.parserType(originalType, identifier);
      fileName = String.format("Data_%s", identifier);
      oidMap.put(Integer.toString(id), String.format("\"%s\"", identifier));
    }
    
    public String extendsClass() {
      switch (originalType) {
        case 0:
          return "LFhero";
        case 1:
        case 2:
        case 4:
        case 6:
          return "LFweapon";
        case 3:
          return "LFblast";
        case 5:
          return "LFmisc";
      }
      System.out.printf("%s with unknown originalType: %d", identifier, originalType);
      return "";
    }
    
  }
  
  final IDinfo info;
  final PrintWriter writer;
  public Parser(IDinfo i, PrintWriter w) {
    info = i;
    writer = w;
  }
  
  static final HashSet<String> blastBmpKeySet = new HashSet<>(Arrays.asList(new String[] {
    "weapon_hit_sound", "weapon_drop_sound", "weapon_broken_sound", "w", "h", "row", "col"
  }));
  public void blastBmpTag(final String bmpData) {
    writer.printf("\t\tsuper(\"%s\", %s);\n", info.identifier, info.objectType);
    
    Matcher picMatcher = picPattern.matcher(bmpData);
    while (picMatcher.find()) {
      writer.printf("\t\tloadImageCells(\"%s\", %s, %s, %s, %s);\n",
        picMatcher.group(1).replaceAll("\\\\", "/"),
        picMatcher.group(2), picMatcher.group(3), picMatcher.group(4), picMatcher.group(5));
    }
    
    Pattern statePattern = Pattern.compile("([_A-Za-z]+) *: *([-0-9A-Za-z\\.\\\\]+)");
    Matcher stateMatcher = statePattern.matcher(bmpData);
    HashMap<String, String> stateMap = new HashMap<>();
    while (stateMatcher.find()) {
      String key = stateMatcher.group(1);
      String val = stateMatcher.group(2);
      if (blastBmpKeySet.contains(key))
        stateMap.put(key, val.replaceAll("\\\\", "/"));
      else
        System.out.println("\tstate unknown field: " + key);
    }
    writer.printf("\t\tsetState(\"%s\", \"%s\", \"%s\");\n\t\t\n",
      stateMap.getOrDefault("weapon_hit_sound", ""),
      stateMap.getOrDefault("weapon_drop_sound", ""),
      stateMap.getOrDefault("weapon_broken_sound", "")
    );
    return;
  }
  
  static final HashSet<String> weaponBmpKeySet = new HashSet<>(Arrays.asList(new String[] {
    "weapon_hp", "weapon_drop_hurt", "weapon_hit_sound", "weapon_drop_sound", "weapon_broken_sound",
    "w", "h", "row", "col"
  }));
  public void weaponBmpTag(final String bmpData) {
    writer.printf("\t\tsuper(\"%s\", %s);\n", info.identifier, info.objectType);
    
    Matcher picMatcher = picPattern.matcher(bmpData);
    while (picMatcher.find()) {
      writer.printf("\t\tloadImageCells(\"%s\", %s, %s, %s, %s);\n",
        picMatcher.group(1).replaceAll("\\\\", "/"),
        picMatcher.group(2), picMatcher.group(3), picMatcher.group(4), picMatcher.group(5));
    }
    
    Pattern statePattern = Pattern.compile("([_A-Za-z]+) *: *([-0-9A-Za-z\\.\\\\]+)");
    Matcher stateMatcher = statePattern.matcher(bmpData);
    HashMap<String, String> stateMap = new HashMap<>();
    while (stateMatcher.find()) {
      String key = stateMatcher.group(1);
      String val = stateMatcher.group(2);
      if (weaponBmpKeySet.contains(key))
        stateMap.put(key, val.replaceAll("\\\\", "/"));
      else
        System.out.println("\tstate unknown field: " + key);
    }
    writer.printf("\t\tsetState(%s, %s, \"%s\", \"%s\", \"%s\");\n\t\t\n",
      stateMap.getOrDefault("weapon_hp", "200"),
      stateMap.getOrDefault("weapon_drop_hurt", "35"),
      stateMap.getOrDefault("weapon_hit_sound", ""),
      stateMap.getOrDefault("weapon_drop_sound", ""),
      stateMap.getOrDefault("weapon_broken_sound", "")
    );
    return;
  }
  
  static final HashSet<String> wslKeySet = new HashSet<>(Arrays.asList(new String[] {
    "entry", "dvx", "dvy", "bdefend", "injury", "fall", "vrest", "arest", "effect"
  }));
  
  public void weaponStrTag(final String content) {
    Pattern wslPattern = Pattern.compile("(entry.*?)(?:(?=entry)|(?=$))", Pattern.DOTALL);
    Matcher tagMatcher = wslTag.matcher(content);
    if (tagMatcher.find()) {
      Matcher wslMatcher = wslPattern.matcher(tagMatcher.group(1));
      while (wslMatcher.find()) {
        HashMap<String, String> valMap = findKeyValue(wslKeySet, wslMatcher.group(1));
        int effect = Integer.parseInt(valMap.getOrDefault("effect", "0"));
        String[] itrData = LFeffect.parserKindMap(0, effect, 1000);
        writer.printf("\t\tsetStrength(%s, new LFitr(%s, %s, %s, %s, %s, %s, %s, %s));\n",
          valMap.getOrDefault("entry", "0"),
          itrData[0],
          valMap.getOrDefault("dvx", "LFitr._DVX"),
          valMap.getOrDefault("dvy", "LFitr._DVY"),
          valMap.getOrDefault("bdefend", "LFitr._BDEF"),
          valMap.getOrDefault("injury", "LFitr._INJU"),
          valMap.getOrDefault("fall", "LFitr._FALL"),
          /* default: hero-arest nonhero-verst */
          valMap.containsKey("arest") ? ("-" + valMap.get("arest")) :
            (valMap.containsKey("vrest") ? valMap.get("vrest") :
            ((info.originalType == 0) ? "LFitr._AREST" : "LFitr._VREST")),
          itrData[1]
        );
      }
      writer.printf("\t\t\n");
    }
    return;
  }
  
  public void heroBmpTag(final String bmpData) {
    String name = "";
    String head = "";
    Matcher icoMatcher = icoPattern.matcher(bmpData);
    while (icoMatcher.find()) {
      switch (icoMatcher.group(1)) {
        case "name":
          name = icoMatcher.group(2);
          break;
        case "head":
          head = icoMatcher.group(2);
          break;
        case "small":
        case "w":
        case "h":
        case "row":
        case "col":
          /* not used here */
          break;
        default:
          System.out.println("\tunknown field: " + icoMatcher.group(2));
      }
    }
    if (name.isEmpty())  System.out.println("\tname field is empty.");
    if (head.isEmpty())  System.out.println("\thead field is empty.");
    writer.printf("\t\tsuper(\"%s\", %s, \"%s\");\n", name, info.objectType, head.replaceAll("\\\\", "/"));
    
    Matcher picMatcher = picPattern.matcher(bmpData);
    while (picMatcher.find()) {
      writer.printf("\t\tloadImageCells(\"%s\", %s, %s, %s, %s);\n",
        picMatcher.group(1).replaceAll("\\\\", "/"),
        picMatcher.group(2), picMatcher.group(3), picMatcher.group(4), picMatcher.group(5));
    }
    
    Pattern statePattern = Pattern.compile("([_A-Za-z]+) +([-0-9\\.]+)");
    writer.println("\t\t// stamina");
    Matcher stateMatcher = statePattern.matcher(bmpData);
    while (stateMatcher.find()) {
      switch (stateMatcher.group(1)) {
        case "walking_speed":
          writer.printf("\t\tValue_walking_speed = %s;\n", stateMatcher.group(2));
          break;
        case "walking_speedz":
          writer.printf("\t\tValue_walking_speedz = %s;\n", stateMatcher.group(2));
          break;
        case "running_speed":
          writer.printf("\t\tValue_running_speed = %s;\n", stateMatcher.group(2));
          break;
        case "running_speedz":
          writer.printf("\t\tValue_running_speedz = %s;\n", stateMatcher.group(2));
          break;
        case "heavy_walking_speed":
          writer.printf("\t\tValue_heavy_walking_speed = %s;\n", stateMatcher.group(2));
          break;
        case "heavy_walking_speedz":
          writer.printf("\t\tValue_heavy_walking_speedz = %s;\n", stateMatcher.group(2));
          break;
        case "heavy_running_speed":
          writer.printf("\t\tValue_heavy_running_speed = %s;\n", stateMatcher.group(2));
          break;
        case "heavy_running_speedz":
          writer.printf("\t\tValue_heavy_running_speedz = %s;\n", stateMatcher.group(2));
          break;
        case "jump_height":
          writer.printf("\t\tValue_jump_height = %s;\n", stateMatcher.group(2));
          break;
        case "jump_distance":
          writer.printf("\t\tValue_jump_distance = %s;\n", stateMatcher.group(2));
          break;
        case "jump_distancez":
          writer.printf("\t\tValue_jump_distancez = %s;\n", stateMatcher.group(2));
          break;
        case "dash_height":
          writer.printf("\t\tValue_dash_height = %s;\n", stateMatcher.group(2));
          break;
        case "dash_distance":
          writer.printf("\t\tValue_dash_distance = %s;\n", stateMatcher.group(2));
          break;
        case "dash_distancez":
          writer.printf("\t\tValue_dash_distancez = %s;\n", stateMatcher.group(2));
          break;
        case "rowing_height":
          writer.printf("\t\tValue_rowing_height = %s;\n", stateMatcher.group(2));
          break;
        case "rowing_distance":
          writer.printf("\t\tValue_rowing_distance = %s;\n", stateMatcher.group(2));
          break;
      }
    }
    writer.println("\t\t");
    return;
  }
  
  static final HashSet<String> frameKeySet = new HashSet<>(Arrays.asList(new String[] {
    "pic", "state", "wait", "next", "dvx", "dvy", "dvz", "centerx", "centery", "mp",
    "hit_a", "hit_d", "hit_j", "hit_Fa", "hit_Fj", "hit_Ua", "hit_Uj", "hit_Da", "hit_Dj", "hit_ja"
  }));
  static final HashMap<String, LFstate> stateMap = LFstate.buildParserMap();
  
  private String currFrameNumb;
  private String currFrameName;
  private int currStateInt = 0;
  
  public void frameInfo(final Matcher frameMatcher) {
    currFrameNumb = frameMatcher.group(1);
    currFrameName = frameMatcher.group(2);
    String temp01 = frameMatcher.group(3);
    String temp02 = bdyPattern.matcher(temp01).replaceAll("");
    String temp03 = itrPattern.matcher(temp02).replaceAll("");
    String temp04 = opointPattern.matcher(temp03).replaceAll("");
    String temp05 = wpointPattern.matcher(temp04).replaceAll("");
    String temp06 = cpointPattern.matcher(temp05).replaceAll("");
    String temp07 = bpointPattern.matcher(temp06).replaceAll("");
    
    HashMap<String, String> valMap = findKeyValue(frameKeySet, temp07);
    currStateInt = Integer.parseInt(valMap.get("state"));
    LFstate currState = stateMap.get(valMap.get("state"));
    String nextString = valMap.get("next");
    int invisibility = 0;
    if (info.originalType == 0) {
      int nextInt = Integer.parseInt(nextString);
      if (1300 > nextInt && nextInt >= 1100) {
        invisibility = nextInt - 1100;
        nextString = "Act_999";
      }
      int currInt = Integer.parseInt(currFrameNumb);
      if (currInt == 215) {
        currState = LFstate.LAND;
      } else if (currFrameName.equals("heavy_obj_walk")) {
        currState = LFstate.HWALK;
      } else if (currFrameName.equals("heavy_obj_run")) {
        currState = LFstate.HRUN;
      } else if (currState == LFstate.ROW) {
        if (currInt != 100 && currInt != 101 && currInt != 108 && currInt != 109)
          currState = LFstate.NORM;
      }
      if ((currStateInt == 18) && (currInt < 203 || currInt > 206))
        currState = LFstate.NORM;
    } else if (currStateInt == 18) {
      currState = LFstate.NORM;
    }
    String dvz = valMap.getOrDefault("dvz", "0");
    if ((info.originalType == 0) && (currStateInt == 301 || currStateInt == 19))
      dvz = "3";// uncertain value
    writer.printf("\t\tsetFrame(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
      currFrameNumb,
      valMap.get("pic"),
      currState.parserText(),
      valMap.get("wait"),
      nextString,
      valMap.getOrDefault("dvx", "0"),
      valMap.getOrDefault("dvy", "0"),
      dvz,
      valMap.get("centerx"),
      valMap.get("centery")
    );
    if (valMap.containsKey("mp"))
      writer.printf(".mp(%s)", valMap.get("mp"));
    if (info.identifier.equals("Louis") && currFrameNumb.equals("300"))
      writer.printf(".lim(0.333)");
    String skillKey = "";
    if (!valMap.getOrDefault("hit_a",  "0").equals("0"))
      skillKey += String.format(".hit_a(%s)", valMap.get("hit_a"));
    if (!valMap.getOrDefault("hit_d",  "0").equals("0"))
      skillKey += String.format(".hit_d(%s)", valMap.get("hit_d"));
    if (info.originalType == 3) {
      /* type3 with hit_j=={0,50} has no z-velocity */
      int type3_hit_j = Integer.parseInt(valMap.getOrDefault("hit_j", "0"));
      if (type3_hit_j != 0 && type3_hit_j != 50)
        skillKey += String.format(".hit_j(%d)", type3_hit_j - 50);
    } else {
      if (!valMap.getOrDefault("hit_j",  "0").equals("0"))
        skillKey += String.format(".hit_j(%s)", valMap.get("hit_j"));
    }
    if (!valMap.getOrDefault("hit_Fa", "0").equals("0"))
      skillKey += String.format(".hit_Fa(%s)", valMap.get("hit_Fa"));
    if (!valMap.getOrDefault("hit_Fj", "0").equals("0"))
      skillKey += String.format(".hit_Fj(%s)", valMap.get("hit_Fj"));
    if (!valMap.getOrDefault("hit_Ua", "0").equals("0"))
      skillKey += String.format(".hit_Ua(%s)", valMap.get("hit_Ua"));
    if (!valMap.getOrDefault("hit_Uj", "0").equals("0"))
      skillKey += String.format(".hit_Uj(%s)", valMap.get("hit_Uj"));
    if (!valMap.getOrDefault("hit_Da", "0").equals("0"))
      skillKey += String.format(".hit_Da(%s)", valMap.get("hit_Da"));
    if (!valMap.getOrDefault("hit_Dj", "0").equals("0"))
      skillKey += String.format(".hit_Dj(%s)", valMap.get("hit_Dj"));
    if (!valMap.getOrDefault("hit_ja", "0").equals("0"))
      skillKey += String.format(".hit_ja(%s)", valMap.get("hit_ja"));
    if (!skillKey.isEmpty())  writer.print("\n\t\t    " + skillKey);
    String extra = LFextra.parserState(currStateInt);
    if (extra != null)
      writer.printf("\n\t\t    .add(%s)", extra);
    if (invisibility > 0)
      writer.printf("\n\t\t    .add(%s)", LFextra.parserInvisibility(invisibility));
    return;
  }
  
  static final HashSet<String> bdyKeySet = new HashSet<>(Arrays.asList(new String[] { "kind", "x", "y", "w", "h" }));
  
  public void bdyBlock(final String frameData) {
    Matcher matcher = bdyPattern.matcher(frameData);
    while (matcher.find()) {
      HashMap<String, String> valMap = findKeyValue(bdyKeySet, matcher.group(1));
      writer.printf("\n\t\t    .add(new LFbdy(%s, %s, %s, %s%s))",
        valMap.get("x"),
        valMap.get("y"),
        valMap.get("w"),
        valMap.get("h"),
        LFbdy.parserBdy(currStateInt, info.identifier)
      );
    }
    return;
  }
  
  static final HashSet<String> itrKeySet = new HashSet<>(Arrays.asList(new String[] {
    "kind", "x", "y", "w", "h", "zwidth", "dvx", "dvy",
    "bdefend", "injury", "fall", "vrest", "arest", "catchingact", "caughtact", "effect"
  }));
  
  public void itrBlock(final String frameData) {
    Matcher matcher = itrPattern.matcher(frameData);
    while (matcher.find()) {
      HashMap<String, String> valMap = findKeyValue(itrKeySet, matcher.group(1));
      int effect = Integer.parseInt(valMap.getOrDefault("effect", "0"));
      int itrKindInt = Integer.parseInt(valMap.get("kind"));
      String[] itrData = LFeffect.parserKindMap(itrKindInt, effect, currStateInt);
      if (itrData.length == 2) {// damage kind
        String fallspecial = (itrKindInt == 10 || itrKindInt == 11) ? "70" :
                    valMap.getOrDefault("fall", "LFitr._FALL");
        writer.printf("\n\t\t    .add(new LFitr(%s, xywhz(%s, %s, %s, %s%s), %s, %s, %s, %s, %s, %s, %s))",
          itrData[0],
          valMap.get("x"),
          valMap.get("y"),
          valMap.get("w"),
          valMap.get("h"),
          valMap.containsKey("zwidth") ? (", " + valMap.get("zwidth")) : "",
          valMap.getOrDefault("dvx", "LFitr._DVX"),
          valMap.getOrDefault("dvy", (itrKindInt == 8) ? "100" : "LFitr._DVY"),
          valMap.getOrDefault("bdefend", "LFitr._BDEF"),
          valMap.getOrDefault("injury", "LFitr._INJU"),
          fallspecial,
          /* default: hero-arest nonhero-verst */
          valMap.containsKey("arest") ? ("-" + valMap.get("arest")) :
            (valMap.containsKey("vrest") ? valMap.get("vrest") :
            ((info.originalType == 0) ? "LFitr._AREST" : "LFitr._VREST")),
          itrData[1]
        );
      } else if (itrData[2].equals("CatchType")) {
        writer.printf("\n\t\t    .add(new LFitr(%s, xywhz(%s, %s, %s, %s%s), %s, %s, %s))",
          itrData[0],
          valMap.get("x"),
          valMap.get("y"),
          valMap.get("w"),
          valMap.get("h"),
          valMap.containsKey("zwidth") ? (", " + valMap.get("zwidth")) : "",
          valMap.getOrDefault("catchingact", "LFitr._C1"),
          valMap.getOrDefault("caughtact"  , "LFitr._C2"),
          itrData[1]
        );
      } else {// other kind
        writer.printf("\n\t\t    .add(new LFitr(%s, xywhz(%s, %s, %s, %s%s), %s))",
          itrData[0],
          valMap.get("x"),
          valMap.get("y"),
          valMap.get("w"),
          valMap.get("h"),
          valMap.containsKey("zwidth") ? (", " + valMap.get("zwidth")) : "",
          itrData[1]
        );
      }
    }
    return;
  }
  
  static final HashSet<String> cpointKeySet = new HashSet<>(Arrays.asList(new String[] {
    "kind", "x", "y", "injury", "vaction", "aaction", "jaction", "taction",
    "throwvx", "throwvy", "throwvz", "throwinjury", "fronthurtact", "backhurtact",
    "hurtable", "decrease", "dircontrol", "cover"
  }));
  
  public void cpointBlock(final String frameData) {
    Matcher matcher = cpointPattern.matcher(frameData);
    if (matcher.find()) {
      HashMap<String, String> valMap = findKeyValue(cpointKeySet, matcher.group(1));
      if (valMap.get("kind").equals("1")) {// catcher
        String[] catcherData = LFcpoint.parserCpoint(
          valMap.getOrDefault("hurtable", "0"),
          valMap.getOrDefault("cover", "0"),
          valMap.getOrDefault("throwinjury", "0")
        );
        writer.printf("\n\t\t    .cpoint(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
          valMap.get("x"),
          valMap.get("y"),
          valMap.getOrDefault("injury", "0"),
          valMap.getOrDefault("vaction", "0"),
          valMap.getOrDefault("taction", "0"),
          valMap.getOrDefault("aaction", "0"),
          valMap.getOrDefault("jaction", "0"),
          valMap.getOrDefault("throwvx", "0"),
          valMap.getOrDefault("throwvy", "0"),
          valMap.getOrDefault("throwvz", "0"),
          catcherData[0],// throwIjnury
          catcherData[1],// catchFlags
          valMap.getOrDefault("dircontrol", "0"),
          valMap.getOrDefault("decrease", "0")
        );
      } else {// catchee
        writer.printf("\n\t\t    .cpoint(%s, %s, %s, %s)",
          valMap.get("x"),
          valMap.get("y"),
          valMap.getOrDefault("fronthurtact", "LFcpoint._FHACT"),
          valMap.getOrDefault("backhurtact" , "LFcpoint._BHACT")
        );
      }
    }
    return;
  }
  
  static final HashSet<String> wpointKeySet = new HashSet<>(Arrays.asList(new String[] {
    "kind", "x", "y", "weaponact", "attacking", "dvx", "dvy", "dvz", "cover"
  }));
  
  public void wpointBlock(final Matcher frameMatcher) {
    final String frameData = frameMatcher.group(3);
    Matcher matcher = wpointPattern.matcher(frameData);
    if (matcher.find()) {
      HashMap<String, String> valMap = findKeyValue(wpointKeySet, matcher.group(1));
      writer.printf("\n\t\t    .wpoint(%s, %s, %s, %s, %s, %s, %s, %s)",
        valMap.get("x"),
        valMap.get("y"),
        valMap.getOrDefault("weaponact", "0"),
        valMap.get("kind").equals("3") ? "-1" : valMap.getOrDefault("attacking", "1"),
        valMap.getOrDefault("dvx", "0"),
        valMap.getOrDefault("dvy", "0"),
        valMap.getOrDefault("dvz", "0"),
        valMap.getOrDefault("cover", "0")
      );
    }
    return;
  }
  
  static final HashSet<String> opointKeySet = new HashSet<>(Arrays.asList(new String[] {
    "kind", "x", "y", "action", "dvx", "dvy", "oid", "facing"
  }));
  
  public void opointBlock(final Matcher frameMatcher) {
    final String frameData = frameMatcher.group(3);
    Matcher matcher = opointPattern.matcher(frameData);
    if (matcher.find()) {
      HashMap<String, String> valMap = findKeyValue(opointKeySet, matcher.group(1));
      String oid = oidMap.get(valMap.get("oid"));
      writer.printf("\n\t\t    .opoint(%s, %s, %s, %s, %s, %s, %s, %s%s)",
        LFopoint.parserType(valMap.get("kind")),
        valMap.get("x"),
        valMap.get("y"),
        valMap.get("dvx"),
        valMap.get("dvy"),
        oid,
        valMap.get("action"),
        valMap.get("facing"),
        (oid.equals("\"Rudolf\"") || oid.equals("\"Julian\"")) ? ", 10.0" : ""
      );
    }
    return;
  }
  
  public static void main(String args[]) throws Exception {
    HashMap<String, IDinfo> dataTxt = new HashMap<>();
    /* [RegExp]
    id: +(\d+) +effect: +(\d+) .*?data\\(.*?)\.dat
    dataTxt.put\("(\3).txt", new IDinfo\((\1), (\2), "(\3)"\)\);
    */
    dataTxt.put("template.txt", new IDinfo(0, 0, "template"));
    dataTxt.put("julian.txt", new IDinfo(52, 0, "julian"));
    dataTxt.put("firzen.txt", new IDinfo(51, 0, "firzen"));
    dataTxt.put("louisEX.txt", new IDinfo(50, 0, "louisEX"));
    dataTxt.put("bat.txt", new IDinfo(38, 0, "bat"));
    dataTxt.put("justin.txt", new IDinfo(39, 0, "justin"));
    dataTxt.put("knight.txt", new IDinfo(37, 0, "knight"));
    dataTxt.put("jan.txt", new IDinfo(36, 0, "jan"));
    dataTxt.put("monk.txt", new IDinfo(35, 0, "monk"));
    dataTxt.put("sorcerer.txt", new IDinfo(34, 0, "sorcerer"));
    dataTxt.put("jack.txt", new IDinfo(33, 0, "jack"));
    dataTxt.put("mark.txt", new IDinfo(32, 0, "mark"));
    dataTxt.put("hunter.txt", new IDinfo(31, 0, "hunter"));
    dataTxt.put("bandit.txt", new IDinfo(30, 0, "bandit"));
    dataTxt.put("deep.txt", new IDinfo(1, 0, "deep"));
    dataTxt.put("john.txt", new IDinfo(2, 0, "john"));
    dataTxt.put("henry.txt", new IDinfo(4, 0, "henry"));
    dataTxt.put("rudolf.txt", new IDinfo(5, 0, "rudolf"));
    dataTxt.put("louis.txt", new IDinfo(6, 0, "louis"));
    dataTxt.put("firen.txt", new IDinfo(7, 0, "firen"));
    dataTxt.put("freeze.txt", new IDinfo(8, 0, "freeze"));
    dataTxt.put("dennis.txt", new IDinfo(9, 0, "dennis"));
    dataTxt.put("woody.txt", new IDinfo(10, 0, "woody"));
    dataTxt.put("davis.txt", new IDinfo(11, 0, "davis"));
    /* [RegExp]
    id: +(\d+) +effect: +(\d+) .*?data\\(.*?)\.dat +#(.*?)(?=\n)
    dataTxt.put\("(\3).txt", new IDinfo\((\1), (\2), "(\4)"\)\);
    */
    dataTxt.put("weapon0.txt", new IDinfo(100, 1, "stick"));
    dataTxt.put("weapon1.txt", new IDinfo(150, 2, "stone"));
    dataTxt.put("weapon2.txt", new IDinfo(101, 1, "hoe"));
    dataTxt.put("weapon3.txt", new IDinfo(151, 2, "wooden_box"));
    dataTxt.put("weapon4.txt", new IDinfo(120, 1, "knife"));
    dataTxt.put("weapon5.txt", new IDinfo(121, 4, "baseball"));
    dataTxt.put("weapon6.txt", new IDinfo(122, 6, "milk"));
    dataTxt.put("weapon7.txt", new IDinfo(213, 1, "ice_sword"));
    dataTxt.put("weapon8.txt", new IDinfo(123, 6, "beer"));
    dataTxt.put("weapon9.txt", new IDinfo(124, 1, "boomerang"));
    dataTxt.put("weapon10.txt", new IDinfo(217, 2, "louis_armour1"));
    dataTxt.put("weapon11.txt", new IDinfo(218, 2, "louis_armour2"));
    /* [RegExp]
    id: +(\d+) +effect: +(\d+) .*?data\\(.*?)\.dat(?=\n)
    dataTxt.put\("(\3).txt", new IDinfo\((\1), (\2), "(\3)"\)\);
    */
    dataTxt.put("john_ball.txt", new IDinfo(200, 3, "john_ball"));
    dataTxt.put("henry_arrow1.txt", new IDinfo(201, 1, "henry_arrow1"));
    dataTxt.put("rudolf_weapon.txt", new IDinfo(202, 1, "rudolf_weapon"));
    dataTxt.put("deep_ball.txt", new IDinfo(203, 3, "deep_ball"));
    dataTxt.put("henry_wind.txt", new IDinfo(204, 3, "henry_wind"));
    dataTxt.put("dennis_ball.txt", new IDinfo(205, 3, "dennis_ball"));
    dataTxt.put("woody_ball.txt", new IDinfo(206, 3, "woody_ball"));
    dataTxt.put("davis_ball.txt", new IDinfo(207, 3, "davis_ball"));
    dataTxt.put("henry_arrow2.txt", new IDinfo(208, 3, "henry_arrow2"));
    dataTxt.put("freeze_ball.txt", new IDinfo(209, 3, "freeze_ball"));
    dataTxt.put("firen_ball.txt", new IDinfo(210, 3, "firen_ball"));
    dataTxt.put("firen_flame.txt", new IDinfo(211, 3, "firen_flame"));
    dataTxt.put("freeze_column.txt", new IDinfo(212, 3, "freeze_column"));
    dataTxt.put("john_biscuit.txt", new IDinfo(214, 3, "john_biscuit"));
    dataTxt.put("dennis_chase.txt", new IDinfo(215, 3, "dennis_chase"));
    dataTxt.put("jack_ball.txt", new IDinfo(216, 3, "jack_ball"));
    dataTxt.put("jan_chaseh.txt", new IDinfo(219, 3, "jan_chaseh"));
    dataTxt.put("jan_chase.txt", new IDinfo(220, 3, "jan_chase"));
    dataTxt.put("firzen_chasef.txt", new IDinfo(221, 3, "firzen_chasef"));
    dataTxt.put("firzen_chasei.txt", new IDinfo(222, 3, "firzen_chasei"));
    dataTxt.put("firzen_ball.txt", new IDinfo(223, 3, "firzen_ball"));
    dataTxt.put("bat_ball.txt", new IDinfo(224, 3, "bat_ball"));
    dataTxt.put("bat_chase.txt", new IDinfo(225, 3, "bat_chase"));
    dataTxt.put("justin_ball.txt", new IDinfo(226, 3, "justin_ball"));
    dataTxt.put("julian_ball.txt", new IDinfo(228, 3, "julian_ball"));
    dataTxt.put("julian_ball2.txt", new IDinfo(229, 3, "julian_ball2"));
    dataTxt.put("etc.txt", new IDinfo(998, 5, "etc"));
    dataTxt.put("broken_weapon.txt", new IDinfo(999, 5, "broken_weapon"));
    /* parse all input */
    for (String x: args) {
      IDinfo i = dataTxt.getOrDefault(x, x.equals("stage.txt") ? stagedummy : null);
      PrintWriter pw = new PrintWriter(".\\" + i.fileName + ".java", "utf-8");
      String content = new Scanner(new File(x)).useDelimiter("\\Z").next();
      Parser program = new Parser(i, pw);
      program.doTask(content);
      pw.close();
    }
    return;
  }
  
  public void doTask(final String content) {
    if (info == stagedummy) {
      parseStage(content);
      return;
    }
    writer.printf("final class %s extends %s {\n", info.fileName, info.extendsClass());
    writer.printf("\t\n\tprivate %s() {\n", info.fileName);
    
    final Matcher bmpMatcher = bmpTag.matcher(content);
    if (bmpMatcher.find()) {
      switch (info.originalType) {
        case 0:
          heroBmpTag(bmpMatcher.group(1));
          break;
        case 1:
        case 2:
        case 4:
        case 6:
          weaponBmpTag(bmpMatcher.group(1));
          weaponStrTag(content);
          break;
        case 3:
          blastBmpTag(bmpMatcher.group(1));
          break;
        case 5:
          System.out.printf("Type %d is now working.\n", info.originalType);
      }
    }
    
    int frameCount = 0;
    final Matcher frameMatcher = frameTag.matcher(content);
    while (frameMatcher.find()) {
      try {
        /* these frames have no use, IMO */
        if (frameMatcher.group(1).equals("399") || frameMatcher.group(2).equals("dash_defend"))
          continue;
        ++frameCount;
        writer.printf("\t\t// %s %s\n", frameMatcher.group(1), frameMatcher.group(2));
        frameInfo(frameMatcher);
        opointBlock(frameMatcher);
        wpointBlock(frameMatcher);
        cpointBlock(frameMatcher.group(3));
        bdyBlock(frameMatcher.group(3));
        itrBlock(frameMatcher.group(3));
        Matcher wavMatcher = wavPattern.matcher(frameMatcher.group(3));
        if (wavMatcher.find())
          writer.printf("\n\t\t    .sound(\"%s\")", wavMatcher.group(1).replaceAll("\\\\", "/"));
        writer.println(".build();\n\t\t");
      } catch (Exception ex) {
        System.out.printf(">>> Exception in Frame %s\n", frameMatcher.group(1));
        ex.printStackTrace();
      }
    }
    writer.printf("\t\t/* Total %d frames */\n", frameCount);
    writer.printf("\t\tsuper.preprocess();\n");
    writer.printf("\t}\n");
    writer.printf("\t\n");
    writer.printf("\tpublic static final %s singleton = new %s();\n", info.fileName, info.fileName);
    writer.printf("}\n");
    System.out.printf("%s: %d\n", info.fileName, frameCount);
    return;
  }
  
  public static HashMap<String, String> findKeyValue(final HashSet<String> keySet, String source) {
    HashMap<String, String> valMap = new HashMap<>();
    Matcher valMatcher = valPattern.matcher(source);
    while (valMatcher.find()) {
      String key = valMatcher.group(1);
      String val = valMatcher.group(2);
      if (keySet.contains(key))
        valMap.put(key, val.equals("-842150451") ? "0" :
          (val.contains(".") ? Double.toString(Double.parseDouble(val))
                     : Integer.toString(Integer.parseInt(val))));
      else
        System.out.println("\tunknown field: " + key);
    }
    return valMap;
  }
  
  static String stageObject(HashMap<String, String> valMap, boolean boss, boolean soldier) {
    String result = "";
    for (String k: phaseKeySet) {
      String v = valMap.get(k);
      if (v != null)
        result += String.format("%s: %s, ", k, v);
    }
    return String.format("{ %stype: %s }", result, boss ? "boss" : (soldier ? "soldier" : "NONE"));
  }
  
  /* stage use only */
  static final String checkBoss = ".*?< *boss *>.*?";
  static final String checkSoldier = ".*?< *soldier *>.*?";
  static final Pattern phaseTag = Pattern.compile("<phase>(.*?)<phase_end>", Pattern.DOTALL);
  static final Pattern  general = Pattern.compile(" *([_A-Za-z0-9]+) *: *([_A-Za-z0-9\\.\\\\]+) *");
  static final Pattern  comment = Pattern.compile("#(.*?)\n");
  static final HashSet<String> phaseKeySet = new HashSet<>(Arrays.asList(new String[] {
    "id", "bound", "hp", "act", "x", "y", "ratio", "reserve", "times", "join", "join_reserve", "when_clear_goto_phase"
  }));
  
  public void parseStage(String content) {
    /* <stage_end> dose not always exist */
    for (String stageContent: content.split("<stage>")) {
      String remaining = phaseTag.matcher(stageContent).replaceAll("");
      
      String stageInfo = "";
      Matcher valMatcher = valPattern.matcher(remaining);
      while (valMatcher.find()) {
        if (valMatcher.group(1).equals("id"))
          stageInfo += "[" + valMatcher.group(2) + "]";
        else
          System.out.printf("unexpected field (%s, %s)\n", valMatcher.group(1), valMatcher.group(2));
      }
      Matcher cmtMatcher = comment.matcher(remaining);
      while (cmtMatcher.find())
        stageInfo += "/* " + cmtMatcher.group(1).trim() + " */";
      
      writer.println(stageInfo);
      
      final Matcher phaseMatcher = phaseTag.matcher(stageContent);
      while (phaseMatcher.find()) {
        ArrayList<String> objects = new ArrayList<>();
        int bound = 0;
        String music = "";
        for (String line: phaseMatcher.group(1).split("\r?\n")) {
          HashMap<String, String> valMap = findKeyValue(phaseKeySet, line);
          if (valMap.containsKey("id")) {
            objects.add(stageObject(valMap, line.matches(checkBoss), line.matches(checkSoldier)));
          } else {
            Matcher gm = general.matcher(line);
            while (gm.find()) {
              switch (gm.group(1)) {
                case "bound":
                  bound = Integer.parseInt(gm.group(2));
                  break;
                case "music":
                  music = gm.group(2);
                  break;
                default:
                  System.out.printf("unexpected field (%s: %s)\n", gm.group(1), gm.group(2));
              }
            }
          }
        }
        writer.printf("\taddPhase(%d, \"%s\")", bound, music);
        for (String os: objects)
          writer.printf("\n\t\t.(%s)", os);
        writer.println(";\n\t");
      }
    }
    return;
  }
  
}

