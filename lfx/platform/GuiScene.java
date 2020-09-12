package lfx.platform;

import java.util.function.Consumer;
import javafx.scene.Scene;

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
   *          changes the window view to accepted Scene
   * @return  this Scene
   */
  Scene makeScene(Consumer<Scene> sceneChanger);

}
