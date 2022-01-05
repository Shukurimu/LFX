package util;

import java.util.Random;

public class ReservoirSampling<T> {
  private final Random random;
  private int total = 0;
  private T current = null;

  public ReservoirSampling(Random random) {
    this.random = random;
  }

  public T getNext(T newSample) {
    ++total;
    if (random.nextInt(total) == 0) {
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
