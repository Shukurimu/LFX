package lfx.component;

import java.util.EnumSet;
import java.util.List;
import lfx.component.Itr;
import lfx.component.Itr.Effect;
import lfx.component.Type;

public class Bdy {
  public static final int ZWIDTH = 12;

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

  public final int x;
  public final int y;
  public final int w;
  public final int h;
  public final int zwidth;
  public final EnumSet<Attribute> attributes;

  @SafeVarargs
  public Bdy(int x, int y, int w, int h, int zwidth, Attribute... attributes) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.zwidth = zwidth;
    this.attributes = EnumSet.of(attributes);
  }

  @SafeVarargs
  public Bdy(int x, int y, int w, int h, Attribute... attributes) {
    this(x, y, w, h, ZWIDTH, attributes);
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
