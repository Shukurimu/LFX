package ecosystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Library {
  private static final Map<String, Observable> registry = new HashMap<>(128);
  private static final List<Observable> droppableWeapons = new ArrayList<>();
  private static final List<String> nonNeutral = List.of("HenryArrow1", "RudolfWeapon", "IceSword");
  private static final List<String> selectable = new ArrayList<>(40);

  /**
   * Don't let anyone instantiate this class.
   */
  private Library() {}

  public static void register(String identifier, Observable prototype) {
    registry.put(identifier, prototype);
    switch (prototype.getType()) {
      case HERO -> selectable.add(identifier);
      case LIGHT, HEAVY, SMALL, DRINK -> {
        if (!nonNeutral.contains(identifier)) {
          droppableWeapons.add(prototype);
        }
      }
      default -> {}
    }
    return;
  }

  public static Optional<Observable> getPrototype(String identifier) {
    return Optional.ofNullable(registry.get(identifier));
  }

  public static List<String> getSelectable() {
    return selectable;
  }

}
