package lfx.util;

public final class Box {
  public static final int ZWIDTH = 12;
  public static final Box GLOBAL = new Box(-123456789, 223456789, -40000, 80000, 80000);
  public static final Box TRUE_GLOBAL = new Box(-123456789, 223456789, -1234567, 2234567, 80000);
  public static final Box INVULNERABLE = new Box(0, 1, 99999, 1);  // pseudo invulnerable

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
    this(x, y, w, h, ZWIDTH, ZWIDTH);
  }

}
