package lfx.component;

import lfx.util.Point;

public class Cpoint extends Point {
  public final Action vAction;
  public final Action tAction;
  public final Action aAction;
  public final Action jAction;
  public final int decrease;
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
  public final Action frontHurtAction;
  public final Action backHurtAction;

  private Cpoint(Builder builder) {
    super(builder.x, builder.y);
    vAction = builder.vAction;
    tAction = builder.tAction;
    aAction = builder.aAction;
    jAction = builder.jAction;
    decrease = builder.decrease;
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
    frontHurtAction = backHurtAction = Action.UNASSIGNED;
  }

  private Cpoint(int x, int y, Action frontHurtAction, Action backHurtAction) {
    super(x, y);
    this.frontHurtAction = frontHurtAction;
    this.backHurtAction = backHurtAction;
    vAction = tAction = aAction = jAction = Action.UNASSIGNED;
    decrease = throwvx = throwvy = throwvz = throwinjury = injury = 0;
    dircontrol = transform = face2face = cover = hurtable = throwing = false;
  }

  public static Cpoint grabee(int x, int y, Action frontHurtAction, Action backHurtAction) {
    return new Cpoint(x, y, frontHurtAction, backHurtAction);
  }

  public static Cpoint grabee(int x, int y, int fronthurtact, int backhurtact) {
    return new Cpoint(x, y, new Action(fronthurtact), new Action(backhurtact));
  }

  public static Builder graber(int x, int y, Action vAction, int decrease) {
    return new Builder(x, y, vAction, decrease);
  }

  public static Builder graber(int x, int y, int vaction, int decrease) {
    return new Builder(x, y, new Action(vaction), decrease);
  }

  public static class Builder {
    private int x;
    private int y;
    private Action vAction;
    private Action tAction = Action.UNASSIGNED;
    private Action aAction = Action.UNASSIGNED;
    private Action jAction = Action.UNASSIGNED;
    private int decrease;
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

    private Builder(int x, int y, Action vAction, int decrease) {
      this.x = x;
      this.y = y;
      this.vAction = vAction;
      this.decrease = decrease;
    }

    public Builder taction(Action tAction) {
      this.tAction = tAction;
      return this;
    }

    public Builder taction(int taction) {
      return this.taction(new Action(taction));
    }

    public Builder aaction(Action aAction) {
      this.aAction = aAction;
      return this;
    }

    public Builder aaction(int aaction) {
      return this.aaction(new Action(aaction));
    }

    public Builder jaction(Action jAction) {
      this.jAction = jAction;
      return this;
    }

    public Builder jaction(int jaction) {
      return this.jaction(new Action(jaction));
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

    public Builder hurtable() {
      hurtable = true;
      return this;
    }

    public Builder transform() {
      transform = true;
      return this;
    }

    Cpoint build() {
      return new Cpoint(this);
    }

  }

}
