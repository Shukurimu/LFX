package component;

/**
 * Apparently there are several functionalities which can take effect over time.
 * For instance, healing effect keeps regenerating target hero in 100 timeunits.
 * This class is introduced to serve those things, in addition to
 * some special {@code itr}, {@code state}, and {@code next} as well.
 */
public enum Effect {
  /** Results from {@code Itr.BLOCK}. */
  MOVE_BLOCKING,
  /** Results from {@code Itr.FORCE_ACTION}. */
  FORCE_SUPER_PUNCH,
  /** Results from {@code Itr.SONATA}. */
  SONATA_UNFLIPPABLE,
  /** Results in {@code Itr.THROWN_DAMAGE} and landing injury. */
  THROWN_ATTACK,
  /** Transformation from grabbing mechanism. */
  GRAB_TRANSFORM,
  /** Regenerates 1 hp on average.  Effect ends if hp reaches hp2nd. */
  REGENERATION,
  /** Same as REGENERATION but always last for full 100 timeunit. */
  HEALING,
  /** next: 1100 ~ 1200 in LF2. */
  INVISIBLE,
  /** Replenishable block non-element kind attack.  e.g., Louis & Knight. */
  STANDARD_SHIELD,
  /** Replenishable block all kind attack.  e.g., Julian. */
  OMNI_SHIELD;

  @Override
  public String toString() {
    return String.join(".", getDeclaringClass().getSimpleName(), name());
  }

}
