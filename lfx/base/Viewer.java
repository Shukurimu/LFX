package lfx.base;

import lfx.util.ImageCell;

public class Viewer {
  public final ImageCell imageCell;
  public final boolean faceRight;
  public final double anchorX;
  public final double anchorY;
  public final double pz;
  public final double hp;
  public final double hp2nd;
  public final double hpMax;
  public final double mp;
  public final double mpMax;

  public Viewer(ImageCell imageCell, boolean faceRight, double anchorX, double anchorY, double pz,
                double hp, double hp2nd, double hpMax, double mp, double mpMax) {
    this.imageCell = imageCell;
    this.faceRight = faceRight;
    this.anchorX = anchorX;
    this.anchorY = anchorY;
    this.pz = pz;
    this.hp = hp;
    this.hp2nd = hp2nd;
    this.hpMax = hpMax;
    this.mp = mp;
    this.mpMax = mpMax;
    // import javafx.scene.image.ImageView;
    // imageView.setX(anchorX);
    // imageView.setY(anchorY + pz);
    // imageView.setViewOrder(pz);
    // imageView.setImage(cell.get(faceRight));
  }

}
