package component;

// https://lf-empire.de/lf2-empire/data-changing/reference-pages/182-states?showall=1

public enum State {
  NORMAL(true),
  // Hero
  STAND (true),  // UDLFajd
  WALK  (true),  // UDLFajd
  RUN   (true),  // UDLFajd
  HEAVY_WALK(true),  // UDLFa
  HEAVY_RUN (true),  // UDLFa
  JUMP    (false),  // flyingFlag UDLF
  DASH    (false),  // flyingFlag UDLF
  LANDING (false),  // flyingFlag ajd (crouch2)
  FLIP    (false),  // flyingFlag UDLFj
  DRINK   (true),  // connected with weapon
  GRAB    (true),  // complicated cpoint
  DEFEND  (true),  // goes to act 111 if hit
  FALL    (true),  // changes action accroding to vy
  FIRE    (true),  // changes action accroding to vy
  ICE     (true),  // one hit to fall; breaking or not depends on vxvy
  LYING   (true),  // loops in same frame if no hp
  TRY_TRANSFORM(true),  // goes to default action if failed
  // Weapon
  IN_THE_SKY    (true),
  ON_HAND       (true),
  THROWING      (true),
  ON_GROUND     (true),
  JUST_ON_GROUND(true),
  // Energy
  HITTING (true),
  HIT     (true),
  REBOUND (true),
  ENERGY  (true),
  PIERCE  (true),
  UNIMPLEMENTED(true);

  public final boolean resetFlying;

  private State(boolean resetFlying) {
    this.resetFlying = resetFlying;
  }

  @Override
  public String toString() {
    return String.format("State.%s", name());
  }

}
