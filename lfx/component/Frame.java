package lfx.component;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.EnumMap;
import lfx.component.Cpoint;
import lfx.component.Effect;
import lfx.component.Opoint;
import lfx.component.State;
import lfx.component.Wpoint;
import lfx.util.Act;

final class Frame {
  public final int pic;
  public final State state;
  public final int curr;
  public final int wait;
  public final int next;
  public final int dvx;
  public final int dvy;
  public final int dvz;
  public final int centerx;
  public final int centery;
  public final int cost;
  public final String sound;
  public final Map<Act, Integer> combo;
  public final Map<Effect.Kind, Effect> effect;
  public final List<Bdy> bdy;
  public final List<Itr> itr;
  public final List<Opoint> opoint;
  public final Cpoint cpoint;
  public final Wpoint wpoint;

  public Frame(int pic, State state, int curr, int wait, int next,
               int dvx, int dvy, int dvz, int centerx, int centery, int cost, String sound,
               Map<Act, Integer> combo, Map<Effect.Kind, Effect> effect,
               List<Bdy> bdy, List<Itr> itr, List<Opoint> opoint, Cpoint cpoint, Wpoint wpoint) {
    this.pic = pic;
    this.state = state;
    this.curr = curr;
    this.wait = wait;
    this.next = next;
    this.dvx = dvx;
    this.dvy = dvy;
    this.dvz = dvz;
    this.centerx = centerx;
    this.centery = centery;
    this.cost = cost;
    this.sound = sound;
    this.combo = combo;
    this.effect = effect;
    this.bdy = bdy;
    this.itr = itr;
    this.opoint = opoint;
    this.cpoint = cpoint;
    this.wpoint = wpoint;
  }

  public double calcVX(double v, boolean faceRight) {
    if (dvx == Act.FIX_POS)
      return 0.0;
    if (dvx == 0)
      return v;
    if (v == 0.0)
      return faceRight ? dvx : (-dvx);
    int aDvx = faceRight ? dvx : (-dvx);
    return ((aDvx < 0) == (v < 0.0)) ? ((aDvx < 0) ? (aDvx < v ? aDvx : v) : (aDvx > v ? aDvx : v)) : aDvx;
  }

  public double calcVY(double v) {
      return (dvy == DV_550) ? 0.0 : (v + dvy);
  }

  /* check if the input triggers combo
     if yes, then calls LFhero.tryCombo()
     return true if finally LFhero does the combo */
  public boolean inputCombo(final LFhero hero, final LFcontrol ctrl) {
    // TODO: Frizen defusion
    /* Rudolf transform has higher priority */
    if ((ctrl.combo == LFact.hit_ja) && (hero.origin != hero)) {
        hero.origin.initialization(hero.px, hero.py, hero.pz, LFhero.Act_transformback);
        LFX.currMap.transform(hero, hero.origin);
        return true;
    }
    int comboFrameNo;
    if (ctrl.combo != LFact.NOP && (comboFrameNo = comboList[ctrl.combo.index]) != NOP)
        return hero.tryCombo( ctrl.combo, hero.getFrame(comboFrameNo), comboFrameNo < 0);
    if (ctrl.do_d && (comboFrameNo = comboList[LFact.hit_d.index]) != NOP)
        return hero.tryCombo(LFact.hit_d, hero.getFrame(comboFrameNo), comboFrameNo < 0);
    if (ctrl.do_j && (comboFrameNo = comboList[LFact.hit_j.index]) != NOP)
        return hero.tryCombo(LFact.hit_j, hero.getFrame(comboFrameNo), comboFrameNo < 0);
    if (ctrl.do_a && (comboFrameNo = comboList[LFact.hit_a.index]) != NOP)
        return hero.tryCombo(LFact.hit_a, hero.getFrame(comboFrameNo), comboFrameNo < 0);
    return false;
  }

  static class Builder {
    private final Frame[] frame;
    public Image image1 = null;
    public Image image2 = null;
    public State state = null;
    public int curr = 0;
    public int wait = 0;
    public int next = 0;
    public int dvx = 0, dvy = 0, dvz = 0;
    public int centerx = 0, centery = 0;
    public int mpCost = 0;
    public int[] comboList = new int[10];
    public ArrayList<LFbdy> bdy = new ArrayList<>(4);
    public ArrayList<LFitr> itr = new ArrayList<>(4);
    public EnumMap<LFextra.Kind, LFextra> ext = new EnumMap<>(LFextra.Kind.class);
    public ArrayList<LFopoint> opoint = new ArrayList<>(2);
    public LFcpoint cpoint = null;
    public LFwpoint wpoint = null;
    public String sound = "";
    public double limit = 0.0;

    public Builder(Frame[] f, int c, Image i1, Image i2, State s,
            int w, int n, int dx, int dy, int dz, int cx, int cy) {
        frame = f;
        image1 = i1;
        image2 = i2;
        state = s;
        curr = c;
        wait = w;
        next = n;
        dvx = dx;
        dvy = dy;
        dvz = dz;
        centerx = cx;
        centery = cy;
        Arrays.fill(comboList, Frame.NOP);
    }

    public Builder add(LFbdy b) {
        bdy.add(b);
        return this;
    }

    public Builder add(LFitr i) {
        itr.add(i);
        return this;
    }

    public Builder add(LFextra.Kind k, LFextra e) {
        ext.put(k, e);
        return this;
    }

    public Builder opoint(int k, int xx, int yy, int dx, int dy, String i, int a, int f) {
        opoint.add(new LFopoint(k, xx, yy, dx, dy, i, a, f));
        return this;
    }

    public Builder opoint(int k, int xx, int yy, int dx, int dy, String i, int a, int f, double h) {
        opoint.add(new LFopoint(k, xx, yy, dx, dy, i, a, f, h));
        return this;
    }

    public Builder cpoint(int xx, int yy, int i,
                        int va, int ta, int aa, int ja,
                        int tx, int ty, int tz, int ti,
                        int cf, int dc, int d) {
        cpoint = new LFcpoint(xx, yy, i, va, ta, aa, ja, tx, ty, tz, ti, cf, dc, d);
        return this;
    }

    public Builder cpoint(int xx, int yy, int tx, int ty) {
        cpoint = new LFcpoint(xx, yy, tx, ty);
        return this;
    }

    public Builder wpoint(int xx, int yy, int wa, int a, int dx, int dy, int dz, int c) {
        wpoint = new LFwpoint(xx, yy, wa, a, dx, dy, dz, c);
        return this;
    }

    public Builder sound(String s) {
        sound = s;
        return this;
    }

    public Builder hit_a(int jumpto) {
        comboList[LFact.hit_a.index] = jumpto;
        return this;
    }

    public Builder hit_j(int jumpto) {
        comboList[LFact.hit_j.index] = jumpto;
        return this;
    }

    public Builder hit_d(int jumpto) {
        comboList[LFact.hit_d.index] = jumpto;
        return this;
    }

    public Builder hit_Ua(int jumpto) {
        comboList[LFact.hit_Ua.index] = jumpto;
        return this;
    }

    public Builder hit_Uj(int jumpto) {
        comboList[LFact.hit_Uj.index] = jumpto;
        return this;
    }

    public Builder hit_Fa(int jumpto) {
        comboList[LFact.hit_Fa.index] = jumpto;
        return this;
    }

    public Builder hit_Fj(int jumpto) {
        comboList[LFact.hit_Fj.index] = jumpto;
        return this;
    }

    public Builder hit_Da(int jumpto) {
        comboList[LFact.hit_Da.index] = jumpto;
        return this;
    }

    public Builder hit_Dj(int jumpto) {
        comboList[LFact.hit_Dj.index] = jumpto;
        return this;
    }

    public Builder hit_ja(int jumpto) {
        comboList[LFact.hit_ja.index] = jumpto;
        return this;
    }

    public Builder mp(int m) {
        mpCost = m;
        return this;
    }

    public Builder lim(double l) {
        limit = l;
        return this;
    }

    public void build() {
        bdy.trimToSize();
        itr.trimToSize();
        opoint.trimToSize();
        frame[curr] = new Frame(
            image1, image2, state, curr, wait, next,
            dvx, dvy, dvz, centerx, centery, mpCost, limit,
            comboList, bdy, itr, ext,
            opoint, cpoint, wpoint, sound);
        return;
    }

  }

}
