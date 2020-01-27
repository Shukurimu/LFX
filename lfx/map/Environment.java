package lfx.map;

import java.util.List;
import lfx.object.Observable;

public interface Environment {
  double ITEM_ADDITIONAL_WIDTH = 50.0;
  double DROP_PROBABILITY = 1.0 / 6.0 / 30.0;

  boolean isUnlimitedMode();
  double applyFriction(double vx);
  double applyGravity(double vy);
  int requestIndependentTeamId();
  int getTimestamp();

  /** [hi_x, lo_x, hi_z, lo_z] */
  List<Double> getZBound();
  List<Double> getHeroXBound();
  List<Double> getItemXBound();

  List<Observable> getHeroView();

  void spawnHero(List<Observable> objectList);
  void spawnWeapon(List<Observable> objectList);
  void spawnEnergy(List<Observable> objectList);

}
