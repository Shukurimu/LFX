
final class LFwpoint {
  public final int x, y;
  public final int waction;// weaponact
  public final int attacking;// < 0 to drop
  public final int dvx;
  public final int dvy;
  public final int dvz;
  public final boolean cover;

  public LFwpoint() {
    x = y = waction = attacking = dvx = dvy = dvz = 0;
    cover = false;
  }

  public LFwpoint(int xx, int yy, int wa, int a, int dx, int dy, int dz, int c) {
    x = xx;
    y = yy;
    waction = wa;
    attacking = a;
    dvx = dx;
    dvy = dy;
    dvz = dz;
    cover = (c == 1);
  }

}
