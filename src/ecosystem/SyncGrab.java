package ecosystem;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import base.Vector;
import component.Action;
import component.Cpoint;

class SyncGrab {
  private static final List<SyncGrab> active = new ArrayList<>();

  /**
   * Intentionally drop the target.
   * 1. Countdown timer reaches zero.
   * 2. Perform transformation.
   */
  private static final Cpoint TIMEUP = Cpoint.doThrow(100, 100, Action.HERO_FACEUP_FALL2, 0, Vector.of(8, -2));

  final Observable actor;
  final Hero victim;
  final int initTimestamp;

  /**
   * From where the timer starts to countdown.
   * This value reduces by decrease field every timestamp.
   * Reaching zero is a reason to drop victim in most cases.
   */
  private int countdown = 305;
  private int injury = 0;
  private int throwInjury = 0;
  private Vector throwVelocity = Vector.ZERO;
  private Cpoint actorCpoint = null;

  private SyncGrab(Observable actor, Hero victim, int initTimestamp) {
    this.actor = actor;
    this.victim = victim;
    this.initTimestamp = initTimestamp;
  }

  /**
   * Deals with the race condition on grabbing.
   *
   * @param actor         the object performs the grab action
   * @param victim        the object will be grabbed
   * @param initTimestamp the initial action time
   * @return {@code true} if successed
   */
  static synchronized boolean tryRegister(Observable actor, Hero victim, int initTimestamp) {
    for (ListIterator<SyncGrab> it = active.listIterator(); it.hasNext();) {
      SyncGrab s = it.next();
      if (s.victim == victim) {
        if (s.initTimestamp == initTimestamp) {
          return false;
        } else {
          it.remove();
          break;
        }
      }
    }
    active.add(new SyncGrab(actor, victim, initTimestamp));
    return true;
  }

  static Optional<SyncGrab> getGrabber(Observable o) {
    return active.stream().filter(x -> x.actor == o && x.actorCpoint != TIMEUP).findFirst();
  }

  static Optional<SyncGrab> getVictim(Hero o) {
    return active.stream().filter(x -> x.victim == o).findFirst();
  }

  static void update() {
    for (ListIterator<SyncGrab> it = active.listIterator(); it.hasNext(); ) {
      SyncGrab s = it.next();
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
    if (!victim.exists() || !actor.exists()) {
      return true;
    }
    actorCpoint = actor.getCurrentFrame().cpoint;
    if (actorCpoint == null || victim.getCurrentFrame().cpoint == null) {
      return true;
    }
    if (actorCpoint.decrease > 0) {
      countdown -= actorCpoint.decrease;
      // Does not drop on positive decrease.
    } else {
      countdown += actorCpoint.decrease;
      if (countdown < 0) {
        actorCpoint = TIMEUP;
      }
    }
    if (actorCpoint.isThrowing()) {
      injury = 0;
      throwInjury = actorCpoint.injury;
      throwVelocity = actor.getAbsoluteVelocity(actorCpoint.velocity);
    } else {
      injury = actorCpoint.injury != 0 && actor.isFirstTimeunit() ? actorCpoint.injury : 0;
    }
    return false;
  }

  boolean updateVictim(Cpoint victimCpoint) {
    victim.setRelativePosition(
        actor.getRelativePosition(actorCpoint),
        victimCpoint, actorCpoint.cover);
    return actor.isFaceRight() ^ actorCpoint.opposideFacing;
  }

  int getInjury() {
    return injury;
  }

  int getThrowInjury() {
    return throwInjury;
  }

  Vector getThrowVelocity() {
    return throwVelocity;
  }

  Action getVictimAction() {
    return actorCpoint.vAction;
  }

  boolean isVictimHurtable() {
    return actorCpoint.hurtable;
  }

  @Override
  public String toString() {
    return String.format("SyncGrab[countdown=%d, %s]", countdown, actorCpoint);
  }

}
