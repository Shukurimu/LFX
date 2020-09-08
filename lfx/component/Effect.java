package lfx.component;

import java.util.Map;

/**
 * Apparently there are several functionalities which can take effect over time.
 * For instance, healing effect keeps regenerating target hero in 100 timeunits.
 * This class substitutes for specialized `state` and `next` as well.
 */
public enum Effect {
  MOVE_BLOCKING {
    @Override public Value of() {
      return new Value(this, 0, 0, 0.0);
    }
  },
  FORCE_ACT {
    @Override public Value of() {  // SuperPunch
      return new Value(this, 0, 70, 0.0);
    }
    @Override public Value of(int actNumber) {
      return new Value(this, 0, actNumber, 0.0);
    }
  },
  CREATE_ARMOUR {
    @Override public Value of() {
      return new Value(this, 0, 0, 0.0);
    }
  },
  TRANSFORM_INTO,
  TRANSFORM_BACK,
  TELEPORT_ENEMY {
    @Override public Value of() {
      return new Value(this, 0, 0, 120.0);
    }
  },
  TELEPORT_TEAM {
    @Override public Value of() {
      return new Value(this, 0, 0, 60.0);
    }
  },
  HEALING {
    @Override public Value of(int healAmount) {
      return new Value(this, 100, 0, healAmount / 100.0);
    }
    @Override public Value of(double healAmount, int overTimeunit) {
      return new Value(this, overTimeunit, 0, healAmount / overTimeunit);
    }
  },
  INVISIBLE {
    @Override public Value of(int invisibleTime) {
      return new Value(this, invisibleTime, 0, 0.0);
    }
  },
  UNFLIPPABLE {  // sonata
    @Override public Value of() {
      return new Value(this, Integer.MAX_VALUE, 0, 0.0);
    }
  },
  LANDING_ACT {
    @Override public Value of(int actNumber) {
      return new Value(this, Integer.MAX_VALUE, actNumber, 0.0);
    }
  },
  LANDING_INJURY {
    @Override public Value of(int injury) {
      return new Value(this, Integer.MAX_VALUE, injury, 0.0);
    }
  };

  public Value of() {
    throw new UnsupportedOperationException();
  }

  public Value of(int someValue) {
    throw new UnsupportedOperationException();
  }

  public Value of(double someValue, int moreValue) {
    throw new UnsupportedOperationException();
  }

  public static class Value implements Cloneable {
    public final Effect effect;
    public final int intValue;
    public final double doubleValue;
    private int effectiveTime;

    public Value(Effect effect, int effectiveTime, int intValue, double doubleValue) {
      this.effect = effect;
      this.intValue = intValue;
      this.doubleValue = doubleValue;
      this.effectiveTime = effectiveTime;
    }

    /**
     * Reduces the effective time by 1 and checks its existence.
     *
     * @return  true if no longer provide functionality
     */
    public boolean elapse() {
      return 0 > --effectiveTime;
    }

    /**
     * Calculates proper state of this Value.
     * It is usually used in frame transition, applying innate Effect.
     *
     * @param   effectStatus
     *          the target map would like to be updated
     */
    public void stack(Map<Effect, Value> effectStatus) {
      Value oldValue = effectStatus.get(effect);
      if (oldValue == null) {
        effectStatus.put(effect, this.clone());
      } else if (oldValue.effectiveTime < effectiveTime) {
        oldValue.effectiveTime = effectiveTime;
      }
      return;
    }

    @Override
    public Value clone() {
      try {
        return (Value) super.clone();
      } catch (CloneNotSupportedException ex) {
        ex.printStackTrace();
        return null;
      }
    }

  }

}
