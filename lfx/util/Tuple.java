package lfx.util;

public final class Tuple<T, U> {
  public final T first;
  public final U second;

  public Tuple(T first, U second) {
    this.first = first;
    this.second = second;
  }

  public static <T, U> Tuple<T, U> of(T first, U second) {
    return new Tuple<>(first, second);
  }

  public static <T> Tuple<T, T> of(T single) {
    return new Tuple<>(single, single);
  }

}
