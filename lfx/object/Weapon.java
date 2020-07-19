package lfx.object;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lfx.component.Itr;
import lfx.object.Observable;
import lfx.util.Area;
import lfx.util.Tuple;

public interface Weapon extends Observable {
  double INITIAL_MP = 750.0;
  Map<String, Double> SPECIAL_MP = Map.of("Milk", 500.0 / 3.0);
  Set<String> NON_NEUTRAL_SET = Set.of("IceSword", "LouisArmour1", "LouisArmour2");
  List<Double> MILK_REGENERATION = List.of(1.667, 1.6, 0.8);  // (mp, hp, hp2nd)
  List<Double> BEER_REGENERATION = List.of(6.000, 0.0, 0.0);

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
  List<Tuple<Itr, Area>> getStrengthItrs(int wusage);

  int ACT_RANGE = 16;
  int ACT_IN_THE_SKY = 0;
  int ACT_ON_HAND = 20;
  int ACT_THROWING = 40;
  int ACT_ON_GROUND = 60;
  int ACT_STABLE_ON_GROUND = 64;
  int ACT_JUST_ON_GROUND = 70;

  int ACT_BOUNCING_NORMAL = 0;
  int ACT_BOUNCING_LIGHT = 7;

  int HEAVY_RANGE = 6;
  int HEAVY_IN_THE_SKY = 10;
  int HEAVY_ON_GROUND = 20;
  int HEAVY_STABLE_ON_GROUND = 20;
  int HEAVY_JUST_ON_GROUND = 21;

}
