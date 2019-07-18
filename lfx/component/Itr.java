package lfx.component;

import java.util.List;

public final class Itr {
  public static final int ZWIDTH = 12;
  public static final int DVX = 0;
  public static final int DVY = -7;
  public static final int BDEFEND = 0;
  public static final int FALL = 20;
  public static final int INJURY = 0;
  public static final int VREST = 9;  // weapon default
  public static final int AREST = -7;  // hero default
  public static final List<> nowhere = List.of(0, 0, 0, 0, 0);
  public static final List<> everywhere = List.of(-1000000, -1000000, 2000000, 2000000, 3000);

  public enum Effect {
    // basic move
    WEAPON_ATK(""),
    VORTEX    (""),
    LET_SPUNCH(""),
    FENCE     (""),
    HEAL      (""),
    PICK      (""),
    PICK_ROLL (""),
    GRASP_DOP (""),
    GRASP_BDY (""),

    SONATA  (""),
    REFLECT (""),
    FALLING (""),
    SILENT  (""),

    NONE    (""),
    OTHER   (""),
    PUNCH   ("data/001.wav"),
    STAB    ("data/032.wav"),
    FIRE    ("data/070.wav"),
    ICE     ("data/065.wav"),
    WEAKFIRE("data/070.wav"),
    WEAKICE ("data/065.wav");

    public final String sound;

    private Effect(String sound) {
      this.sound = sound;
    }

    @Override
    public String toString() {
      return String.format("%s.%s", this.getClass().getSimpleName(), super.toString());
    }

  }

  public final Effect effect;
  public final int x;
  public final int y;
  public final int w;
  public final int h;
  public final int zwidth;
  public final int dvx;  // also catchingact
  public final int dvy;  // also caughtact
  public final int bdefend;
  public final int injury;
  public final int fall;
  public final int vrest;  // negative value -> arest
  public final int scope;

  // range must have size 4 (x, y, w, h) or 5 (x, y, w, h, zwidth)
  public Itr(Effect effect, List<Integer> range, int dvx, int dvy,
             int bdefend, int injury, int fall, int vrest, int scope) {
    this.effect = effect;
    this.x = range.get(0);
    this.y = range.get(1);
    this.w = range.get(2);
    this.h = range.get(3);
    this.zwidth = range.size() == 4 ? ZWIDTH : range.get(4);
    this.dvx = dvx;
    this.dvy = dvy;
    this.bdefend = bdefend;
    this.injury = injury;
    this.fall = fall;
    this.vrest = vrest;
    this.scope = scope;
  }

  // grasp
  public Itr(Effect effect, List<Integer> range, int catchingact, int caughtact, int scope) {
    this(effect, range, catchingact, caughtact,
         0, 0, 0, 0, scope);
  }

  // only effect (e.g., ice column block)
  public Itr(Effect effect, List<Integer> range, int scope) {
    this(effect, range, 0, 0,
         0, 0, 0, 0, scope);
  }

  // weapon strength list
  public Itr(Effect effect, int dvx, int dvy,
             int bdefend, int injury, int fall, int vrest, int scope) {
    this(effect, nowhere, dvx, dvy,
         bdefend, injury, fall, vrest, scope);
  }

  /* weapon strength list to LFarea */
  public Itr(LFitr itrField, LFitr strength) {
    this(strength.effect, itrField.x, itrField.y, itrField.w, itrField.h, itrField.z,
      strength.dvx, strength.dvy, strength.bdefend, strength.injury, strength.fall,
      strength.vrest, strength.scope
    );
  }
  /* explosion-effect negative dvx goes two directions */
  public double calcDvx(LFbdyarea ba) {
    if (itr.effect.explosionType) {
      return (px < ba.px) ? (-itr.dvx) : itr.dvx;
    } else {
      return faceRight ? itr.dvx : (-itr.dvx);
    }
  }

  /* The followings are the self implementation of several itr kinds
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
