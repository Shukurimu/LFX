package lfx.util;

public class Area {
  private double x1 = 0.0;
  private double x2 = 0.0;
  private double y1 = 0.0;
  private double y2 = 0.0;
  private double z1 = 0.0;
  private double z2 = 0.0;

  public static Area build(double cx, double cy, boolean faceRight,
                           int x, int y, int w, int h, int zwidth) {
    Area area = new Area();
    area.x1 = cx + faceRight ? x : -(x + w);
    area.x2 = area.x1 + w;
    area.y1 = cy + y;
    area.y2 = area.y1 + h;
    area.z1 = cz - zwidth;
    area.z2 = cz + zwidth;
    return area;
  }

  public boolean collidesWith(Area another) {
    return x1 < another.x2 && x2 > another.x1 &&
           z1 < another.z2 && z2 > another.z1 &&
           y1 < another.y2 && y2 > another.y1;
  }

}
