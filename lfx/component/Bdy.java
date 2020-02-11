package lfx.component;

import java.util.EnumSet;
import java.util.Set;
import lfx.component.Itr;
import lfx.util.Box;
import lfx.util.Const;

public final class Bdy {

  public enum Attribute {
    // exclusive
    FRIENDLY_FIRE,
    DANCE_OF_PAIN,
    ROLLING_PICKABLE,
    // inclusive
    IMMUNE_WEAKFIRE,
    IMMUNE_WEAKICE,
    IMMUNE_FALL_40;
  }

  public final Box box;
  public final Set<Attribute> attr;

  public Bdy(Box box) {
    this.box = box;
    this.attr = Set.of();
  }

  public Bdy(int x, int y, int w, int h) {
    this(new Box(x, y, w, h));
  }

  public Bdy(Box box, String attrString) {
    this.box = box;
    Set<Attribute> tempSet = EnumSet.noneOf(Attribute.class);
    for (Attribute attr : Attribute.values()) {
      if (attrString.contains(attr.toString())) {
        tempSet.add(attr);
      }
    }
    this.attr = Set.copyOf(tempSet);
  }

  public Bdy(int x, int y, int w, int h, String attrString) {
    this(new Box(x, y, w, h), attrString);
  }

  public boolean interactsWith(Itr itr, int scopeView) {
    if (attr.contains(Attribute.FRIENDLY_FIRE)) {
      scopeView = Const.getBothView(scopeView);
    }
    if ((scopeView & itr.scope) == 0) {
      return false;
    }
    if (!attr.contains(Attribute.DANCE_OF_PAIN) && itr.kind == Itr.Kind.GRASP_DOP) {
      return false;
    }
    if (!attr.contains(Attribute.ROLLING_PICKABLE) && itr.kind == Itr.Kind.ROLL_PICK) {
      return false;
    }
    if (attr.contains(Attribute.IMMUNE_WEAKFIRE) && itr.kind == Itr.Kind.WEAKFIRE) {
      return false;
    }
    if (attr.contains(Attribute.IMMUNE_WEAKICE) && itr.kind == Itr.Kind.WEAKICE) {
      return false;
    }
    if (attr.contains(Attribute.IMMUNE_FALL_40) && itr.fall <= 40) {
      return false;
    }
    return true;
  }

}
/*
  public static List<Attribute> parserBdy(int originalState, String identifier) {
    if (identifier.equals("Freezecolumn"))
      return List.of(Attribute.FRIENDLY_FIRE);
    switch (originalState) {
      case 13:
        return List.of(Attribute.IMMUNE_WEAKICE, Attribute.FRIENDLY_FIRE);
      case 16:
        return List.of(Attribute.DANCE_OF_PAIN);
      case 18:
      case 19:
        return List.of(Attribute.IMMUNE_WEAKFIRE);
      case 12:
        return List.of(Attribute.IMMUNE_FALL_40);
      default:
        return List.of();
    }
  }
*/