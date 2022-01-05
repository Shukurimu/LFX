package util;

public record Vector(double x, double y, double z) {
  public static final Vector ZERO = new Vector(0.0, 0.0, 0.0);

  public static Vector of(double x, double y) {
    return (x != 0.0 || y != 0.0) ? new Vector(x, y, 0.0) : ZERO;
  }

  public static Vector of(double x, double y, double z) {
    return (x != 0.0 || y != 0.0 || z != 0.0) ? new Vector(x, y, z) : ZERO;
  }

  public static double findComponent(double v1a, double v1b, double v2a) {
    return Math.sqrt(v1a * v1a + v1b * v1b - v2a * v2a);
  }

}
