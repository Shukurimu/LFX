package lfx.component;

/**
 * Apparently there are several functionalities which can take effect over time.
 * For instance, John's healing effect regenerates target within 100 timeunits.
 * This class substitutes for specialized `state` and `next` as well.
 */

public enum Effect {
  MOVE_BLOCKING,
  ATTACK_SPUNCH,
  CREATE_ARMOUR,
  LANDING_ACT,
  TRANSFORM_INTO,
  TRANSFORM_BACK,
  TELEPORT_ENEMY,
  TELEPORT_TEAM,
  HEALING,
  INVISIBILITY,
  SONATA,
  LANDING_INJURY;

  @Override
  public String toString() {
    return String.format("%s.%s", this.getDeclaringClass().getSimpleName(), super.toString());
  }

  public static class Value {
    private int effectiveTime;
    public final int intValue;
    public final double doubleValue;
    public final String stringValue;

    private Value(int effectiveTime, int intValue, double doubleValue, String stringValue) {
      this.effectiveTime = effectiveTime;
      this.intValue = intValue;
      this.doubleValue = doubleValue;
      this.stringValue = stringValue;
    }

    /**
     * Reduces the effective time by 1 and checks its existence.
     *
     * @return true if no longer provide functionality
     */
    public boolean elapse() {
      return 0 > --effectiveTime;
    }

    /**
     * Calculates proper state of this Value.
     * It is usually used in frame transition, applying innate Effect.
     *
     * @param effect the target Effect of this Value
     * @param oldValue current Value, or null if not under specified Effect
     * @return final Value
     */
    public Value stack(Effect effect, Value oldValue) {
      if (oldValue == null) {
        return new Value(effectiveTime, intValue, doubleValue, stringValue);
      }
      if (oldValue.effectiveTime < effectiveTime) {
        oldValue.effectiveTime = effectiveTime;
      }
      return oldValue;
    }

    public static Value once() {
      return new Value(1, 0, 0.0, null);
    }

    public static Value once(int intValue) {
      return new Value(1, intValue, 0.0, null);
    }

    // TELEPORT_xxx
    public static Value once(double doubleValue) {
      return new Value(1, 0, doubleValue, null);
    }

    public static Value once(String stringValue) {
      return new Value(1, 0, 0.0, stringValue);
    }

    // INVISIBILITY
    public static Value last(int timeunit) {
      return new Value(timeunit, 0, 0.0, null);
    }

    // HEALING
    public static Value last(int timeunit, double doubleValue) {
      return new Value(timeunit, 0, doubleValue, null);
    }

    // LANDING_ACT
    public static Value until(int intValue) {
      return new Value(Integer.MAX_VALUE, intValue, 0.0, null);
    }

  }

}
