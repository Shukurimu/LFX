package lfx.base;

import lfx.util.Util;

public class Action {
  public static final Action UNASSIGNED = new Action("UNASSIGNED");
  public static final Action DEFAULT = new Action("DEFAULT");
  public static final Action DEFAULT_REVERSE = new Action("DEFAULT_REVERSE");
  public static final Action REPEAT = new Action("REPEAT");
  public static final Action REMOVAL = new Action("REMOVAL");
  public static final Action JOHN_CHASE = new Action("JOHN_CHASE");
  public static final Action JOHN_CHASE_FAST = new Action("JOHN_CHASE_FAST");
  public static final Action DENNIS_CHASE = new Action("DENNIS_CHASE");

  public final int index;  // positive
  public final boolean changeFacing;
  private final String actionName;
  private final int indexTo;  // exclusive

  public Action(int rawActNumber) {
    if (rawActNumber >= 0) {
      index = rawActNumber;
      changeFacing = false;
    } else {
      index = -rawActNumber;
      changeFacing = true;
    }
    actionName = null;
    indexTo = index;
  }

  private Action(String actionName) {
    index = indexTo = 0;
    changeFacing = false;
    this.actionName = actionName;
  }

  private Action(int indexFrom, int range) {
    this.index = indexFrom;
    this.indexTo = indexFrom + range;
    changeFacing = false;
    actionName = null;
  }

  public boolean includes(int actionNumber) {
    return indexTo > actionNumber && actionNumber >= index;
  }

  public Action shifts(int delta) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return actionName == null ?
        String.format("Action(%3d, %b)", index, changeFacing) :
        String.format("Action.%s", actionName);
  }

  private static final Action[] generateInnerStates(Action base) {
    Action[] array = new Action[base.indexTo - base.index];
    for (int i = 0, act = base.index; act < base.indexTo; ++i, ++act) {
      array[i] = new Action(act, 0);
    }
    return array;
  }

  public static final Action HERO_STANDING = new Action(0, 5);
  public static final Action HERO_WALKING = new Action(5, 4) {
    final Action[] innerStates = generateInnerStates(this);
    @Override public Action shifts(int delta) {
      return innerStates[delta];
    }
  };
  public static final Action HERO_RUNNING = new Action(9, 3) {
    final Action[] innerStates = generateInnerStates(this);
    @Override public Action shifts(int delta) {
      return innerStates[delta];
    }
  };
  public static final Action HERO_HEAVY_WALK = new Action(12, 4) {
    final Action[] innerStates = generateInnerStates(this);
    @Override public Action shifts(int delta) {
      return innerStates[delta];
    }
  };
  public static final Action HERO_HEAVY_RUN = new Action(16, 3) {
    final Action[] innerStates = generateInnerStates(this);
    @Override public Action shifts(int delta) {
      return innerStates[delta];
    }
  };
  public static final Action HERO_HEAVY_STOP_RUN = new Action(19, 1);
  public static final Action HERO_WEAPON_ATK1 = new Action(20, 5);
  public static final Action HERO_WEAPON_ATK2 = new Action(25, 5);
  public static final Action HERO_JUMP_WEAPON_ATK = new Action(30, 5);
  public static final Action HERO_RUN_WEAPON_ATK = new Action(35, 5);
  public static final Action HERO_DASH_WEAPON_ATK = new Action(40, 5);
  public static final Action HERO_LIGHT_WEAPON_THROW = new Action(45, 5);
  public static final Action HERO_HEAVY_WEAPON_THROW = new Action(50, 2);
  public static final Action HERO_SKY_WEAPON_THROW = new Action(52, 3);
  public static final Action HERO_DRINK = new Action(55, 5);
  public static final Action HERO_PUNCH1 = new Action(60, 5);
  public static final Action HERO_PUNCH2 = new Action(65, 5);
  public static final Action HERO_SUPER_PUNCH = new Action(70, 10);
  public static final Action HERO_JUMP_ATK = new Action(80, 5);
  public static final Action HERO_RUN_ATK = new Action(85, 5);
  public static final Action HERO_DASH_ATK = new Action(90, 5);
  public static final Action HERO_DASH_DEF = new Action(95, 0);
  public static final Action HERO_FLIP1 = new Action(100, 2);
  public static final Action HERO_ROLLING = new Action(102, 6);
  public static final Action HERO_FLIP2 = new Action(108, 2);
  public static final Action HERO_DEFEND = new Action(110, 1);
  public static final Action HERO_DEFEND_HIT = new Action(111, 1);
  public static final Action HERO_BROKEN_DEF = new Action(112, 3);
  public static final Action HERO_PICK_LIGHT = new Action(115, 1);
  public static final Action HERO_PICK_HEAVY = new Action(116, 4);
  public static final Action HERO_CATCH = new Action(120, 10);
  public static final Action HERO_CAUGHT = new Action(130, 20);
  public static final Action HERO_FORWARD_FALL = new Action(180, 6);
  public static final Action HERO_FORWARD_FALL1 = new Action(180, 1);
  public static final Action HERO_FORWARD_FALL2 = new Action(181, 1);
  public static final Action HERO_FORWARD_FALL3 = new Action(182, 1);
  public static final Action HERO_FORWARD_FALL4 = new Action(183, 1);
  public static final Action HERO_FORWARD_FALL5 = new Action(184, 1);
  public static final Action HERO_FORWARD_FALLR = new Action(185, 1);
  public static final Action HERO_BACKWARD_FALL = new Action(186, 6);
  public static final Action HERO_BACKWARD_FALL1 = new Action(186, 1);
  public static final Action HERO_BACKWARD_FALL2 = new Action(187, 1);
  public static final Action HERO_BACKWARD_FALL3 = new Action(188, 1);
  public static final Action HERO_BACKWARD_FALL4 = new Action(189, 1);
  public static final Action HERO_BACKWARD_FALL5 = new Action(190, 1);
  public static final Action HERO_BACKWARD_FALLR = new Action(191, 1);
  public static final Action HERO_ICE = new Action(200, 3);
  public static final Action HERO_UPWARD_FIRE = new Action(203, 2);
  public static final Action HERO_DOWNWARD_FIRE = new Action(205, 2);
  public static final Action HERO_TIRED = new Action(207, 0);
  public static final Action HERO_JUMP = new Action(210, 2);
  public static final Action HERO_JUMPAIR = new Action(212, 1);  // gain jumping force
  public static final Action HERO_DASH1 = new Action(213, 1);
  public static final Action HERO_DASH2 = new Action(214, 1);  // reverse
  public static final Action HERO_CROUCH1 = new Action(215, 3);  // jump and flip landing
  public static final Action HERO_STOPRUN = new Action(218, 1);
  public static final Action HERO_CROUCH2 = new Action(219, 1);
  public static final Action HERO_INJURE1 = new Action(220, 1);
  public static final Action HERO_FRONTHURT = new Action(221, 1);
  public static final Action HERO_INJURE2 = new Action(222, 1);
  public static final Action HERO_BACKHURT = new Action(223, 1);
  public static final Action HERO_INJURE3 = new Action(224, 2);
  public static final Action HERO_DOP = new Action(226, 4);
  public static final Action HERO_LYING1 = new Action(230, 1);
  public static final Action HERO_LYING2 = new Action(231, 1);
  public static final Action HERO_THROW_LYING_MAN = new Action(232, 3);
  public static final Action HERO_TRANSFORM_BACK = new Action(245, 0);

  public static final Action LIGHT_IN_THE_SKY = new Action(0, 16) {
    final Action[] innerStates = generateInnerStates(this);
    @Override public Action shifts(int noNeed) {
      return Util.getRandomElement(innerStates);
    }
  };
  public static final Action LIGHT_ON_HAND = new Action(20, 16) {
    final Action[] innerStates = generateInnerStates(this);
    @Override public Action shifts(int onHandActNumber) {
      return innerStates[onHandActNumber - 20];  // drop
    }
  };
  public static final Action LIGHT_THROWING = new Action(40, 16) {
    final Action[] innerStates = generateInnerStates(this);
    @Override public Action shifts(int onHandActNumber) {
      return innerStates[onHandActNumber - 20];  // throw
    }
  };
  public static final Action LIGHT_ON_GROUND = new Action(60, 5);
  public static final Action LIGHT_STABLE_ON_GROUND = new Action(64, 1);
  public static final Action LIGHT_JUST_ON_GROUND = new Action(70, 3);
  // public static final Action LIGHT_BOUNCING_NORMAL = 0;
  // public static final Action LIGHT_BOUNCING_LIGHT = 7;
  public static final Action HEAVY_IN_THE_SKY = new Action(0, 6) {
    final Action[] innerStates = generateInnerStates(this);
    @Override public Action shifts(int noNeed) {
      return Util.getRandomElement(innerStates);
    }
  };
  public static final Action HEAVY_ON_HAND = new Action(10, 1) {
    final Action[] innerStates = generateInnerStates(this);
    @Override public Action shifts(int onHandActNumber) {
      return innerStates[onHandActNumber - 10];  // drop
    }
  };
  public static final Action HEAVY_THROWING = new Action(0, 6) {
    final Action[] innerStates = generateInnerStates(this);
    @Override public Action shifts(int onHandActNumber) {
      return innerStates[onHandActNumber - 10];  // throw
    }
  };
  public static final Action HEAVY_ON_GROUND = new Action(20, 1);
  public static final Action HEAVY_STABLE_ON_GROUND = new Action(20, 1);
  public static final Action HEAVY_JUST_ON_GROUND = new Action(21, 1);

  public static final Action ENERGY_FLYING = new Action(0, 0);
  public static final Action ENERGY_HITTING = new Action(10, 0);
  public static final Action ENERGY_HIT = new Action(20, 0);
  public static final Action ENERGY_REBOUND = new Action(30, 0);
  public static final Action ENERGY_DISAPPEAR = new Action(40, 0);

  public static final Action DENNIS_CHASE_STRAIGHT = new Action(1, 2);
  public static final Action DENNIS_CHASE_CHANGEDIR = new Action(3, 2);
  public static final Action DENNIS_CHASE_AWAY = new Action(5, 2);

}
