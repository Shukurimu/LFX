package lfx.map;

import java.util.List;

public interface Environment {
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
