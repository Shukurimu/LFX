package lfx.map;

import java.util.List;
import javafx.scene.layout.Pane;
import lfx.object.Observable;
import lfx.util.Util;

public interface Field extends Environment {
  double ITEM_ADDITIONAL_WIDTH = 50.0;
  double DROP_PROBABILITY = 1.0 / 6.0 / 30.0;
  double FIELD_WIDTH = 794;
  double FIELD_HEIGHT = 550 - 128;
  double WIDTH_DIV2 = FIELD_WIDTH / 2.0;
  double WIDTH_DIV24 = FIELD_WIDTH / 24.0;
  double CAMERA_SPEED_THRESHOLD = 0.9;

  double getBoundWidth();
  Pane getVisualNodePane();
  int getObjectCount();

  boolean switchUnlimitedMode();
  void reviveAll();
  void dropNeutralWeapons();
  void destroyWeapons();
  void disperseEnergies();

  void stepOneFrame();

  static double calcCameraPos(List<Observable> tracingList, double currentPos) {
    // Camera moving policy is modified from F.LF.
    double position = 0.0;
    int weight = 0;
    for (Observable o : tracingList) {
      position += o.getPosX();
      weight += o.getFacing() ? 1 : -1;
    }
    position = weight * WIDTH_DIV24 + position / tracingList.size() - WIDTH_DIV2;
    position = Util.clamp(position, getBoundWidth() - FIELD_WIDTH, 0.0);
    return position < currentPos ? Math.max(currentPos - CAMERA_SPEED_THRESHOLD, position)
                                 : Math.min(currentPos + CAMERA_SPEED_THRESHOLD, position);
  }

}
