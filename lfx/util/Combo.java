package lfx.util;

public enum Combo {
  NONE  (0),
  hit_a (1),
  hit_j (1),
  hit_d (1),
  hit_Fa(2),
  hit_Fj(2),
  hit_Ua(2),
  hit_Uj(2),
  hit_Da(2),
  hit_Dj(2),
  hit_ja(3);

  public final int priority;

  private Combo(int priority) {
    this.priority = priority;
  }

  @Override
  public String toString() {
    return String.format("%s.%s", this.getClass().getSimpleName(), super.toString());
  }

}
