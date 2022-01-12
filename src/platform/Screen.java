package platform;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

public interface Screen {
  public static final double CONFIG_BUTTON_WIDTH = 120;
  public static final double DEFAULT_FPS = 30.0;
  public static final double DEFAULT_MSPF = 1000.0 / DEFAULT_FPS;
  public static final double CANVAS_WIDTH = 794 / 4;
  public static final double CANVAS_HEIGHT = 60;
  public static final double TEXTLABEL_HEIGHT = 20;
  public static final double WINDOW_WIDTH = 794;
  public static final double WINDOW_HEIGHT = (550 - 128) + CANVAS_HEIGHT + TEXTLABEL_HEIGHT * 2;
  public static final double PORTRAIT_SIZE = 180;

  Scene makeScene();

  default Scene sceneWrapper(Parent parent, double computePrefWidth, double computePrefHeight) {
    double width = 1600;
    double height = 900;
    parent.getTransforms().setAll(new Scale(width / computePrefWidth, height / computePrefHeight));
    return new Scene(parent, width, height, Color.BLACK);
  }


}
