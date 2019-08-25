package lfx.component;

import lfx.util.Point;

public class Wpoint extends Point {
  /** This small value is added to pz while being held,
      so that the weapon image is rendered in front/back of the character. */
  public static final double Z_OFFSET = 1e-3;

  public enum Usage {
    DROP    (false),
    HARMLESS(false),
    STAND   (true),
    JUMP    (true),
    RUN     (true),
    DASH    (true);

    public final boolean attacking;

    private Usage(boolean attacking) {
      this.attacking = attacking;
    }

    @Override
    public String toString() {
      return String.format("%s.%s", this.getDeclaringClass().getSimpleName(), super.toString());
    }

  }

  public final int weaponact;
  public final int dvx;
  public final int dvy;
  public final int dvz;
  public final Usage usage;
  public final double zOffset;  // rendering z-offset

  private Wpoint(int x, int y, int weaponact, int cover,
                 int dvx, int dvy, int dvz, Usage usage) {
    super(x, y);
    this.weaponact = weaponact;
    this.dvx = dvx;
    this.dvy = dvy;
    this.dvz = dvz;
    this.usage = usage;
    this.zOffset = cover == 0 ? Z_OFFSET : -Z_OFFSET;
  }

  /** Just being held */
  public Wpoint(int x, int y, int weaponact, int cover) {
    this(x, y, weaponact, cover, 0, 0, 0, Usage.HARMLESS);
  }

  /** Attack */
  public Wpoint(int x, int y, int weaponact, int cover, Usage usage) {
    this(x, y, weaponact, cover, 0, 0, 0, usage);
  }

  /** Thrown */
  public Wpoint(int x, int y, int weaponact, int cover, int dvx, int dvy, int dvz) {
    this(x, y, weaponact, cover, dvx, dvy, dvz, Usage.DROP);
  }

}
