package lfx.map;

import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import lfx.game.Observable;

public class Viewer extends VBox {
  private final Observable object;
  private final ImageView guiComponent = new ImageView();

  public Viewer(Observable object) {
    this.object = object;
    this.getChildren().add(guiComponent);
  }

  public Observable getObject() {
    return object;
  }

  public void update(double cameraX) {
    double[] anchors = object.getImageAnchors();
    this.setTranslateX(anchors[0] + cameraX);
    this.setTranslateY(anchors[1] + anchors[2]);
    this.setViewOrder(anchors[2]);
    // A child with a lower viewOrder will be in front of a child with a higher viewOrder.
    guiComponent.setImage(object.getImage().get(object.getFacing()));
    return;
  }

}
