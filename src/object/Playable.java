package object;

import java.util.List;

public interface Playable {
  List<String> RANDOMABLE = List.of(
      "Deep", "John", "Henry", "Rudolf", "Louis",
      "Firen", "Freeze", "Dennis", "Woody", "Davis");

  String getPortraitPath();

  String getIdentifier();

  default void fillStatus(double[] placeholder) {}

  static Playable SELECTION_IDLE = new Playable() {

    @Override
    public String getPortraitPath() {
      return "";
    }

    @Override
    public String getIdentifier() {
      return "";
    }

  };

  static Playable SELECTION_RANDOM = new Playable() {

    @Override
    public String getPortraitPath() {
      return "";
    }

    @Override
    public String getIdentifier() {
      return "<RANDOM>";
    }

  };

}
