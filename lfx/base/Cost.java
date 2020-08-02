package lfx.base;

public class Cost {
  public static final Cost FREE = new Cost(0, 0);

  public final int mp;
  public final int hp;

  private Cost(int mp, int hp) {
    this.mp = mp;
    this.hp = hp;
  }

  public static Cost of(int mp) {
    return new Cost(mp % 1000, mp / 1000 * 10);
  }

  public static Cost of(int mp, int hp) {
    return new Cost(mp, hp);
  }

  @Override
  public String toString() {
    return String.format("Cost(%4d, %4d)", mp, hp);
  }

}
