package lfx.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lfx.base.Action;
import lfx.base.Cost;
import lfx.base.Order;
import lfx.component.Bdy;
import lfx.component.Cpoint;
import lfx.component.Effect;
import lfx.component.Frame;
import lfx.component.Itr;
import lfx.component.Opoint;
import lfx.component.State;
import lfx.component.Wpoint;
import lfx.util.ImageCell;
import lfx.util.Tuple;

public class DataCollector {
  public static final int FRAME_COUNT = 400;

  private List<ImageCell> imageList;
  private List<Frame> frameList;

  public DataCollector(List<ImageCell> imageList, int size) {
    this.imageList = imageList;
    frameList = new ArrayList<>(Collections.nCopies(size, Frame.DUMMY));
  }

  public DataCollector(List<ImageCell> imageList) {
    this(imageList, FRAME_COUNT);
  }

  static int nonNullValue(Integer originValue, int defaultValue) {
    return originValue == null ? originValue.intValue() : defaultValue;
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
    Map<Order, Action> combo = new EnumMap<>(Order.class);
    List<Bdy> bdyList = new ArrayList<>();
    List<Itr> itrList = new ArrayList<>();
    List<Opoint> opointList = new ArrayList<>();
    Cost cost = Cost.FREE;
    Wpoint wpoint = null;
    Cpoint cpoint = null;
    String sound = null;
    for (Object e : elements) {
      if (e instanceof Tuple) {
        @SuppressWarnings("unchecked")
        Tuple<Order, Action> kv = (Tuple<Order, Action>) e;
        kv.first.insert(combo, kv.second);
      } else if (e instanceof Bdy) {
        bdyList.add((Bdy) e);
      } else if (e instanceof Itr) {
        itrList.add((Itr) e);
      } else if (e instanceof Opoint) {
        opointList.add((Opoint) e);
      } else if (e instanceof Cost) {
        cost = (Cost) e;
      } else if (e instanceof Wpoint) {
        wpoint = (Wpoint) e;
      } else if (e instanceof Cpoint) {
        cpoint = (Cpoint) e;
      } else if (e instanceof Cpoint.Builder) {
        cpoint = ((Cpoint.Builder) e).build();
      } else if (e instanceof String) {
        sound = (String) e;
      } else {
        System.out.println("NotImplemented: " + e.toString());
      }
    }
    frameList.set(curr,
        new Frame(imageList.get(pic), centerx, centery,
                  state, curr, wait, next,
                  dvx, dvy, dvz, cost,
                  combo, Map.of(),
                  bdyList, itrList, opointList,
                  cpoint, wpoint, sound
    ));
    return;
  }

  public List<Frame> getFrameList() {
    return Collections.unmodifiableList(frameList);
  }

}
