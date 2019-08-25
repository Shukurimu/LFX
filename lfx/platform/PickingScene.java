package lfx.platform;

import java.util.ArrayList;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.util.Duration;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import lfx.platform.Controller;
import lfx.platform.Graphical;
import lfx.util.Global;

public class PickingScene extends GridPane {
  public static final double ICON_SIZE = 180.0;
  public static final Font CARD_FONT = Font.font(24.0);
  public static final List<Color> COLOR_POOL = List.of(Color.BLACK, Color.BLUE);
  private final List<Graphical> candidateList;
  private final List<PickingCard> pickingList;
  private final Timeline render = new Timeline(new KeyFrame(new Duration(1000.0 / 8.0), e -> {
    ++animationTimestamp;
    pickingList.forEach(card -> card.animate());
  }));
  private int animationTimestamp = 0;

  private enum Phase {
    /** Condiction: all but not zero DONE. */
    INIT(0b11, false),
    HERO(0b01, true),
    TEAM(0b01, true),
    DONE(0b10, false);

    private static final int MAX_INDEX = Phase.values().length - 1;
    public final int readyBits;
    public final boolean choice;

    private Phase(int readyBits, boolean choice) {
      this.readyBits = readyBits;
      this.choice = choice;
    }

    public Phase prev() {
      return Phase.values()[Math.max(this.ordinal() - 1, 0)];
    }

    public Phase next() {
      return Phase.values()[Math.min(this.ordinal() + 1, MAX_INDEX)];
    }

    public static Map<Phase, Integer> getInitialSetting() {
      Map<Phase, Integer> setting = new EnumMap<>();
      for (Phase phase: Phase.values()) {
        if (phase.choice)
          setting.put(phase, 0)
      }
      return setting;
    }

  }

  private int getCurrentLength(Phase phase) {
    switch (phase) {
      case HERO:
        return candidateList.size();
      case TEAM:
        return Global.MAX_TEAMS;
      default:
        return 0;
    }
  }

  class PickingCard extends VBox {
    private Phase phase = Phase.INIT;
    private final Controller controller;
    private final Map<Phase, Integer> setting = Phase.getInitialSetting();
    private final Label name = new Label();
    private final Label hero = new Label();
    private final Label team = new Label();
    private final ImageView icon = new ImageView();

    public PickingCard(Controller controller) {
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

    public int keyInput(KeyCode c) {
      if (c == controller.code_d) {
        phase = Phase.INIT;
      } else if (c == controller.code_a) {
        phase = phase.next();
      } else if (c == controller.code_j) {
        phase = phase.prev();
      } else if (c == controller.code_L) {
        final int length = getCurrentLength(phase);
        setting.computeIfPresent(phase, (l_phase, l_value) -> l_value == 0 ? length - 1 : l_value - 1);
      } else if (c == controller.code_R) {
        final int length = getCurrentLength(phase);
        setting.computeIfPresent(phase, (l_phase, l_value) -> l_value == length - 1 ? 0 : l_value + 1);
      } else if (c == controller.code_U) {  // default
        setting.replace(phase, 0);
      } else if (c == controller.code_D) {  // random
        setting.replace(phase, Global.randomBounds(1, getCurrentLength(phase)));
      } else {
        return;
      }
      Graphical selection = candidateList.get(setting.get(Phase.HERO).intValue());
      switch (phase) {
        case INIT:
          icon.setImage(WAIT_FOR_JOIN);
          hero.setText("");
          break;
        case HERO:
          icon.setImage(candidateList.get(setting.get(Phase.HERO)));
          hero.setText(candidateList.get(setting.get(Phase.HERO)));
          team.setText("");
          break;
        case TEAM:
          team.setText(Global.TEAM_STRING.get(setting.get(Phase.TEAM)));
          break;
      }
      return readyBits();
    }

    public void animate() {
      switch (phase) {
        case INIT:
          break;
        case HERO:
          hero.setTextFill(COLOR_POOL.get(animationTimestamp & 1));
          break;
        case TEAM:
          hero.setTextFill(COLOR_POOL.get(0));
          team.setTextFill(COLOR_POOL.get(animationTimestamp & 1));
          break;
        case DONE:
          team.setTextFill(COLOR_POOL.get(0));
          break;
      }
      return;
    }

  }

  public PickingScene(List<Controller> controllerList, List<Graphical> candidateList) {
    this.candidateList = candidateList;
    pickingList = new ArrayList<>();
    for (Controller controller: controllerList) {
      PickingCard card = new PickingCard(controller);
      pickingList.add(card);
      this.addRow(1, card);
      // this.setVgrow(c, javafx.scene.layout.Priority.ALWAYS);
      // this.setHgrow(c, javafx.scene.layout.Priority.ALWAYS);
    }
    Label head = new Label("ALL PICK");
    head.setMinHeight(80.0);
    head.setFont(Font.font(null, FontWeight.BOLD, 52.0));
    this.add(head, 0, 0, controllerList.size(), 1);
    // this.setHgrow(head, javafx.scene.layout.Priority.ALWAYS);
    GridPane.setHalignment(head, HPos.CENTER);
    Label tail = new Label("xxxxxxxx");
    tail.setMinHeight(60.0);
    tail.setFont(Font.font(null, FontWeight.MEDIUM, 20.0));
    this.add(tail, 0, 2, controllerList.size(), 1);
    // this.setHgrow(tail, javafx.scene.layout.Priority.ALWAYS);
    GridPane.setHalignment(tail, HPos.RIGHT);

    // this.setAlignment(Pos.CENTER);
    this.setHgap(9.0);
    this.setVgap(16.0);
    // this.setPrefSize(LFmap.defaultWorldWidth, LFmap.SCENE_HEIGHT);

    // this.setPrefSize(javafx.scene.layout.Region.USE_COMPUTED_SIZE, javafx.scene.layout.Region.USE_COMPUTED_SIZE);
    // this.setMinSize(LFmap.defaultWorldWidth, LFmap.SCENE_HEIGHT);

    render.setCycleCount(Animation.INDEFINITE);
    render.play();
  }

  public Scene makeScene() {
    Scene scene = LFX.sceneBuilder(this, this.computePrefWidth(0), this.computePrefHeight(0));
    scene.setOnKeyPressed(e -> {
      KeyCode keyCode = e.getCode();
      if (keyCode == KeyCode.ESCAPE)
        javafx.application.Platform.exit();
      int ready = LFcard.NONE;
      for (LFcard c: cardArray)
        ready &= c.keyInput(keyCode);
      if (ready == LFcard.DONE) {
        render.stop();
        ArrayList<LFhero> a = new ArrayList<>(4);
        for (LFcard c: cardArray)
          a.add(c.makeHero());
        LFX.goToLFmap(a);
      }
    });
    return scene;
  }

}
