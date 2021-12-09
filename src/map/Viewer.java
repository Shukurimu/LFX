package map;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import game.Observable;

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

  /**
   * A child with a lower viewOrder will be in front of a child with a higher viewOrder.
   */
  public void update(double cameraX) {
    boolean facing = object.getFacing();
    Image image = object.getImage().get(facing);
    double[] anchors = object.getImageAnchors();
    this.setTranslateX((facing ? anchors[0] : anchors[0] - image.getWidth()) - cameraX);
    this.setTranslateY(anchors[1] + anchors[2]);
    this.setViewOrder(anchors[2]);
    guiComponent.setImage(image);
    return;
  }

}
