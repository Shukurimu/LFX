package lfx.base;

import lfx.base.Order;

public interface Controller {
  void update();
  void consumeKeys();

  boolean press_U();
  boolean press_D();
  boolean press_L();
  boolean press_R();
  boolean press_a();
  boolean press_j();
  boolean press_d();
  boolean pressRun();
  boolean pressWalk();
  boolean pressX();
  boolean pressZ();
  double valueX();
  double valueZ();
  Direction getDirection();
  boolean getFacing(boolean originalFacing);
  boolean reverseFacing(boolean originalFacing);
  Order getOrder();

}
