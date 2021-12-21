package component;

import base.Direction;
import base.Point;

public class Opoint extends Point {
  // (test) upper most item z-velocity for those quantity more than 1
  public static final double Z_RANGE = 5.0;
  // TODO: customized hp or mp
  public final boolean release;
  public final int dvx;
  public final int dvy;
  public final int amount;
  public final Action action;
  public final String oid;
  public final Direction direction;

  private Opoint(boolean release, int x, int y, int dvx, int dvy, int amount,
      Action action, String oid, Direction direction) {
    super(x, y);
    this.release = release;
    this.dvx = dvx;
    this.dvy = dvy;
    this.amount = amount;
    this.action = action;
    this.oid = oid;
    this.direction = direction;
  }

  public static Opoint front(int x, int y, String oid,
      int dvx, int dvy, Action action, int amount) {
    return new Opoint(true, x, y, dvx, dvy, amount, action, oid, Direction.SAME);
  }

  public static Opoint back(int x, int y, String oid,
      int dvx, int dvy, Action action, int amount) {
    return new Opoint(true, x, y, dvx, dvy, amount, action, oid, Direction.OPPOSITE);
  }

  public static Opoint hold(int x, int y, String oid) {
    return new Opoint(false, x, y, 0, 0, 1, Action.DEFAULT, oid, Direction.SAME);
  }

  // TODO: CREATE_ARMOUR
  // https://www.lf-empire.de/en/lf2-empire/data-changing/reference-pages/183-effect-3-hitfa
  // hit_Fa:  Effect:
  public static final String JanAngel = "JanAngel";
  //      5   Creates Jan's Healing Angels in a ratio 1:1 for teammates.
  public static final String JanDevil = "JanDevil";
  //      6   Creates Jan's Flying Devils in a ratio 1:1 for enemies. The upper limit is 7.
  public static final String BatBat = "BatBat";
  //      8   Creates three bats for up to four players. While playing against more
  //          enemies, it'll create one additional bat for every two enemies beyond the
  //          first four. Chases after a random target.
  public static final String FirzenDisaster = "FirzenDisaster";
  //      9   Creates four Ice-/Fire-balls if you are playing against four or less
  //          enemies. When playing against more enemies, it activate the balls in a ratio
  //          of 1:1 for each additional enemy. The upper limit is 10 balls (the number of
  //          fire- or ice-balls may be random, their sum is capped, however).
  public static final String FirzenVolcano = "FirzenVolcano";
  //     11   Creates Firzen's Explosion: id211 frame 109 explode & frame 50 groundfire,
  //          id212 frame 100 icicles, id 221 frame 81 overwhelming disaster.
  // no dedicated for this.
  //     13   Creates Skull-Blast that chases after a random target.

}
