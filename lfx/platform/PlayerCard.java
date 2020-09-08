package lfx.platform;

import java.util.EnumMap;
import java.util.Map;
import lfx.base.Controller;
import lfx.game.Hero;
import lfx.game.Library;
import lfx.game.Playable;
import lfx.util.ElementSelector;
import lfx.util.Selectable;

public class PlayerCard {
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
    return pickingPhaseSelector.get().displayTeamText ? "Team " + teamIdSelector.get() : "";
  }

  public void reset() {
    pickingPhaseSelector.setDefault();
    return;
  }

  public Hero makeHero() {
    Playable current = playableSelector.get();
    Hero hero = Library.instance().getClone(current);
    hero.setProperty(null, teamIdSelector.get().intValue(), true);
    return hero;
  }

  protected void update() {
    controller.update();
    if (controller.press_a()) {
      pickingPhaseSelector.setNext();
    } else if (controller.press_j()) {
      pickingPhaseSelector.setPrevious();
    } else if (controller.press_L()) {
      settings.get(pickingPhaseSelector.get()).setPrevious();
    } else if (controller.press_R()) {
      settings.get(pickingPhaseSelector.get()).setNext();
    } else if (controller.press_U()) {
      settings.get(pickingPhaseSelector.get()).setDefault();
    }
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

}
