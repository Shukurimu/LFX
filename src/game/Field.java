package game;

public interface Field extends Environment {
  double ITEM_ADDITIONAL_WIDTH = 50.0;
  double DROP_PROBABILITY = 1.0 / 6.0 / 30.0;
  double FIELD_WIDTH = 794;
  double FIELD_HEIGHT = 550 - 128;
  double WIDTH_DIV2 = FIELD_WIDTH / 2.0;
  double WIDTH_DIV24 = FIELD_WIDTH / 24.0;
  double CAMERA_SPEED_THRESHOLD = 0.9;

  double getBoundWidth();
  boolean switchUnlimitedMode();
  boolean reviveAll();
  boolean dropNeutralWeapons();
  boolean destroyWeapons();
  boolean disperseEnergies();

  void spawnObject(Observable object);
  void stepOneFrame();

}
