package ecosystem;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class Library {
  private static final System.Logger logger = System.getLogger("");
  private static final Map<String, Observable> registry = new HashMap<>(128);
  private static final List<Observable> droppableWeapons = new ArrayList<>();
  // private static final List<String> nonNeutral = List.of("HenryArrow1", "RudolfWeapon", "IceSword");
  private static final List<String> nonNeutral = List.of("HenryArrow1", "RudolfWeapon");
  private static final List<String> heroList = new ArrayList<>(40);

  /**
   * Don't let anyone instantiate this class.
   */
  private Library() {}

  public static void register(String identifier, Observable prototype) {
    registry.put(identifier, prototype);
    switch (prototype.getType()) {
      case HERO -> heroList.add(identifier);
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
    if (identifier.equals(Playable.SELECTION_RANDOM.getIdentifier())) {
      double randomValue = Math.random() * heroList.size();
      identifier = heroList.get((int) randomValue);
      logger.log(Level.INFO, "{0,number,0.0000} = {1}", randomValue, identifier);
    }
    return Optional.ofNullable(registry.get(identifier));
  }

  public static List<String> getHeroList() {
    return heroList;
  }

  public static Optional<Observable> getClonedWeapon(Random random) {
    if (droppableWeapons.isEmpty()) {
      return Optional.empty();
    } else {
      int randomValue = random.nextInt(droppableWeapons.size());
      return Optional.of(droppableWeapons.get(randomValue));
    }
  }

  public static List<Observable> getClonedWeapons() {
    return droppableWeapons.stream().map(o -> o.makeClone()).toList();
  }

}
