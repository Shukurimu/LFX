package lfx.map;

import java.util.List;
import javafx.scene.layout.Pane;
import javafx.scene.Node;
import lfx.map.Environment;
import lfx.object.Hero;

public interface Field extends Environment {

  Pane getScreenPane();
  List<Node> getFxNodeList();

  boolean switchUnlimitedMode();
  void reviveAll();
  void dropNeutralWeapons();
  void destroyWeapons();
  void disperseEnergies();

  void stepOneFrame();
  double calculateViewpoint(List<Hero> tracingList, double origin);

}
