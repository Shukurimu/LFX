package lfx.util;

/**
 * Mainly used in Hero's hidden counter.
 */
public final class Looper {
  private int index = -1;
  private final int[] data;

  @SafeVarargs
  public Looper(int... data) {
    this.data = data;
  }

  public int next() {
    if (++index == data.length) {
      index = 0;
    }
    return data[index];
  }

  public int reset() {
    return data[index = 0];
  }

}
