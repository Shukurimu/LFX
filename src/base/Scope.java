package base;

public final class Scope {
  public static final int HERO   = 0x100;
  public static final int WEAPON = 0x010;
  public static final int ENERGY = 0x001;

  /**
   * Don't let anyone instantiate this class.
   */
  private Scope() {}

  /**
   * Returns {@code Scope}'s enemy view.
   *
   * @param baseScope the base {@code Scope}
   * @return the enemy view
   */
  public static int getEnemyView(int baseScope) {
    return baseScope << 1;
  }

  /**
   * Returns {@code Scope}'s teammate view.
   *
   * @param baseScope the base {@code Scope}
   * @return the teammate view
   */
  public static int getTeammateView(int baseScope) {
    return baseScope << 2;
  }

  // Itr's Target Selectors
  public static final int ENEMY_HERO = getEnemyView(HERO);
  public static final int TEAMMATE_HERO = getTeammateView(HERO);
  public static final int ALL_HERO = ENEMY_HERO | TEAMMATE_HERO;
  public static final int ENEMY_WEAPON = getEnemyView(WEAPON);
  public static final int TEAMMATE_WEAPON = getTeammateView(WEAPON);
  public static final int ALL_WEAPON = ENEMY_WEAPON | TEAMMATE_WEAPON;
  public static final int ENEMY_ENERGY = getEnemyView(ENERGY);
  public static final int TEAMMATE_ENERGY = getTeammateView(ENERGY);
  public static final int ALL_ENERGY = ENEMY_ENERGY | TEAMMATE_ENERGY;
  public static final int ALL_NON_HERO = ALL_WEAPON | ALL_ENERGY;
  public static final int EVERYTHING = ALL_HERO | ALL_WEAPON | ALL_ENERGY;

}
