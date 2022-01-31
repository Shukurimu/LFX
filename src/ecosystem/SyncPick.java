package ecosystem;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Random;

import base.Vector;
import component.Action;
import component.Wpoint;

class SyncPick {
  private static final List<SyncPick> active = new ArrayList<>();
  private static final Random random = new Random(888L);
  private static final Wpoint INITIAL = Wpoint.hold(0, 0, Action.UNASSIGNED, 0);

  final Hero actor;
  final Weapon victim;
  final int initTimestamp;

  private Vector throwVelocity = Vector.ZERO;
  private Wpoint actorWpoint = INITIAL;

  private SyncPick(Hero actor, Weapon victim, int initTimestamp) {
    this.actor = actor;
    this.victim = victim;
    this.initTimestamp = initTimestamp;
  }

  /**
   * Deals with the race condition on picking.
   *
   * @param actor         the object performs the pick action
   * @param victim        the object will be picked
   * @param initTimestamp the initial action time
   * @return {@code true} if successed
   */
  static synchronized boolean tryRegister(Hero actor, Weapon victim, int initTimestamp) {
    for (ListIterator<SyncPick> it = active.listIterator(); it.hasNext();) {
      SyncPick s = it.next();
      if (s.victim == victim) {
        if (s.initTimestamp == initTimestamp) {
          return false;
        } else {
          it.remove();
          break;
        }
      }
    }
    active.add(new SyncPick(actor, victim, initTimestamp));
    return true;
  }

  static Optional<SyncPick> getPicker(Hero o) {
    return active.stream().filter(x -> x.actor == o).findFirst();
  }

  static Optional<SyncPick> getVictim(Weapon o) {
    return active.stream().filter(x -> x.victim == o).findFirst();
  }

  static void update() {
    for (ListIterator<SyncPick> it = active.listIterator(); it.hasNext(); ) {
      SyncPick s = it.next();
      if (s.victim.exists() && s.actor.exists() && s.validate()) {
        it.remove();
      }
    }
    return;
  }

  /**
   * Updates state and returns if still valid.
   *
   * @return {@code true} if invalid to be removed
   */
  private boolean validate() {
    actorWpoint = actor.getCurrentFrame().wpoint;
    if (actorWpoint == null || victim.getCurrentFrame().wpoint == null) {
      return true;
    }
    if (actorWpoint.usage == Wpoint.Usage.RELEASE) {
      double vx = 1.0 - random.nextDouble(2.0);
      double vy = 0.0 - random.nextDouble(2.0);
      throwVelocity = actor.getAbsoluteVelocity(Vector.of(vx, vy));
    }
    if (actorWpoint.velocity != Vector.ZERO) {
      throwVelocity = actor.getAbsoluteVelocity(actorWpoint.velocity);
    }
    return false;
  }

  boolean updateVictim(Wpoint victimWpoint) {
    victim.setRelativePosition(
      actor.getRelativePosition(actorWpoint),
      victimWpoint, actorWpoint.cover);
    return actor.isFaceRight();
  }

  Vector getThrowVelocity() {
    return throwVelocity;
  }

  boolean isAttacking() {
    return actorWpoint.usage.attacking;
  }

  Wpoint.Usage getUsage() {
    return actorWpoint.usage;
  }

  Action getVictimAction() {
    return actorWpoint.weaponact;
  }

  @Override
  public String toString() {
    return String.format("SyncPick[%s]", actorWpoint);
  }

}
