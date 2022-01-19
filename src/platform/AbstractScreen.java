package platform;

import java.lang.System.Logger.Level;
import java.util.EnumMap;
import java.util.Map;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import base.InputMonitor;

public abstract class AbstractScreen implements Screen {
  private static final System.Logger logger = System.getLogger("");
  private static final Map<KeyCode, InputMonitor> keyboardMonitor = new EnumMap<>(KeyCode.class);
  private static Stage primaryStage = null;

  private Screen previousScreen = null;
  private Scene currentScene = null;

  @Override
  public void setPrevious(Screen previousScreen) {
    this.previousScreen = previousScreen;
    return;
  }

  @Override
  public void gotoPrevious() {
    primaryStage.setScene(previousScreen.getScene());
    return;
  }

  @Override
  public void gotoNext(Screen nextScreen) {
    primaryStage.setScene(nextScreen.getScene());
    nextScreen.setPrevious(this);
    return;
  }

  /**
   * Sets application's primary stage.
   * It's needed to change {@code Scene}.
   *
   * @param stage the primaryStage
   */
  static void setPrimaryStage(Stage stage) {
    primaryStage = stage;
    return;
  }

  /**
   * Exits if pressing ESCAPE.
   */
  static void setEscapeExit() {
    keyboardMonitor.put(KeyCode.ESCAPE, new InputMonitor() {
      @Override
      public void setPressed() {
        javafx.application.Platform.exit();
      }
    });
    logger.log(Level.INFO, "Press ESCAPE to exit.");
    return;
  }

  /**
   * Requests the {@code InputMonitor} of given key.
   * Note that several {@code Input} can be set to same physical key.
   *
   * @param keyCode targeting key
   * @return an {@code InputMonitor} of the key
   */
  static InputMonitor requestMonitor(KeyCode keyCode) {
    return keyboardMonitor.computeIfAbsent(keyCode, k -> new InputMonitor());
  }

  /**
   * Makes the main container of this {@code Screen}.
   *
   * @return a {@code Parent} containing GUI components.
   */
  protected abstract Parent makeParent();

  /**
   * Defines corresponding actions of each key press.
   *
   * @param keyCode the pressed key
   */
  protected abstract void keyHandler(KeyCode keyCode);

  @Override
  public final Scene getScene() {
    if (currentScene != null) {
      return currentScene;
    }
    Scene scene = new Scene(makeParent(), WINDOW_WIDTH, WINDOW_HEIGHT);
    scene.setOnKeyPressed(event -> {
      KeyCode keyCode = event.getCode();
      keyboardMonitor.getOrDefault(keyCode, InputMonitor.NULL_MONITOR).setPressed();
      logger.log(Level.DEBUG, "Pressed {0}", keyCode);
      keyHandler(keyCode);
    });
    scene.setOnKeyReleased(event -> {
      KeyCode keyCode = event.getCode();
      keyboardMonitor.getOrDefault(keyCode, InputMonitor.NULL_MONITOR).setReleased();
      logger.log(Level.DEBUG, "Released {0}", keyCode);
    });
    return currentScene = scene;
  }

}
