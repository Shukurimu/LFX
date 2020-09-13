package lfx.game;

import lfx.util.ImageCell;

public interface Playable {
  ImageCell getPortrait();
  String getName();

  static Playable SELECTION_IDLE = new Playable() {
    @Override public ImageCell getPortrait() {
      return ImageCell.getSelectionIdleImage();
    }
    @Override public String getName() {
      return "";
    }
  };

  static Playable SELECTION_RANDOM = new Playable() {
    @Override public ImageCell getPortrait() {
      return ImageCell.getSelectionRandomImage();
    }
    @Override public String getName() {
      return "<RANDOM>";
    }
  };

}
