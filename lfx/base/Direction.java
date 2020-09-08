package lfx.base;

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
  };

  public abstract boolean getFacing(boolean origin);

  @Override
  public String toString() {
    return String.format("Direction.%s", name());
  }

}
