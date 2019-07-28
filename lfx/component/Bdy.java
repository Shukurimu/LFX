package lfx.component;

import java.util.EnumSet;
import java.util.List;
import lfx.component.Itr;
import lfx.component.Itr.Effect;
import lfx.component.Type;
import lfx.util.Box;

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

    @Override
    public String toString() {
      return String.format("%s.%s", this.getDeclaringClass().getSimpleName(), super.toString());
    }

  };

  public final Box box;
  public final EnumSet<Attribute> attributes;

  @SafeVarargs
  public Bdy(Box box, Attribute... attributes) {
    this.box = box;
    this.attributes = EnumSet.of(attributes);
  }

  public boolean interactsWith(Itr itr, int scopeView) {
    if (attributes.containsKey(Attribute.FRIENDLY_FIRE))
      scopeView = Type.grantTeamScope(scopeView);
    if ((scopeView & itr.scope) == 0)
      return false;
    if (!attributes.containsKey(Attribute.DANCE_OF_PAIN) && itr.effect == Effect.GRASP_DOP)
      return false;
    if (!attributes.containsKey(Attribute.ROLLING_PICKABLE) && itr.effect == Effect.ROLL_PICK)
      return false;
    if (attributes.containsKey(Attribute.IMMUNE_WEAKFIRE) && itr.effect == Effect.WEAKFIRE)
      return false;
    if (attributes.containsKey(Attribute.IMMUNE_WEAKICE) && itr.effect == Effect.WEAKICE)
      return false;
    if (attributes.containsKey(Attribute.IMMUNE_FALL_40) && itr.fall <= 40)
      return false;
    return true;
  }

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

}
