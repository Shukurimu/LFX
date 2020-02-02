package lfx.platform;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.function.Consumer;
import java.util.List;
import java.util.Map;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lfx.map.BaseMap;
import lfx.object.AbstractObject;
import lfx.object.Hero;
import lfx.platform.KeyboardController;
import lfx.util.Const;
import lfx.util.Controller;
import lfx.util.Input;
import lfx.util.Tuple;
import lfx.util.Util;

public class PickingScene extends GridPane {
  public static final double ICON_SIZE = 180.0;
  public static final Font CARD_FONT = Font.font(24.0);
  public static final List<Color> ANIMATION_COLOR_POOL = List.of(Color.BLACK, Color.BLUE);
  public static final Image IDLING_IMAGE;
  public static final Tuple<String, Image> RANDOM_CHOICE;
  private static final List<Tuple<String, Image>> heroList = new ArrayList<>(32);
  private final List<Card> cardList = new ArrayList<>();
  private final EventHandler<KeyEvent> keyPressHandler;
  private final Timeline render;
  private int animationTimestamp = 0;

  static {
    Canvas canvas = new Canvas(Const.PORTRAIT_SIZE, Const.PORTRAIT_SIZE);
    GraphicsContext gc = canvas.getGraphicsContext2D();
    IDLING_IMAGE = canvas.snapshot(null, null);

    gc.setFill(Color.BLACK);
    gc.fillRect(0.0, 0.0, Const.PORTRAIT_SIZE, Const.PORTRAIT_SIZE);
    gc.setTextBaseline(VPos.CENTER);
    gc.setTextAlign(TextAlignment.CENTER);
    gc.setFill(Color.WHITE);
    gc.setFont(Font.font(null, FontWeight.BOLD, Const.PORTRAIT_SIZE - 2.0 * 15.0));
    gc.fillText("?", Const.PORTRAIT_SIZE / 2.0, Const.PORTRAIT_SIZE / 2.0);
    RANDOM_CHOICE = new Tuple<>("RANDOM", canvas.snapshot(null, null));
  }

  private enum Phase {
    // Condition: all and at least one DONE.
    INIT(0b11, null),
    HERO(0b01, Integer.valueOf(0)),
    TEAM(0b01, Integer.valueOf(0)),
    DONE(0b10, null);

    private static final Phase[] valArray = values();
    public final int readyBits;
    public final Integer defaultValue;

    private Phase(int readyBits, Integer defaultValue) {
      this.readyBits = readyBits;
      this.defaultValue = defaultValue;
    }

    public Phase prev() {
      return valArray[Math.max(this.ordinal() - 1, 0)];
    }

    public Phase next() {
      return valArray[Math.min(this.ordinal() + 1, valArray.length + 1)];
    }

    public static Map<Phase, Integer> buildSetting() {
      Map<Phase, Integer> setting = new EnumMap<>(Phase.class);
      for (Phase phase : valArray) {
        if (phase.defaultValue != null) {
          setting.put(phase, phase.defaultValue);
        }
      }
      return setting;
    }

  }

  private int getItemCount(Phase phase) {
    switch (phase) {
      case HERO:
        return heroList.size();
      case TEAM:
        return Const.TEAM_NUM;
      default:
        return 0;
    }
  }

  private void refreshPlayableList() {
    heroList.clear();
    heroList.add(RANDOM_CHOICE);
    // TODO: hidden character
    for (Map.Entry<String, Hero> entry : AbstractObject.getHeroEntry()) {
      heroList.add(new Tuple<>(entry.getKey(), entry.getValue().getPortrait()));
    }
    return;
  }

  private class Card extends VBox {
    private Phase phase = Phase.INIT;
    private final Controller controller;
    private final Input input = new Input();
    private final Map<Phase, Integer> setting = Phase.buildSetting();
    private final Label name = new Label();
    private final Label hero = new Label();
    private final Label team = new Label();
    private final ImageView icon = new ImageView();

    public Card(Controller controller) {
      this.controller = controller;
      this.setAlignment(Pos.CENTER);
      this.setMinHeight(300);
      this.setSpacing(6.0);
      name.setFont(CARD_FONT);
      icon.setFitWidth(ICON_SIZE);
      icon.setFitHeight(ICON_SIZE);
      hero.setFont(CARD_FONT);
      team.setFont(CARD_FONT);
      this.getChildren().addAll(name, icon, hero, team);
    }

    public int getReadyBits() {
      controller.updateSimpleInput(input);
      if (input.do_a) {
        phase = phase.next();
      } else if (input.do_j) {
        phase = phase.prev();
      } else if (phase.defaultValue != null && input.do_L) {
        int length = getItemCount(phase);
        int origin = setting.get(phase);
        setting.put(phase, (origin - 1 + length) % length);
      } else if (phase.defaultValue != null && input.do_R) {
        int length = getItemCount(phase);
        int origin = setting.get(phase);
        setting.put(phase, (origin + 1 + length) % length);
      } else if (phase.defaultValue != null && input.do_U) {  // default
        setting.put(phase, 0);
      } else if (phase.defaultValue != null && input.do_D) {  // random
        setting.put(phase, Util.randomBounds(1, getItemCount(phase)));
      } else {
        return phase.readyBits;
      }
      Tuple<String, Image> focus = heroList.get(setting.get(Phase.HERO));
      switch (phase) {
        case INIT:
          icon.setImage(IDLING_IMAGE);
          hero.setText("");
          team.setText("");
          break;
        case HERO:
          icon.setImage(focus.second);
          hero.setText(focus.first);
          team.setText("");
          break;
        case TEAM:
          team.setText(Const.TEAM_NAMES.get(setting.get(Phase.TEAM)));
          break;
      }
      return phase.readyBits;
    }

    public void animate() {
      switch (phase) {
        case INIT:
          break;
        case HERO:
          hero.setTextFill(ANIMATION_COLOR_POOL.get(animationTimestamp & 1));
          break;
        case TEAM:
          hero.setTextFill(ANIMATION_COLOR_POOL.get(0));
          team.setTextFill(ANIMATION_COLOR_POOL.get(animationTimestamp & 1));
          break;
        case DONE:
          team.setTextFill(ANIMATION_COLOR_POOL.get(0));
          break;
      }
      return;
    }

    public Hero makeHero(int defaultTeamId) {
      if (phase == Phase.INIT) {
        return null;
      }
      int index = setting.get(Phase.HERO);
      if (index == 0) {
        index = Util.randomBounds(1, heroList.size());
      }
      String heroName = heroList.get(index).first;
      int teamId = setting.get(Phase.TEAM);
      teamId = teamId == 0 ? defaultTeamId : teamId;
      for (Map.Entry<String, Hero> entry : AbstractObject.getHeroEntry()) {
        if (heroName == entry.getKey()) {
          Hero clone = entry.getValue().makeClone(teamId, Util.randomBool());
          return clone;
        }
      }
      System.err.println("Hero not found: " + heroName);
      return null;
    }

  }

  public PickingScene(Consumer<Scene> sceneChanger, List<Controller> controllerList) {
    Consumer<String> pickingSceneBridge = (String info) -> {
      PickingScene scene = new PickingScene(sceneChanger, controllerList);
      sceneChanger.accept(scene.makeScene());
    };

    render = new Timeline(new KeyFrame(new Duration(1000.0 / 8.0), e -> {
      ++animationTimestamp;
      cardList.forEach(card -> card.animate());
    }));

    keyPressHandler = (KeyEvent event) -> {
      KeyCode keyCode = event.getCode();
      if (keyCode == KeyCode.ESCAPE) {
        Platform.exit();
        return;
      }
      KeyboardController.press(keyCode);
      int readyBits = Phase.INIT.readyBits;
      for (Card card : cardList) {
        readyBits &= card.getReadyBits();
      }
      if (readyBits == Phase.DONE.readyBits) {
        render.stop();
        List<Hero> playerList = new ArrayList<>(Const.PLAYER_NUM);
        for (int i = 0; i < cardList.size(); ++i) {
          playerList.add(cardList.get(i).makeHero(-i-1));
        }
        Arena arena = new Arena(new BaseMap(990, 200, 400), pickingSceneBridge);
        arena.setPlayers(playerList);
        sceneChanger.accept(arena.makeScene());
      }
    };

    refreshPlayableList();
    for (Controller controller : controllerList) {
      Card card = new Card(controller);
      cardList.add(card);
      this.addRow(1, card);
    }

    Label head = new Label("ALL PICK");
    head.setMinHeight(80.0);
    head.setFont(Font.font(null, FontWeight.BOLD, 52.0));
    GridPane.setHalignment(head, HPos.CENTER);
    this.add(head, 0, 0, controllerList.size(), 1);

    Label tail = new Label("<untitled>");
    tail.setMinHeight(60.0);
    tail.setFont(Font.font(null, FontWeight.MEDIUM, 20.0));
    GridPane.setHalignment(tail, HPos.RIGHT);
    this.add(tail, 0, 2, controllerList.size(), 1);

    this.setHgap(9.0);
    this.setVgap(16.0);
  }

  public Scene makeScene() {
    Scene scene = new Scene(this, Const.WINDOW_WIDTH, Const.WINDOW_HEIGHT);
    scene.setOnKeyPressed(keyPressHandler);
    scene.setOnKeyReleased(event -> KeyboardController.release(event.getCode()));
    render.setCycleCount(Animation.INDEFINITE);
    render.play();
    return scene;
  }

}
