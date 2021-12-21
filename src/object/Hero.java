package object;

import base.Controller;
import component.Wpoint;

public interface Hero extends Observable, Playable {
  double DEFEND_INJURY_REDUCTION = 0.10;
  double DEFEND_DVX_REDUCTION = 0.10;
  double FALLING_BOUNCE_VY = -4.25; // guess
  double LANDING_VELOCITY_REMAIN = 0.5; // guess
  double CONTROL_VZ = 2.5; // press U or D; test
  double DIAGONAL_VX_RATIO = 1.0 / 1.4; // test
  double ICED_FALLDOWN_DAMAGE = 10.0;
  double SONATA_FALLDOWN_DAMAGE = 10.0;

  @Override
  Hero makeClone();

  /**
   * Sets the {@code Controller} for this object.
   */
  void setController(Controller controller);

  /**
   * Gets the {@code Wpoint} of current {@code Frame}.
   */
  Wpoint getWpoint();

  /**
   * Deals with the race condition on grabbing.
   *
   * @param actor the object performs the grab action
   * @return true if successed
   */
  boolean tryGrab(Observable actor);

  /**
   * The transition of walking and running do not follow standard wait-next rule.
   * They use a hidden frame counter to manager instead.
   */
  class HiddenFrameCounter {
    private int index = -1;
    private final int[] indexes;

    private HiddenFrameCounter(int[] indexes) {
      this.indexes = indexes;
    }

    public int next() {
      if (++index == indexes.length) {
        index = 0;
      }
      return indexes[index];
    }

    public int reset() {
      return indexes[index = 0];
    }

    public static HiddenFrameCounter forWalking() {
      return new HiddenFrameCounter(new int[] { 2, 3, 2, 1, 0, 1 });
    }

    public static HiddenFrameCounter forRunning() {
      return new HiddenFrameCounter(new int[] { 0, 1, 2, 1 });
    }

  }

  // https://www.lf-empire.de/lf2-empire/data-changing/reference-pages/185-id-properties
  //  1 Deep     - melee
  //  2 John     - melee
  //  4 Henry    - ranged
  //  5 Rudolf   - ranged, 10hp when opointed by self
  //  6 Louis    - melee, armor defense 1 when frame < 20 or state4/5/7 doesn't resist fire/ice,
  //               085.wav when hit, hit_ja available when hp < 1/3 (or lf2.net enabled)
  //  7 Firen    - melee, fuse to id: 51 frame 290 with 8 when hp < 1/3 (or lf2.net enabled)
  //  8 Freeze   - melee, fuse to id: 51 frame 290 with 7 when hp < 1/3 (or lf2.net enabled),
  //               balls turn into id: 209 when hit (see 209 for details)
  //  9 Dennis   - melee
  // 10 Woody    - melee
  // 11 Davis    - melee
  // 30 Bandit   - melee, randomly selected with id 3000 in stage
  // 31 Hunter   - ranged, randomly selected with id 3000 in stage
  // 32 Mark     - melee
  // 33 Jack     - melee
  // 34 Sorcerer - melee
  // 35 Monk     - melee
  // 36 Jan      - melee
  // 37 Knight   - melee, armor defense 15 doesn't resist fire/ice, 085.wav when hit
  // 38 Bat      - melee
  // 39 Justin   - melee
  // 50 LouisEX  - melee, state: 9995 turns any object into id: 50
  // 51 Firzen   - melee, fusion of 7 and 8, fast mp regeneration, enemies in stage mode×2
  // 52 Julian   - melee, 10hp when opointed by self, armor defense 15 resists fire/ice,
  //               002.wav when hit, fast mp regeneration, enemies in stage mode×3
  //
  // 0-29 - characters included in the random function if not in the first line
  // 30-39 & 50-59 - locked character ids, unlocked with lf2.net
  // 30-37 & 39 - no pink "com" in stage mode & does not blink when getting up from lying.
  // 40-41 no pink "com" in stage mode, but still blinks when getting up from lying.

}
