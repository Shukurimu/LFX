package platform;

import java.util.EnumMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import setting.AbstractController;
import setting.Input;
import setting.InputMonitor;

public class KeyboardController extends AbstractController {
  private static final Map<KeyCode, InputMonitor> keyMonitor = new EnumMap<>(KeyCode.class);

  private KeyboardController(Map<Input, InputMonitor> inputMap) {
    super(inputMap);
  }

  public static KeyboardController ofSetting(String[] stringArray) {
    Map<Input, InputMonitor> inputMap = new EnumMap<>(Input.class);
    for (Input input : Input.values()) {
      KeyCode code = KeyCode.UNDEFINED;
      try {
        code = KeyCode.valueOf(stringArray[input.ordinal()]);
      } catch (Exception ex) {
        System.err.println("Invalid KeyCode: " + stringArray[input.ordinal()]);
      }
      // Several keys can be set to same physical key.
      InputMonitor monitor = keyMonitor.get(code);
      if (monitor == null) {
        monitor = new InputMonitor();
        keyMonitor.put(code, monitor);
      }
      inputMap.put(input, monitor);
    }
    return new KeyboardController(inputMap);
  }

  public static boolean press(KeyCode code) {
    if (code == KeyCode.ESCAPE) {
      Platform.exit();
      return false;
    }
    InputMonitor monitor = keyMonitor.get(code);
    if (monitor == null) {
      return false;
    } else {
      monitor.setPressed();
      return true;
    }
  }

  public static boolean release(KeyCode code) {
    InputMonitor monitor = keyMonitor.get(code);
    if (monitor == null) {
      return false;
    } else {
      monitor.setReleased();
      return true;
    }
  }

}
