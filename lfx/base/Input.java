package lfx.base;

import lfx.base.Direction;

public final class Input {

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

    @Override
    public String toString() {
      return String.format("%s.%s", this.getDeclaringClass().getSimpleName(), super.toString());
    }

  }

  public boolean do_U = false;
  public boolean do_D = false;
  public boolean do_L = false;
  public boolean do_R = false;
  public boolean do_a = false;
  public boolean do_j = false;
  public boolean do_d = false;
  public boolean do_LL = false;
  public boolean do_RR = false;
  public boolean do_Z = false;
  public boolean do_F = false;
  public Combo combo = null;

  public void set(boolean do_U, boolean do_D, boolean do_L, boolean do_R,
                  boolean do_a, boolean do_j, boolean do_d,
                  boolean do_LL, boolean do_RR, StringBuilder validSequence) {
    this.do_U = do_U;
    this.do_D = do_D;
    this.do_L = do_L;
    this.do_R = do_R;
    this.do_a = do_a;
    this.do_j = do_j;
    this.do_d = do_d;
    this.do_LL = do_LL;
    this.do_RR = do_RR;
    do_Z = do_U ^ do_D;
    do_F = do_L ^ do_R;
    this.combo = parseCombo(validSequence);
    return;
  }

  // Input sequence must start from unconsumed keys.
  public static Combo parseCombo(StringBuilder validSequence) {
    for (Combo combo : Combo.values()) {
      if (validSequence.indexOf(combo.keySquence) >= 0) {
        return combo;
      }
    }
    return null;
  }

}
