package lfx.component;

import java.util.EnumSet;
import lfx.component.Itr;
import lfx.component.Itr.Effect;

public final class Bdy {
  public static final int ZWIDTH = 12;

  public enum Attribute {
    FRIENDLY_FIRE,
    IMMUNE_WEAKFIRE,
    IMMUNE_WEAKICE,
    IMMUNE_FALL_40;

    @Override
    public String toString() {
      return String.format("%s.%s", this.getClass().getSimpleName(), super.toString());
    }

  };

  public final int x;
  public final int y;
  public final int w;
  public final int h;
  public final int zwidth;
  public final EnumSet<Attribute> attrs;

  public Bdy(int x, int y, int w, int h, int zwidth, Attribute... attrs) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.zwidth = zwidth;
    this.attrs = EnumSet.of(attrs);
  }

  // use default zwidth if not specified
  public Bdy(int x, int y, int w, int h, Attribute... attrs) {
    this(x, y, w, h, DEFAULT_ZWIDTH, attrs);
  }

  public boolean interactsWith(ItrInfo info) {
    for (Attribute a: attrs) {
      switch (a) {
        case FRIENDLY_FIRE:
          break;
        case IMMUNE_WEAKFIRE:
          if (info.effect.contains(Effect.WEAKFIRE))
            return false;
          break;
        case IMMUNE_WEAKICE:
          if (info.effect.contains(Effect.WEAKICE))
            return false;
          break;
        case IMMUNE_FALL_40:
          if (info.fall <= 40)
            return false;
          break;
      }
    }
    return false;
  }

  public static List<Attribute> parserBdy(int originalState, String identifier) {
    if (identifier.equals("Freezecolumn"))
      return List.of(Attribute.FRIENDLY_FIRE);
    switch (originalState) {
      case 13:
        return List.of(Attribute.FRIENDLY_FIRE, Attribute.IMMUNE_WEAKICE);
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
