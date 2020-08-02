package lfx.util;

public class Area {
  public final double x1;
  public final double x2;
  public final double y1;
  public final double y2;
  public final double z1;
  public final double z2;

  public Area(double x1, double x2, double y1, double y2, double z1, double z2) {
    this.x1 = x1;
    this.x2 = x2;
    this.y1 = y1;
    this.y2 = y2;
    this.z1 = z1;
    this.z2 = z2;
  }

  public boolean collidesWith(Area another) {
    return x1 < another.x2 && x2 > another.x1
        && z1 < another.z2 && z2 > another.z1
        && y1 < another.y2 && y2 > another.y1;
  }

  @Override
  public String toString() {
    return String.format("Area(x %5d ~ %5d, y %3d ~ %5d, z %3d ~ %d", x1, x2, y1, y2, z1, z2);
  }

}
