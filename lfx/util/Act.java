
enum Act {
  NOP  (-1, null),
  hit_a (0, null),
  hit_j (1, null),
  hit_d (2, null),
  hit_Ua(3, null),
  hit_Uj(4, null),
  hit_Da(7, null),
  hit_Dj(8, null),
  hit_ja(9, null),
  hit_Fa(5, null),
  hit_La(5, Boolean.FALSE),
  hit_Ra(5, Boolean.TRUE),
  hit_Fj(6, null),
  hit_Lj(6, Boolean.FALSE),
  hit_Rj(6, Boolean.TRUE);

  public final int index;
  public final Boolean facing;

  private LFact(int i, Boolean f) {
    index = i;
    facing = f;
  }

}
