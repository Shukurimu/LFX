package lfx.component;

import java.util.EnumSet;

public enum Extension {
  DRINK {
    @Override public Value of() {
      return Value.oneshot();
    }
  },
  LANDING_ACT {
    @Override public Value of(int actNumber) {
      return new Value(Integer.MAX_VALUE, actNumber, 0.0, null);
    }
  },
  TRANSFORM_TO {
    @Override public Value of(String identifier) {
      return new Value(0, 0, 0.0, identifier);
    }
  },
  TRANSFORM_BACK {
    @Override public Value of(String identifier) {
      return new Value(0, 0, 0.0, identifier);
    }
  },
  TELEPORT_ENEMY {
    @Override public Value of(double distance) {
      return new Value(0, 0, distance, null);
    }
  },
  TELEPORT_TEAM {
    @Override public Value of(double distance) {
      return new Value(0, 1, distance, null);
    }
  },
  HEALING {  // limited by potential hp
    @Override public Value of(int time, double rate) {
      return new Value(time, 0, rate, null);
    }
  },
  REGENERATION {  // limited by maximum hp
    @Override public Value of(int time, double rate) {
      return new Value(time, 0, rate, null);
    }
  },
  INVISIBILITY {
    @Override public Value of(int time) {
      return new Value(time, 0, 0.0, null);
    }
  },
  SONATA {
    @Override public Value of(int injury) {
      return new Value(0, injury, 0.0, null);
    }
  },
  CREATE_ARMOUR {
    @Override public Value of() {
      return Value.oneshot();
    }
  },
  LANDING_INJURY {  // throw-lying-man, frozen, sonata
    @Override public Value of(int injury) {
      return new Value(Integer.MAX_VALUE, injury, 0.0, null);
    }
  },
  ATTACK_SPUNCH {
    @Override public Value of() {
      return Value.oneshot();
    }
  },
  MOVE_BLOCKING {
    @Override public Value of() {
      return Value.oneshot();
    }
  };

  public Value of() {
    throw UnsupportedOperationException();
  }

  public Value of(int x) {
    throw UnsupportedOperationException();
  }

  public Value of(int x, int y) {
    throw UnsupportedOperationException();
  }

  public Value of(int x, double y) {
    throw UnsupportedOperationException();
  }

  public Value of(int x, String y) {
    throw UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return String.format("%s.%s", this.getDeclaringClass().getSimpleName(), super.toString());
  }

  public class Value {
    private int effectiveTime;
    public final int intValue;
    public final double doubleValue;
    public final String stringValue;

    Value(int effectiveTime, int intValue, double doubleValue, String stringValue) {
      this.effectiveTime = effectiveTime;
      this.intValue = intValue;
      this.doubleValue = doubleValue;
      this.stringValue = stringValue;
    }

    public static Value oneshot() {
      return new Value(0, 0, 0.0, null);
    }

    public boolean lapse() {
      return --effectiveTime < 0;
    }

  }

}
