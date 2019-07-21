package lfx.component;

public class Wpoint {
  public static final double Z_OFFSET = 1e-3;

  public final int x;
  public final int y;
  public final int weaponact;
  public final int attacking;  // set a negative value to drop
  public final int dvx;
  public final int dvy;
  public final int dvz;
  public final double zOffset;  // rendering z-offset

  public Wpoint(int x, int y, int weaponact, int attacking,
                int dvx, int dvy, int dvz, int cover) {
    this.x = x;
    this.y = y;
    this.weaponact = weaponact;
    this.attacking = attacking;
    this.dvx = dvx;
    this.dvy = dvy;
    this.dvz = dvz;
    this.zOffset = cover == 0 ? Z_OFFSET : -Z_OFFSET;
  }

}
