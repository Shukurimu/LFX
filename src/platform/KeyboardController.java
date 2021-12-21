package platform;

import java.util.EnumMap;
import java.util.Map;

import javafx.scene.input.KeyCode;

import setting.AbstractController;
import setting.Input;
import setting.InputMonitor;

public class KeyboardController extends AbstractController {
  private static final Map<KeyCode, InputMonitor> keyMonitor = new EnumMap<>(KeyCode.class);

  private KeyboardController(Map<Input, InputMonitor> inputMap) {
    super(inputMap);
  }

  /**
   * Registers control keys.
   *
   * @param stringArray an array of {@code KeyCode} String
   * @return the corresponding {@code Controller}
   */
  public static KeyboardController ofSetting(String[] stringArray) {
    Map<Input, InputMonitor> inputMap = new EnumMap<>(Input.class);
    for (Input input : Input.values()) {
      KeyCode code = null;
      try {
        code = KeyCode.valueOf(stringArray[input.ordinal()]);
      } catch (IllegalArgumentException ex) {
        code = KeyCode.UNDEFINED;
        System.err.println("Invalid KeyCode: " + stringArray[input.ordinal()]);
      } catch (IndexOutOfBoundsException ex) {
        code = KeyCode.UNDEFINED;
        System.err.println("Insufficient Elements");
      }
      // Several keys can be set to same physical key.
      inputMap.put(input, keyMonitor.computeIfAbsent(code, k -> new InputMonitor()));
    }
    return new KeyboardController(inputMap);
  }

  public static boolean press(KeyCode code) {
    if (code == KeyCode.ESCAPE) {
      javafx.application.Platform.exit();
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
