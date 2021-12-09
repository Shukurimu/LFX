package base;

public enum Type {
  HERO  (true, false, false, 1.000, 10.0, 1.0, 0.00),
  SMALL (false, true, false, 0.500,  9.0, 0.6, -0.4),
  DRINK (false, true, false, 0.667,  9.0, 0.6, -0.4),
  HEAVY (false, true, false, 1.000, 10.0, 0.3, -0.2),
  LIGHT (false, true, false, 1.000, 10.0, 0.6, -0.4),
  ENERGY(false, false, true, 1.000, 10.0, 1.0, 0.00),
  OTHERS(false, false, false, 1.000, 10.0, 1.0, 0.00);

  public final boolean isHero;
  public final boolean isWeapon;
  public final boolean isEnergy;
  public final double gravityRatio;
  public final double threshold;
  public final double vxLast;
  public final double vyLast;

  private Type(boolean isHero, boolean isWeapon, boolean isEnergy,
               double gravityRatio, double threshold, double vxLast, double vyLast) {
    this.isHero = isHero;
    this.isWeapon = isWeapon;
    this.isEnergy = isEnergy;
    this.gravityRatio = gravityRatio;
    this.threshold = threshold;
    this.vxLast = vxLast;
    this.vyLast = vyLast;
  }

}
