package lfx.component;

import java.util.HashMap;
import java.util.Map;
// https://lf-empire.de/lf2-empire/data-changing/reference-pages/182-states?showall=1

public enum State {
  NORMAL(true),
  // Hero
  STAND (true),  // UDLFajd
  WALK  (true),  // UDLFajd
  RUN   (true),  // UDLFajd
  HEAVY_WALK(true),  // UDLFa
  HEAVY_RUN (true),  // UDLFa
  JUMP    (true),  // flyingFlag UDLF 
  DASH    (true),  // flyingFlag UDLF
  LAND    (true),  // flyingFlag ajd (crouch2)
  ROW     (true),  // flyingFlag UDLFj
  DRINK   (true),  // connected with weapon
  GRASP   (true),  // complicated cpoint
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

  public final boolean basicMove;

  private State(boolean basicMove) {
    this.basicMove = basicMove;
  }

  @Override
  public String toString() {
    return String.format("%s.%s", this.getDeclaringClass().getSimpleName(), super.toString());
  }

}
