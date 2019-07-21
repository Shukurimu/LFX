package lfx.component;

public class Effect {
  /** The purpose of this class is to make special moves more functional.
      For instance, healing faster, landing to other than Frame94,
      or even applying posion attack (I have heard in some DC version). */
  public enum Kind {
    LANDING_ACT,
    TRANSFORM_TO,
    TRANSFORM_BACK,
    TELEPORT_ENEMY,
    TELEPORT_TEAM,
    HEALING,  // limited by potential hp
    REGENERATION,  // limited by max hp
    INVISIBILITY,
    SONATA,
    CREATE_ARMOUR,
    LANDING_INJURY,  // throw lying man
    MOVE_BLOCKING;

    @Override
    public String toString() {
      return String.format("%s.%s", this.getClass().getSimpleName(), super.toString());
    }

  }

  private int effectiveTime;
  public final int intValue;
  public final double doubleValue;
  public final String stringValue;

  public Effect(int effectiveTime, int intValue, double doubleValue, String stringValue) {
    this.effectiveTime = effectiveTime;
    this.intValue = intValue;
    this.doubleValue = doubleValue;
    this.stringValue = stringValue;
  }

  // e.g., INVISIBILITY
  public Effect(int effectiveTime) {
    this(effectiveTime, 0, 0.0, "");
  }

  // e.g., LANDING_ACT
  public Effect(int effectiveTime, int intValue) {
    this(effectiveTime, intValue, 0.0, "");
  }

  // e.g., HEALING
  public Effect(int effectiveTime, double doubleValue) {
    this(effectiveTime, 0, doubleValue, "");
  }

  // e.g., TRANSFORM
  public Effect(int effectiveTime, String stringValue) {
    this(effectiveTime, 0, 0.0, stringValue);
  }

  // e.g., TELEPORT
  public Effect() {
    this(0, 0, 0.0, "");
  }

  public Effect stackOn(Kind k, Effect e) {
    return this.clone();
  }

  /** The Effect will be removed when this method returns true.
      Called once per update tick.
      Set a negative effectiveTime for those condition-based actions (e.g., LANDING_ACT) */
  public boolean lapse() {
    return --effectiveTime == 0;
  }

  public static String parserState(int originalState) {
    switch (originalState) {
      case 9996:
        return "LFextra.Kind.ARMOUR, LFextra.oneTime()";
      case 1700:
        return "LFextra.Kind.HEALING, new LFextra(100, 1.0)";
      case 400:
        return "LFextra.Kind.TELEPORT_ENEMY, new LFextra(1, 120.0)";
      case 401:
        return "LFextra.Kind.TELEPORT_TEAM, new LFextra(1, 60.0)";
      case 100:
        return "LFextra.Kind.LANDING, new LFextra(-1, 94)";
      case 501:
        return "LFextra.Kind.TRANSFORM_BACK, LFextra.oneTime()";
      default:
        return null;
    }
  }

  public static String parserInvisibility(int invisibility) {
      return String.format("LFextra.Kind.INVISIBLE, new LFextra(%d)", invisibility);
  }

}
