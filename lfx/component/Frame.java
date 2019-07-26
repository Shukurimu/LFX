package lfx.component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lfx.component.Cpoint;
import lfx.component.Effect;
import lfx.component.Opoint;
import lfx.component.State;
import lfx.component.Wpoint;
import lfx.util.Act;

public class Frame {
  public final int pic;
  public final State state;
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

  public Frame(int pic, State state, int wait, int next,
               int dvx, int dvy, int dvz, int centerx, int centery, int cost, String sound,
               Map<Act, Integer> combo, Map<Effect.Kind, Effect> effect,
               List<Bdy> bdy, List<Itr> itr, List<Opoint> opoint, Cpoint cpoint, Wpoint wpoint) {
    this.pic = pic;
    this.state = state;
    this.wait = wait;
    this.next = next;
    this.dvx = dvx;
    this.dvy = dvy;
    this.dvz = dvz;
    this.centerx = centerx;
    this.centery = centery;
    this.cost = cost;
    this.sound = sound;
    this.combo = Map.copyOf(combo);
    this.effect = Map.copyOf(effect);
    this.bdy = List.copyOf(bdy);
    this.itr = List.copyOf(itr);
    this.opoint = List.copyOf(opoint);
    this.cpoint = cpoint;
    this.wpoint = wpoint;
  }

  public double calcVX(double vx, boolean faceRight) {
    if (dvx == Act.FIX_POS)
      return 0.0;
    if (dvx == 0)
      return vx;
    // same direction results in larger magnitude
    double absDvx = faceRight ? dvx : -dvx;
    return absDvx < 0 ? (vx < 0.0 ? Math.min(absDvx, vx) : absDvx)
                      : (vx > 0.0 ? Math.max(absDvx, vx) : absDvx);
  }

  public double calcVY(double vy) {
      return (dvy == Act.FIX_POS) ? 0.0 : (vy + dvy);
  }

  /** Check if the input triggers combo.
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
    public int pic;
    public State state;
    public int wait;
    public int next;
    public int dvx;
    public int dvy;
    public int dvz;
    public int centerx;
    public int centery;
    public int cost = 0;
    public String sound = null;
    public Map<Act, Integer> combo = new EnumMap<>();
    public Map<Effect.Kind, Effect> effect = new EnumMap<>();
    public List<Bdy> bdy = new ArrayList<>();
    public List<Itr> itr = new ArrayList<>();
    public List<Opoint> opoint = new ArrayList<>();
    public Cpoint cpoint = null;
    public Wpoint wpoint = null;

    public Builder(int pic, State state, int wait, int next,
                   int dvx, int dvy, int dvz, int centerx, int centery) {
      this.pic = pic;
      this.state = state;
      this.wait = wait;
      this.next = next;
      this.dvx = dvx;
      this.dvy = dvy;
      this.dvz = dvz;
      this.centerx = centerx;
      this.centery = centery;
    }

    public Builder set(Act act, int actNumber) {
      combo.put(act, actNumber);
      return this;
    }

    public Builder set(Effect.Kind kind, Effect e) {
      effect.put(kind, e);
      return this;
    }

    public Builder add(Bdy b) {
      bdy.add(b);
      return this;
    }

    public Builder add(Itr i) {
      itr.add(i);
      return this;
    }

    public Builder add(Opoint o) {
      opoint.add(o);
      return this;
    }

    public Builder set(Cpoint c) {
      cpoint = c;
      return this;
    }

    public Builder set(Wpoint w) {
      wpoint = w;
      return this;
    }

    public Builder set(String s) {
      sound = s;
      return this;
    }

    public Builder mp(int c) {
      cost = c;
      return this;
    }

    public Frame build() {
      return new Frame(pic, state, wait, next,
                       dvx, dvy, dvz, centerx, centery, cost, sound,
                       combo, effect, bdy, itr, opoint, cpoint, wpoint);
    }

  }

}
