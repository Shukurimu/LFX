package lfx.platform;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lfx.base.Controller;
// import lfx.map.BaseMap;
import lfx.game.Playable;

public class PickingScene extends GuiScene {
  private final GridPane guiContainer;
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
      guiComponent.setAlignment(Pos.CENTER);
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
    guiContainer = new GridPane();
    for (Controller controller : controllerList) {
      PlayerCardView cardView = new PlayerCardView(controller);
      cardViewList.add(cardView);
      guiContainer.addRow(1, cardView.guiComponent);
    }

    Label head = new Label("ALL PICK");
    head.setMinHeight(80.0);
    head.setFont(Font.font(null, FontWeight.BOLD, 52.0));
    GridPane.setHalignment(head, HPos.CENTER);
    guiContainer.add(head, 0, 0, controllerList.size(), 1);

    Label tail = new Label("<untitled>");
    tail.setMinHeight(60.0);
    tail.setFont(Font.font(null, FontWeight.MEDIUM, 20.0));
    GridPane.setHalignment(tail, HPos.RIGHT);
    guiContainer.add(tail, 0, 2, controllerList.size(), 1);

    guiContainer.setHgap(9.0);
    guiContainer.setVgap(16.0);
  }

  @Override
  public Scene makeScene(Consumer<Scene> sceneChanger) {
    Scene scene = new Scene(guiContainer, WINDOW_WIDTH, WINDOW_HEIGHT);
    Consumer<String> pickingSceneBridge = (String info) -> {
      System.out.println(info);
      cardViewList.forEach(c -> c.reset());
      sceneChanger.accept(scene);
    };
    scene.setOnKeyPressed(event -> {
      KeyboardController.press(event.getCode());
      cardViewList.forEach(c -> c.update());
      if (PlayerCard.isReady(cardViewList)) {
        // c.makeHero();
        // Arena arena = new Arena(new BaseMap(990, 200, 400));
        // arena.setPlayers(playerList);
        // sceneChanger.accept(arena.makeScene(pickingSceneBridge));
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
