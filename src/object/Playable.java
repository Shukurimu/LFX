package object;

import java.util.List;

public interface Playable {
  List<String> RANDOMABLE = List.of(
      "Deep", "John", "Henry", "Rudolf", "Louis",
      "Firen", "Freeze", "Dennis", "Woody", "Davis");

  String getPortrait();

  String getIdentifier();

  default boolean active() {
    return true;
  }

  default void fillStatus(double[] placeholder) {}

  static Playable SELECTION_IDLE = new Playable() {

    @Override
    public String getPortrait() {
      return null;// ImageCell.getSelectionIdleImage();
    }

    @Override
    public String getIdentifier() {
      return "";
    }

    @Override
    public boolean active() {
      return false;
    }

  };

  static Playable SELECTION_RANDOM = new Playable() {

    @Override
    public String getPortrait() {
      return null;// ImageCell.getSelectionRandomImage();
    }

    @Override
    public String getIdentifier() {
      return "<RANDOM>";
    }

  };

}
