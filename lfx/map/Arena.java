package lfx.map;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.function.Consumer;
import java.util.List;
import java.util.Map;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.util.Duration;
import lfx.map.Environment;
import lfx.map.Layer;
import lfx.platform.KeyboardController;
import lfx.platform.StatusBoard;
import lfx.util.Const;
import lfx.util.Util;

public final class Field extends GridPane {
  private final Environment env;
  private final List<Node> fxNodeList;
  private final List<StatusBoard> boardList = new ArrayList<>(4);
  private final Label middleText1 = new Label();
  private final Label middleText2 = new Label();
  private final Label bottomText1 = new Label();
  private final Label bottomText2 = new Label();
  private final ScrollPane scrollPane = new ScrollPane();
  private final Consumer<String> pickingSceneBridge;
  private final Timeline render;
  private double viewport = 0.0;

  public Field(Environment env, Consumer<String> pickingSceneBridge) {
    this.env = env;
    this.pickingSceneBridge = pickingSceneBridge;
    viewport = boundWidth / 2.0;
    render = new Timeline(new KeyFrame(new Duration(1000.0 / Const.DEFAULT_FPS), this::step));

    middleText1.setTextFill(Color.AQUA);
    middleText1.setMinHeight(Const.TEXTLABEL_HEIGHT);
    GridPane.setHalignment(middleText1, HPos.LEFT);
    this.add(middleText1, 0, 1, 2, 1);

    middleText2.setTextFill(Color.VIOLET);
    middleText2.setMinHeight(Const.TEXTLABEL_HEIGHT);
    GridPane.setHalignment(middleText2, HPos.RIGHT);
    this.add(middleText2, 2, 1, 2, 1);

    List<Layer> layerElements = new ArrayList<>(10);
    layerElements.add(new Element("OriginalBack", 0, 0, (int)xwidth));
    layerElements.add(new Element("OriginalFront", 0, 0, (int)xwidth));

    Pane fxNodeLayer = new Pane();
    fxNodeList = fxNodeLayer.getChildren();
    Pane wrapper = new Pane();
    wrapper.setMaxSize(WIDTH, HEIGHT);
    wrapper.setMinSize(WIDTH, HEIGHT);
    ObservableList<Node> wrapperChildren = wrapper.getChildren();
    layerElements.forEach(e -> wrapperChildren.add(e.pic));
    wrapperChildren.add(fxNodeLayer);

    scrollPane.setContent​(wrapper);
    scrollPane.setMaxSize(WIDTH, HEIGHT);
    scrollPane.setMinSize(WIDTH, HEIGHT);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setHmax(boundWidth - WIDTH);
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
    // hero.initialize(boundWidth * 0.3 + Global.randomBounds(0.0, boundWidth), 0, getRandomZ(), act);
    for (Hero hero : heroList) {
      StatusBoard board = new StatusBoard(hero, hero.getPortrait());
      this.addRow(0, board.getFxNode());
      if (hero != null) {
        boardList.add(board);
        spawnHeros(List.of(hero));
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

  @Override
  protected void updateObservableList(List<Graphical> targetList, List<Graphical> targetQueue) {
    return;
  }

  private void step​(ActionEvent event) {
    step();

    // Camera movement policy is modified from F.LF.
    double position = 0.0;
    int weight = 0;
    for (StatusBoard board : boardList) {
      hero.updateViewport(board);
      board.draw();
      position += board.px;
      weight += board.faceRight ? 1 : -1;
    }
    position = weight * WIDTH_DIV24 + position / boardList.size() - WIDTH_DIV2;
    position = Util.clamp(position, boundWidth - WIDTH, 0.0);
    double speed = (position - viewport) * CAMERA_SPEED_FACTOR;
    viewport = Math.abs(speed) < CAMERA_SPEED_THRESHOLD ? position : (viewport + speed);
    scrollPane.setHvalue(viewport);

    middleText1.setText("MapTime: " + mapTime);
    middleText2.setText(unlimitedMode ? "[F6] Unlimited Mode" : "");
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
        if (render.getCurrentRate() == 0.0)
          render.play();
        else
          render.stop();
        break;
      case F2:
        if (render.getCurrentRate() == 0.0)
          this.handle(null);
        else
          render.stop();
        break;
      case F3:
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
        switchUnlimitedMode();
        break;
      case F7:
        reviveAll();
        break;
      case F8:
        dropNeutralWeapons();
        break;
      case F9:
        destroyWeapons();
        break;
      case F10:
        disperseBlasts();
        break;
      case ESCAPE:
        render.stop();
        javafx.application.Platform.exit();
        break;
    }
    return;
  }

}
