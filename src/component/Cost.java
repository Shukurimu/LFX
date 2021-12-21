package component;

public record Cost(int mp, int hp) {
  public static final Cost FREE = new Cost(0, 0);
  // TODO: Louis transformation

  public static Cost of(int mp) {
    return mp == 0 ? FREE : new Cost(mp % 1000, mp / 1000 * 10);
  }

  public static Cost of(int mp, int hp) {
    return mp == 0 && hp == 0 ? FREE : new Cost(mp, hp);
  }

}
