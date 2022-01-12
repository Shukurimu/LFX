package util;

public final class Tuple<T, U> {
  private static final Tuple<?, ?> NULLISH = new Tuple<>(null, null);
  public final T first;
  public final U second;

  public Tuple(T first, U second) {
    this.first = first;
    this.second = second;
  }

  /**
   * Returns a tuple containing given elements.
   *
   * @param <T> the {@code Tuple}'s first element type
   * @param <U> the {@code Tuple}'s second element type
   * @param o1 the {@code Tuple}'s first element
   * @param o2 the {@code Tuple}'s second element
   * @return a {@code Tuple}
   */
  public static <T, U> Tuple<T, U> of(T o1, U o2) {
    return new Tuple<>(o1, o2);
  }

  /**
   * Returns a nullish tuple containing null elements.
   *
   * @param <T> the {@code Tuple}'s first element type
   * @param <U> the {@code Tuple}'s second element type
   * @return a nullish {@code Tuple}
   */
  @SuppressWarnings("unchecked")
  public static <T, U> Tuple<T, U> of() {
    return (Tuple<T, U>) NULLISH;
  }

  @Override
  public String toString() {
    return String.format("(<%s>%s, <%s>%s)",
        first.getClass().getSimpleName(), first,
        second.getClass().getSimpleName(), second);
  }

}
