package util;

public class ElementSelector<T> implements Selectable<T> {
  private final boolean loop;
  private final T[] validElements;
  private final int maxValidIndex;
  private int currentIndex = 0;

  @SafeVarargs
  public ElementSelector(boolean loop, T... elements) {
    this.loop = loop;
    validElements = elements;
    maxValidIndex = elements.length - 1;
  }

  @SafeVarargs
  public ElementSelector(T... elements) {
    this(true, elements);
  }

  @Override
  public T get() {
    return validElements[currentIndex];
  }

  @Override
  public void setPrevious() {
    currentIndex = currentIndex == 0 ? (loop ? maxValidIndex : 0) : currentIndex - 1;
    return;
  }

  @Override
  public void setNext() {
    currentIndex = currentIndex == maxValidIndex ? (loop ? 0 : maxValidIndex) : currentIndex + 1;
    return;
  }

  @Override
  public void setDefault() {
    currentIndex = 0;
    return;
  }

}
