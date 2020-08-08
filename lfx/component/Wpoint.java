package lfx.component;

import lfx.base.Action;
import lfx.util.Point;

public class Wpoint extends Point {

  public enum Usage {
    THROW,
    JUST_HOLD,
    NORMAL,
    JUMP,
    RUN,
    DASH;
  }

  public final Action weaponact;
  public final int dvx;
  public final int dvy;
  public final int dvz;
  public final Usage usage;
  public final double zOffset;

  private Wpoint(int x, int y, Action weaponact, int cover,
                 int dvx, int dvy, int dvz, Usage usage) {
    super(x, y);
    this.weaponact = weaponact;
    this.dvx = dvx;
    this.dvy = dvy;
    this.dvz = dvz;
    this.usage = usage;
    this.zOffset = cover == 0 ? Z_OFFSET : -Z_OFFSET;
  }

  // Holding
  public Wpoint(int x, int y, Action weaponact, int cover) {
    this(x, y, weaponact, cover, 0, 0, 0, Usage.JUST_HOLD);
  }

  // Attacking.Drop
  public Wpoint(int x, int y, Action weaponact, int cover, Usage usage) {
    this(x, y, weaponact, cover, 0, 0, 0, usage);
  }

  // Throw
  public Wpoint(int x, int y, Action weaponact, int cover, int dvx, int dvy, int dvz) {
    this(x, y, weaponact, cover, dvx, dvy, dvz, Usage.THROW);
  }

}
