package lfx.object;

import lfx.base.Scope;
import lfx.object.Observable;

public interface Energy extends Observable {
  int DEF_SCOPE = Scope.ITR_ENERGY;
  int ACT_FLYING = 0;
  int ACT_HITTING = 10;
  int ACT_HIT = 20;
  int ACT_REBOUND = 30;
  int ACT_DISAPPEAR = 40;

  double CHASE_AX = 0.7;
  double CHASE_VXMAX = 14.0;
  double CHASE_VXOUT = 17.0;
  double CHASE_VY = 1.0;
  double CHASE_AZ = 0.4;
  double CHASE_VZMAX = 2.2;
  int DESTROY_TIME = 16;
  /** Reflect itr results in about 8 TimeUnit vrest between creator and spawned objects.
      e.g., Bouncing ball between two John's shields. */
  int REFLECT_VREST = 8;

  /** Frame Attributes (TODO: use enum) */
  int FA_DENNIS_CHASE = 2;
  int FA_JOHN_DISK_CHASE = 1;
  int FA_JOHN_DISK_FAST = 10;

  void rebound();
  void disperse();

}

// https://www.lf-empire.de/en/lf2-empire/data-changing/reference-pages/183-effect-3-hitfa
