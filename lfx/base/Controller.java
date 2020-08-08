package lfx.base;

import lfx.base.Order;

public interface Controller {
  void update();
  void consumeKeys();
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
    return pressX() | pressZ();
  }

  default boolean pressX() {
    return press_L() ^ press_R();
  }

  default boolean pressZ() {
    return press_U() ^ press_D();
  }

  default double valueX() {
    return pressX() ? (press_R() ? 1.0 : -1.0) : 0.0;
  }

  default double valueZ() {
    return pressZ() ? (press_D() ? 1.0 : -1.0) : 0.0;
  }

  default boolean getFacing(boolean originalFacing) {
    return pressX() ? press_R() : originalFacing;
  }

  default boolean reverseFacing(boolean originalFacing) {
    return pressX() && (press_R() != originalFacing);
  }

}
