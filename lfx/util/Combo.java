package lfx.util;

import lfx.util.Direction;

public enum Combo {
  // Order is also considered as priority.
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
  hit_d ("d",   Direction.SAME);

  public final String keySquence;
  public final Direction direction;

  private Combo(String keySquence, Direction direction) {
    this.keySquence = keySquence;
    this.direction = direction;
  }

  // Input sequence must start from unconsumed keys.
  public static Combo parse(StringBuilder validSequence) {
    for (Combo combo : Combo.values()) {
      if (validSequence.indexOf(combo.keySquence) >= 0) {
        return combo;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return String.format("%s.%s", this.getDeclaringClass().getSimpleName(), super.toString());
  }

}
