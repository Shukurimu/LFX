package lfx.util;

import lfx.util.Combo;

public final class Input {
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
                  boolean do_LL, boolean do_RR, Combo combo) {
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
    this.combo = combo;
    return;
  }

}
