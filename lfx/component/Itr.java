package lfx.component;

import lfx.component.Type;
import lfx.util.Box;

// https://lf-empire.de/lf2-empire/data-changing/frame-elements/174-itr-interaction?showall=1

public final class Itr {
  public static final int LAG = 3;
  public static final int DVX = 0;
  public static final int DVY = -7;
  public static final int BDEFEND = 0;
  public static final int FALL = 20;
  public static final int INJURY = 0;
  public static final int VREST = 9;  // weapon default
  public static final int AREST = -7;  // hero default
  public static final int HERO_SCOPE =
      Type.HERO.enemyView() | Type.BLAST.allView() | Type.WEAPON.allView();
  public static final int WEAPON_SCOPE = HERO_SCOPE;
  public static final int BLAST_SCOPE =
      Type.HERO.enemyView() | Type.BLAST.enemyView() | Type.WEAPON.allView();
  public static final int NON_HERO_SCOPE = Type.BLAST.allView() | Type.WEAPON.allView();

  public enum Effect {
    GRASP_DOP (""),  // kind1
    PICK      (""),  // kind2
    GRASP_BDY (""),  // kind3
    THROW_ATK (""),  // kind4
    W_STRENGTH(""),  // kind5
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

    private Effect(String sound) {
      this.sound = sound;
    }

    @Override
    public String toString() {
      return String.format("%s.%s", this.getDeclaringClass().getSimpleName(), super.toString());
    }

  }

  public final Box box;
  public final Effect effect;
  public final int dvx;  // also catchingact
  public final int dvy;  // also caughtact
  public final int bdefend;
  public final int injury;
  public final int fall;
  public final int vrest;  // negative value as arest
  public final int scope;
  private int lag = LAG;
  private boolean explosion = false;

  public Itr(Box box, Effect effect, int dvx, int dvy,
             int bdefend, int injury, int fall, int vrest, int scope) {
    this.box = box;
    this.effect = effect;
    this.dvx = dvx;
    this.dvy = dvy;
    this.bdefend = bdefend;
    this.injury = injury;
    this.fall = fall;
    this.vrest = vrest;
    this.scope = scope;
  }

  // grasp (use complete constructor if you really want to grasp teammate)
  public Itr(Box box, boolean forceGrasp, int catchingact, int caughtact) {
    this(box, forceGrasp ? Effect.GRASP_BDY : Effect.GRASP_DOP,
         catchingact, caughtact, 0, 0, 0, 0, Type.HERO.enemyView());
  }

  // effect only (PICK, BLOCK, ...)
  public Itr(Box box, Effect effect, int scope) {
    this(box, effect, 0, 0, 0, 0, 0, 0, scope);
  }

  // weapon strength list (treated as hero's attack)
  public Itr(Effect effect, int dvx, int dvy, int bdefend, int injury, int fall, int vrest) {
    this(null, effect, dvx, dvy, bdefend, injury, fall, vrest, HERO_SCOPE);
  }

  public Itr smooth() {
    lag = 0;
    return this;
  }

  public Itr exp() {
    explosion = true;
    return this;
  }

  public int calcLag(int originalValue) {
    return Math.max(originalValue, lag);
  }

  /** explosion-effect negative dvx goes two directions */
  public double calcDvx(boolean faceRight) {
    return explosion ? (px < ba.px ? -dvx : dvx)
                     : (faceRight ? dvx : -dvx);
  }

  /** The followings are the self implementation of several itr kinds
      since no documentation discussing about this effect
      the result is very likely different from LF2 */
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
                    return new String[] {/* State19 Effect2 works as same as Effect20 (IMO) */
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
    }

}
