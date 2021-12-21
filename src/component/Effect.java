package component;

/**
 * Apparently there are several functionalities which can take effect over time.
 * For instance, healing effect keeps regenerating target hero in 100 timeunits.
 * This class substitutes for specialized `state` and `next` as well.
 */
public enum Effect {
  // special itr-kind
  MOVE_BLOCKING,
  FORCE_SUPER_PUNCH,
  SONATA_UNFLIPPABLE,
  // special state oneshot
  REGENERATION,  // regen 8hp every 8tu over 100tu; the 1st 8hp is applied within 4tu.
  HEALING,  // always exists for 100 tu
  INVISIBLE,
  // throwinjury condition
  LANDING_INJURY;
  // GENERAL_SHIELD, // Julian*2
  // PLAIN_SHIELD, // Louis*1 Knight*2

}
