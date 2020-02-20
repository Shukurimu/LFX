package lfx.component;

import lfx.util.Box;
import lfx.util.Const;
import lfx.util.Scope;
import lfx.util.Tuple;

public final class Itr {
  public static final int DVX = 0;
  public static final int DVY = -7;
  public static final int FALL = 20;
  public static final int BDEFEND = 0;
  public static final int INJURY = 0;
  public static final int AREST = -7;  // hero default
  public static final int VREST = +9;  // weapon default

  public enum Kind {
    GRASP_DOP (false,  true, ""),  // kind1
    PICK      (false,  true, ""),  // kind2
    GRASP_BDY (false,  true, ""),  // kind3
    THROW_ATK ( true, false, ""),  // kind4
    STRENGTH  (false, false, ""),  // kind5
    LET_SPUNCH(false, false, ""),  // kind6
    ROLL_PICK (false,  true, ""),  // kind7
    HEAL      (false, false, ""),  // kind8
    SHIELD    ( true, false, ""),  // kind9
    SONATA    ( true, false, ""),  // kind10.kind11
    BLOCK     (false, false, ""),  // kind14
    VORTEX    ( true, false, ""),  // kind15
    // kind16 is replaced by ICE and calling smooth()
    PUNCH   (true, false, "data/001.wav"),  // effect0
    STAB    (true, false, "data/032.wav"),  // effect1
    FIRE    (true, false, "data/070.wav"),  // effect2
    ICE     (true, false, "data/065.wav"),  // effect3
    WEAKFIRE(true, false, "data/070.wav"),  // effect20.effect21
    WEAKICE (true, false, "data/065.wav"),  // effect30
    NONE    (true, false, "");
    // effect4 is replaced by NON_HERO_SCOPE
    // effect23 is replaced by calling exp()

    public final boolean damage;
    public final boolean callback;
    public final String sound;

    private Kind(boolean damage, boolean callback, String sound) {
      this.damage = damage;
      this.callback = callback;
      this.sound = sound;
    }

  }

  public final Box box;
  public final Kind kind;
  public final int dvx;  // also catchingact
  public final int dvy;  // also caughtact
  public final int fall;
  public final int bdefend;
  public final int injury;
  public final int vrest;  // negative value as arest
  public final int scope;
  public final boolean nolag;
  public final boolean explosion;

  public Itr(Box box, Kind kind, Integer dvx, Integer dvy,
             Integer fall, Integer bdefend, Integer injury, int vrest, int scope, String attr) {
    this.box = box;
    this.kind = kind;
    this.dvx = dvx == null ? DVX : dvx.intValue();
    this.dvy = dvy == null ? DVY : dvy.intValue();
    this.fall = fall == null ? FALL : fall.intValue();
    this.bdefend = bdefend == null ? BDEFEND : bdefend.intValue();
    this.injury = injury == null ? INJURY : injury.intValue();
    this.vrest = vrest;
    this.scope = scope;
    nolag = attr.contains("nolag");
    explosion = attr.contains("exp");
  }

  public Itr(Box box, String kind, Integer dvx, Integer dvy,
             Integer fall, Integer bdefend, Integer injury, int vrest, int scope, String attr) {
    this(box, Kind.valueOf(kind), dvx, dvy, fall, bdefend, injury, vrest, scope, attr);
  }

  public Itr(Box box, Kind kind, Integer dvx, Integer dvy,
             Integer fall, Integer bdefend, Integer injury, int vrest, int scope) {
    this(box, kind, dvx, dvy, fall, bdefend, injury, vrest, scope, "");
  }

  public Itr(Box box, String kind, Integer dvx, Integer dvy,
             Integer fall, Integer bdefend, Integer injury, int vrest, int scope) {
    this(box, Kind.valueOf(kind), dvx, dvy, fall, bdefend, injury, vrest, scope, "");
  }

  // grab (use constructor if you really want to grab teammate)
  public static Itr grab(Box box, boolean forceGrasp, int catchingact, int caughtact) {
    return new Itr(box, forceGrasp ? Kind.GRASP_BDY : Kind.GRASP_DOP,
                   catchingact, caughtact, 0, 0, 0, 0,
                   Scope.ITR_ENEMY_HERO, "");
  }

  // kind only (PICK, BLOCK, ...)
  public static Itr kind(Box box, String kind, int scope) {
    return new Itr(box, Kind.valueOf(kind), 0, 0, 0, 0, 0, 0, scope, "");
  }

  // weapon on hand
  public static Itr onHand(Box box) {
    return new Itr(box, Kind.NONE, 0, 0, 0, 0, 0, 0, 0, "");
  }

  // weapon strength list (treated as hero's attack)
  public static Itr strength(String kind, int dvx, int dvy,
                             int fall, int bdefend, int injury, int vrest) {
    return new Itr(null, Kind.valueOf(kind), dvx, dvy,
                   fall, bdefend, injury, vrest, Scope.ITR_HERO, "");
  }

  public int calcLag(int originalValue) {
    return nolag ? originalValue : Math.max(originalValue, Const.LAG);
  }

  // Explosion kind negative dvx goes two directions.
  public Tuple<Double, Boolean> calcDvx(double px, boolean faceRight) {
    Double vx = explosion ? (px < (box.x + box.w / 2) ? -dvx : dvx)
                          : Double.valueOf(faceRight ? dvx : -dvx);
    Boolean facing = faceRight == (dvx >= 0.0);
    return new Tuple<>(vx, facing);
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
