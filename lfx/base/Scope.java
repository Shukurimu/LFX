package lfx.base;

public final class Scope {
  public static final int HERO   = 0x100;
  public static final int WEAPON = 0x010;
  public static final int ENERGY = 0x001;

  public static int getSideView(int baseScope, boolean teammate) {
    return teammate ? (baseScope << 1) : baseScope;
  }

  public static int getBothView(int baseScope) {
    return getSideView(baseScope, true) | getSideView(baseScope, false);
  }

  private Scope() {}

  // hero selections
  public static final int ITR_ENEMY_HERO = getSideView(HERO, false);
  public static final int ITR_TEAMMATE_HERO = getSideView(HERO, true);
  public static final int ITR_ALL_HERO = ITR_ENEMY_HERO | ITR_TEAMMATE_HERO;
  // weapon selections
  public static final int ITR_ENEMY_WEAPON = getSideView(WEAPON, false);
  public static final int ITR_TEAMMATE_WEAPON = getSideView(WEAPON, true);
  public static final int ITR_ALL_WEAPON = ITR_ENEMY_WEAPON | ITR_TEAMMATE_WEAPON;
  // energy selections
  public static final int ITR_ENEMY_ENERGY = getSideView(ENERGY, false);
  public static final int ITR_TEAMMATE_ENERGY = getSideView(ENERGY, true);
  public static final int ITR_ALL_ENERGY = ITR_ENEMY_ENERGY | ITR_TEAMMATE_ENERGY;
  // default itr scopes
  public static final int ITR_HERO   = ITR_ENEMY_HERO | ITR_ALL_WEAPON | ITR_ALL_ENERGY;
  public static final int ITR_WEAPON = ITR_ENEMY_HERO | ITR_ALL_WEAPON | ITR_ALL_ENERGY;
  public static final int ITR_ENERGY = ITR_ENEMY_HERO | ITR_ALL_WEAPON | ITR_ENEMY_ENERGY;
  public static final int ITR_EVERYTHING = ITR_ALL_HERO | ITR_ALL_WEAPON | ITR_ALL_ENERGY;

  public static final int ITR_NON_HERO = ITR_ALL_WEAPON | ITR_ALL_ENERGY;
  public static final int ITR_NON_ENERGY = ITR_ALL_HERO | ITR_ALL_ENERGY;

}
