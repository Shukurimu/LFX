package base;

public record Region(double x1, double x2,
                     double y1, double y2,
                     double z1, double z2) {
  public static final double DEFAULT_HALF_ZWIDTH = 12;
  public static final Region EMPTY = new Region(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

  public static Region of(double x, double y, double w, double h) {
    return new Region(x, w, y, h, DEFAULT_HALF_ZWIDTH, DEFAULT_HALF_ZWIDTH);
  }

  public static Region of(double x, double y, double w, double h, double zwidth) {
    return new Region(x, w, y, h, zwidth * 0.5, zwidth * 0.5);
  }

  public boolean collidesWith(Region another) {
    return x1 < another.x2 && x2 > another.x1
        && z1 < another.z2 && z2 > another.z1
        && y1 < another.y2 && y2 > another.y1;
  }

  /**
   * Returns a {@code Region} representing absolute coordinate in the field.
   *
   * @param  anchorX the starting x coordinate
   * @param  anchorY the starting y coordinate
   * @param  posZ the base z coordinate
   * @param  faceRight the source object's facing
   * @return an absolute {@code Region}
   */
  public Region toAbsolute(double anchorX, double anchorY, double posZ, boolean faceRight) {
    double startPosX = faceRight ? anchorX + x1 : anchorX - x1 - x2;
    double startPosY = anchorY + y1;
    return new Region(startPosX, startPosX + x2,
                      startPosY, startPosY + y2,
                      posZ - z1, posZ + z2
    );
  }

}
