package lfx.base;

import lfx.base.Input;

public interface Controller {
  void updateSimpleInput(Input input);
  void updateInput(Input input);
  void consumeKeys();

}
