package platform;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;
import java.util.Map;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.util.Duration;
import game.field.AbstractField;
import game.Hero;
import game.Observable;
import map.Background;
import map.StatusBoard;
import map.Viewer;

public class VersusArena extends AbstractField implements GuiScene {
  private final GridPane guiContainer = new GridPane();
  private final List<Node> viewerList;
  private final List<Observable> tracingList = new ArrayList<>(8);
  private final List<StatusBoard> statusBoardList = new ArrayList<>(8);
  private final Label middleText1 = makeLabel(Color.AQUA, HPos.LEFT);
  private final Label middleText2 = makeLabel(Color.VIOLET, HPos.RIGHT);
  private final Label bottomText1 = makeLabel(Color.GOLD, HPos.LEFT);
  private final Label bottomText2 = makeLabel(Color.LIME, HPos.RIGHT);
  private final Timeline render;
  private double cameraPosition = 0.0;

  public VersusArena(Background bg, List<Hero> initialObjectList) {
    super(bg.width, bg.top, bg.bottom);
    for (Hero object : initialObjectList) {
      spawnObject(object);
      tracingList.add(object);
      statusBoardList.add(new StatusBoard(object));
    }
    tracingList.removeIf(o -> o == null);

    Pane objectLayer = new Pane();
    viewerList = objectLayer.getChildren();
    objectLayer.setViewOrder(-1.0);
    objectLayer.setMaxSize(FIELD_WIDTH, FIELD_HEIGHT);
    objectLayer.setMinSize(FIELD_WIDTH, FIELD_HEIGHT);

    Pane screenLayer = new Pane(objectLayer);
    screenLayer.getChildren().addAll(bg.elementList);
    screenLayer.setMaxSize(FIELD_WIDTH, FIELD_HEIGHT);
    screenLayer.setMinSize(FIELD_WIDTH, FIELD_HEIGHT);

    statusBoardList.forEach(s -> guiContainer.addRow(0, s));
    guiContainer.add(middleText1, 0, 1, 2, 1);
    guiContainer.add(middleText2, 2, 1, 2, 1);
    guiContainer.add(screenLayer, 0, 2, 4, 1);
    guiContainer.add(bottomText1, 0, 3, 2, 1);
    guiContainer.add(bottomText2, 2, 3, 2, 1);
    // TODO: rename Background
    guiContainer.setBackground(new javafx.scene.layout.Background(
        new javafx.scene.layout.BackgroundFill(Color.BLACK, null, null)
    ));
    render = new Timeline(new KeyFrame(new Duration(DEFAULT_MSPF), this::keyFrameHandler));
  }

  private static Label makeLabel(Color color, HPos hPos) {
    Label label = new Label();
    label.setTextFill(color);
    label.setMinHeight(TEXTLABEL_HEIGHT);
    GridPane.setHalignment(label, hPos);
    return label;
  }

  @Override
  protected List<Observable> retainExistingObjects(List<Observable> originalList) {
    List<Observable> removedItemList = super.retainExistingObjects(originalList);
    viewerList.removeIf(v -> removedItemList.contains(((Viewer) v).getObject()));
    return removedItemList;
  }

  @Override
  protected Map<Boolean, List<Observable>> partitionHeroItem(List<Observable> mixingList) {
    for (Observable object : mixingList) {
      viewerList.add(new Viewer(object));
    }
    return super.partitionHeroItem(mixingList);
  }

  private void keyFrameHandler(ActionEvent event) {
    stepOneFrame();
    cameraPosition = calcCameraPos(tracingList, cameraPosition);

    viewerList.forEach(n -> ((Viewer) n).update(cameraPosition));
    statusBoardList.forEach(s -> s.update());
    middleText1.setText("FxNode: " + viewerList.size());
    bottomText1.setText(
          String.format("MapTime %d   Camera %.2f", getTimestamp(), cameraPosition)
    );
    bottomText2.setText(
          String.format("(%s) %s",
                        javafx.application.Platform.isFxApplicationThread() ? "Fx" : "NonFx",
                        Thread.currentThread().getName()
          )
    );
    return;
  }

  @Override
  public Scene makeScene(Consumer<Scene> sceneChanger) {
    Scene scene = new Scene(guiContainer, WINDOW_WIDTH, WINDOW_HEIGHT);
    scene.setOnKeyPressed(event -> {
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
          System.out.println("GoBack by pressing F4.");
          sceneChanger.accept(null);
          break;
        case F5:
          // Reset rate if pressed while game paused.
          render.setRate(render.getCurrentRate() == 0.0 || render.getRate() == 2.0 ? 1.0 : 2.0);
          break;
        case F6:
          middleText2.setText(switchUnlimitedMode() ? "[F6] Unlimited Mode" : "");
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
          disperseEnergies();
          break;
      }
      return;
    });
    scene.setOnKeyReleased(event -> {
      KeyboardController.release(event.getCode());
    });
    render.setCycleCount(Animation.INDEFINITE);
    render.play();
    return scene;
  }

}
