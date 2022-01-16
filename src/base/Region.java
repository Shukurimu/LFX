package base;

import util.IntMap;

public record Region(double x1, double x2, double y1, double y2, double z1, double z2) {

  /**
   * Half of default z-width value.
   */
  public static final double DEFAULT_HALF_ZWIDTH = 12;

  /**
   * Representing zero region, which affects nothing.
   */
  public static final Region EMPTY = new Region(0, 0, 0, 0, 0, 0);

  /**
   * Creates a relative {@code Region} with default z-width.
   *
   * @param x starting x coordinate
   * @param y starting y coordinate
   * @param w the width of this {@code Region}
   * @param h the height of this {@code Region}
   * @return a relative {@code Region}
   */
  public static Region of(double x, double y, double w, double h) {
    return new Region(x, w, y, h, DEFAULT_HALF_ZWIDTH, DEFAULT_HALF_ZWIDTH);
  }

  /**
   * Creates a relative {@code Region} with specified z-width.
   * z coverage spans equally toward furthest and nearest.
   *
   * @param x starting x coordinate
   * @param y starting y coordinate
   * @param w the width of this {@code Region}
   * @param h the height of this {@code Region}
   * @param z the z-width of this {@code Region}
   * @return a relative {@code Region}
   */
  public static Region of(double x, double y, double w, double h, double zwidth) {
    return new Region(x, w, y, h, zwidth * 0.5, zwidth * 0.5);
  }

  /**
   * Returns a {@code Region} representing absolute coordinate.
   *
   * @param anchorX   starting x coordinate
   * @param anchorY   starting y coordinate
   * @param posZ      the base z coordinate
   * @param faceRight source object's facing
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

  /**
   * Checks if this {@code Region} collides with another one.
   * Both should be in absolute coordinate.
   *
   * @param another {@code Region} to check
   * @return {@code true} if two {@code Region} intersect
   */
  public boolean collidesWith(Region another) {
    return x1 < another.x2 && x2 > another.x1
        && z1 < another.z2 && z2 > another.z1
        && y1 < another.y2 && y2 > another.y1;
  }

  @Override
  public String toString() {
    return String.format("Region[%.0f ~ %.0f, %.0f ~ %.0f, %.0f ~ %.0f]", x1, x2, y1, y2, z1, z2);
  }

  // ==================== Parser Utility ====================

  /**
   * Extracts and prepares {@code Region} setting from a itr or bdy block.
   * Targeting fields include x, y, w, h, and optional zwidth.
   *
   * @param data a map containing key-value pairs
   * @return a statement to create a {@code Region}
   */
  public static String extract(IntMap data) {
    int x = data.pop("x");
    int y = data.pop("y");
    int w = data.pop("w");
    int h = data.pop("h");
    int zwidth = data.pop("zwidth", -1);
    if (zwidth == -1) {
      return "Region.of(%d, %d, %d, %d)".formatted(x, y, w, h);
    } else {
      return "Region.of(%d, %d, %d, %d, %d)".formatted(x, y, w, h, zwidth);
    }
  }

}
