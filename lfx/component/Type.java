package lfx.component;

public enum Type {
  LIGHT(true,  0x001, 1.000, -20, 10.0, 0.6, -0.4),  // type1
  HEAVY(true,  0x001, 1.000, -10, 10.0, 0.3, -0.2),  // type2
  SMALL(true,  0x001, 0.500, -20,  9.0, 0.6, -0.4),  // type4
  DRINK(true,  0x001, 0.667, -20,  9.0, 0.6, -0.4),  // type6
  BLAST(false, 0x010, 0.000, 0, 0.0, 0.0, 0.0),  // type3
  HERO (false, 0x100, 1.000, 0, 0.0, 0.0, 0.0),  // type0
  OTHER(false, 0x000, 1.000, 0, 0.0, 0.0, 0.0);

  public final boolean isWeapon;
  public final int scopeBits;
  public final double gRatio;
  public final int throwOffset;  // onhand to throwing offset
  public final double threshold;
  public final double vxLast;
  public final double vyLast;

  private Type(boolean isWeapon, int scopeBits, double g, int o, double t, double vx, double vy) {
    this.isWeapon = isWeapon;
    this.scopeBits = scopeBits;
    gRatio = g;
    throwOffset = o;
    threshold = t;
    vxLast = vx;
    vyLast = vy;
  }

  @Override
  public String toString() {
    return String.format("%s.%s", this.getDeclaringClass().getSimpleName(), super.toString());
  }

  public int enemyView() {
    return scopeBits;
  }

  public int teamView() {
    return scopeBits << 1;
  }

  public int allView() {
    return scopeBits | (scopeBits << 1);
  }

  public static int grantTeamScope(int scopeView) {
    return scopeView | (scopeView << 1);
  }

  public static String parserType(int originalType, String originalID) {
    /* I think these two weapons in fact use the small effect properties */
    if (originalID.equals("Henryarrow1") || originalID.equals("Rudolfweapon"))
      return "LFtype." + SMALL;
    switch (originalType) {
      case 1:
        return "LFtype." + LIGHT;
      case 2:
        return "LFtype." + HEAVY;
      case 6:
        return "LFtype." + DRINK;
      case 4:
        return "LFtype." + SMALL;
      case 3:
        return "LFtype." + BLAST;
      case 0:
        return "LFtype." + HERO;
      default:
        return "LFtype." + NULL;
    }
  }

}
