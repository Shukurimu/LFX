package platform;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import base.Controller;
import ecosystem.Playable;
import util.Selector;

public class DraftCard {

  enum Phase {
    UNASSIGNED     (0, true),
    SELECTING_HERO (0, false),
    SELECTING_TEAM (0, false),
    FINISHED       (1, true);

    public final int playerCount;
    public final boolean stable;

    private Phase(int playerCount, boolean stable) {
      this.playerCount = playerCount;
      this.stable = stable;
    }

    static final List<Phase> ORDER = List.of(UNASSIGNED, SELECTING_HERO, SELECTING_TEAM, FINISHED);

  }

  final Controller controller;
  private final Selector<Phase> phaseSelector = new Selector<>(false, Phase.ORDER);
  private final Selector<String> heroSelector;
  private final Selector<Integer> teamSelector = new Selector<>(true, List.of(0, 1, 2, 3, 4));
  private final Map<Phase, Selector<?>> manager = new EnumMap<>(Phase.class);

  public DraftCard(Controller controller, List<String> heroChoice) {
    this.controller = controller;
    heroSelector = new Selector<>(true, heroChoice);
    manager.put(Phase.UNASSIGNED, Selector.NO_CHOICE);
    manager.put(Phase.SELECTING_HERO, heroSelector);
    manager.put(Phase.SELECTING_TEAM, teamSelector);
    manager.put(Phase.FINISHED, Selector.NO_CHOICE);
  }

  protected String getPlayableIdentifier() {
    return switch (phaseSelector.current()) {
      case UNASSIGNED -> Playable.SELECTION_IDLE.getIdentifier();
      case SELECTING_HERO, SELECTING_TEAM, FINISHED -> heroSelector.current();
    };
  }

  protected int getTeamId() {
    return teamSelector.current().intValue();
  }

  protected String getTeamText() {
    return switch (phaseSelector.current()) {
      case UNASSIGNED, SELECTING_HERO -> "";
      case SELECTING_TEAM, FINISHED -> {
        Integer teamId = teamSelector.current();
        yield teamId.equals(0) ? "Independent" : "Team " + teamId;
      }
    };
  }

  protected void update() {
    controller.update();
    if (controller.press_a()) {
      phaseSelector.next();
    } else if (controller.press_j()) {
      phaseSelector.previous();
    } else if (controller.press_L()) {
      manager.get(phaseSelector.current()).previous();
    } else if (controller.press_R()) {
      manager.get(phaseSelector.current()).next();
    } else if (controller.press_U()) {
      manager.get(phaseSelector.current()).set(0);
    }
    return;
  }

  public static boolean isReady(List<DraftCard> draftCardList) {
    int playerCount = 0;
    for (DraftCard card : draftCardList) {
      Phase phase = card.phaseSelector.current();
      if (!phase.stable) {
        return false;
      }
      playerCount += phase.playerCount;
    }
    return playerCount > 0;
  }

}
