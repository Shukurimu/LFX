package util;

public class ElementSelector<T> implements Selectable<T> {
  private final boolean loop;
  private final T[] elements;
  private int index = 0;

  @SafeVarargs
  public ElementSelector(boolean loop, T... elements) {
    this.loop = loop;
    this.elements = elements;
  }

  @SafeVarargs
  public ElementSelector(T... elements) {
    this(true, elements);
  }

  @Override
  public T get() {
    return elements[index];
  }

  @Override
  public void setPrevious() {
    index = index == 0 ? (loop ? elements.length - 1 : 0) : index - 1;
    return;
  }

  @Override
  public void setNext() {
    index = index + 1 == elements.length ? (loop ? 0 : index) : index + 1;
    return;
  }

  @Override
  public void setDefault() {
    index = 0;
    return;
  }

}
