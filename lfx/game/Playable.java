package lfx.game;

import lfx.util.ImageCell;

public interface Playable {
  ImageCell getPortrait();
  String getName();

  static Playable SELECTION_IDLE = new Playable() {
    @Override public ImageCell getPortrait() {
      return ImageCell.SELECTION_IDLE_IMAGE;
    }
    @Override public String getName() {
      return "";
    }
  };

  static Playable SELECTION_RANDOM = new Playable() {
    @Override public ImageCell getPortrait() {
      return ImageCell.SELECTION_RANDOM_IMAGE;
    }
    @Override public String getName() {
      return "<RANDOM>";
    }
  };

}