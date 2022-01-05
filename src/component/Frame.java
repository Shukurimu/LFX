package component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import base.KeyOrder;
import base.Type;
import util.IntMap;

public class Frame {
  public static final Frame NULL_FRAME = new Frame();
  public static final int RESET_VELOCITY = 550;

  public final State state;
  public final int curr;
  public final int wait;
  public final Action next;
  public final int pic;
  public final int centerx;
  public final int centery;
  public final int dvx;
  public final int dvy;
  public final int dvz;
  public final Cost cost;
  public final Map<KeyOrder, Action> combo;
  public final List<Bdy> bdyList;
  public final List<Itr> itrList;
  public final Opoint opoint;
  public final Wpoint wpoint;
  public final Cpoint cpoint;

  private Frame(
      State state, int curr, int wait, Action next,
      int pic, int centerx, int centery,
      int dvx, int dvy, int dvz, Cost cost, Map<KeyOrder, Action> combo,
      List<Bdy> bdyList, List<Itr> itrList, Opoint opoint, Wpoint wpoint, Cpoint cpoint) {
    this.state = state;
    this.curr = curr;
    this.wait = wait;
    this.next = next;
    this.pic = pic;
    this.centerx = centerx;
    this.centery = centery;
    this.dvx = dvx;
    this.dvy = dvy;
    this.dvz = dvz;
    this.cost = cost;
    this.combo = Collections.unmodifiableMap(combo);
    this.bdyList = Collections.unmodifiableList(bdyList);
    this.itrList = Collections.unmodifiableList(itrList);
    this.opoint = opoint;
    this.wpoint = wpoint;
    this.cpoint = cpoint;
  }

  /** Constructor for NULL_FRAME. */
  private Frame() {
    state = State.NORMAL;
    curr = wait = 999999;
    next = Action.UNASSIGNED;
    pic = -1;
    centerx = centery = 0;
    dvx = dvy = dvz = RESET_VELOCITY;
    cost = Cost.FREE;
    combo = Map.of();
    bdyList = List.of();
    itrList = List.of();
    opoint = null;
    wpoint = null;
    cpoint = null;
  }

  public double calcVX(double vx, boolean faceRight) {
    if (dvx == RESET_VELOCITY) {
      return 0.0;
    }
    if (dvx == 0) {
      return vx;
    }
    // same direction results in larger magnitude
    double absDvx = faceRight ? dvx : -dvx;
    return absDvx < 0 ? (vx < 0.0 ? Math.min(absDvx, vx) : absDvx)
                      : (vx > 0.0 ? Math.max(absDvx, vx) : absDvx);
  }

  @Override
  public String toString() {
    return String.format("Frame[%d, state=%s, wait=%d]", curr, state, wait);
  }

  /**
   * A builder class to easily construct an immutable {@code Frame}.
   */
  public static class Builder {
    public State state;
    public int curr;
    public int wait;
    public Action next;
    public int pic = -1;
    public int centerx = 0;
    public int centery = 0;
    public int dvx = 0;
    public int dvy = 0;
    public int dvz = 0;
    public Cost cost = Cost.FREE;
    public Map<KeyOrder, Action> combo = new EnumMap<>(KeyOrder.class);
    public List<Bdy> bdyList = new ArrayList<>();
    public List<Itr> itrList = new ArrayList<>();
    public Opoint opoint = null;
    public Wpoint wpoint = null;
    public Cpoint cpoint = null;

    private Builder(int actionNumber, State state, int wait, Action next) {
      this.state = state;
      this.curr = actionNumber;
      this.wait = wait;
      this.next = next;
    }

    public Builder pic(int index, int centerx, int centery) {
      this.pic = index;
      this.centerx = centerx;
      this.centery = centery;
      return this;
    }

    public Builder move(int dvx, int dvy, int dvz) {
      this.dvx = dvx;
      this.dvy = dvy;
      this.dvz = dvz;
      return this;
    }

    public Builder combo(KeyOrder keyOrder, Action action) {
      combo.put(keyOrder, action);
      return this;
    }

    public Builder hit_Fa(Action action) {
      combo.put(KeyOrder.hit_La, action);
      combo.put(KeyOrder.hit_Ra, action);
      return this;
    }

    public Builder hit_Fj(Action action) {
      combo.put(KeyOrder.hit_Lj, action);
      combo.put(KeyOrder.hit_Rj, action);
      return this;
    }

    public Builder add(Object e) {
      if (e instanceof String x) {
        throw new UnsupportedOperationException(e.toString());
      } else if (e instanceof Bdy x) {
        bdyList.add(x);
      } else if (e instanceof Itr x) {
        itrList.add(x);
      } else if (e instanceof Cost x) {
        cost = x;
      } else if (e instanceof Opoint x) {
        opoint = x;
      } else if (e instanceof Wpoint x) {
        wpoint = x;
      } else if (e instanceof Cpoint x) {
        cpoint = x;
      } else {
        throw new IllegalArgumentException(e.toString());
      }
      return this;
    }

    private Frame build() {
      return new Frame(
          state, curr, wait, next,
          pic, centerx, centery,
          dvx, dvy, dvz, cost, combo,
          bdyList, itrList, opoint, wpoint, cpoint);
    }

  }

  /**
   * A helper class to manage a {@code Frame} series.
   */
  public static class Collector {
    private final List<Builder> builderList;

    public Collector() {
      builderList = new ArrayList<>(Collections.nCopies(Action.ACTION_LIMIT, null));
    }

    public Builder addFrame(int actionNumber, State state, int wait, Action next) {
      Builder x = new Frame.Builder(actionNumber, state, wait, next);
      builderList.set(actionNumber, x);
      return x;
    }

    public List<Frame> toFrameList() {
      return builderList.stream().map(x -> x == null ? NULL_FRAME : x.build()).toList();
    }

  }

  // ==================== Parser Utility ====================

  /**
   * Extracts and prepares {@code Frame} setting.
   *
   * @param data        a map containing key-value pairs
   * @param type        the {@code Type} of the owner of this {@code Frame}
   * @param rawState    original state of the enclosing frame
   * @param frameNumber frame's index
   * @return a {@code List} of statement to create a {@code Frame}
   * @throws IllegalArgumentException for invalid kind
   */
  public static List<String> extract(IntMap data, Type type, int rawState, int frameNumber) {
    List<String> result = new ArrayList<>();

    result.add(".addFrame(%d, %s, %d, %s).pic(%d, %d, %d)".formatted(
        frameNumber, State.process(type, rawState, frameNumber),
        data.pop("wait"), Action.processNext(data.pop("next")),
        data.pop("pic"), data.pop("centerx"), data.pop("centery")));

    int dvx = data.pop("dvx", 0);
    int dvy = data.pop("dvy", 0);
    int dvz = data.pop("dvz", 0);
    int mp = data.pop("mp", 0);

    if (type.isHero) {
      if (rawState == 19 || rawState == 301) {
        dvz = 3;
      }
    } else if (type.isEnergy) {
      dvz = data.pop("hit_j", 50) - 50;
      mp = data.pop("hit_a", 0);
    }
    if ((dvz | dvy | dvz) != 0) {
      result.add(".move(%d, %d, %d)".formatted(dvx, dvy, dvz));
    }

    if (mp != 0) {
      result.add(Cost.process(mp));
    }

    var entryIter = data.entrySet().iterator();
    while (entryIter.hasNext()) {
      Map.Entry<String, Integer> e = entryIter.next();
      entryIter.remove();
      if (e.getValue().intValue() == 0) {
        continue;
      }
      String comboFormat = "";
      if (e.getKey().equals("hit_Fa")) {
        comboFormat = ".hit_Fa(%s)";
      } else if (e.getKey().equals("hit_Fj")) {
        comboFormat = ".hit_Fj(%s)";
      } else {
        try {
          KeyOrder order = KeyOrder.valueOf(e.getKey());
          comboFormat = ".combo(%s, %%s)".formatted(order);
        } catch (IllegalArgumentException ex) {
          // not a hit_xx entry
          continue;
        }
      }
      result.add(comboFormat.formatted(Action.processGoto(e.getValue())));
    }
    return result;
  }

}
