package lfx.map;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

public class Layer {
  public final Node pic;
  public final double x;
  public final double y;
  public final double width;

  public Layer(String path, double x, double y, double width) {
    this.x = x;
    this.y = y;
    this.width = width;
    if (path == "NativeBase") {
      pic = new Rectangle(
          width, 400, new LinearGradient(0f, 1f, 1f, 0f, true, CycleMethod.NO_CYCLE, new Stop[] {
              new Stop(0.00, Color.web("#f8bd55")),
              new Stop(0.14, Color.web("#c0fe56")),
              new Stop(0.28, Color.web("#5dfbc1")),
              new Stop(0.43, Color.web("#64c2f8")),
              new Stop(0.57, Color.web("#be4af7")),
              new Stop(0.71, Color.web("#ed5fc2")),
              new Stop(0.85, Color.web("#ef504c")),
              new Stop(1.00, Color.web("#f2660f"))
          })
      );
    } else {  // NativePath
      pic = new Rectangle(width, 200 + 12, new Color(0.0, 0.0, 0.0, 0.3));
      pic.setLayoutY(200 - 7);
    }
    pic.setLayoutX(0.0);
  }

}
