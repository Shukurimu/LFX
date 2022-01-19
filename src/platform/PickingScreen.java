package platform;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import base.Controller;
import ecosystem.Library;
import ecosystem.Playable;
import map.Background;

public class PickingScreen extends AbstractScreen {
  private static final Font TEXT_FONT = Font.font(24.0);
  private static final double ICON_SIZE = 180.0;

  private final List<DraftView> draftViewList;
  private final List<DraftCard> draftCardList;
  private final List<Controller> controllerList;
  private final Controller unionController;
  private final BooleanProperty pickingReady = new SimpleBooleanProperty();

  private static class DraftView extends VBox {
    final Text name = new Text("Player");
    final Text hero = new Text();
    final Text team = new Text();
    final ImageView icon = new ImageView();

    DraftView() {
      super(6.0);
      name.setFont(TEXT_FONT);
      hero.setFont(TEXT_FONT);
      team.setFont(TEXT_FONT);
      icon.setFitWidth(ICON_SIZE);
      icon.setFitHeight(ICON_SIZE);
      setAlignment(Pos.CENTER);
      setMinHeight(300);
      getChildren().addAll(name, icon, hero, team);
    }

    void update(DraftCard draftCard) {
      String identifier = draftCard.getPlayableIdentifier();
      hero.setText(identifier);
      team.setText(draftCard.getTeamText());
      icon.setImage(ResourceManager.getPortrait(identifier));
      return;
    }

  }

  public PickingScreen() {
    controllerList = ResourceManager.controllerList;
    unionController = Controller.union(controllerList);

    List<String> heroChoice = new ArrayList<>();
    heroChoice.add(Playable.SELECTION_RANDOM.getIdentifier());
    heroChoice.addAll(Library.getSelectable());
    draftCardList = controllerList.stream().map(c -> new DraftCard(c, heroChoice)).toList();
    draftViewList = Stream.generate(DraftView::new).limit(4).toList();
  }

  @Override
  public void keyHandler(KeyCode keyCode) {
    if (pickingReady.get()) {
      unionController.update();
      if (unionController.press_j()) {
        pickingReady.set(false);
        return;
      }
      if (!unionController.press_a()) {
        return;
      }
      Background bg = new Background();
      BattleScreen arena = new BattleScreen(bg);
      draftCardList.forEach(arena::addByDraftCard);
      gotoNext(arena);
    } else {
      for (int i = 0; i < 4; ++i) {
        DraftCard draftCard = draftCardList.get(i);
        draftCard.update();
        draftViewList.get(i).update(draftCard);
      }
      pickingReady.set(DraftCard.isReady(draftCardList));
    }
  }

  @Override
  protected Parent makeParent() {
    GridPane guiContainer = new GridPane();

    Text header = new Text("ALL PICK");
    header.setFont(Font.font(null, FontWeight.BOLD, 52.0));
    guiContainer.add(header, 0, 0, 4, 1);
    GridPane.setHalignment(header, HPos.CENTER);

    Text footer = new Text();
    footer.textProperty().bind(Bindings.convert(pickingReady));
    footer.setFont(Font.font(20.0));
    guiContainer.add(footer, 0, 2, 4, 1);
    GridPane.setHalignment(footer, HPos.RIGHT);

    ColumnConstraints columnConstraints = new ColumnConstraints(WINDOW_WIDTH / 4.0);
    for (DraftView view : draftViewList) {
      guiContainer.addRow(1, view);
      guiContainer.getColumnConstraints().add(columnConstraints);
      GridPane.setHalignment(view, HPos.CENTER);
    }

    guiContainer.setHgap(0.0);
    guiContainer.setVgap(16.0);
    return guiContainer;
  }

}
