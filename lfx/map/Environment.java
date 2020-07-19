package lfx.map;

import java.util.List;

public interface Environment {
  double ITEM_ADDITIONAL_WIDTH = 50.0;
  double DROP_PROBABILITY = 1.0 / 6.0 / 30.0;

  boolean isUnlimitedMode();
  double applyFriction(double vx);
  double applyGravity(double vy);
  int requestIndependentTeamId();
  int getTimestamp();

  /**
   * Returns valid bound of certain object.
   *
   * @return List of high and low values
   */
  List<Double> getZBound();
  List<Double> getHeroXBound();
  List<Double> getItemXBound();

}
