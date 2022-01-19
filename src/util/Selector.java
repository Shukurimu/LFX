package util;

import java.util.List;

public class Selector<T> {
  public static final Selector<Integer> NO_CHOICE = new Selector<>(false, List.of(0));

  private final boolean loop;
  private final List<T> elements;
  private int index = 0;

  public Selector(boolean loop, List<T> elements) {
    this.loop = loop;
    this.elements = elements;
  }

  public T current() {
    return elements.get(index);
  }

  public T previous() {
    index = index == 0 ? (loop ? elements.size() - 1 : 0) : index - 1;
    return elements.get(index);
  }

  public T next() {
    index = index + 1 == elements.size() ? (loop ? 0 : index) : index + 1;
    return elements.get(index);
  }

  public T set(int index) {
    return elements.get(this.index = index);
  }

}
