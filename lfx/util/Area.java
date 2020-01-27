package lfx.util;

import lfx.util.Box;

public final class Area {
  // Calculate the coordinate of Box.
  public final double x1;
  public final double x2;
  public final double y1;
  public final double y2;
  public final double z1;
  public final double z2;

  public Area(double anchorX, double anchorY, double pz, boolean faceRight, Box box) {
    x1 = anchorX + (faceRight ? box.x : (box.w - box.x));
    x2 = x1 + box.w;
    y1 = anchorY + box.y;
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
