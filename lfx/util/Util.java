package lfx.util;

import java.util.concurrent.ThreadLocalRandom;

public final class Util {

  private Util() {}

  public static double clamp(double value, double hi, double lo) {
    return value >= hi ? hi : (value < lo ? lo : value);
  }

  public static int randomBounds(int lo, int hi) {
    return ThreadLocalRandom.current().nextInt(lo, hi);
  }

  public static double randomBounds(double lo, double hi) {
    return ThreadLocalRandom.current().nextDouble(lo, hi);
  }

  public static boolean randomBool() {
    return ThreadLocalRandom.current().nextBoolean();
  }

}
