package lfx.platform;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.time.Instant;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import lfx.base.Controller;
import lfx.base.Order;
import lfx.setting.Keyboard;
import lfx.util.Tuple;

public class KeyboardController implements Controller {
  private static final Map<KeyCode, KeyMonitor> mapping = new EnumMap<>(KeyCode.class);
  private static final Instant DEFAULT_PRESSED_INSTANT = Instant.now().plusSeconds(86400L);

  private static class KeyMonitor implements Comparable<KeyMonitor> {
    int pressCount = 0;
    Instant pressInstant = DEFAULT_PRESSED_INSTANT;
    boolean doublePress = false;

    void setPressed() {
      if (++pressCount == 1) {  // > 1 means holding.
        Instant now = Instant.now();
        doublePress = pressInstant.plusMillis(Keyboard.VALID_KEY_INTERVAL).isAfter(now);
        pressInstant = now;
      }
      return;
    }

    void setReleased() {
      pressCount = 0;
      return;
    }

    boolean pressedAfter(Instant instant) {
      return pressInstant.isAfter(instant);
    }

    boolean pressedBefore(Instant instant) {
      return pressInstant.isBefore(instant);
    }

    @Override
    public int compareTo(KeyMonitor o) {
      return pressInstant.compareTo(o.pressInstant);
    }

  }

  private final List<Tuple<KeyMonitor, String>> keyList = new ArrayList<>(8);
  private final Map<Keyboard, KeyMonitor> monitorMap = new EnumMap<>(Keyboard.class);
  private Instant validInstant = Instant.EPOCH;
  private Instant consumeInstant = Instant.EPOCH;

  public KeyboardController(String[] stringArray) {
    for (Keyboard keyboard : Keyboard.values()) {
      KeyCode code = KeyCode.UNDEFINED;
      try {
        code = KeyCode.valueOf(stringArray[keyboard.ordinal()]);
      } catch (Exception ex) {
        System.err.println("Invalid KeyCode: " + stringArray[keyboard.ordinal()]);
      }
      // Several keys can be set to same physical key.
      KeyMonitor monitor = mapping.get(code);
      if (monitor == null) {
        monitor = new KeyMonitor();
        mapping.put(code, monitor);
      }
      monitorMap.put(keyboard, monitor);
      keyList.add(new Tuple<>(monitor, keyboard.symbol));
    }
    return;
  }

  @Override
  public void update() {
    validInstant = Instant.now().plusMillis(Keyboard.VALID_KEY_INTERVAL);
    return;
  }

  @Override
  public void consumeKeys() {
    consumeInstant = Instant.now();
    return;
  }

  @Override
  public Order getOrder() {
    StringBuilder sequence = new StringBuilder(8);
    keyList.sort((e1, e2) -> e1.first.compareTo(e2.first));
    for (Tuple<KeyMonitor, String> tuple : keyList) {
      if (tuple.first.pressedAfter(consumeInstant)) {
        sequence.append(tuple.second);
      }
    }
    for (Order order : Order.ORDER_LIST) {
      if (sequence.indexOf(order.keySequence) >= 0) {
        return order;
      }
    }
    return null;
  }

  @Override
  public boolean press_U() {
    return monitorMap.get(Keyboard.Up).pressCount > 0;
  }

  @Override
  public boolean press_D() {
    return monitorMap.get(Keyboard.Down).pressCount > 0;
  }

  @Override
  public boolean press_L() {
    return monitorMap.get(Keyboard.Left).pressCount > 0;
  }

  @Override
  public boolean press_R() {
    return monitorMap.get(Keyboard.Right).pressCount > 0;
  }

  @Override
  public boolean press_a() {
    KeyMonitor monitor = monitorMap.get(Keyboard.Attack);
    return monitor.pressedAfter(consumeInstant) && monitor.pressedBefore(validInstant);
  }

  @Override
  public boolean press_j() {
    KeyMonitor monitor = monitorMap.get(Keyboard.Jump);
    return monitor.pressedAfter(consumeInstant) && monitor.pressedBefore(validInstant);
  }

  @Override
  public boolean press_d() {
    KeyMonitor monitor = monitorMap.get(Keyboard.Defend);
    return monitor.pressedAfter(consumeInstant) && monitor.pressedBefore(validInstant);
  }

  public boolean pressRunL() {
    KeyMonitor monitor = monitorMap.get(Keyboard.Left);
    return monitor.doublePress && monitor.pressedBefore(validInstant);
  }

  public boolean pressRunR() {
    KeyMonitor monitor = monitorMap.get(Keyboard.Right);
    return monitor.doublePress && monitor.pressedBefore(validInstant);
  }

  @Override
  public boolean pressRun() {
    return pressRunL() ^ pressRunR();
  }

  public static boolean press(KeyCode code) {
    if (code == KeyCode.ESCAPE) {
      Platform.exit();
      return false;
    }
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

}
