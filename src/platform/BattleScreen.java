package platform;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import base.Controller;
import ecosystem.Hero;
import ecosystem.Library;
import ecosystem.Observable;
import map.Background;

public class BattleScreen extends AbstractScreen {
  private static final System.Logger logger = System.getLogger("");
  public static final double DEFAULT_MSPF = 1000.0 / 30.0;  // DPS

  private final List<ViewerBoard> statusBoardList = new ArrayList<>(8);
  private final Text middleText1 = makeText(Color.AQUA, HPos.LEFT);
  private final Text middleText2 = makeText(Color.VIOLET, HPos.RIGHT);
  private final Text bottomText1 = makeText(Color.GOLD, HPos.LEFT);
  private final Text bottomText2 = makeText(Color.LIME, HPos.RIGHT);
  private final FieldScene field;
  private final Timeline render;
  private Hero focus = null;

  BattleScreen(Background bg) {
    field = new FieldScene(bg.width, bg.top, bg.bottom);
    // fieldGroup.getChildren().addAll(bg.elementList);
    middleText1.textProperty().bind(Bindings.format("Camera %.2f", field.getCameraXProperty()));
    render = new Timeline(new KeyFrame(new Duration(DEFAULT_MSPF), this::keyFrameHandler));
  }

  private static Text makeText(Color color, HPos hPos) {
    Text text = new Text();
    text.setFill(color);
    GridPane.setHalignment(text, hPos);
    GridPane.setValignment(text, VPos.CENTER);
    return text;
  }

  public void addByDraftCard(DraftCard draftCard) {
    String identifier = draftCard.getPlayableIdentifier();
    Optional<Observable> result = Library.getPrototype(identifier);
    ViewerBoard board;
    if (result.isPresent() && result.get() instanceof Hero x) {
      logger.log(Level.INFO, x);
      Hero clone = focus = (Hero) x.makeClone();
      board = ViewerBoard.of(clone, ResourceManager.getPortrait(identifier));
      Controller controller = draftCard.controller;
      field.addPlayer(clone, controller, draftCard.getTeamId());
    } else {
      board = ViewerBoard.ofEmpty();
    }
    statusBoardList.add(board);
  }

  private void keyFrameHandler(ActionEvent event) {
    field.stepOneFrame();
    statusBoardList.forEach(s -> s.update());
    middleText2.setText(focus.getAbsolutePosition().toString());
    bottomText1.setText("Time %d".formatted(field.getTimestamp()));
    bottomText2.setText("Node %d".formatted(field.getObjectCount()));
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
        gotoPrevious();
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
    statusBoardList.forEach(s -> guiContainer.addRow(0, s));
    guiContainer.add(middleText1, 0, 1, 2, 1);
    guiContainer.add(middleText2, 2, 1, 2, 1);
    guiContainer.add(field.getScene(), 0, 2, 4, 1);
    guiContainer.add(bottomText1, 0, 3, 2, 1);
    guiContainer.add(bottomText2, 2, 3, 2, 1);
    guiContainer.getRowConstraints().addAll(
      new RowConstraints(SPECTATOR_HEIGHT),
      new RowConstraints(TEXT_INFO_HEIGHT),
      new RowConstraints(BATTLE_FIELD_HEIGHT),
      new RowConstraints(TEXT_INFO_HEIGHT)
    );
    guiContainer.setBackground(new javafx.scene.layout.Background(
        new javafx.scene.layout.BackgroundFill(Color.BLACK, null, null)
    ));

    render.setCycleCount(Animation.INDEFINITE);
    render.play();
    return guiContainer;
  }

}
