package component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import base.KeyOrder;
import util.Tuple;

public class DataCollector {
  public static final int FRAME_COUNT = 400;

  private List<Frame> frameList;

  public DataCollector(int size) {
    frameList = new ArrayList<>(Collections.nCopies(size, Frame.DUMMY));
  }

  public DataCollector() {
    this(FRAME_COUNT);
  }

  public void add(int curr, int wait, State state, int pic, int centerx, int centery,
                  Action next, Object... elements) {
    add(curr, wait, state, pic, centerx, centery, 0, 0, 0, next, elements);
    return;
  }

  public void add(int curr, int wait, State state, int pic, int centerx, int centery,
                  int dvx, Action next, Object... elements) {
    add(curr, wait, state, pic, centerx, centery, dvx, 0, 0, next, elements);
    return;
  }

  public void add(int curr, int wait, State state, int pic, int centerx, int centery,
                  int dvx, int dvy, Action next, Object... elements) {
    add(curr, wait, state, pic, centerx, centery, dvx, dvy, 0, next, elements);
    return;
  }

  public void add(int curr, int wait, State state, int pic, int centerx, int centery,
                  int dvx, int dvy, int dvz, Action next, Object... elements) {
    if (next == Action.UNASSIGNED) {
      throw new IllegalArgumentException("Invalid Frame.next: " + next.toString());
    }
    Map<KeyOrder, Action> combo = new EnumMap<>(KeyOrder.class);
    List<Bdy> bdyList = new ArrayList<>(4);
    List<Itr> itrList = new ArrayList<>(4);
    List<Opoint> opointList = new ArrayList<>(4);
    Cost cost = Cost.FREE;
    Wpoint wpoint = null;
    Cpoint cpoint = null;
    for (Object e : elements) {
      if (e instanceof Tuple) {
        @SuppressWarnings("unchecked")
        Tuple<KeyOrder, Action> kv = (Tuple<KeyOrder, Action>) e;
        combo.put(kv.first, kv.second);
      } else if (e instanceof Bdy x) {
        bdyList.add(x);
      } else if (e instanceof Itr x) {
        itrList.add(x);
      } else if (e instanceof Opoint x) {
        opointList.add(x);
      } else if (e instanceof Cost x) {
        cost = x;
      } else if (e instanceof Wpoint x) {
        wpoint = x;
      } else if (e instanceof Cpoint x) {
        cpoint = x;
      } else if (e instanceof Cpoint.Builder x) {
        cpoint = x.build();
      } else {
        System.out.println("NotImplemented: " + e.toString());
      }
    }
    frameList.set(curr,
        new Frame(0, centerx, centery,
                  state, curr, wait, next,
                  dvx, dvy, dvz, cost,
                  combo,
                  bdyList, itrList, opointList,
                  cpoint, wpoint
    ));
    return;
  }

  public List<Frame> getFrameList() {
    return Collections.unmodifiableList(frameList);
  }

}
