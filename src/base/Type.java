package base;

import java.util.HashMap;
import java.util.Map;

import util.Tuple;

public enum Type {
  HERO   (true, false, false, 1.000, 10.0, 1.0, 0.00),
  SMALL  (false, true, false, 0.500,  9.0, 0.6, -0.4),
  DRINK  (false, true, false, 0.667,  9.0, 0.6, -0.4),
  HEAVY  (false, true, false, 1.000, 10.0, 0.3, -0.2),
  LIGHT  (false, true, false, 1.000, 10.0, 0.6, -0.4),
  ENERGY (false, false, true, 1.000, 10.0, 1.0, 0.00),
  OTHERS (false, false, false, 1.000, 10.0, 1.0, 0.00);

  public final boolean isHero;
  public final boolean isWeapon;
  public final boolean isEnergy;
  public final double gravityRatio;
  public final double threshold;
  public final double vxLast;
  public final double vyLast;

  private Type(boolean isHero, boolean isWeapon, boolean isEnergy,
               double gravityRatio, double threshold, double vxLast, double vyLast) {
    this.isHero = isHero;
    this.isWeapon = isWeapon;
    this.isEnergy = isEnergy;
    this.gravityRatio = gravityRatio;
    this.threshold = threshold;
    this.vxLast = vxLast;
    this.vyLast = vyLast;
  }

  @Override
  public String toString() {
    return String.join(".", getDeclaringClass().getSimpleName(), name());
  }

  // ==================== Parser Utility ====================

  private static Map<Integer, String> oid2identifier = Map.of();
  private static Map<String, Tuple<Type, String>> fileInfo = Map.of();

  /**
   * Gets the corresponding identifier of given oid.
   *
   * @param oid used in original LF2
   * @return the identifier
   */
  public static String getIdentifier(int oid) {
    if (oid2identifier.isEmpty()) {
      parseDataTxt();
    }
    return oid2identifier.get(oid);
  }

  /**
   * Gets the information of given file.
   *
   * @param fileName the original file name
   * @return {@code Type} and identifier of the file
   */
  public static Tuple<Type, String> getInfo(String fileName) {
    if (fileInfo.isEmpty()) {
      parseDataTxt();
    }
    return fileInfo.get(fileName);
  }

  /**
   * Builds information map from data.txt.
   */
  private static void parseDataTxt() {
    String dataTxt = """
    id:  0  type: 0  file: data\\template.dat
    id:  52  type: 0  file: data\\julian.dat
    id:  51  type: 0  file: data\\firzen.dat
    id:  50  type: 0  file: data\\louisEX.dat
    id:  38  type: 0  file: data\\bat.dat
    id:  39  type: 0  file: data\\justin.dat
    id:  37  type: 0  file: data\\knight.dat
    id:  36  type: 0  file: data\\jan.dat
    id:  35  type: 0  file: data\\monk.dat
    id:  34  type: 0  file: data\\sorcerer.dat
    id:  33  type: 0  file: data\\jack.dat
    id:  32  type: 0  file: data\\mark.dat
    id:  31  type: 0  file: data\\hunter.dat
    id:  30  type: 0  file: data\\bandit.dat
    id:  1  type: 0  file: data\\deep.dat
    id:  2  type: 0  file: data\\john.dat
    id:  4  type: 0  file: data\\henry.dat
    id:  5  type: 0  file: data\\rudolf.dat
    id:  6  type: 0  file: data\\louis.dat
    id:  7  type: 0  file: data\\firen.dat
    id:  8  type: 0  file: data\\freeze.dat
    id:  9  type: 0  file: data\\dennis.dat
    id: 10  type: 0  file: data\\woody.dat
    id: 11  type: 0  file: data\\davis.dat

    id: 100  type: 1  file: data\\weapon0.dat   #stick
    id: 101  type: 1  file: data\\weapon2.dat   #hoe
    id: 120  type: 1  file: data\\weapon4.dat   #knife
    id: 121  type: 4  file: data\\weapon5.dat   #baseball
    id: 122  type: 6  file: data\\weapon6.dat   #milk
    id: 150  type: 2  file: data\\weapon1.dat   #stone
    id: 151  type: 2  file: data\\weapon3.dat   #wooden_box
    id: 123  type: 6  file: data\\weapon8.dat   #beer
    id: 124  type: 1  file: data\\weapon9.dat   #<
    id: 217  type: 2  file: data\\weapon10.dat  #louis_armour
    id: 218  type: 2  file: data\\weapon11.dat  #louis_armour
    id: 300  type: 5  file: data\\criminal.dat  #criminal

    id: 200  type: 3  file: data\\john_ball.dat
    id: 201  type: 1  file: data\\henry_arrow1.dat
    id: 202  type: 1  file: data\\rudolf_weapon.dat
    id: 203  type: 3  file: data\\deep_ball.dat
    id: 204  type: 3  file: data\\henry_wind.dat
    id: 205  type: 3  file: data\\dennis_ball.dat
    id: 206  type: 3  file: data\\woody_ball.dat
    id: 207  type: 3  file: data\\davis_ball.dat
    id: 208  type: 3  file: data\\henry_arrow2.dat
    id: 209  type: 3  file: data\\freeze_ball.dat
    id: 210  type: 3  file: data\\firen_ball.dat
    id: 211  type: 3  file: data\\firen_flame.dat
    id: 212  type: 3  file: data\\freeze_column.dat
    id: 213  type: 1  file: data\\weapon7.dat   #ice_sword
    id: 214  type: 3  file: data\\john_biscuit.dat
    id: 215  type: 3  file: data\\dennis_chase.dat
    id: 216  type: 3  file: data\\jack_ball.dat
    id: 219  type: 3  file: data\\jan_chaseh.dat
    id: 220  type: 3  file: data\\jan_chase.dat
    id: 221  type: 3  file: data\\firzen_chasef.dat
    id: 222  type: 3  file: data\\firzen_chasei.dat
    id: 223  type: 3  file: data\\firzen_ball.dat
    id: 224  type: 3  file: data\\bat_ball.dat
    id: 225  type: 3  file: data\\bat_chase.dat
    id: 226  type: 3  file: data\\justin_ball.dat
    id: 228  type: 3  file: data\\julian_ball.dat
    id: 229  type: 3  file: data\\julian_ball2.dat

    id: 998  type: 5  file: data\\etc.dat
    id: 999  type: 5  file: data\\broken_weapon.dat
    """;

    String pattern = "id: *(\\d+) *type: *(\\d).+?data.([^.]+)\\.dat *#?(.*?)\n";
    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(dataTxt);

    oid2identifier = new HashMap<>(100);
    fileInfo = new HashMap<>(100);
    while (matcher.find()) {
      String rawId = matcher.group(1);
      String rawType = matcher.group(2);
      String fileName = matcher.group(3);
      String alias = matcher.group(4);

      String identifier = alias.isEmpty() ? fileName : alias.equals("<") ? "boomerang" : alias;
      StringBuilder builder = new StringBuilder(32);
      for (String s : identifier.split("_")) {
        builder.append(s.substring(0, 1).toUpperCase());
        builder.append(s.substring(1));
      }
      identifier = builder.toString();

      oid2identifier.put(Integer.valueOf(rawId), identifier);
      Type type = switch (Integer.parseInt(rawType)) {
        case 0 -> HERO;
        case 1 -> LIGHT;
        case 2 -> HEAVY;
        case 3 -> ENERGY;
        case 4 -> SMALL;
        case 6 -> DRINK;
        default -> OTHERS;
      };
      fileInfo.put(fileName, new Tuple<>(type, identifier));
    }
    return;
  }

}
