package component;

import java.util.List;
import java.util.Map;

import base.KeyOrder;

public class Frame {
  public static final Frame DUMMY = new Frame();
  public static final int RESET_VELOCITY = 550;

  public final int pic;
  public final int centerx;
  public final int centery;
  public final State state;
  public final int curr;
  public final int wait;
  public final Action next;
  public final int dvx;
  public final int dvy;
  public final int dvz;
  public final Cost cost;
  public final Map<KeyOrder, Action> combo;
  public final List<Bdy> bdyList;
  public final List<Itr> itrList;
  public final List<Opoint> opointList;
  public final Cpoint cpoint;
  public final Wpoint wpoint;

  Frame(int pic, int centerx, int centery,
        State state, int curr, int wait, Action next,
        int dvx, int dvy, int dvz, Cost cost,
        Map<KeyOrder, Action> combo,
        List<Bdy> bdyList, List<Itr> itrList, List<Opoint> opointList,
        Cpoint cpoint, Wpoint wpoint) {
    this.pic = pic;
    this.centerx = centerx;
    this.centery = centery;
    this.state = state;
    this.curr = curr;
    this.wait = wait;
    this.next = next;
    this.dvx = dvx;
    this.dvy = dvy;
    this.dvz = dvz;
    this.cost = cost;
    this.combo = Map.copyOf(combo);
    this.bdyList = List.copyOf(bdyList);
    this.itrList = List.copyOf(itrList);
    this.opointList = List.copyOf(opointList);
    this.cpoint = cpoint;
    this.wpoint = wpoint;
  }

  private Frame() {  // DUMMY
    pic = 0;
    centerx = centery = 0;
    state = State.NORMAL;
    curr = wait = 999999;
    next = Action.UNASSIGNED;
    dvx = dvy = dvz = RESET_VELOCITY;
    cost = Cost.FREE;
    combo = Map.of();
    bdyList = List.of();
    itrList = List.of();
    opointList = List.of();
    cpoint = null;
    wpoint = null;
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
    return String.format("Frame%d(center %d %d, %s, %d, %s, dv %d %d %d, %s)",
                         curr, centerx, centery, state, wait, next,
                         dvx, dvy, dvz, cost);
  }

}
