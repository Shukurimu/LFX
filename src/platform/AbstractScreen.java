package platform;

import java.lang.System.Logger.Level;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import base.InputMonitor;

public abstract class AbstractScreen implements Screen {
  private static final System.Logger logger = System.getLogger("");
  private static final Map<KeyCode, InputMonitor> keyboardMonitor = new EnumMap<>(KeyCode.class);
  static {
    keyboardMonitor.put(KeyCode.ESCAPE, new InputMonitor() {
      @Override
      public void setPressed() {
        javafx.application.Platform.exit();
      }
    });
  }

  // Several keys can be set to same physical key.
  static InputMonitor requestMonitor(KeyCode keyCode) {
    return keyboardMonitor.computeIfAbsent(keyCode, k -> new InputMonitor());
  }

  static Consumer<Scene> sceneChanger = null;

  protected abstract Parent makeParent();

  protected void keyHandler(KeyCode keyCode) {
    return;
  }

  @Override
  public final Scene makeScene() {
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
    return scene;
  }

}
