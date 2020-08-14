package lfx.util;

import java.util.Iterator;
import java.util.List;

public class UnionReadOnlyIterable<E> implements Iterable<E> {
  private final List<E> EMPTY_LIST = List.of();
  private final List<Iterable<E>> targetList;

  public UnionReadOnlyIterable(Iterable<E> e1) {
    targetList = List.of(e1);
  }

  public UnionReadOnlyIterable(Iterable<E> e1, Iterable<E> e2) {
    targetList = List.of(e1, e2);
  }

  public UnionReadOnlyIterable(Iterable<E> e1, Iterable<E> e2, Iterable<E> e3) {
    targetList = List.of(e1, e2, e3);
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<>() {
      private Iterator<Iterable<E>> outer = targetList.iterator();
      private Iterator<E> inner = EMPTY_LIST.iterator();
      @Override public boolean hasNext() {
        while (!inner.hasNext()) {
          if (outer.hasNext()) {
            inner = outer.next().iterator();
          } else {
            return false;
          }
        }
        return true;
      }
      @Override public E next() {
        return inner.next();
      }
      @Override public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

}
