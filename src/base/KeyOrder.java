package base;

public enum KeyOrder {
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

  public final String keySequence;
  public final Direction direction;

  private KeyOrder(String keySequence, Direction direction) {
    this.keySequence = keySequence;
    this.direction = direction;
  }

}
