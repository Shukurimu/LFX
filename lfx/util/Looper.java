package lfx.util;

public final class Looper {
  private int index = -1;
  private final int[] data;

  public Looper(int[] data) {
    this.data = data;
  }

  public int next() {
    return data.get(index = (index + 1) % data.length);
  }

  public int reset() {
    return data.get(index = 0);
  }

}
