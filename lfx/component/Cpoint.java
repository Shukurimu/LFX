package lfx.component;

import java.util.HashMap;
import java.util.Map;
import lfx.util.Const;
import lfx.util.Point;

public class Cpoint extends Point {
  public final int decrease;
  public final int vaction;
  public final int taction;
  public final int aaction;
  public final int jaction;
  public final int throwvx;  // also graspee's fronthurtact
  public final int throwvy;  // also graspee's backhurtact
  public final int throwvz;
  public final int throwinjury;
  public final int injury;
  public final boolean dircontrol;
  public final boolean transform;
  public final boolean face2face;
  public final boolean cover;
  public final boolean hurtable;
  public final boolean throwing;

  // grasper
  @SafeVarargs
  public Cpoint(int x, int y, int vaction, int decrease, String... metaArray) {
    super(x, y);
    this.decrease = decrease;
    this.vaction = vaction;
    Map<String, Integer> meta = new HashMap<>(16);
    for (String info : metaArray) {
      String[] kv = info.split(" ");
      meta.put(kv[0], Integer.valueOf(kv[1]));
    }
    taction = meta.getOrDefault("taction", Const.NOP);
    aaction = meta.getOrDefault("aaction", Const.NOP);
    jaction = meta.getOrDefault("jaction", Const.NOP);
    throwvx = meta.getOrDefault("throwvx", 0);
    throwvy = meta.getOrDefault("throwvy", 0);
    throwvz = meta.getOrDefault("throwvz", 0);
    throwinjury = meta.getOrDefault("throwinjury", 0);
    injury = meta.getOrDefault("injury", 0);
    dircontrol = meta.getOrDefault("dircontrol", 0) == 1;
    transform = meta.getOrDefault("transform", 0) == 1;
    int rawCover = meta.getOrDefault("cover", 0);
    face2face = rawCover < 10;
    cover = (rawCover & 1) == 1;
    hurtable = meta.getOrDefault("hurtable", 1) == 1;
    throwing = meta.getOrDefault("throwing", 0) == 1
               || throwvx != 0 || throwvy != 0 || throwvz != 0 || throwinjury != 0;
  }

  // graspee
  public Cpoint(int x, int y, int frontHurtAct, int backHurtAct) {
    super(x, y);
    throwvx = frontHurtAct;
    throwvy = backHurtAct;
    decrease = vaction = taction = aaction = jaction = throwvz = throwinjury = injury = 0;
    dircontrol = transform = face2face = cover = hurtable = throwing = false;
  }

}
