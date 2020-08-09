package lfx.platform;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javafx.scene.input.KeyCode;
import lfx.base.Controller;
import lfx.base.Direction;
import lfx.base.Order;
import lfx.util.Const;
import lfx.util.Tuple;

public class KeyboardController implements Controller {
  private static final Map<KeyCode, KeyMonitor> mapping = new EnumMap<>(KeyCode.class);
  private final List<Tuple<KeyMonitor, String>> keyList = new ArrayList<>(Const.KEY_NUM);
  private final KeyMonitor[] monitorArray = new KeyMonitor[Const.KEY_NUM];
  private final StringBuilder sequence = new StringBuilder(8);
  private long updatedTime =
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
      keyList.add(new Tuple<>(monitor, Const.KEY_SYMBOLS.get(i)));
      monitorArray[i] = monitor;
    }
    return;
  }

  @Override
  public void update() {
    sequence.setLength​(0);
    keyList.sort((m1, m2) -> Long.compare(m1.first.pressTime, m2.first.pressTime));
    for (Tuple<KeyMonitor, String> tuple : keyList) {
      if (tuple.first.pressTime > consumedTime) {
        break;
      }
      sequence.append(tuple.second);
    }
    updatedTime = System.currentTimeMillis();
    return;
  }

  @Override
  public void consumeKeys() {
    consumedTime = System.currentTimeMillis();
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
    return monitorArray[4].isValid(currentTime);
  }

  @Override
  public boolean press_j() {
    return monitorArray[5].isValid(currentTime);
  }

  @Override
  public boolean press_d() {
    return monitorArray[6].isValid(currentTime);
  }

  @Override
  public boolean pressRunL() {
    return monitorArray[2].doublePress && monitorArray[2].isValid(currentTime);
  }

  @Override
  public boolean pressRunL() {
    return monitorArray[3].doublePress && monitorArray[3].isValid(currentTime);
  }

  @Override
  public boolean pressWalkX() {
    return press_L() ^ press_R();
  }

  @Override
  public boolean pressWalkZ() {
    return press_U() ^ press_D();
  }

  @Override
  public boolean pressWalk() {
    return pressWalkX() | pressWalkZ();
  }

  @Override
  public Direction getDirection() {
    if (press_L()) {
      return press_R() ? Direction.SAME : Direction.LEFT;
    } else {
      return press_R() ? Direction.RIGHT : Direction.SAME;
    }
  }

  @Override
  public Order getOrder() {
    for (Order order : Order.values()) {
      if (sequence.indexOf(order.keySquence) >= 0) {
        return order;
      }
    }
    return null;
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

  static class KeyMonitor {
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
