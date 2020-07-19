package lfx.base;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class VisualNode {
  private final ImageView imageView = new ImageView();

  public ImageView getFxNode() {
    return imageView;
  }

  public void updateImage(double anchorX, double anchorY, double pz, Image image) {
    imageView.setX(anchorX);
    imageView.setY(anchorY + pz);
    imageView.setViewOrder(pz);
    imageView.setImage(image);
    return;
  }

}
