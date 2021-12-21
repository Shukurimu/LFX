package component;

import base.Region;
import base.Scope;

public class Itr {

  public enum Kind {
    // https://www.lf-empire.de/lf2-empire/data-changing/reference-pages/181-effects
    PUNCH           ("data/001.wav"),  // kind: 0  effect: 0
    STAB            ("data/032.wav"),  // kind: 0  effect: 1
    FIRE            ("data/070.wav"),  // kind: 0  effect: 2
    WEAK_FIRE       ("data/070.wav"),  // kind: 0  effect: 20.21
    ICE             ("data/065.wav"),  // kind: 0  effect: 3
    WEAK_ICE        ("data/065.wav"),  // kind: 0  effect: 30
    GRAB_DOP        (null),  // kind: 1
    PICK            (null),  // kind: 2
    GRAB_BDY        (null),  // kind: 3
    THROWN_DAMAGE   (null),  // kind: 4
    WEAPON_STRENGTH (null),  // kind: 5
    FORCE_ACTION    (null),  // kind: 6
    ROLL_PICK       (null),  // kind: 7
    HEAL            (null),  // kind: 8
    SHIELD          (null),  // kind: 9
    SONATA          (null),  // kind: 10
    BLOCK           (null),  // kind: 14
    VORTEX          (null);  // kind: 15

    public final String soundPath;

    private Kind(String soundPath) {
      this.soundPath = soundPath;
    }
  }

  public static final Itr NULL_ITR = new Itr(Kind.PUNCH, Region.EMPTY, 0, 1);

  /***/
  public final Kind kind;
  /**
   * A relative {@code Region} representing x, y, w, h, zwidth.
   */
  public final Region relative;
  /**
   * The scope of this {@code Itr} that can be applied to.
   */
  public final int scope;
  /**
   * A negative value means arest.
   */
  public final int vrest;
  /**
   * Due to the variety of {@code Itr}, an {@code Object} is used.
   * The underlying logics vary by {@code Kind}.
   */
  public final Object data;

  public Itr(Kind kind, Region relative, int scope, int vrest, Object data) {
    if (vrest == 0) {
      throw new IllegalArgumentException("Vrest cannot be zero.");
    }
    this.kind = kind;
    this.relative = relative;
    this.scope = scope;
    this.vrest = vrest;
    this.data = data;
  }

  public Itr(Kind kind, Region relative, int scope, int vrest) {
    this(kind, relative, scope, vrest, null);
  }

  public boolean interactsWith(Bdy bdy, int bdyScopeView) {
    bdyScopeView = Scope.getBothView(bdyScopeView);
    if ((bdyScopeView & scope) == 0) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return String.format("Itr[%s, %s, scope=%x, vrest=%d]", kind, relative, scope, vrest);
  }

}

/*
 * // https://lf-empire.de/lf2-empire/data-changing/frame-elements/174-itr-interaction?showall=1
 *
 * public static String state18(String originalScope, boolean is18) {
 * if (!is18) return originalScope;
 * char[] newScope = originalScope.toCharArray();
 * newScope[3] = (newScope[2] == '1') ? '1' : newScope[3];
 * newScope[5] = (newScope[4] == '1') ? '1' : newScope[5];
 * newScope[7] = (newScope[6] == '1') ? '1' : newScope[7];
 * return String.valueOf(newScope);
 * }
 * public static String[] parserKindMap(int originalKind, int originalEffect,
 * int originalState) {
 * boolean is18 = originalState == 18;
 * switch (originalKind) {
 * case 0:
 * switch (originalEffect) {
 * case 1:
 * return new String[] { STAB.parserText(), state18("0b101110", is18) };
 * case 2:
 * return new String[] {// State19 Effect2 works as same as Effect20 (IMO)
 * ((originalState == 19) ? FIRE2 : FIRE).parserText(), state18("0b101110",
 * is18) };
 * case 20:
 * return new String[] { FIRE2.parserText(), state18("0b001110", is18) };
 * case 21:
 * return new String[] { FIRE2.parserText(), state18("0b101110", false) };
 * case 22:
 * return new String[] { EXFIRE.parserText(), state18("0b101110", false) };
 * case 23:
 * return new String[] { EXPLO.parserText(), state18("0b101110", is18) };
 * case 3:
 * return new String[] { ICE.parserText(), state18("0b101110", is18) };
 * case 30:
 * return new String[] { ICE2.parserText(), state18("0b101110", is18) };
 * case 4:
 * return new String[] { PUNCH.parserText(), state18("0b111100", is18) };
 * default:
 * return new String[] { OTHER.parserText(), state18("0b101110", is18) };
 * }
 * case 4:
 * return new String[] { FALLING.parserText(), state18("0b111101", is18) };
 * case 9:
 * return new String[] { REFLECT.parserText(), state18("0b111110", is18) };
 * case 10:
 * case 11:
 * return new String[] { SONATA.parserText(), state18("0b001110", is18) };
 * case 16:
 * return new String[] { SPICE.parserText(), state18("0b000010", is18) };
 * case 8:
 * return new String[] { HEAL.parserText(), state18("0b000011", is18) };
 * case 1:
 * return new String[] { GRASPDOP.parserText(), state18("0b000010", is18),
 * "CatchType" };
 * case 3:
 * return new String[] { GRASPBDY.parserText(), state18("0b000010", is18),
 * "CatchType" };
 * case 2:
 * return new String[] { PICKSTAND.parserText(),state18("0b001100", is18),
 * "StrongType" };
 * case 7:
 * return new String[] { PICKROLL.parserText(), state18("0b001100", is18),
 * "StrongType" };
 * case 6:
 * return new String[] { LETSP.parserText(), state18("0b000010", is18),
 * "StrongType" };
 * case 14:
 * return new String[] { FENCE.parserText(), state18("0b111111", is18),
 * "StrongType" };
 * case 15:
 * return new String[] { VORTEX.parserText(), state18("0b001110", is18),
 * "StrongType" };
 * case 5:
 * return new String[] { WPSTREN.parserText(), state18("0b000000", is18),
 * "StrongType" };
 * default:
 * System.out.printf("\tUnknown kind %s\n", originalKind);
 * return null;
 * }
 * }
 */
