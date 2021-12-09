package platform;

public enum PickingPhase {
  UNASSIGNED    (0, true, false),
  SELECTING_HERO(0, false, false),
  SELECTING_TEAM(0, false, true),
  FINISHED      (1, true, true);

  public final int playerCount;
  public final boolean stable;
  public final boolean displayTeamText;

  private PickingPhase(int playerCount, boolean stable, boolean displayTeamText) {
    this.playerCount = playerCount;
    this.stable = stable;
    this.displayTeamText = displayTeamText;
  }

}
