package component;

import base.Point;

public class Wpoint extends Point {

  public enum Usage {
    THROW,
    DROP,
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
  public final boolean cover;

  private Wpoint(int x, int y, Action weaponact, int cover,
                 int dvx, int dvy, int dvz, Usage usage) {
    super(x, y);
    this.weaponact = weaponact;
    this.dvx = dvx;
    this.dvy = dvy;
    this.dvz = dvz;
    this.usage = usage;
    this.cover = cover == 0;
  }

  public static Wpoint hold(int x, int y, Action weaponact, int cover) {
    return new Wpoint(x, y, weaponact, cover, 0, 0, 0, Usage.JUST_HOLD);
  }

  public static Wpoint attack(int x, int y, Action weaponact, int cover, String usage) {
    return new Wpoint(x, y, weaponact, cover, 0, 0, 0, Usage.valueOf(usage));
  }

  public static Wpoint release(int x, int y, Action weaponact, int cover,
                               boolean throwing, int dvx, int dvy, int dvz) {
    return new Wpoint(x, y, weaponact, cover, dvx, dvy, dvz,
                      throwing ? Usage.THROW : Usage.DROP);
  }

}
