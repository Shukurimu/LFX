package lfx.object;

import java.util.List;
import java.util.Map;
import lfx.component.Itr;
import lfx.component.Wpoint;
import lfx.object.Observable;
import lfx.util.Area;
import lfx.util.Tuple;

public interface Weapon extends Observable {
  double INITIAL_MP = 750.0;
  Map<String, Double> SPECIAL_MP = Map.of("Milk", 500.0 / 3.0);
  List<Double> MILK_REGENERATION = List.of(1.667, 1.6, 0.8);  // (mp, hp, hp2nd)
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
  List<Tuple<Itr, Area>> getStrengthItrs(Wpoint.Usage usage);

}
