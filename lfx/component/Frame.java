package lfx.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javafx.scene.image.Image;
import lfx.base.Input;
import lfx.component.Bdy;
import lfx.component.Cpoint;
import lfx.component.Effect;
import lfx.component.Itr;
import lfx.component.Opoint;
import lfx.component.State;
import lfx.component.Wpoint;
import lfx.util.Const;
import lfx.util.Tuple;

public final class Frame {
  public static final Frame DUMMY = new Frame();
  public final Image pic1;
  public final Image pic2;
  public final int centerx;
  public final int centery;
  public final State state;
  public final int curr;
  public final int wait;
  public final int next;
  public final int dvx;
  public final int dvy;
  public final int dvz;
  public final int cost;
  public final Map<Input.Combo, Integer> combo;
  public final Map<Effect, Effect.Value> effect;
  public final List<Bdy> bdyList;
  public final List<Itr> itrList;
  public final List<Opoint> opointList;
  public final Cpoint cpoint;
  public final Wpoint wpoint;
  public final String sound;

  public Frame(Image pic1, Image pic2, int centerx, int centery,
               State state, int curr, int wait, int next,
               int dvx, int dvy, int dvz, int cost,
               Map<Input.Combo, Integer> combo, Map<Effect, Effect.Value> effect,
               List<Bdy> bdyList, List<Itr> itrList, List<Opoint> opointList,
               Cpoint cpoint, Wpoint wpoint, String sound) {
    this.pic1 = pic1;
    this.pic2 = pic2;
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
    this.sound = sound;
    this.combo = Map.copyOf(combo);
    this.effect = Map.copyOf(effect);
    this.bdyList = List.copyOf(bdyList);
    this.itrList = List.copyOf(itrList);
    this.opointList = List.copyOf(opointList);
    this.cpoint = cpoint;
    this.wpoint = wpoint;
  }

  private Frame() {  // DUMMY
    pic1 = pic2 = null;
    centerx = centery = cost = 0;
    state = State.UNIMPLEMENTED;
    curr = wait = next = 999999;
    dvx = dvy = dvz = Const.DV_550;
    sound = null;
    combo = Map.of();
    effect = Map.of();
    bdyList = List.of();
    itrList = List.of();
    opointList = List.of();
    cpoint = null;
    wpoint = null;
  }

  public double calcVX(double vx, boolean faceRight) {
    if (dvx == Const.DV_550) {
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

  public double calcVY(double vy) {
    return dvy == Const.DV_550 ? 0.0 : (vy + dvy);
  }

  public static final class Collector {
    List<Frame> data;
    List<Tuple<Image, Image>> imageList;

    public Collector(int size, List<Tuple<Image, Image>> imageList) {
      data = new ArrayList<>(Collections.nCopies(size, DUMMY));
      this.imageList = imageList;
    }

    static int nonNullValue(Integer originValue, int defaultValue) {
      return originValue == null ? originValue.intValue() : defaultValue;
    }

    public void add(int curr, int wait, int next, State state,
                    int picIndex, int centerx, int centery, Object... elements) {
      Map<String, Integer> optField = Map.of();
      List<Bdy> bdyList = new ArrayList<>();
      List<Itr> itrList = new ArrayList<>();
      List<Opoint> opointList = new ArrayList<>();
      Wpoint wpoint = null;
      Cpoint cpoint = null;
      String sound = null;
      for (Object e : elements) {
        if (e instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Integer> temp = (Map<String, Integer>) e;
          optField = temp;
        } else if (e instanceof Bdy) {
          bdyList.add((Bdy) e);
        } else if (e instanceof Itr) {
          itrList.add((Itr) e);
        } else if (e instanceof Wpoint) {
          wpoint = (Wpoint) e;
        } else if (e instanceof Opoint) {
          opointList.add((Opoint) e);
        } else if (e instanceof Cpoint) {
          cpoint = (Cpoint) e;
        } else if (e instanceof String) {
          sound = (String) e;
        } else {
          System.out.println("NotImplemented: " + e.toString());
        }
      }

      int dvx = nonNullValue(optField.remove("dvx"), 0);
      int dvy = nonNullValue(optField.remove("dvy"), 0);
      int dvz = nonNullValue(optField.remove("dvz"), 0);
      int cost = nonNullValue(optField.remove("mp"), 0);
      Map<Input.Combo, Integer> combo = new EnumMap<>(Input.Combo.class);
      optField.forEach((string, act) -> combo.put(Input.Combo.valueOf(string), act));

      Tuple<Image, Image> pics = imageList.get(picIndex);
      data.set(curr, new Frame(pics.first, pics.second, centerx, centery,
                               state, curr, wait, next,
                               dvx, dvy, dvz, cost,
                               combo, Map.of(),
                               bdyList, itrList, opointList,
                               cpoint, wpoint, sound)
      );
      return;
    }

    public List<Frame> getFrameList() {
      return Collections.unmodifiableList(data);
    }

  }

}
