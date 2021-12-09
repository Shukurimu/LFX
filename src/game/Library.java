package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import util.Util;

public class Library {
  private static final Library singleton = new Library();
  private final Weapon DUMMY = null;  // TODO: fake object
  private final Map<String, Hero> heroMapping = new HashMap<>(32);
  private final Map<String, Weapon> weaponMapping = new HashMap<>(16);
  private final Map<String, Energy> energyMapping = new HashMap<>(64);
  private final List<Map<String, ? extends Observable>> searchOrderList =
      List.of(energyMapping, weaponMapping, heroMapping);
  private final List<String> RANDOMABLE_LIST =
      List.of("Deep", "John", "Henry", "Rudolf", "Louis",
              "Firen", "Freeze", "Dennis", "Woody", "Davis");
  private final Set<String> NON_DROPPABLE_SET = Set.of("IceSword", "LouisArmour1", "LouisArmour2");
  private final List<Weapon> droppableWeaponOriginList = new ArrayList<>(16);

  private void assertSingleton(Observable oldInstance) {
    if (oldInstance != null) {
      throw new UnsupportedOperationException("Duplicated instance: " + oldInstance.toString());
    }
    return;
  }

  public void register(Hero origin) {
    assertSingleton(heroMapping.putIfAbsent(origin.getIdentifier(), origin));
    return;
  }

  public void register(Weapon origin) {
    assertSingleton(weaponMapping.putIfAbsent(origin.getIdentifier(), origin));
    if (!NON_DROPPABLE_SET.contains(origin.getIdentifier())) {
      droppableWeaponOriginList.add(origin);
    }
    return;
  }

  public void register(Energy origin) {
    assertSingleton(energyMapping.putIfAbsent(origin.getIdentifier(), origin));
    return;
  }

  public Hero getClone(Playable playable) {
    String name = playable.getName();
    if (playable == Playable.SELECTION_RANDOM) {
      // TODO: now always Template
      name = "Template";
    }
    System.out.println("Clone: " + name);
    return heroMapping.get(name).makeClone();
  }

  Observable getOrigin(String identifier) {
    for (Map<String, ? extends Observable> mapping : searchOrderList) {
      Observable origin = mapping.get(identifier);
      if (origin != null) {
        return origin;
      }
    }
    System.err.println("Oid not found: " + identifier);
    return DUMMY;
  }

  public Observable getClone(String identifier) {
    return getOrigin(identifier).makeClone();
  }

  public List<Observable> getCloneList(String identifier, int amount) {
    Observable origin = getOrigin(identifier);
    List<Observable> cloneList = new ArrayList<>(amount);
    for (int i = 0; i < amount; ++i) {
      cloneList.add(origin.makeClone());
    }
    return cloneList;
  }

  public List<Playable> getPlayableList() {
    return List.copyOf(heroMapping.values());
  }

  public Observable getRandomWeapon() {
    return Util.getRandomElement(droppableWeaponOriginList).makeClone();
  }

  public List<Observable> getDroppableWeaponList() {
    List<Observable> cloneList = new ArrayList<>(droppableWeaponOriginList.size());
    for (Weapon origin : droppableWeaponOriginList) {
      cloneList.add(origin.makeClone());
    }
    return cloneList;
  }

  public List<Observable> getArmourSetList() {
    Observable armour1 = weaponMapping.getOrDefault("LouisArmour1", DUMMY);
    Observable armour2 = weaponMapping.getOrDefault("LouisArmour2", DUMMY);
    return List.of(
        armour1.makeClone(),
        armour1.makeClone(),
        armour1.makeClone(),
        armour1.makeClone(),
        armour2.makeClone()
    );
  }

  private Library() {}

  public static Library instance() {
    return singleton;
  }

}
