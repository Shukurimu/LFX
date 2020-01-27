package lfx.component;

import lfx.util.Box;
import lfx.util.Const;

public final class Itr {
  public static final int DVX = 0;
  public static final int DVY = -7;
  public static final int BDEFEND = 0;
  public static final int FALL = 20;
  public static final int INJURY = 0;
  public static final int AREST = -7;  // hero default
  public static final int VREST = +9;  // weapon default
  public static final int HERO_SCOPE = Const.getSideView(Const.SCOPE_VIEW_HERO, false)
                                     | Const.getBothView(Const.SCOPE_VIEW_WEAPON)
                                     | Const.getBothView(Const.SCOPE_VIEW_ENERGY);
  public static final int WEAPON_SCOPE = HERO_SCOPE;
  public static final int ENERGY_SCOPE = Const.getSideView(Const.SCOPE_VIEW_HERO, false)
                                       | Const.getBothView(Const.SCOPE_VIEW_WEAPON)
                                       | Const.getSideView(Const.SCOPE_VIEW_ENERGY, false);
  public static final int NON_HERO_SCOPE = Const.getBothView(Const.SCOPE_VIEW_WEAPON)
                                         | Const.getBothView(Const.SCOPE_VIEW_ENERGY);

  public enum Kind {
    GRASP_DOP (""),  // kind1
    PICK      (""),  // kind2
    GRASP_BDY (""),  // kind3
    THROW_ATK (""),  // kind4
    STRENGTH  (""),  // kind5
    LET_SPUNCH(""),  // kind6
    ROLL_PICK (""),  // kind7
    HEAL      (""),  // kind8
    SHIELD    (""),  // kind9
    SONATA    (""),  // kind10.kind11
    BLOCK     (""),  // kind14
    VORTEX    (""),  // kind15
    // kind16 is replaced by ICE and calling smooth()
    PUNCH   ("data/001.wav"),  // effect0
    STAB    ("data/032.wav"),  // effect1
    FIRE    ("data/070.wav"),  // effect2
    ICE     ("data/065.wav"),  // effect3
    WEAKFIRE("data/070.wav"),  // effect20.effect21
    WEAKICE ("data/065.wav"),  // effect30
    NONE    ("");
    // effect4 is replaced by NON_HERO_SCOPE
    // effect23 is replaced by calling exp()

    public final String sound;

    private Kind(String sound) {
      this.sound = sound;
    }

    @Override
    public String toString() {
      return String.format("%s.%s", this.getDeclaringClass().getSimpleName(), super.toString());
    }

  }

  public final Box box;
  public final Kind kind;
  public final int dvx;  // also catchingact
  public final int dvy;  // also caughtact
  public final int bdefend;
  public final int injury;
  public final int fall;
  public final int vrest;  // negative value as arest
  public final int scope;
  public final boolean nolag;
  public final boolean explosion;

  public Itr(Box box, Kind kind, int dvx, int dvy,
             int fall, int bdefend, int injury, int vrest, int scope, String attr) {
    this.box = box;
    this.kind = kind;
    this.dvx = dvx;
    this.dvy = dvy;
    this.fall = fall;
    this.bdefend = bdefend;
    this.injury = injury;
    this.vrest = vrest;
    this.scope = scope;
    nolag = attr.contains("nolag");
    explosion = attr.contains("exp");
  }

  public Itr(Box box, Kind kind, int dvx, int dvy,
             int fall, int bdefend, int injury, int vrest, int scope) {
    this(box, kind, dvx, dvy, fall, bdefend, injury, vrest, scope, "");
  }

  // grasp (use constructor if you really want to grasp teammate)
  public static Itr grasp(Box box, boolean forceGrasp, int catchingact, int caughtact) {
    return new Itr(box, forceGrasp ? Kind.GRASP_BDY : Kind.GRASP_DOP,
                   catchingact, caughtact, 0, 0, 0, VREST,
                   Const.getSideView(Const.SCOPE_VIEW_HERO, false), "");
  }

  // kind only (PICK, BLOCK, ...)
  public static Itr kind(Box box, Kind kind, int scope) {
    return new Itr(box, kind, 0, 0, 0, 0, 0, 0, scope, "");
  }

  // weapon strength list (treated as hero's attack)
  public static Itr strength(Kind kind, int dvx, int dvy,
                             int fall, int bdefend, int injury, int vrest) {
    return new Itr(null, kind, dvx, dvy, fall, bdefend, injury, vrest, HERO_SCOPE, "");
  }

  public int calcLag(int originalValue) {
    return nolag ? originalValue : Math.max(originalValue, Const.LAG);
  }

  // Explosion kind negative dvx goes two directions.
  public double calcDvx(double px, boolean faceRight) {
    return explosion ? (px < (box.x + box.w / 2) ? -dvx : dvx)
                     : (faceRight ? dvx : -dvx);
  }

}

/*

// https://lf-empire.de/lf2-empire/data-changing/frame-elements/174-itr-interaction?showall=1

  // The followings are the self implementation of several itr kinds
  //  since no documentation discussing about this kind
  //  the result is very likely different from LF2
  public static final double SONATA_VELOCITY_DEDUCTION = 0.7;
  public static final double SONATA_Y_RATIO = 0.7;

  public double sonataVxz(double v) {
    return v * SONATA_VELOCITY_DEDUCTION;
  }

  public double sonataVy(double py, double vy) {
    return ((vy > itr.dvy) && ((y1 - y2) * SONATA_Y_RATIO + y2 < py)) ? Math.max(itr.dvy, vy + itr.dvy) : vy;
  }

  public static final double VORTEX_DISTANCE_MULTIPLIER = 0.12;

  public double vortexAx(double x) {
    final double length = x2 - x1;
    return Math.sin(Math.PI * (px - x) / length) * Math.sqrt(length) * VORTEX_DISTANCE_MULTIPLIER;
  }

  public double vortexAz(double z) {
    final double length = z2 - z1;
    return Math.sin(Math.PI * (pz - z) / length) * Math.sqrt(length) * VORTEX_DISTANCE_MULTIPLIER;
  }

  public double vortexAy(double y, double vy) {
    return (LFX.currMap.gravity * ((y1 - y) / (y2 - y1) - 1.0)) - ((vy > 0.0) ? (vy * 0.16) : 0.0);
  }


  public static String state18(String originalScope, boolean is18) {
    if (!is18) return originalScope;
    char[] newScope = originalScope.toCharArray();
    newScope[3] = (newScope[2] == '1') ? '1' : newScope[3];
    newScope[5] = (newScope[4] == '1') ? '1' : newScope[5];
    newScope[7] = (newScope[6] == '1') ? '1' : newScope[7];
    return String.valueOf(newScope);
  }
  public static String[] parserKindMap(int originalKind, int originalEffect, int originalState) {
    boolean is18 = originalState == 18;
    switch (originalKind) {
        case 0:
            switch (originalEffect) {
                case 1:
                    return new String[] { STAB.parserText(),   state18("0b101110", is18) };
                case 2:
                    return new String[] {// State19 Effect2 works as same as Effect20 (IMO)
        ((originalState == 19) ? FIRE2 : FIRE).parserText(),   state18("0b101110", is18) };
                case 20:
                    return new String[] { FIRE2.parserText(),  state18("0b001110", is18) };
                case 21:
                    return new String[] { FIRE2.parserText(),  state18("0b101110", false) };
                case 22:
                    return new String[] { EXFIRE.parserText(), state18("0b101110", false) };
                case 23:
                    return new String[] { EXPLO.parserText(),  state18("0b101110", is18) };
                case 3:
                    return new String[] { ICE.parserText(),    state18("0b101110", is18) };
                case 30:
                    return new String[] { ICE2.parserText(),   state18("0b101110", is18) };
                case 4:
                    return new String[] { PUNCH.parserText(),  state18("0b111100", is18) };
                default:
                    return new String[] { OTHER.parserText(),  state18("0b101110", is18) };
            }
        case 4:
            return new String[] { FALLING.parserText(),  state18("0b111101", is18) };
        case 9:
            return new String[] { REFLECT.parserText(),  state18("0b111110", is18) };
        case 10:
        case 11:
            return new String[] { SONATA.parserText(),   state18("0b001110", is18) };
        case 16:
            return new String[] { SPICE.parserText(),    state18("0b000010", is18) };
        case 8:
            return new String[] { HEAL.parserText(),     state18("0b000011", is18) };
        case 1:
            return new String[] { GRASPDOP.parserText(), state18("0b000010", is18), "CatchType" };
        case 3:
            return new String[] { GRASPBDY.parserText(), state18("0b000010", is18), "CatchType" };
        case 2:
            return new String[] { PICKSTAND.parserText(),state18("0b001100", is18), "StrongType" };
        case 7:
            return new String[] { PICKROLL.parserText(), state18("0b001100", is18), "StrongType" };
        case 6:
            return new String[] { LETSP.parserText(),    state18("0b000010", is18), "StrongType" };
        case 14:
            return new String[] { FENCE.parserText(),    state18("0b111111", is18), "StrongType" };
        case 15:
              return new String[] { VORTEX.parserText(),   state18("0b001110", is18), "StrongType" };
          case 5:
              return new String[] { WPSTREN.parserText(),  state18("0b000000", is18), "StrongType" };
          default:
              System.out.printf("\tUnknown kind %s\n", originalKind);
              return null;
      }
    }*/