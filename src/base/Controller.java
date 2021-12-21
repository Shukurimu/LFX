package base;

public interface Controller {
  void consume();
  void update();
  KeyOrder getKeyOrder();

  boolean press_U();
  boolean press_D();
  boolean press_L();
  boolean press_R();
  boolean press_a();
  boolean press_j();
  boolean press_d();
  boolean pressRun();

  default boolean pressX() {
    return press_L() ^ press_R();
  }

  default boolean pressZ() {
    return press_U() ^ press_D();
  }

  default boolean pressWalk() {
    return pressX() || pressZ();
  }

  default double valueX() {
    return press_L() ? (press_R() ? 0.0 : -1.0) : (press_R() ? 1.0 : 0.0);
  }

  default double valueZ() {
    return press_U() ? (press_D() ? 0.0 : -1.0) : (press_D() ? 1.0 : 0.0);
  }

  default boolean getFacing(boolean originalFacing) {
    return press_L() ? (press_R() && originalFacing)
                     : (press_R() || originalFacing);
  }

  default boolean reverseFacing(boolean originalFacing) {
    return press_L() ? (!press_R() && originalFacing)
                     : (press_R() && !originalFacing);
  }

  Controller NULL_CONTROLLER = new Controller() {
    @Override public void consume() {}
    @Override public void update() {}
    @Override public KeyOrder getKeyOrder() { return null; }
    @Override public boolean press_U() { return false; }
    @Override public boolean press_D() { return false; }
    @Override public boolean press_L() { return false; }
    @Override public boolean press_R() { return false; }
    @Override public boolean press_a() { return false; }
    @Override public boolean press_j() { return false; }
    @Override public boolean press_d() { return false; }
    @Override public boolean pressRun() { return false; }
  };

}
