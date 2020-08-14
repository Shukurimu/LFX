package lfx.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lfx.object.Energy;
import lfx.object.Hero;
import lfx.object.Observable;
import lfx.object.Weapon;
import lfx.util.Util;

public class Library {
  private static final Library singleton = new Library();
  private final Weapon DUMMY = null;  // TODO: fake Weapon

  private final Map<String, Hero> heroMapping = new LinkedHashMap<>(32);
  private final Map<String, Weapon> weaponMapping = new HashMap<>(16);
  private final Map<String, Energy> energyMapping = new HashMap<>(64);
  private final List<Map<String, ? extends Observable>> searchOrderList = List.of(
      energyMapping, weaponMapping, heroMapping
  );
  private final Set<String> NON_DROPPABLE_SET = Set.of("IceSword", "LouisArmour1", "LouisArmour2");
  private final List<Weapon> droppableWeaponOriginList;

  private void assertSingleton(Observable oldInstance) {
    if (oldInstance != null) {
      throw new UnsupportedOperationException("Duplicated instance: " + oldInstance.toString());
    }
    return;
  }

  void register(Hero origin) {
    assertSingleton(heroMapping.putIfAbsent(origin.getIdentifier(), origin));
    return;
  }

  void register(Weapon origin) {
    assertSingleton(weaponMapping.putIfAbsent(origin.getIdentifier(), origin));
    return;
  }

  void register(Energy origin) {
    assertSingleton(energyMapping.putIfAbsent(origin.getIdentifier(), origin));
    return;
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

  private Library() {
    List<Weapon> droppableList = new ArrayList<>(weaponMapping.size());
    for (Map.Entry<String, Weapon> entry : weaponMapping.entrySet()) {
      if (!NON_DROPPABLE_SET.contains(entry.getKey())) {
        droppableList.add(entry.getValue());
      }
    }
    droppableWeaponOriginList = List.copyOf(droppableList);
  }

  public static Library instance() {
    return singleton;
  }

}
