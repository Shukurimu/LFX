package lfx.platform;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lfx.base.Controller;
import lfx.game.Playable;
import lfx.map.Background;

public class PickingScene implements GuiScene {
  private final GridPane guiContainer = new GridPane();
  private final List<PlayerCardView> cardViewList = new ArrayList<>(4);

  private static class PlayerCardView extends PlayerCard {
    static final Font TEXT_FONT = Font.font(24.0);
    static final double ICON_SIZE = 180.0;
    private final VBox guiComponent;
    private final Label name = new Label();
    private final Label hero = new Label();
    private final Label team = new Label();
    private final ImageView icon = new ImageView();

    PlayerCardView(Controller controller) {
      super(controller);
      name.setFont(TEXT_FONT);
      icon.setFitWidth(ICON_SIZE);
      icon.setFitHeight(ICON_SIZE);
      hero.setFont(TEXT_FONT);
      team.setFont(TEXT_FONT);
      guiComponent = new VBox(/* spacing */ 6.0, name, icon, hero, team);
      guiComponent.setAlignment(Pos.CENTER);  // defaults to Pos.TOP_LEFT
      guiComponent.setMinHeight(300);
    }

    @Override
    public void update() {
      super.update();
      Playable playable = getPlayable();
      icon.setImage(playable.getPortrait().get());
      hero.setText(playable.getName());
      team.setText(getTeamText());
      return;
    }

  }

  public PickingScene(List<Controller> controllerList) {
    double columnWidth = WINDOW_WIDTH / controllerList.size();
    ColumnConstraints columnConstraints = new ColumnConstraints(columnWidth);
    for (Controller controller : controllerList) {
      PlayerCardView cardView = new PlayerCardView(controller);
      cardViewList.add(cardView);
      guiContainer.addRow(1, cardView.guiComponent);
      guiContainer.getColumnConstraints().add(columnConstraints);
      GridPane.setHalignment(cardView.guiComponent, HPos.CENTER);
    }

    Label head = new Label("ALL PICK");
    head.setMinHeight(80.0);
    head.setFont(Font.font(null, FontWeight.BOLD, 52.0));
    guiContainer.add(head, 0, 0, controllerList.size(), 1);
    GridPane.setHalignment(head, HPos.CENTER);

    Label tail = new Label("<untitled>");
    tail.setMinHeight(60.0);
    tail.setFont(Font.font(null, FontWeight.MEDIUM, 20.0));
    guiContainer.add(tail, 0, 2, controllerList.size(), 1);
    GridPane.setHalignment(tail, HPos.RIGHT);

    guiContainer.setHgap(0.0);
    guiContainer.setVgap(16.0);
  }

  @Override
  public Scene makeScene(Consumer<Scene> sceneChanger) {
    Scene scene = new Scene(guiContainer, WINDOW_WIDTH, WINDOW_HEIGHT);
    Consumer<Scene> pickingSceneBridge = (Scene nouse) -> {
      cardViewList.forEach(c -> c.reset());
      sceneChanger.accept(scene);
    };
    scene.setOnKeyPressed(event -> {
      KeyboardController.press(event.getCode());
      cardViewList.forEach(c -> c.update());
      if (PlayerCard.isReady(cardViewList)) {
        Background bg = new Background();
        VersusArena arena = new VersusArena(bg, PlayerCard.getHeroList(cardViewList));
        sceneChanger.accept(arena.makeScene(pickingSceneBridge));
      }
    });
    scene.setOnKeyReleased(event -> {
      KeyboardController.release(event.getCode());
    });
    // Force initialization to display proper contents.
    cardViewList.forEach(c -> c.update());
    return scene;
  }

}
