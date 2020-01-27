package lfx.util;

import lfx.util.Util;

public enum Direction {
  SAME {
    @Override public boolean getFacing(boolean origin) {
      return origin;
    }
  },
  OPPOSITE {
    @Override public boolean getFacing(boolean origin) {
      return !origin;
    }
  },
  RIGHT {
    @Override public boolean getFacing(boolean origin) {
      return true;
    }
  },
  LEFT {
    @Override public boolean getFacing(boolean origin) {
      return false;
    }
  },
  RANDOM {
    @Override public boolean getFacing(boolean origin) {
      return Util.randomBool();
    }
  };

  public abstract boolean getFacing(boolean origin);

  @Override
  public String toString() {
    return String.format("%s.%s", this.getDeclaringClass().getSimpleName(), super.toString());
  }

}
