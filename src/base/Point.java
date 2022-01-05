package base;

public class Point {

  /**
   * The {@code Point} representing origin, or zero offset.
   */
  public static final Point ORIGIN = new Point(0.0, 0.0);

  /**
   * A small value adjustment for rendering order.
   */
  public static final double Z_EPSILON = 1e-3;

  /**
   * The x coordinate.
   */
  public final double x;

  /**
   * The y coordinate.
   */
  public final double y;

  /**
   * Creates a new instance of {@code Point}.
   *
   * @param x the x coordinate
   * @param y the y coordinate
   */
  protected Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

}
