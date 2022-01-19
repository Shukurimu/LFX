package platform;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

import ecosystem.Field;

public interface Screen {
  double SPECTATOR_HEIGHT = 60;
  double TEXT_INFO_HEIGHT = 20;
  double BATTLE_FIELD_HEIGHT = Field.FIELD_HEIGHT;
  double WINDOW_WIDTH = 794;
  double WINDOW_HEIGHT = SPECTATOR_HEIGHT + TEXT_INFO_HEIGHT * 2 + BATTLE_FIELD_HEIGHT;

  /**
   * Sets the previous {@code Scene}.
   *
   * @param previousScreen the {@code Screen} of previous {@code Scene}
   */
  void setPrevious(Screen previousScreen);

  /**
   * Changes the {@code Scene} to previous one.
   */
  void gotoPrevious();

  /**
   * Changes the {@code Scene} to the given one.
   *
   * @param nextScreen the {@code Screen} of next {@code Scene}
   */
  void gotoNext(Screen nextScreen);

  /**
   * Gets the {@code Scene} of thie {@code Screen}.
   *
   * @return a {@code Scene}
   */
  Scene getScene();

  default Scene sceneWrapper(Parent parent, double computePrefWidth, double computePrefHeight) {
    double width = 1600;
    double height = 900;
    parent.getTransforms().setAll(new Scale(width / computePrefWidth, height / computePrefHeight));
    return new Scene(parent, width, height, Color.BLACK);
  }

}
