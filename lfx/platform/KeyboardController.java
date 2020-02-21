package lfx.platform;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javafx.scene.input.KeyCode;
import lfx.base.Controller;
import lfx.base.Input;
import lfx.util.Const;
import lfx.util.Tuple;

public final class KeyboardController implements Controller {
  private static final Map<KeyCode, KeyMonitor> mapping = new EnumMap<>(KeyCode.class);
  private final List<Tuple<KeyMonitor, String>> keyList = new ArrayList<>(Const.KEY_NUM);
  private final KeyMonitor[] monitorArray = new KeyMonitor[Const.KEY_NUM];
  private long consumedTime = 0L;

  public KeyboardController(String[] stringArray) {
    for (int i = 0; i < Const.KEY_NUM; ++i) {
      KeyCode code = KeyCode.UNDEFINED;
      try {
        code = KeyCode.valueOf(stringArray[i]);
      } catch (Exception ex) {
        System.err.println("Invalid KeyCode: " + stringArray[i]);
      }
      mapping.putIfAbsent(code, new KeyMonitor());
      KeyMonitor monitor = mapping.get(code);
      keyList.add(Tuple.of(monitor, Const.KEY_SYMBOLS.get(i)));
      monitorArray[i] = monitor;
    }
    return;
  }

  public void updateSimpleInput(Input input) {
    input.do_U = monitorArray[0].pressCount > 0;
    input.do_D = monitorArray[1].pressCount > 0;
    input.do_L = monitorArray[2].pressCount > 0;
    input.do_R = monitorArray[3].pressCount > 0;
    input.do_a = monitorArray[4].pressCount > 0;
    input.do_j = monitorArray[5].pressCount > 0;
    return;
  }

  public void updateInput(Input input) {
    keyList.sort((m1, m2) -> Long.compare(m1.first.pressTime, m2.first.pressTime));
    StringBuilder sequence = new StringBuilder(8);
    for (Tuple<KeyMonitor, String> tuple : keyList) {
      if (tuple.first.pressTime > consumedTime) {
        break;
      }
      sequence.append(tuple.second);
    }
    long currentTime = System.currentTimeMillis();
    input.set(monitorArray[0].pressCount > 0,  // do_U
              monitorArray[1].pressCount > 0,  // do_D
              monitorArray[2].pressCount > 0,  // do_L
              monitorArray[3].pressCount > 0,  // do_R
              monitorArray[4].isValid(currentTime),  // do_a
              monitorArray[5].isValid(currentTime),  // do_j
              monitorArray[6].isValid(currentTime),  // do_d
              monitorArray[2].doublePress && monitorArray[2].isValid(currentTime),  // do_LL
              monitorArray[3].doublePress && monitorArray[3].isValid(currentTime),  // do_RR
              sequence
    );
    return;
  }

  public void consumeKeys() {
    consumedTime = System.currentTimeMillis();
    return;
  }

  public static boolean press(KeyCode code) {
    KeyMonitor monitor = mapping.get(code);
    if (monitor == null) {
      return false;
    } else {
      monitor.setPressed();
      return true;
    }
  }

  public static boolean release(KeyCode code) {
    KeyMonitor monitor = mapping.get(code);
    if (monitor == null) {
      return false;
    } else {
      monitor.setReleased();
      return true;
    }
  }

  public static class KeyMonitor {
    private int pressCount = 0;
    private long pressTime = 0L;
    private boolean doublePress = false;

    public void setPressed() {
      if (++pressCount == 1) {
        long current = System.currentTimeMillis();
        doublePress = isValid(current);
        pressTime = current;
      }
      return;
    }

    public void setReleased() {
      pressCount = 0;
      return;
    }

    public boolean isValid(long basedTime) {
      return pressTime + Const.VALID_KEY_INTERVAL > basedTime;
    }

  }

}
