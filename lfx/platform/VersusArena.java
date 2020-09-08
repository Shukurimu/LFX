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
import javafx.scene.Scene;
import javafx.util.Duration;
import lfx.map.Field;
import lfx.map.Layer;
import lfx.map.StatusBoard;
import lfx.game.Hero;
import lfx.util.Const;

public class VersusArena extends GridPane {
  public static final double MSPF = 1000.0 / Const.DEFAULT_FPS;
  private final Field field;
  private final List<Hero> tracingList;
  private final List<StatusBoard> boardList;
  private final ScrollPane scrollPane;
  private final Consumer<String> gobackLink;
  private final Timeline render;
  private final Label middleText1 = makeLabel(Color.AQUA, HPos.LEFT);
  private final Label middleText2 = makeLabel(Color.VIOLET, HPos.RIGHT);
  private final Label bottomText1 = makeLabel(Color.GOLD, HPos.LEFT);
  private final Label bottomText2 = makeLabel(Color.LIME, HPos.RIGHT);
  private double cameraPosition = 0.0;

  private Arena(Field field, List<Hero> tracingList, List<StatusBoard> boardList,
                ScrollPane scrollPane, Consumer<String> gobackLink) {
    this.field = field;
    this.tracingList = tracingList;
    this.boardList = boardList;
    this.scrollPane = scrollPane;
    this.gobackLink = gobackLink;
    render = new Timeline(new KeyFrame(new Duration(MSPF), this::keyFrameHandler));
    for (StatusBoard board : boardList) {
      this.addRow(0, board.getFxNode());
    }
    this.add(middleText1, 0, 1, 2, 1);
    this.add(middleText2, 2, 1, 2, 1);
    this.add(scrollPane,  0, 2, 4, 1);
    this.add(bottomText1, 0, 3, 2, 1);
    this.add(bottomText2, 2, 3, 2, 1);
  }

  private static Label makeLabel(Color color, HPos hPos) {
    Label label = new Label();
    label.setTextFill(color);
    label.setMinHeight(Const.TEXTLABEL_HEIGHT);
    GridPane.setHalignment(label, hPos);
    return label;
  }

  public static Arena build(Field field, List<Hero> heroList, Consumer<String> gobackLink) {
    List<Hero> tracingList = new ArrayList<>(4);
    List<StatusBoard> boardList = new ArrayList<>(4);
    for (Hero hero : heroList) {
      if (hero != null) {
        field.spawnObject(hero);
        tracingList.add(hero);
        boardList.add(new StatusBoard(hero));
      }
    }

    double xwidth = field.getBoundWidth();
    Pane screen = new Pane(
        field.getVisualNodePane(),
        // Currently support native background only.
        (new Layer("NativeBase", 0, 0, xwidth)).pic,
        (new Layer("NativePath", 0, 0, xwidth)).pic
    );
    screen.setMaxSize(Const.FIELD_WIDTH, Const.FIELD_HEIGHT);
    screen.setMinSize(Const.FIELD_WIDTH, Const.FIELD_HEIGHT);

    ScrollPane scrollPane = new ScrollPane(screen);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setHmax(xwidth - Const.FIELD_WIDTH);
    scrollPane.setVmax(0.0);

    return new Arena(field, tracingList, boardList, scrollPane, gobackLink);
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
    cameraPosition = Field.calcCameraPos(tracingList, cameraPosition);
    boardList.forEach(board -> board.draw());
    scrollPane.setHvalue(cameraPosition);

    middleText1.setText("MapTime: " + field.getTimestamp());
    bottomText1.setText("FxNode: " + fxNodeList.size());
    bottomText2.setText(
          String.format("ThreadName: %s   FxThread: %s",
                        Thread.currentThread().getName(),
                        javafx.application.Platform.isFxApplicationThread()
          )
    );
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
        gobackLink.accept("Go Back by pressing F4.");
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
    }
    return;
  }

}
