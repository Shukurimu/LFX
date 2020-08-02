package lfx.component;

import lfx.util.Const;
import lfx.util.Point;

public class Wpoint extends Point {
  public final int weaponact;
  public final int dvx;
  public final int dvy;
  public final int dvz;
  public final int usage;
  public final double zOffset;

  private Wpoint(int x, int y, int weaponact, int cover,
                 int dvx, int dvy, int dvz, int usage) {
    super(x, y);
    this.weaponact = weaponact;
    this.dvx = dvx;
    this.dvy = dvy;
    this.dvz = dvz;
    this.usage = usage;
    this.zOffset = cover == 0 ? Z_OFFSET : -Z_OFFSET;
  }

  // Holding
  public Wpoint(int x, int y, int weaponact, int cover) {
    this(x, y, weaponact, cover, 0, 0, 0, 0);
  }

  // Attacking.Drop
  public Wpoint(int x, int y, int weaponact, int cover, int usage) {
    this(x, y, weaponact, cover, 0, 0, 0, usage);
  }

  // Throw
  public Wpoint(int x, int y, int weaponact, int cover, int dvx, int dvy, int dvz) {
    this(x, y, weaponact, cover, dvx, dvy, dvz, -1);
  }

}
