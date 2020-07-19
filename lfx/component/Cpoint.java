package lfx.component;

import lfx.util.Const;
import lfx.util.Point;

public class Cpoint extends Point {
  public final int decrease;
  public final int vaction;
  public final int taction;
  public final int aaction;
  public final int jaction;
  public final int throwvx;
  public final int throwvy;
  public final int throwvz;
  public final int throwinjury;
  public final int injury;
  public final boolean dircontrol;
  public final boolean transform;
  public final boolean face2face;
  public final boolean cover;
  public final boolean hurtable;
  public final boolean throwing;
  public final int frontHurtAct;
  public final int backHurtAct;

  public static class Builder {
    private int x;
    private int y;
    private int decrease;
    private int vaction;
    private int taction = Const.NOP;
    private int aaction = Const.NOP;
    private int jaction = Const.NOP;
    private int throwvx;
    private int throwvy;
    private int throwvz;
    private int throwinjury;
    private int injury;
    private int cover;
    private boolean dircontrol;
    private boolean transform;
    private boolean hurtable;
    private boolean throwing;

    public Builder(int x, int y, int vaction, int decrease) {
      this.x = x;
      this.y = y;
      this.decrease = decrease;
      this.vaction = vaction;
    }

    public Builder taction(int taction) {
      this.taction = taction;
      return this;
    }

    public Builder aaction(int aaction) {
      this.aaction = aaction;
      return this;
    }

    public Builder jaction(int jaction) {
      this.jaction = jaction;
      return this;
    }

    public Builder doThrow(int throwvx, int throwvy, int throwvz, int throwinjury) {
      this.throwvx = throwvx;
      this.throwvy = throwvy;
      this.throwvz = throwvz;
      this.throwinjury = throwinjury;
      this.throwing = true;
      return this;
    }

    public Builder injury(int injury) {
      this.injury = injury;
      return this;
    }

    public Builder cover(int cover) {
      this.cover = cover;
      return this;
    }

    public Builder dircontrol() {
      dircontrol = true;
      return this;
    }

    public Builder transform() {
      transform = true;
      return this;
    }

    public Builder hurtable() {
      hurtable = true;
      return this;
    }

    public Cpoint build() {
      return new Cpoint(this);
    }

  }

  private Cpoint(Builder builder) {
    super(builder.x, builder.y);
    decrease = builder.decrease;
    vaction = builder.vaction;
    taction = builder.taction;
    aaction = builder.aaction;
    jaction = builder.jaction;
    throwvx = builder.throwvx;
    throwvy = builder.throwvy;
    throwvz = builder.throwvz;
    throwinjury = builder.throwinjury;
    injury = builder.injury;
    dircontrol = builder.dircontrol;
    transform = builder.transform;
    face2face = builder.cover < 10;
    cover = (builder.cover & 1) == 1;
    hurtable = builder.hurtable;
    throwing = builder.throwing;
    frontHurtAct = backHurtAct = 0;
  }

  public Cpoint(int x, int y, int frontHurtAct, int backHurtAct) {
    super(x, y);
    this.frontHurtAct = frontHurtAct;
    this.backHurtAct = backHurtAct;
    vaction = taction = aaction = jaction = Const.NOP;
    decrease = throwvx = throwvy = throwvz = throwinjury = injury = 0;
    dircontrol = transform = face2face = cover = hurtable = throwing = false;
  }

}
