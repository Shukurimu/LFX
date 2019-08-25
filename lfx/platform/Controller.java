package lfx.platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import javafx.scene.input.KeyCode;

import lfx.util.Tuple;

public final class Controller {
  public static final int NOTHING = 0;
  public static final int HOLDING = 0b01;
  public static final int DBPRESS = 0b10;
  public static final long VALID_INTERVAL = 200L;

  public final KeyCode code_U;
  public final KeyCode code_D;
  public final KeyCode code_L;
  public final KeyCode code_R;
  public final KeyCode code_a;
  public final KeyCode code_j;
  public final KeyCode code_d;

  public final boolean activated;
  private final ArrayList<Tuple<KeyRecord, String>> keyList = new ArrayList<>(7);
  private KeyRecord key_U = null;
  private KeyRecord key_D = null;
  private KeyRecord key_L = null;
  private KeyRecord key_R = null;
  private KeyRecord key_a = null;
  private KeyRecord key_j = null;
  private KeyRecord key_d = null;
  private long consumedTime = 0L;

  private Controller() {
    activated = false;
    code_U = code_D = code_L = code_R = code_a = code_j = code_d = null;
  }

  public Controller(String settings) {
    String[] keyArray = settings.split(" ");
    activated = true;
    code_U = getOrUndefined(keyArray[0]);
    code_D = getOrUndefined(keyArray[1]);
    code_L = getOrUndefined(keyArray[2]);
    code_R = getOrUndefined(keyArray[3]);
    code_a = getOrUndefined(keyArray[4]);
    code_j = getOrUndefined(keyArray[5]);
    code_d = getOrUndefined(keyArray[6]);
    return;
  }

  /** Called when a new scene is created. */
  public void register(Map<KeyCode, KeyRecord> keyCodeRecorder) {
    keyList.add(new Tuple(key_U = keyCodeRecorder.putIfAbsent(code_U, new KeyRecord()), "U"));
    keyList.add(new Tuple(key_D = keyCodeRecorder.putIfAbsent(code_D, new KeyRecord()), "D"));
    keyList.add(new Tuple(key_L = keyCodeRecorder.putIfAbsent(code_L, new KeyRecord()), "L"));
    keyList.add(new Tuple(key_R = keyCodeRecorder.putIfAbsent(code_R, new KeyRecord()), "R"));
    keyList.add(new Tuple(key_a = keyCodeRecorder.putIfAbsent(code_a, new KeyRecord()), "a"));
    keyList.add(new Tuple(key_j = keyCodeRecorder.putIfAbsent(code_j, new KeyRecord()), "j"));
    keyList.add(new Tuple(key_d = keyCodeRecorder.putIfAbsent(code_d, new KeyRecord()), "d"));
    consumedTime = 1L;
    return;
  }

  /** Called per TimeUnit to get user input. */
  public Direction getKeyStatus(Set<Command> commandSet, long engineTime) {
    keyList.sort((o1, o2) -> o1.first.pressTime - o2.first.pressTime);
    StringBuilder sequence = new StringBuilder(8);
    for (Tuple<KeyRecord, String> tuple: keyList) {
      if (key_d.pressTime >= consumedTime)
        break;
      sequence.append(tuple.second);
    }
    commandSet.clear();
    if (key_U.pressCount > 0)  commandSet.add(Command.hit_U);
    if (key_D.pressCount > 0)  commandSet.add(Command.hit_D);
    if (key_L.pressCount > 0)  commandSet.add(Command.hit_L);
    if (key_R.pressCount > 0)  commandSet.add(Command.hit_R);
    if (key_a.isValidPress())  commandSet.add(Command.hit_a);
    if (key_j.isValidPress())  commandSet.add(Command.hit_j);
    if (key_d.isValidPress())  commandSet.add(Command.hit_d);
    if (key_L.doublePress & key_L.isValidPress())  commandSet.add(Command.hit_LL);
    if (key_R.doublePress & key_R.isValidPress())  commandSet.add(Command.hit_RR);
    Tuple<Command, Direction> combo = Command.getCombo(sequence);
    commandSet.add(combo.first);
    return combo.second;
  }

  public String[] asStringArray() {
    return new String[] {
      code_U.toString(),
      code_D.toString(),
      code_L.toString(),
      code_R.toString(),
      code_a.toString(),
      code_j.toString(),
      code_d.toString(),
    };
  }

  public static KeyCode getOrUndefined(String s) {
    KeyCode c = KeyCode.UNDEFINED;
    try {
      c = KeyCode.valueOf(s);
    } catch (Exception e) {
      System.err.println("Invalid KeyCode: " + s);
    }
    return c;
  }

  public static class KeyRecord implements Comparable<KeyRecord> {
    private int pressCount = 0;
    private long pressTime = 0L;
    private boolean doublePress = false;

    public void setPressed() {
      if (++pressCount == 1) {
        long systemTime = System.currentTimeMillis();
        doublePress = pressTime + VALID_INTERVAL > systemTime;
        pressTime = systemTime;
      }
      return;
    }

    public void setReleased() {
      pressCount = 0;
      return;
    }

    public boolean isValidPress(long basedTime) {
      return pressTime + VALID_INTERVAL > basedTime;
    }

  }

}
