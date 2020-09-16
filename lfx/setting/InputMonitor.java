package lfx.setting;

import java.time.Instant;

public class InputMonitor implements Comparable<InputMonitor> {
  private static final Instant DEFAULT_PRESSED_INSTANT = Instant.now().plusSeconds(86400L);
  private Instant timePressedPrevious = DEFAULT_PRESSED_INSTANT;
  private Instant timePressedCurrent = DEFAULT_PRESSED_INSTANT;
  private int pressCount = 0;

  public void setPressed() {
    if (++pressCount == 1) {
      timePressedPrevious = timePressedCurrent;
      timePressedCurrent = Instant.now();
    }
    return;
  }

  public void setReleased() {
    pressCount = 0;
    return;
  }

  public boolean isPressed() {
    return pressCount > 0;
  }

  public boolean isDoublePressed(long validIntervalMs) {
    return timePressedCurrent.isBefore(timePressedPrevious.plusMillis(validIntervalMs));
  }

  public boolean pressedAfter(Instant instant) {
    return timePressedCurrent.isAfter(instant);
  }

  public boolean pressedBefore(Instant instant) {
    return timePressedCurrent.isBefore(instant);
  }

  @Override
  public int compareTo(InputMonitor o) {
    return timePressedCurrent.compareTo(o.timePressedCurrent);
  }

}
