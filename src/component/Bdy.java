package component;

import base.Box;
import base.Scope;

/**
 * To simplify itr-bdy logic, some state-related attributes are transplanted here.
 */
public class Bdy {
  public static final int FRIENDLY_FIRE    = 1 << 0;  // frozen; FreezeColumn
  public static final int DANCE_OF_PAIN    = 1 << 1;
  public static final int ROLLING_PICKABLE = 1 << 2;
  public static final int IMMUNE_WEAK_FIRE = 1 << 3;
  public static final int IMMUNE_WEAK_ICE  = 1 << 4;  // frozen
  public static final int IMMUNE_FALL_40   = 1 << 5;

  public final Box box;
  public final int attributes;

  public Bdy(Box box, int attributes) {
    this.box = box;
    this.attributes = attributes;
  }

  public Bdy(int x, int y, int w, int h, int attributes) {
    this(new Box(x, y, w, h), attributes);
  }

  public Bdy(Box box) {
    this(box, 0);
  }

  public Bdy(int x, int y, int w, int h) {
    this(new Box(x, y, w, h), 0);
  }

  public boolean interactsWith(Itr itr, int scopeView) {
    if ((attributes & FRIENDLY_FIRE) != 0) {
      scopeView = Scope.getBothView(scopeView);
    }
    if ((scopeView & itr.scope) == 0) {
      return false;
    }
    if ((attributes & DANCE_OF_PAIN) == 0 && itr.kind == Itr.Kind.GRAB_DOP) {
      return false;
    }
    if ((attributes & ROLLING_PICKABLE) == 0 && itr.kind == Itr.Kind.ROLL_PICK) {
      return false;
    }
    if ((attributes & IMMUNE_WEAK_FIRE) != 0 && itr.kind == Itr.Kind.WEAK_FIRE) {
      return false;
    }
    if ((attributes & IMMUNE_WEAK_ICE) != 0 && itr.kind == Itr.Kind.WEAK_ICE) {
      return false;
    }
    if ((attributes & IMMUNE_FALL_40) != 0 && itr.fall <= 40) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return String.format("Bdy(%s, %d)", box.toString(), attributes);
  }

}
