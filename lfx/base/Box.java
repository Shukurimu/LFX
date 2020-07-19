package lfx.base;

import lfx.util.Const;

/**
 * Indicate the coverage of Bdy and Itr.
 */
public final class Box {
  public static final Box LINEAR = new Box(-12345678, 23456789, -12345, 23456, 12345);
  public static final Box GLOBAL = new Box(-12345678, 23456789, -12345678, 23456789, 12345);
  public static final Box HIDDEN = new Box(0, 1, 654321, 1);  // pseudo-invulnerable

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

  public Box(int x, int y, int w, int h, int zwidth) {
    this(x, y, w, h, zwidth, zwidth);
  }

  // common usage: default zwidth
  public Box(int x, int y, int w, int h) {
    this(x, y, w, h, Const.ZWIDTH, Const.ZWIDTH);
  }

}
