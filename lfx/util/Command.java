package lfx.util;

import java.util.List;
import lfx.util.Direction;
import lfx.util.Tuple;

public enum Command {
  hit_a,
  hit_j,
  hit_d,
  hit_U,
  hit_D,
  hit_L,
  hit_LL,
  hit_R,
  hit_RR,
  // The followings are exclusive values.
  hit_Fa,
  hit_Fj,
  hit_Ua,
  hit_Uj,
  hit_Da,
  hit_Dj,
  hit_ja,
  hit_aj,
  NONE;

  public static final List<Tuple<String, Tuple<Command, Direction>>> COMBO_LIST = List.of(
      new Tuple<>("Ra", new Tuple<>(hit_Fa, Direction.RIGHT)),
      new Tuple<>("Rj", new Tuple<>(hit_Fj, Direction.RIGHT)),
      new Tuple<>("La", new Tuple<>(hit_Fa, Direction.LEFT)),
      new Tuple<>("Lj", new Tuple<>(hit_Fj, Direction.LEFT)),
      new Tuple<>("Ua", new Tuple<>(hit_Ua, Direction.SAME)),
      new Tuple<>("Uj", new Tuple<>(hit_Uj, Direction.SAME)),
      new Tuple<>("Da", new Tuple<>(hit_Da, Direction.SAME)),
      new Tuple<>("Dj", new Tuple<>(hit_Dj, Direction.SAME)),
      new Tuple<>("ja", new Tuple<>(hit_ja, Direction.SAME)),
      new Tuple<>("aj", new Tuple<>(hit_aj, Direction.SAME))
  );
  public static final Tuple<Command, Direction> NOT_COMBO = new Tuple<>(NONE, Direction.SAME);

  /** Assume all combos start with Key_d. */
  public static Tuple<Command, Direction> getCombo(StringBuilder sequence) {
    int dIndex = sequence.indexOf("d");
    if (dIndex + 3 >= sequence.length())
      return NOT_COMBO;
    String remaining = sequence.substring(dIndex + 1, dIndex + 3);
    for (Tuple<String, Tuple<Command, Direction>> tuple: COMBO_LIST) {
      if (remaining.equals(tuple.first))
        return tuple.second;
    }
    return NOT_COMBO;
  }

  @Override
  public String toString() {
    return String.format("%s.%s", this.getDeclaringClass().getSimpleName(), super.toString());
  }

}
