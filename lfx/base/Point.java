package lfx.base;

public class Point {
  public static final Point ORIGIN = new Point(0.0, 0.0);
  public static final double Z_OFFSET = 1e-3;
  public final double x;
  public final double y;

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

}
