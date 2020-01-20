package lfx.util;

import java.util.concurrent.ThreadLocalRandom;

public final class Global {
  public static final MAX_TEAMS = 5;
  public static final FPS = 30.0;
  private static boolean isUnlimitedMode = false;
  private static int timestamp = 0;

  public static double clamp(double value, double hi, double lo) {
    return value > hi ? hi : value < lo ? lo : value;
  }

  public static double randomBounds(double lo, double hi) {
    return ThreadLocalRandom.current().nextDouble(lo, hi);
  }

  public static int randomBounds(int lo, int hi) {
    return ThreadLocalRandom.current().nextInt(lo, hi);
  }

  public static boolean randomBool() {
    return ThreadLocalRandom.current().nextBoolean();
  }

}
