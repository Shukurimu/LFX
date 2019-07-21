package lfx.component;

final class LFdamage {
  public double center = 0.0;
  public double dvx = 0.0;
  public double dvy = 0.0;
  public int fall = 0;
  public int bdefend = 0;
  public int injury = 0;
  public boolean lag = false;
  public LFeffect effect = LFeffect.NONE;

  public void add(LFitrarea ia, LFbdyarea ba) {
    if (ia.itr.effect.causeLag) {
      lag = true;
      dvx += ia.calcDvx(ba);
      dvy += ia.itr.dvy;
      fall += ia.itr.fall;
      bdefend += ia.itr.bdefend;
    }
    injury += ia.itr.injury;
    effect = ia.itr.effect;
    center = ia.px;
    return;
  }

  public void reset() {
    center = dvx = dvy = 0.0;
    fall = bdefend = injury = 0;
    effect = LFeffect.NONE;
    lag = false;
    return;
  }

}
