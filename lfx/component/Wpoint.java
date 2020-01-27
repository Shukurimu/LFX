package lfx.component;

import lfx.util.Const;
import lfx.util.Point;

public final class Wpoint extends Point {

  public enum Usage {
    DROP    (false),
    HARMLESS(false),
    NORMAL  (true),
    JUMP    (true),
    RUN     (true),
    DASH    (true);

    public final boolean itrable;

    private Usage(boolean itrable) {
      this.itrable = itrable;
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
  public final double zOffset;

  private Wpoint(int x, int y, int weaponact, int cover,
                 int dvx, int dvy, int dvz, Usage usage) {
    super(x, y);
    this.weaponact = weaponact;
    this.dvx = dvx;
    this.dvy = dvy;
    this.dvz = dvz;
    this.usage = usage;
    this.zOffset = cover == 0 ? Const.Z_OFFSET : -Const.Z_OFFSET;
  }

  // Holding
  public Wpoint(int x, int y, int weaponact, int cover) {
    this(x, y, weaponact, cover, 0, 0, 0, Usage.HARMLESS);
  }

  // Attacking
  public Wpoint(int x, int y, int weaponact, int cover, Usage usage) {
    this(x, y, weaponact, cover, 0, 0, 0, usage);
  }

  // Throw
  public Wpoint(int x, int y, int weaponact, int cover, int dvx, int dvy, int dvz) {
    this(x, y, weaponact, cover, dvx, dvy, dvz, Usage.DROP);
  }

}
