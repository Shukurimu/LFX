package component;

import base.Box;
import base.Scope;

public class Itr {
  public static final int ACT_PAUSE = 3;

  // TODO: Add dedicated field for effect (kind0, SHIELD, THROW_DAMAGE) to save some branch.
  public enum Kind {
    GRAB_DOP    (false,  true, ""),  // kind1
    PICK        (false,  true, ""),  // kind2
    GRAB_BDY    (false,  true, ""),  // kind3
    THROW_DAMAGE(false, false, ""),  // kind4
    STRENGTH    (false, false, ""),  // kind5
    FORCE_ACT   (false, false, ""),  // extended kind6: dvy as target actNumber
    ROLL_PICK   (false,  true, ""),  // kind7
    HEAL        (false, false, ""),  // kind8
    SHIELD      ( true, false, ""),  // kind9
    SONATA      (false, false, ""),  // kind10.kind11
    BLOCK       (false, false, ""),  // kind14
    VORTEX      (false, false, ""),  // kind15
    // kind16 is replaced by ICE and calling smooth()
    PUNCH    (true, false, "data/001.wav"),  // effect0
    STAB     (true, false, "data/032.wav"),  // effect1
    FIRE     (true, false, "data/070.wav"),  // effect2
    WEAK_FIRE(true, false, "data/070.wav"),  // effect20.effect21
    ICE      (true, false, "data/065.wav"),  // effect3
    WEAK_ICE (true, false, "data/065.wav"),  // effect30
    NONE     (true, false, "");
    // effect4 is replaced by NON_HERO_SCOPE
    // effect23 is replaced by specifying exp

    public final boolean damage;
    public final boolean raceCondition;
    public final String sound;

    private Kind(boolean damage, boolean raceCondition, String sound) {
      this.damage = damage;
      this.raceCondition = raceCondition;
      this.sound = sound;
    }

  }

  public final Box box;
  public final Kind kind;
  public final int scope;
  public final int vrest;  // negative value as arest
  public final int dvx;  // also catchingact
  public final int dvy;  // also caughtact
  public final int fall;
  public final int bdefend;
  public final int injury;
  public final int actPause;
  public final boolean twoSides;  // effect:23

  public Itr(Box box, Kind kind, int scope, int vrest, int dvx, int dvy,
             int fall, int bdefend, int injury, boolean twoSides, int actPause) {
    this.box = box;
    this.kind = kind;
    this.scope = scope;
    this.vrest = vrest;
    this.dvx = dvx;
    this.dvy = dvy;
    this.fall = fall;
    this.bdefend = bdefend;
    this.injury = injury;
    this.actPause = actPause;
    this.twoSides = twoSides;
  }

  public Itr(Box box, Kind kind, int scope, int vrest, int dvx, int dvy,
             int fall, int bdefend, int injury, boolean twoSides) {
    this(box, kind, scope, vrest, dvx, dvy, fall, bdefend, injury, twoSides, ACT_PAUSE);
  }

  public Itr(Box box, Kind kind, int scope, int vrest, int dvx, int dvy,
             int fall, int bdefend, int injury, int actPause) {
    this(box, kind, scope, vrest, dvx, dvy, fall, bdefend, injury, false, actPause);
  }

  public Itr(Box box, Kind kind, int scope, int vrest, int dvx, int dvy,
             int fall, int bdefend, int injury) {
    this(box, kind, scope, vrest, dvx, dvy, fall, bdefend, injury, false, ACT_PAUSE);
  }

  // Normal grabbing. Use constructor if you really want to grab teammate.
  public static Itr grab(Box box, boolean forceGrasp, int catchingAct, int caughtAct) {
    return new Itr(box, forceGrasp ? Kind.GRAB_BDY : Kind.GRAB_DOP, 0, Scope.ITR_ENEMY_HERO,
                   catchingAct, caughtAct, 0, 0, 0, false, 0);
  }

  // Non-damage: PICK, BLOCK, ...
  public static Itr kind(Box box, Kind kind, int scope) {
    return new Itr(box, kind, scope, 0, 0, 0, 0, 0, 0, false, 0);
  }

  // weapon on hand
  public static Itr onHand(Box box) {
    return new Itr(box, Kind.NONE, 0, 0, 0, 0, 0, 0, 0, false, 0);
  }

  // weapon strength list (treated as hero's attack)
  public static Itr strength(Kind kind, int vrest, int dvx, int dvy,
                             int fall, int bdefend, int injury) {
    return new Itr(null, kind, Scope.ITR_HERO, vrest, dvx, dvy,
                   fall, bdefend, injury, false, ACT_PAUSE);
  }

  public int calcPause(int originalValue) {
    return Math.max(originalValue, actPause);
  }

  /**
   * Explosion kind negative dvx goes two directions.
   * @param px of bdy source
   * @param faceRight of itr source
   * @return expecting dvx caused by this itr
   */
  public double calcDvx(double px, boolean faceRight) {
    return twoSides ? (px < (box.x + box.w / 2) ? dvx : -dvx)
                    : (faceRight ? dvx : -dvx);
  }

  @Override
  public String toString() {
    return String.format("Itr(%s, %s, scope %d, vrest %d, %d %d, %d %d %d, pause %d, %b)",
                         box, kind, scope, vrest, dvx, dvy,
                         fall, bdefend, injury, actPause, twoSides
    );
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
