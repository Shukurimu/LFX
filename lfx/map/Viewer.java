package lfx.map;

import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import lfx.game.Observable;

public class Viewer extends GridPane {
  private final Observable object;
  private final ImageView guiComponent = new ImageView();

  public Viewer(Observable object) {
    this.object = object;
    this.add(guiComponent, 0, 0);
  }

  public Observable getObject() {
    return object;
  }

  public void update() {
    double[] anchors = object.getImageAnchors();
    guiComponent.setX(anchors[0]);
    guiComponent.setY(anchors[1] + anchors[2]);
    guiComponent.setViewOrder(anchors[2]);
    guiComponent.setImage(object.getImage().get(object.getFacing()));
  }

}
