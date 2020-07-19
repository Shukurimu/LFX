package lfx.component;

import lfx.base.Direction;
import lfx.util.Point;

public final class Opoint extends Point {
  // (test) upper most item z-velocity for those quantity more than 1
  public static final double Z_RANGE = 5.0;
  // TODO: customized hp or mp
  public final boolean release;
  public final int dvx;
  public final int dvy;
  public final int amount;
  public final int action;
  public final String oid;
  public final Direction direction;

  public Opoint(boolean release, int x, int y, int dvx, int dvy, int amount,
                int action, String oid, Direction direction) {
    super(x, y);
    this.release = release;
    this.dvx = dvx;
    this.dvy = dvy;
    this.amount = amount;
    this.action = action;
    this.oid = oid;
    this.direction = direction;
  }

  public static Opoint front(int x, int y, String oid, int dvx, int dvy, int action, int amount) {
    return new Opoint(true, x, y, dvx, dvy, amount, action, oid, Direction.SAME);
  }

  public static Opoint back(int x, int y, String oid, int dvx, int dvy, int action, int amount) {
    return new Opoint(true, x, y, dvx, dvy, amount, action, oid, Direction.OPPOSITE);
  }

  public static Opoint hold(int x, int y, String oid) {
    return new Opoint(false, x, y, 0, 0, 1, 0, oid, Direction.SAME);
  }

}
