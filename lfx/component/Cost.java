package lfx.component;

public class Cost {
  public static final Cost FREE = new Cost(0, 0);
  // TODO: Louis transformation has hp limitation.

  public final int mp;
  public final int hp;

  private Cost(int mp, int hp) {
    this.mp = mp;
    this.hp = hp;
  }

  public static Cost of(int mp) {
    return mp == 0 ? FREE : new Cost(mp % 1000, mp / 1000 * 10);
  }

  public static Cost of(int mp, int hp) {
    return mp == 0 && hp == 0 ? FREE : new Cost(mp, hp);
  }

  @Override
  public String toString() {
    return String.format("Cost(%d, %d)", mp, hp);
  }

}
