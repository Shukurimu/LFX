package field;

import base.Type;
import object.Observable;

public interface Field extends Environment {
  double ITEM_ADDITIONAL_WIDTH = 50.0;
  double DROP_PROBABILITY = 1.0 / 6.0 / 30.0;
  double FIELD_WIDTH = 794;
  double FIELD_HEIGHT = 550 - 128;
  double WIDTH_DIV2 = FIELD_WIDTH / 2.0;
  double WIDTH_DIV24 = FIELD_WIDTH / 24.0;
  double CAMERA_SPEED_THRESHOLD = 0.9;

  /** F6 */
  void switchUnlimitedMode();

  /** F7 */
  void reviveAll();

  /** F8 */
  void dropNeutralWeapons();

  /** F9 */
  void destroyWeapons();

  /** F10 */
  void disperseEnergies();

  /**
   * The implementation of {@code Comparator} for processing order.
   * {@code Hero} has higher privilege to spread {@code Itr}s.
   * Otherwise you cannot rebound {@code Energy} without getting hurt,
   * since it can hit you at the same time.
   */
  static int processOrder(Observable e1, Observable e2) {
    Type t1 = e1.getType();
    Type t2 = e2.getType();
    return t1.ordinal() - t2.ordinal();
  }

  /**
   * Updates all game logic.
   * This method is called every timeunit.
   */
  void stepOneFrame();

}
