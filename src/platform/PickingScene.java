package platform;

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
import map.Background;

public class PickingScene extends AbstractScreen {
  private static final Font TEXT_FONT = Font.font(24.0);
  private static final double ICON_SIZE = 180.0;

  private final List<PlayerCardView> playerCardViewList;
  private final List<PlayerCard> playerCardList;
  private final List<Controller> controllerList;
  private final Controller unionController;
  private final BooleanProperty pickingReady = new SimpleBooleanProperty();

  private static class PlayerCardView extends VBox {
    final Text name = new Text("Player");
    final Text hero = new Text();
    final Text team = new Text();
    final ImageView icon = new ImageView();

    PlayerCardView() {
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

    void update(PlayerCard playerCard) {
      String identifier = playerCard.getPlayableIdentifier();
      hero.setText(identifier);
      team.setText(playerCard.getTeamText());
      icon.setImage(ResourceManager.portraitLibrary.get(identifier));
      return;
    }

  }

  public PickingScene() {
    controllerList = ResourceManager.controllerList;
    unionController = Controller.union(controllerList);

    List<String> heroChoice = List.copyOf(ResourceManager.portraitLibrary.keySet());
    playerCardList = controllerList.stream().map(c -> new PlayerCard(c, heroChoice)).toList();
    playerCardViewList = Stream.generate(PlayerCardView::new).limit(4).toList();
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
      System.out.println("Stop here");
      Background bg = new Background();
      VersusArena arena = new VersusArena(bg);
      for (PlayerCard playerCard : playerCardList) {
        arena.addByPlayerCard(playerCard);
      }
      AbstractScreen.sceneChanger.accept(arena.makeScene());
    } else {
      for (int i = 0; i < 4; ++i) {
        PlayerCard playerCard = playerCardList.get(i);
        playerCard.update();
        playerCardViewList.get(i).update(playerCard);
      }
      pickingReady.set(PlayerCard.isReady(playerCardList));
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
    footer.setFont(Font.font(null, FontWeight.MEDIUM, 20.0));
    guiContainer.add(footer, 0, 2, 4, 1);
    GridPane.setHalignment(footer, HPos.RIGHT);

    ColumnConstraints columnConstraints = new ColumnConstraints(WINDOW_WIDTH / 4.0);
    for (PlayerCardView view : playerCardViewList) {
      guiContainer.addRow(1, view);
      guiContainer.getColumnConstraints().add(columnConstraints);
      GridPane.setHalignment(view, HPos.CENTER);
    }

    guiContainer.setHgap(0.0);
    guiContainer.setVgap(16.0);
    return guiContainer;
  }

}
