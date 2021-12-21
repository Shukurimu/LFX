package object;

public interface Energy extends Observable {
  double CHASE_AX = 0.7;
  double CHASE_VXMAX = 14.0;
  double CHASE_VXOUT = 17.0;
  double CHASE_VY = 1.0;
  double CHASE_AZ = 0.4;
  double CHASE_VZMAX = 2.2;
  int DESTROY_TIME = 16;
  /**
   * Reflect itr results in about 8 TimeUnit vrest between creator and spawned
   * objects.
   * e.g., Bouncing ball between two John's shields.
   */
  int REFLECT_VREST = 8;

  @Override
  Energy makeClone();

  void rebound();

  void disperse();

  // https://www.lf-empire.de/lf2-empire/data-changing/reference-pages/185-id-properties
  // 209 - (energy)balls 200,203,205,206,207,215,216 turn into id: 209 frame 40 when hit
  // 219 - created by hit_Fa: 5, ratio chasing your team
  // 220 - created by hit_Fa: 6, jans ratio attack
  // 221 - created by hit_Fa: 9, firzens ratio attack
  // 222 - created by hit_Fa: 9, firzens ratio attack
  // 223 - no shadow, can't move on z-axis
  // 224 - no shadow, can't move on z-axis
  // 225 - created by hit_Fa: 8, bats ratio attack
  //
  // 300 - case sensitive bodys available (e.g.: kind: 1080 leads to frame 80 when hit)
  // 998 - frame 0 created upon DJDJ,
  //       frame 2 created upon DDDD,
  //       frame 4 created upon DADA,
  //       frame 6 created upon enemy joining your team
  // 999 - ice effect frame 120-123, 125-128, 130-133, 135-138, fire effect frame 140-143

}

