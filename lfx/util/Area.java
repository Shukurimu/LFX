package lfx.util;

public final class Area {
  private final double x1 = 0.0;
  private final double x2 = 0.0;
  private final double y1 = 0.0;
  private final double y2 = 0.0;
  private final double z1 = 0.0;
  private final double z2 = 0.0;

  public Area(double cx, double cy, boolean faceRight,
              int x, int y, int w, int h, int zwidth) {
    x1 = cx + faceRight ? x : -(x + w);
    x2 = x1 + w;
    y1 = cy + y;
    y2 = y1 + h;
    z1 = cz - zwidth;
    z2 = cz + zwidth;
  }

  public boolean collidesWith(Area another) {
    return x1 < another.x2 && x2 > another.x1 &&
           z1 < another.z2 && z2 > another.z1 &&
           y1 < another.y2 && y2 > another.y1;
  }

}
