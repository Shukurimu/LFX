package object;

import java.util.List;

import base.Region;
import component.Itr;
import util.Vector;

public interface Weapon extends Observable {
  double INITIAL_MP = 750.0;
  double INITIAL_MILK_MP = 500.0 / 3.0;
  Vector MILK_REGENERATION = new Vector(1.667, 1.6, 0.8);
  Vector BEER_REGENERATION = new Vector(6.000, 0.0, 0.0);

  @Override
  Weapon makeClone();

  boolean isHeavy();

  boolean isDrink();

  boolean isLight();

  boolean isSmall();

  /**
   * Deals with the race condition on picking.
   *
   * @param actor the object performs the pick action
   * @return true if successed
   */
  boolean tryPick(Observable actor);

  void release();

  void destroy();

  default Vector consume() {
    return Vector.ZERO;
  }

  static void setMutualExcluding(List<Observable> weaponList) {
    for (int i = weaponList.size() - 1; i > 0; --i) {
      Observable oi = weaponList.get(i);
      for (int j = i - 1; j >= 0; --j) {
        Observable oj = weaponList.get(j);
        oi.receiveItr(oj, Itr.NULL_ITR, Region.EMPTY);
        oj.sendItr(oi, Itr.NULL_ITR);
      }
    }
    return;
  }

  // https://www.lf-empire.de/lf2-empire/data-changing/reference-pages/185-id-properties
  // 100 stick     - broken id: 999 frame 10-17, 039.wav when hitting id: 121
  // 101 hoe       - broken id: 999 frame 20-27, 30-33
  // 120 knife     - broken id: 999 frame 30-33, 54-57, >A throw
  // 121 baseball  - broken id: 999 frame 60-63
  // 122 milk      - broken id: 999 frame 70-77, 80-83,
  //                 if held with state: 17 object hp decreases and character hp and mp increases
  // 123 beer      - broken id: 999 frame 160-167, 80-83,
  //                 if held with state: 17 object hp decreases and character mp increases fast
  // 124 boomerang - broken id: 999 frame 170-173, >A throw, can be blocked from any direction.
  // 150 stone     - broken id: 999 frame 0-7
  // 151 box       - broken id: 999 frame 40-47, 50-57
  //
  // Only these IDs will be picked up by the AI,
  // ID 122 has the highest priority and Coms holding this weapon will try to escape.
  // 100-199 - dropped at random or upon F8
  //
  // 201 - broken id: 999 frame 4-7, disappears upon hitting a character
  // 202 - disappears upon hitting the ground
  // 213 - broken id: 999 frame 150-157, converts energy balls to iceballs (see 209 for details)
  // 217 - broken id: 999 frame 174-177, created 4 times by state: 9996
  // 218 - broken id: 999 frame 174-177, created by state: 9996

}
