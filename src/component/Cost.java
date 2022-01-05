package component;

/**
 * Define frame cost -- mp.
 * TODO: Louis transformation
 */
public record Cost(int mp, int hp) {

  /**
   * Representing a no-cost instance.
   */
  public static final Cost FREE = new Cost(0, 0);

  /**
   * Creates a {@code Cost} instance only considers mp.
   *
   * @param mp required mana point
   * @return a {@code Cost} instance
   */
  public static Cost of(int mp) {
    return mp == 0 ? FREE : new Cost(mp, 0);
  }

  /**
   * Creates a {@code Cost} instance considers both mp and hp.
   * The hp value other than a multiple of 10 is allowed.
   *
   * @param mp required mana point
   * @param hp required health point
   * @return a {@code Cost} instance
   */
  public static Cost of(int mp, int hp) {
    return mp == 0 && hp == 0 ? FREE : new Cost(mp, hp);
  }

  // ==================== Parser Utility ====================

  /**
   * Prepares for mana or health point consumption.
   *
   * @param rawValue raw number specified in mp field
   * @return a statement to create a {@code Cost}
   */
  public static String process(int rawValue) {
    if (rawValue == 0) {
      return "Cost.FREE";
    }
    int hp = rawValue / 1000 * 10;
    int mp = rawValue - hp * 100;
    return hp == 0 ? "Cost.of(%d)".formatted(mp) : "Cost.of(%d, %d)".formatted(mp, hp);
  }

}
