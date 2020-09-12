package lfx.game;

public interface Energy extends Observable {

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

  @Override default boolean isHero() { return false; }
  @Override Energy makeClone();
  void rebound();
  void disperse();

}

// https://www.lf-empire.de/en/lf2-empire/data-changing/reference-pages/183-effect-3-hitfa
