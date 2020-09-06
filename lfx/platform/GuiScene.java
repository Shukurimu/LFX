package lfx.platform;

import java.util.function.Consumer;
import javafx.scene.Scene;

public abstract class GuiScene {
  protected static final double CONFIG_BUTTON_WIDTH = 120;
  protected static final double DEFAULT_FPS = 30.0;
  protected static final double CANVAS_WIDTH = 794 / 4;
  protected static final double CANVAS_HEIGHT = 60;
  protected static final double TEXTLABEL_HEIGHT = 20;
  protected static final double WINDOW_WIDTH = 794;
  protected static final double WINDOW_HEIGHT = (550 - 128) + CANVAS_HEIGHT + TEXTLABEL_HEIGHT * 2;
  protected static final double PORTRAIT_SIZE = 180;
  protected static final long VALID_KEY_INTERVAL = 200L;

  /**
   * Builds the Scene of this object.
   *
   * @param   sceneChanger
   *          changes the window view to accepted Scene
   * @return  this Scene
   */
  protected abstract Scene makeScene(Consumer<Scene> sceneChanger);

}
