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

  public static class ReservoirSampling<T> {
    private int total = 0;
    private T current = null;

    public T getNext(T newSample) {
      ++total;
      if (randomBounds(0, total) == 0) {
        current = newSample;
      }
      return current;
    }

    public void reset() {
      total = 0;
      current = null;
      return;
    }

  }

}
