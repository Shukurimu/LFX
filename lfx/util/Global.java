package lfx.util;

import java.util.concurrent.ThreadLocalRandom;

public final class Global {
  public static boolean isUnlimitedMode = false;

  public static double clamp(double value, double hi, double lo) {
    return value > hi ? hi : value < lo ? lo : value;
  }

  public static double randomBounds(double origin, double bound) {
    return ThreadLocalRandom.current().nextDouble(origin, bound);
  }

  public static int randomBounds(int origin, int bound) {
    return ThreadLocalRandom.current().nextInt(origin, bound);
  }

  public static boolean randomBool() {
    return ThreadLocalRandom.current().nextBoolean();
  }

}