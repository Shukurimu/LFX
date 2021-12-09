package base;

/**
 * Indicates the coverage of Bdy and Itr.
 */
public class Box {
  public static final int Z_WIDTH = 12;

  public final int x;
  public final int y;
  public final int w;
  public final int h;
  public final int zu;  // upward
  public final int zd;  // downward

  public Box(int x, int y, int w, int h, int zu, int zd) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.zu = zu;
    this.zd = zd;
  }

  public Box(int x, int y, int w, int h, int zWidth) {
    this(x, y, w, h, zWidth, zWidth);
  }

  public Box(int x, int y, int w, int h) {
    this(x, y, w, h, Z_WIDTH, Z_WIDTH);
  }

}
