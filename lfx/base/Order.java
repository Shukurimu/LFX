package lfx.base;

import java.util.List;
import java.util.Map;
import lfx.base.Action;
import lfx.base.Direction;
import lfx.util.Tuple;

public enum Order {
  hit_ja("dja", Direction.SAME),
  hit_aj("daj", Direction.SAME),
  hit_Ua("dUa", Direction.SAME),
  hit_Uj("dUj", Direction.SAME),
  hit_Da("dDa", Direction.SAME),
  hit_Dj("dDj", Direction.SAME),
  hit_Ra("dRa", Direction.RIGHT),
  hit_Rj("dRj", Direction.RIGHT),
  hit_La("dLa", Direction.LEFT),
  hit_Lj("dLj", Direction.LEFT),
  hit_a ("a",   Direction.SAME),
  hit_j ("j",   Direction.SAME),
  hit_d ("d",   Direction.SAME),

  hit_Fa("!", Direction.SAME) {
    @Override public void insert(Map<Order, Action> targetMap, Action action) {
      targetMap.putIfAbsent(hit_Ra, action);
      targetMap.putIfAbsent(hit_La, action);
      return;
    }
  },
  hit_Fj("!", Direction.SAME) {
    @Override public void insert(Map<Order, Action> targetMap, Action action) {
      targetMap.putIfAbsent(hit_Rj, action);
      targetMap.putIfAbsent(hit_Lj, action);
      return;
    }
  };

  // Avoid copying on every call.
  public static final List<Order> ORDER_LIST = List.of(Order.values());
  public final String keySequence;
  public final Direction direction;

  private Order(String keySequence, Direction direction) {
    this.keySequence = keySequence;
    this.direction = direction;
  }

  public Tuple<Order, Action> of(Action action) {
    return new Tuple<Order, Action>(this, action);
  }

  public Tuple<Order, Action> of(int actionNumber) {
    return new Tuple<Order, Action>(this, new Action(actionNumber));
  }

  public void insert(Map<Order, Action> targetMap, Action action) {
    targetMap.put(this, action);
    return;
  }

  @Override
  public String toString() {
    return String.format("Order.%s", name());
  }

}
