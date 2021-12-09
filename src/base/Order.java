package base;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
  // aliases
  hit_Fa("", Direction.SAME, hit_Ra, hit_La),
  hit_Fj("", Direction.SAME, hit_Rj, hit_Lj),
  HIT_NA("", Direction.SAME);

  public static final List<Order> ORDER_LIST = Arrays.stream(Order.values())
                                                     .filter(o -> !o.keySequence.isEmpty())
                                                     .collect(Collectors.toUnmodifiableList());
  public final String keySequence;
  public final Direction direction;
  public final List<Order> additions;

  @SafeVarargs
  private Order(String keySequence, Direction direction, Order... additions) {
    this.keySequence = keySequence;
    this.direction = direction;
    this.additions = List.copyOf(Arrays.asList(additions));
  }

  @Override
  public String toString() {
    return String.format("Order.%s", name());
  }

}
