package platform;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import base.Controller;
import map.Background;
import object.Hero;
import object.Observable;
import util.Tuple;

public class VersusArena extends AbstractScreen {
  private static final System.Logger logger = System.getLogger("");

  private final List<StatusBoard> statusBoardList = new ArrayList<>(8);
  private final Text middleText1 = makeText(Color.AQUA, HPos.LEFT);
  private final Text middleText2 = makeText(Color.VIOLET, HPos.RIGHT);
  private final Text bottomText1 = makeText(Color.GOLD, HPos.LEFT);
  private final Text bottomText2 = makeText(Color.LIME, HPos.RIGHT);
  private final Timeline render;
  private final FieldScene field;
  private Hero focus = null;

  public VersusArena(Background bg) {
    field = new FieldScene(bg.width, bg.top, bg.bottom);
    // fieldGroup.getChildren().addAll(bg.elementList);
    render = new Timeline(new KeyFrame(new Duration(DEFAULT_MSPF), this::keyFrameHandler));
  }

  private static Text makeText(Color color, HPos hPos) {
    Text text = new Text();
    text.setFill(color);
    GridPane.setHalignment(text, hPos);
    return text;
  }

  public void addByPlayerCard(PlayerCard playerCard) {
    String identifier = playerCard.getPlayableIdentifier();
    Tuple<Observable, List<Image>> data = ResourceManager.library.get(identifier);
    StatusBoard board;
    if (data != null && data.first instanceof Hero x) {
      logger.log(Level.INFO, x);
      Hero clone = focus = (Hero) x.makeClone();
      board = StatusBoard.of(clone, ResourceManager.portraitLibrary.get(identifier));
      Controller controller = playerCard.controller;
      field.addPlayer(clone, controller);
    } else {
      board = StatusBoard.ofEmpty();
    }
    statusBoardList.add(board);
  }

  private void keyFrameHandler(ActionEvent event) {
    field.stepOneFrame();
    statusBoardList.forEach(s -> s.update());
    middleText2.setText(focus.getAbsolutePosition().toString());
    bottomText1.setText("FxNode: %d   MapTime %d".formatted(
        field.getObjectCount(), field.getTimestamp()));
    bottomText2.setText(
          String.format("(%s) %s",
                        javafx.application.Platform.isFxApplicationThread() ? "Fx" : "NonFx",
                        Thread.currentThread().getName()
          )
    );
    return;
  }

  @Override
  protected void keyHandler(KeyCode keyCode) {
    switch (keyCode) {
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
        field.switchUnlimitedMode();
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
      default:

    }
    return;
  };


  @Override
  protected Parent makeParent() {
    GridPane guiContainer = new GridPane();
    SubScene objectLayer = field.getScene();
    middleText1.textProperty().bind(Bindings.format("Camera %.2f", field.getCameraXProperty()));
    statusBoardList.forEach(s -> guiContainer.addRow(0, s));
    guiContainer.add(middleText1, 0, 1, 2, 1);
    guiContainer.add(middleText2, 2, 1, 2, 1);
    guiContainer.add(objectLayer, 0, 2, 4, 1);
    guiContainer.add(bottomText1, 0, 3, 2, 1);
    guiContainer.add(bottomText2, 2, 3, 2, 1);
    // TODO: rename Background
    guiContainer.setBackground(new javafx.scene.layout.Background(
        new javafx.scene.layout.BackgroundFill(Color.BLACK, null, null)
    ));

    render.setCycleCount(Animation.INDEFINITE);
    render.play();
    return guiContainer;
  }

}
