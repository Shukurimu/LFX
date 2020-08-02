package lfx.base;

import lfx.base.Direction;
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
  boolean pressWalkX();
  boolean pressWalkZ();
  boolean pressWalk();
  Direction getDirection();
  Order getOrder();

}
