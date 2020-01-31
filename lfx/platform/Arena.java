package lfx.platform;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.util.Duration;
import lfx.map.Environment;
import lfx.map.Field;
import lfx.map.Layer;
import lfx.map.StatusBoard;
import lfx.object.Hero;
import lfx.platform.KeyboardController;
import lfx.util.Const;

public final class Arena extends GridPane {
  private final Field field;
  private final List<Node> fxNodeList;
  private final List<Hero> tracingList = new ArrayList<>(4);
  private final List<StatusBoard> boardList = new ArrayList<>(4);
  private final Label middleText1 = new Label();
  private final Label middleText2 = new Label();
  private final Label bottomText1 = new Label();
  private final Label bottomText2 = new Label();
  private final ScrollPane scrollPane = new ScrollPane();
  private final Consumer<String> pickingSceneBridge;
  private final Timeline render;
  private double viewpoint = 0.0;

  public Arena(Field field, Consumer<String> pickingSceneBridge) {
    this.field = field;
    this.pickingSceneBridge = pickingSceneBridge;
    fxNodeList = field.getFxNodeList();
    render = new Timeline(new KeyFrame(new Duration(1000.0 / Const.DEFAULT_FPS), this::keyFrameHandler));
    double xwidth = field.getHeroXBound().get(0);
    viewpoint = xwidth / 2.0;

    middleText1.setTextFill(Color.AQUA);
    middleText1.setMinHeight(Const.TEXTLABEL_HEIGHT);
    GridPane.setHalignment(middleText1, HPos.LEFT);
    this.add(middleText1, 0, 1, 2, 1);

    middleText2.setTextFill(Color.VIOLET);
    middleText2.setMinHeight(Const.TEXTLABEL_HEIGHT);
    GridPane.setHalignment(middleText2, HPos.RIGHT);
    this.add(middleText2, 2, 1, 2, 1);

    // Currently support native background only.
    field.getScreenPane().getChildren().addAll(
        (new Layer("NativeBase", 0, 0, xwidth)).pic,
        (new Layer("NativePath", 0, 0, xwidth)).pic
    );

    scrollPane.setContent(field.getScreenPane());
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setHmax(xwidth - Const.FIELD_WIDTH);
    scrollPane.setVmax(0.0);
    this.add(scrollPane, 0, 2, 4, 1);

    bottomText1.setTextFill(Color.GOLD);
    bottomText1.setMinHeight(Const.TEXTLABEL_HEIGHT);
    GridPane.setHalignment(bottomText1, HPos.LEFT);
    this.add(bottomText1, 0, 3, 2, 1);

    bottomText2.setTextFill(Color.LIME);
    bottomText2.setMinHeight(Const.TEXTLABEL_HEIGHT);
    GridPane.setHalignment(bottomText2, HPos.RIGHT);
    this.add(bottomText2, 2, 3, 2, 1);
  }

  public void setPlayers(List<Hero> heroList) {
    for (Hero hero : heroList) {
      StatusBoard board = new StatusBoard(hero, hero.getPortrait());
      this.addRow(0, board);
      if (hero != null) {
        // TODO: initialization
        tracingList.add(hero);
        boardList.add(board);
        field.spawnHero(List.of(hero));
      }
    }
    return;
  }

  public Scene makeScene() {
    Scene scene = new Scene(this, Const.WINDOW_WIDTH, Const.WINDOW_HEIGHT);
    scene.setOnKeyPressed(this::keyPressHandler);
    scene.setOnKeyReleased(event -> KeyboardController.release(event.getCode()));
    render.setCycleCount(Animation.INDEFINITE);
    render.play();
    return scene;
  }

  private void keyFrameHandler(ActionEvent event) {
    field.stepOneFrame();

    boardList.forEach(board -> board.draw());
    viewpoint = field.calculateViewpoint(tracingList, viewpoint);
    scrollPane.setHvalue(viewpoint);

    middleText1.setText("MapTime: " + field.getTimestamp());
    bottomText1.setText("FxNode: " + fxNodeList.size());
    bottomText2.setText(String.format("ThreadName: %s   FxThread: %s",
                                      Thread.currentThread().getName(),
                                      javafx.application.Platform.isFxApplicationThread()));
    return;
  }

  private void keyPressHandler(KeyEvent event) {
    KeyCode code = event.getCode();
    if (KeyboardController.press(code)) {
      // No need to check other keys.
      return;
    }
    switch (code) {
      case F1:
        if (render.getCurrentRate() == 0.0) {
          render.play();
        } else {
          render.stop();
        }
        break;
      case F2:
        if (render.getCurrentRate() == 0.0) {
          keyFrameHandler(null);
        } else {
          render.stop();
        }
        break;
      case F3:
        System.out.println("Pressed no functionality key F3.");
        break;
      case F4:
        render.stop();
        pickingSceneBridge.accept("Finished");
        break;
      case F5:
        if (render.getCurrentRate() != 0.0) {
          render.setRate(render.getRate() == 1.0 ? 2.0 : 1.0);
        }
        break;
      case F6:
        middleText2.setText(field.switchUnlimitedMode() ? "[F6] Unlimited Mode" : "");
        break;
      case F7:
        field.reviveAll();
        break;
      case F8:
        field.dropNeutralWeapons();
        break;
      case F9:
        field.destroyWeapons();
        break;
      case F10:
        field.disperseEnergies();
        break;
      case ESCAPE:
        render.stop();
        javafx.application.Platform.exit();
        break;
    }
    return;
  }

}
