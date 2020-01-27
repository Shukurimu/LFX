package lfx.util;

import lfx.util.Input;

public interface Controller {
  void updateSimpleInput(Input input);
  void updateInput(Input input);
  void consumeKeys();

}
