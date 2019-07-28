package lfx.util;

public enum Combo {
  NONE  (0),
  hit_a (2),
  hit_j (2),
  hit_d (2),
  hit_Fa(3),
  hit_Fj(3),
  hit_Ua(3),
  hit_Uj(3),
  hit_Da(3),
  hit_Dj(3),
  hit_ja(4);

  public final int priority;

  private Combo(int priority) {
    this.priority = priority;
  }

  @Override
  public String toString() {
    return String.format("%s.%s", this.getDeclaringClass().getSimpleName(), super.toString());
  }

}
