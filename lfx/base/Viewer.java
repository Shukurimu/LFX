package lfx.base;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Viewer {
  private final ImageView imageView = new ImageView();
  public double hp = 0.0;
  public double hp2nd = 0.0;
  public double mp = 0.0;

  public Viewer() {}

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

  @Override
  public String toString() {
    return String.format("Viewer(hp %6.2f / %6.2f, mp %6.2f)", hp, hp2nd, mp);
  }

}
