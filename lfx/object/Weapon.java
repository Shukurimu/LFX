package lfx.object;

import java.util.List;

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

  @Override Weapon makeClone();
  boolean isHeavy();
  boolean isDrink();
  boolean isLight();
  boolean isSmall();
  void release();
  void destroy();
  Observable getHolder();
  List<Double> consume();

}
