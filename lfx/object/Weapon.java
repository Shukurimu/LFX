package lfx.object;

import java.util.List;
import lfx.object.Observable;

public interface Weapon extends Observable {
  double INITIAL_MP = 750.0;
  double INITIAL_MILK_MP = 500.0 / 3.0;
  List<Double> MILK_REGENERATION = List.of(1.667, 1.6, 0.8);
  List<Double> BEER_REGENERATION = List.of(6.000, 0.0, 0.0);
  List<Double> OTHERS_REGENERATION = List.of(0.0, 0.0, 0.0);

  String Key_hp = "hp";
  String Key_drop_hurt = "drop_hurt";
  String Key_hit_sound = "hit_sound";
  String Key_drop_sound = "drop_sound";
  String Key_broken_sound = "broken_sound";

  boolean isHeavy();
  boolean isDrink();
  boolean isLight();
  boolean isSmall();
  void release();
  void destroy();
  Observable getHolder();
  List<Double> consume();

  enum Subtype {
    SMALL(0.500,  9.0, 0.6, -0.4),
    DRINK(0.667,  9.0, 0.6, -0.4),
    HEAVY(1.000, 10.0, 0.3, -0.2),
    LIGHT(1.000, 10.0, 0.6, -0.4);

    public final double gravityRatio;
    public final double threshold;
    public final double vxLast;
    public final double vyLast;

    private Subtype(double gravityRatio, double threshold, double vxLast, double vyLast) {
      this.gravityRatio = gravityRatio;
      this.threshold = threshold;
      this.vxLast = vxLast;
      this.vyLast = vyLast;
    }

  }

}
