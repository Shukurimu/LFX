package lfx.component;

import java.util.ArrayList;

public class Cpoint {
  public static final double TIMEUP_DVX = +8.0;
  public static final double TIMEUP_DVY = -3.0;
  public static final double DROP_DVY = -2.0;
  public static final int FRONTHURTACT = 221;
  public static final int BACKHURTACT  = 223;
  public static final int DIRCONTROL = 0b10000;
  public static final int TRANSFORM  = 0b01000;
  public static final int CHANGEDIR  = 0b00100;
  public static final int HURTABLE   = 0b00010;
  public static final int COVER      = 0b00001;

  public final int x;
  public final int y;
  public final int injury;
  public final int vaction;
  public final int taction;
  public final int aaction;
  public final int jaction;
  public final int throwvx;  // also graspee's fronthurtact
  public final int throwvy;  // also graspee's backhurtact
  public final int throwvz;
  public final int throwinjury;
  public final int decrease;
  public final boolean dircontrol;
  public final boolean transform;
  public final boolean changedir;
  public final boolean hurtable;
  public final boolean cover;
  public final boolean throwing;

  // grasper
  public Cpoint(int x, int y, int injury,
                int vaction, int taction, int aaction, int jaction,
                int throwvx, int throwvy, int throwvz, int throwinjury,
                int decrease, int meta) {
    this.x = x;
    this.y = y;
    this.injury = injury;
    this.vaction = vaction;
    this.taction = taction;
    this.aaction = aaction;
    this.jaction = jaction;
    this.throwvx = throwvx;
    this.throwvy = throwvy;
    this.throwvz = throwvz;
    this.throwinjury = throwinjury;
    this.decrease = decrease;
    this.dircontrol = (meta & DIRCONTROL) != 0;
    this.transform = (meta & TRANSFORM) != 0;
    this.changedir = (meta & CHANGEDIR) != 0;
    this.hurtable = (meta & HURTABLE) != 0;
    this.cover = (cf & COVER) != 0;
    this.throwing = throwvx != 0 || throwvy != 0 || throwvz != 0;
  }

  // graspee
  public Cpoint(int x, int y, int frontHurtAct, int backHurtAct) {
    this(x, y, 0,
         0, 0, 0, 0,
         frontHurtAct, backHurtAct, 0, 0,
         0, 0);
  }

  public static String[] parserCpoint(String hurtable, String cover, String throwinjury) {
    int injury = Integer.parseInt(throwinjury);
    int coverInt = Integer.parseInt(cover);
    ArrayList<> metaArray = ArrayList<String>(5);
    if ()
      metaArray.add("Cpoint.DIRCONTROL");
    if (injury == -1)
      metaArray.add("Cpoint.TRANSFORM");
    if (Integer.parseInt(hurtable) == 0)
      metaArray.add("Cpoint.HURTABLE");
    if (coverInt >= 10)
      metaArray.add("Cpoint.CHANGEDIR");
    if (coverInt % 10 == 0)
      metaArray.add("Cpoint.COVER");
    return new String[] { Integer.toString(Math.max(0, injury)), String.join(" | ", metaArray) };
  }

}
