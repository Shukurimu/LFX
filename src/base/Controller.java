package base;

public interface Controller {
  void consume();
  void update();
  Order getOrder();

  boolean press_U();
  boolean press_D();
  boolean press_L();
  boolean press_R();
  boolean press_a();
  boolean press_j();
  boolean press_d();
  boolean pressRun();

  default boolean pressWalk() {
    return pressX() || pressZ();
  }

  default boolean pressX() {
    return press_L() ^ press_R();
  }

  default boolean pressZ() {
    return press_U() ^ press_D();
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

}
