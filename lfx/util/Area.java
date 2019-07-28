package lfx.util;

import lfx.util.Box;

public final class Area {
  public final double x1 = 0.0;
  public final double x2 = 0.0;
  public final double y1 = 0.0;
  public final double y2 = 0.0;
  public final double z1 = 0.0;
  public final double z2 = 0.0;

  public Area(double ax, double ay, double pz, boolean faceRight, Box box) {
    x1 = faceRight ? (ax + box.x) : (ax - (box.x + box.w));
    x2 = x1 + box.w;
    y1 = ay + box.y;
    y2 = y1 + box.h;
    z1 = pz - box.zu;
    z2 = pz + box.zd;
  }

  public boolean collidesWith(Area another) {
    return x1 < another.x2 && x2 > another.x1
        && z1 < another.z2 && z2 > another.z1
        && y1 < another.y2 && y2 > another.y1;
  }

}
