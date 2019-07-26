package lfx.component;

public enum Type {
  OTHER(0x000, 0, 0, 0, 0, 0.0, 0.0, 0.0),
  HERO (0x100, 0, 0, 0, 0, 0.0, 0.0, 0.0),
  BLAST(0x010, 0, 0, 0, 0, 0.0, 0.0, 0.0),
  // type1
  LIGHT(0x001, 1.000, -20, 70, 7, 10.0, 0.6, -0.4) {
    @Override public int hitAct(int fp, double vx) {
      return (int)(Math.random() * 16);
    }
  },
  // type2
  HEAVY(0x001, 1.000, -10, 20, LFobject.DEFAULT_ACT, 10.0, 0.3, -0.2) {
    @Override public int hittingAct() {
      return LFobject.DEFAULT_ACT;
    }
    @Override public int hitAct(int fp, double vx) {
      return (fp > 60) ? (int)(Math.random() * 6) : LFobject.DEFAULT_ACT;
    }
  },
  // type4
  SMALL(0x001, 0.500, -20, 70, 0, 9.0, 0.6, -0.4),
  // type6
  DRINK(0x001, 0.667, -20, 70, 0, 9.0, 0.6, -0.4);

  public final int scopeBits;
  public final double gRatio;
  public final int throwOffset;  // onhand to throwing offset
  public final int landingAct;
  public final int bounceAct;
  public final double threshold;
  public final double vxLast;
  public final double vyLast;

  private Type(int scopeBits, double g, int o, int l, int b, double t, double vx, double vy) {
    this.scopeBits = scopeBits;
    gRatio = g;
    throwOffset = o;
    landingAct = l;
    bounceAct = b;
    threshold = t;
    vxLast = vx;
    vyLast = vy;
  }

  public int hittingAct() {
    return (int)(Math.random() * 16);
  }

  public int hitAct(int fp, double vx) {
    return (vx >= 10.0) ? 40 : LFobject.DEFAULT_ACT;
  }

  @Override
  public String toString() {
    return String.format("%s.%s", this.getClass().getSimpleName(), super.toString());
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
