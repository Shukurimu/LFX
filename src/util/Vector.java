package util;

public record Vector(double x, double y, double z) {
  public static final Vector ZERO = new Vector(0, 0, 0);

}
