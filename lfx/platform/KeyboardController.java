package lfx.platform;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.time.Instant;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import lfx.base.Controller;
import lfx.base.Direction;
import lfx.base.Order;
import lfx.util.Const;
import lfx.util.Tuple;

public class KeyboardController implements Controller {
  private static final long VALID_KEY_INTERVAL = 200L;
  private static final Map<KeyCode, KeyMonitor> mapping = new EnumMap<>(KeyCode.class);
  private static class KeyMonitor implements Comparable<KeyMonitor> {
    int pressCount = 0;
    Instant pressInstant = Instant.EPOCH;
    boolean doublePress = false;

    void setPressed() {
      if (++pressCount == 1) {  // > 1 means holding.
        Instant now = Instant.now();
        doublePress = pressInstant.plusMillis(VALID_KEY_INTERVAL).isAfter(now);
        pressInstant = now;
      }
      return;
    }

    void setReleased() {
      pressCount = 0;
      return;
    }

    boolean pressedBefore(Instant instant) {
      return pressInstant.isBefore(instant);
    }

    @Override
    public int compareTo(KeyMonitor o) {
      return pressInstant.compareTo(o.pressInstant);
    }

  }

  private final List<Tuple<KeyMonitor, String>> keyList = new ArrayList<>(Const.KEY_NUM);
  private final KeyMonitor[] monitorArray = new KeyMonitor[Const.KEY_NUM];
  private final StringBuilder sequence = new StringBuilder(8);
  private Instant validInstant = Instant.EPOCH;
  private Instant consumeInstant = Instant.EPOCH;

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
      keyList.add(new Tuple<>(monitor, Const.KEY_SYMBOLS.get(i)));
      monitorArray[i] = monitor;
    }
    return;
  }

  @Override
  public void update() {
    sequence.setLength(0);
    keyList.sort((e1, e2) -> e1.first.compareTo(e2.first));
    for (Tuple<KeyMonitor, String> tuple : keyList) {
      if (tuple.first.pressedBefore(consumeInstant)) {
        break;
      }
      sequence.append(tuple.second);
    }
    validInstant = Instant.now().plusMillis(VALID_KEY_INTERVAL);
    return;
  }

  @Override
  public void consumeKeys() {
    consumeInstant = Instant.now();
    return;
  }

  @Override
  public boolean press_U() {
    return monitorArray[0].pressCount > 0;
  }

  @Override
  public boolean press_D() {
    return monitorArray[1].pressCount > 0;
  }

  @Override
  public boolean press_L() {
    return monitorArray[2].pressCount > 0;
  }

  @Override
  public boolean press_R() {
    return monitorArray[3].pressCount > 0;
  }

  @Override
  public boolean press_a() {
    return monitorArray[4].pressedBefore(validInstant);
  }

  @Override
  public boolean press_j() {
    return monitorArray[5].pressedBefore(validInstant);
  }

  @Override
  public boolean press_d() {
    return monitorArray[6].pressedBefore(validInstant);
  }

  public boolean pressRunL() {
    return monitorArray[2].doublePress && monitorArray[2].pressedBefore(validInstant);
  }

  public boolean pressRunR() {
    return monitorArray[3].doublePress && monitorArray[3].pressedBefore(validInstant);
  }

  @Override
  public boolean pressRun() {
    return pressRunL() ^ pressRunR();
  }

  public boolean pressWalkX() {
    return press_L() ^ press_R();
  }

  public boolean pressWalkZ() {
    return press_U() ^ press_D();
  }

  @Override
  public boolean pressWalk() {
    return pressWalkX() | pressWalkZ();
  }

  public Direction getDirection() {
    if (press_L()) {
      return press_R() ? Direction.SAME : Direction.LEFT;
    } else {
      return press_R() ? Direction.RIGHT : Direction.SAME;
    }
  }

  @Override
  public Order getOrder() {
    for (Order order : Order.ORDER_LIST) {
      if (sequence.indexOf(order.keySequence) >= 0) {
        return order;
      }
    }
    return null;
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
