package lfx.base;

public class Action {
  public static final Action UNASSIGNED = new Action("UNASSIGNED");
  public static final Action DEFAULT = new Action("DEFAULT");
  public static final Action REPEAT = new Action("REPEAT");
  public static final Action REMOVAL = new Action("REMOVAL");
  public static final Action JOHN_CHASE = new Action("JOHN_CHASE");
  public static final Action DENNIS_CHASE = new Action("DENNIS_CHASE");

  public final int index;
  public final boolean changeFacing;
  private final String actionName;

  public Action(int rawActNumber) {
    if (rawActNumber >= 0) {
      index = rawActNumber;
      changeFacing = false;
    } else {
      index = -rawActNumber;
      changeFacing = true;
    }
    actionName = null;
  }

  private Action(String actionName) {
    index = 0;
    changeFacing = false;
    this.actionName = actionName;
  }

  @Override
  public String toString() {
    return actionName == null ?
        String.format("Action(%3d, %b)", index, changeFacing) :
        String.format("Action.%s", actionName);
  }

}
