package component;

import base.Region;

/**
 * To simplify itr-bdy logic, some state-related attributes are transplanted here.
 */
public class Bdy {
  public static final int FRIENDLY_FIRE    = 0x1;  // frozen; FreezeColumn
  public static final int DANCE_OF_PAIN    = 0x10;
  public static final int ROLLING_PICKABLE = 0x100;
  public static final int IMMUNE_WEAK_FIRE = 0x1000;
  public static final int IMMUNE_WEAK_ICE  = 0x10000;  // frozen
  public static final int IMMUNE_FALL_40   = 0x100000;

  public final Region relative;
  public final int attributes;

  private Bdy(Region relative, int attributes) {
    this.relative = relative;
    this.attributes = attributes;
  }

  public static Bdy of(Region relative) {
    return new Bdy(relative, 0);
  }

  public static Bdy of(Region relative, int attributes) {
    return new Bdy(relative, attributes);
  }

  @Override
  public String toString() {
    return String.format("Bdy[%s, attributes=%x]", relative, attributes);
  }

}
