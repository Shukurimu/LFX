package lfx.util;

public final class CycleLooper {
  private int index = -1;
  private final int[] data;

  public Looper(int[] data) {
    this.data = data;
  }

  public int next() {
    if (++index == data.length)
      index = 0;
    return data[index];
  }

  public int reset() {
    return data[index = 0];
  }

}
