package lfx.base;

import java.util.function.UnaryOperator;

public enum Direction {
  SAME    (origin -> origin),
  OPPOSITE(origin -> !origin),
  RIGHT   (origin -> true),
  LEFT    (origin -> false);

  private final UnaryOperator<Boolean> operator;

  private Direction(UnaryOperator<Boolean> operator) {
    this.operator = operator;
  }

  public boolean getFacing(boolean origin) {
    return operator.apply(origin);
  }

  @Override
  public String toString() {
    return String.format("Direction.%s", name());
  }

}
