package lfx.platform;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lfx.base.Controller;
import lfx.game.Hero;
import lfx.game.Library;
import lfx.game.Playable;
import lfx.util.ElementSelector;
import lfx.util.Selectable;

public class PlayerCard {
  private static final Map<Integer, String> TEAM_TEXT = Map.of(
      0, "Independent",
      1, "Team 1",
      2, "Team 2",
      3, "Team 3",
      4, "Team 4"
  );
  private final Controller controller;
  private final Selectable<PickingPhase> pickingPhaseSelector =
      new ElementSelector<>(false, PickingPhase.values());
  private final Selectable<Playable> playableSelector = new PlayableSelector();
  private final Selectable<Integer> teamIdSelector = new ElementSelector<>(0, 1, 2, 3, 4);
  private final Map<PickingPhase, Selectable<?>> settings = new EnumMap<>(PickingPhase.class);

  protected PlayerCard(Controller controller) {
    this.controller = controller;
    settings.put(PickingPhase.SELECTING_HERO, playableSelector);
    settings.put(PickingPhase.SELECTING_TEAM, teamIdSelector);
  }

  protected Playable getPlayable() {
    return pickingPhaseSelector.get() == PickingPhase.UNASSIGNED ?
        Playable.SELECTION_IDLE : playableSelector.get();
  }

  protected String getTeamText() {
    return pickingPhaseSelector.get().displayTeamText ? TEAM_TEXT.get(teamIdSelector.get()) : "";
  }

  public void reset() {
    pickingPhaseSelector.setDefault();
    return;
  }

  protected void update() {
    controller.update();
    if (controller.press_a()) {
      pickingPhaseSelector.setNext();
    } else if (controller.press_j()) {
      pickingPhaseSelector.setPrevious();
    } else if (controller.press_L()) {
      settings.getOrDefault(pickingPhaseSelector.get(), Selectable.EMPTY).setPrevious();
    } else if (controller.press_R()) {
      settings.getOrDefault(pickingPhaseSelector.get(), Selectable.EMPTY).setNext();
    } else if (controller.press_U()) {
      settings.getOrDefault(pickingPhaseSelector.get(), Selectable.EMPTY).setDefault();
    } else {
      return;
    }
    controller.consumeKeys();
    return;
  }

  public static boolean isReady(Iterable<? extends PlayerCard> playerCards) {
    int playerCount = 0;
    for (PlayerCard card : playerCards) {
      PickingPhase phase = card.pickingPhaseSelector.get();
      if (!phase.stable) {
        return false;
      }
      playerCount += phase.playerCount;
    }
    return playerCount > 0;
  }

  /**
   * Builds the Hero this PlayerCard refering to with other properties.
   *
   * @return  targeting Hero, or null if unavailable
   */
  private Hero makeHero() {
    if (pickingPhaseSelector.get() != PickingPhase.FINISHED) {
      return null;
    }
    Playable current = playableSelector.get();
    Hero hero = Library.instance().getClone(current);
    hero.setProperty(null, teamIdSelector.get().intValue(), true);
    return hero;
  }

  public static List<Hero> getHeroList(Iterable<? extends PlayerCard> playerCards) {
    List<Hero> heroList = new ArrayList<>(4);
    for (PlayerCard card : playerCards) {
      heroList.add(card.makeHero());
    }
    return heroList;
  }

}
