package lfx.setting;

import java.time.Instant;
import java.time.ZoneId;

public class InputMonitor implements Comparable<InputMonitor> {
  private static final Instant DEFAULT_PRESSED_INSTANT = Instant.now().plusSeconds(86400L);
  private static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
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

  public boolean pressedBetween(Instant excludedBegin, Instant excludedEnd) {
    return timePressedCurrent.isAfter(excludedBegin)
        && timePressedCurrent.isBefore(excludedEnd);
  }

  @Override
  public int compareTo(InputMonitor o) {
    return timePressedCurrent.compareTo(o.timePressedCurrent);
  }

  @Override
  public String toString() {
    return String.format(
        "InputMonitor(Previous %1$tT.%1$tL, Current %2$tT.%2$tL, Now %3$tT.%3$tL)",
        timePressedPrevious.atZone(DEFAULT_ZONE_ID),
        timePressedCurrent.atZone(DEFAULT_ZONE_ID),
        Instant.now().atZone(DEFAULT_ZONE_ID)
    );
  }

}
