package lfx.component;

import java.util.EnumSet;

public enum Extension {
  DRINK,
  MOVE_BLOCKING,
  ATTACK_SPUNCH,
  LANDING_ACT,     // Value(Integer.MAX_VALUE, actNumber, 0.0, null);
  TRANSFORM_TO,    // Value(0, 0, 0.0, identifier);
  TRANSFORM_BACK,  // Value(0, 0, 0.0, identifier);
  TELEPORT_ENEMY,  // Value(0, 0, distance, null);
  TELEPORT_TEAM,   // Value(0, 1, distance, null);
  HEALING,         // Value(time, 0, rate, null);
  REGENERATION,    // Value(time, 0, rate, null);
  INVISIBILITY,    // Value(time, 0, 0.0, null);
  SONATA,          // Value(0, injury, 0.0, null);
  LANDING_INJURY,  // Value(Integer.MAX_VALUE, injury, 0.0, null);
  CREATE_ARMOUR;

  @Override
  public String toString() {
    return String.format("%s.%s", this.getDeclaringClass().getSimpleName(), super.toString());
  }

  public class Value {
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

    public static Value id(String stringValue) {
      return new Value(0, 0, 0.0, stringValue);
    }

    public static Value condition(int intValue) {
      return new Value(Integer.MAX_VALUE, intValue, 0.0, null);
    }

    public static Value oneshot() {
      return new Value(0, 0, 0.0, null);
    }

    public boolean lapse() {
      return --effectiveTime < 0;
    }

  }

}
