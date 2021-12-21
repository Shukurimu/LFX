package map;

import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import object.Observable;
import util.Vector;

public class Viewer extends VBox {
  private final Observable object;
  private final List<Image> imageList;
  private final int indexBound;
  private final ImageView guiComponent = new ImageView();

  public Viewer(Observable object, List<Image> imageList) {
    this.object = object;
    this.imageList = imageList;
    this.indexBound = imageList.size();
    this.getChildren().add(guiComponent);
  }

  public Observable getObject() {
    return object;
  }

  /**
   * A child with a lower viewOrder will be in front of a child with a higher viewOrder.
   */
  public void update() {
    int index = object.getImageIndex();
    if (index >= indexBound) {
      setVisible(false);
    } else {
      setVisible(true);
      Vector anchor = object.getImageAnchor();
      setTranslateX(anchor.x());
      setTranslateY(anchor.y());
      setTranslateZ(anchor.z());
      setRotate(object.isFaceRight() ? 0 : 180);
      guiComponent.setImage(imageList.get(index));
    }
    return;
  }

}
