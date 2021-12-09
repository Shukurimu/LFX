package platform;

import java.util.function.Consumer;
import javafx.scene.paint.Color;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.transform.Scale;

public interface GuiScene {
  double CONFIG_BUTTON_WIDTH = 120;
  double DEFAULT_FPS = 30.0;
  double DEFAULT_MSPF = 1000.0 / DEFAULT_FPS;
  double CANVAS_WIDTH = 794 / 4;
  double CANVAS_HEIGHT = 60;
  double TEXTLABEL_HEIGHT = 20;
  double WINDOW_WIDTH = 794;
  double WINDOW_HEIGHT = (550 - 128) + CANVAS_HEIGHT + TEXTLABEL_HEIGHT * 2;
  double PORTRAIT_SIZE = 180;

  /**
   * Builds the Scene of this object.
   *
   * @param   sceneChanger
   *          changes the window view (primaryStage) to accepted Scene
   * @return  this Scene
   */
  Scene makeScene(Consumer<Scene> sceneChanger);

  static Scene sceneWrapper(Parent parent, double computePrefWidth, double computePrefHeight) {
    double width = 1600;
    double height = 900;
    parent.getTransforms().setAll(new Scale(width / computePrefWidth, height / computePrefHeight));
    return new Scene(parent, width, height, Color.BLACK);
  }

}
