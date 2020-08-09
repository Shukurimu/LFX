package lfx.base;

import javafx.scene.image.ImageView;
import lfx.util.ImageCell;

public class Viewer {
  private final ImageView imageView = new ImageView();

  public Viewer() {}

  public ImageView getFxNode() {
    return imageView;
  }

  public void update(boolean faceRight, double anchorX, double anchorY, double pz, ImageCell cell) {
    imageView.setX(anchorX);
    imageView.setY(anchorY + pz);
    imageView.setViewOrder(pz);
    imageView.setImage(cell.get(faceRight));
    return;
  }

}
