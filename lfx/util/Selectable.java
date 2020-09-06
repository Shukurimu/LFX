package lfx.util;

public interface Selectable<T> {
  T get();
  void setPrevious();
  void setNext();
  void setDefault();

  static Selectable<Void> EMPTY = new Selectable<>() {
    @Override public Void get() { return null; }
    @Override public void setPrevious() {}
    @Override public void setNext() {}
    @Override public void setDefault() {}
  };

}
