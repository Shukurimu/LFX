package lfx.game;

import lfx.base.Controller;
import lfx.component.Wpoint;
import lfx.util.Point;

public interface Hero extends Observable, Playable {
  double DEFEND_INJURY_REDUCTION = 0.10;
  double DEFEND_DVX_REDUCTION = 0.10;
  double FALLING_BOUNCE_VY = -4.25;  // guess
  double LANDING_VELOCITY_REMAIN = 0.5;  // guess
  double CONTROL_VZ = 2.5;  // press U or D; test
  double DIAGONAL_VX_RATIO = 1.0 / 1.4;  // test
  double ICED_FALLDOWN_DAMAGE = 10.0;
  double SONATA_FALLDOWN_DAMAGE = 10.0;

  @Override Hero makeClone();
  Wpoint getWpoint();
  boolean isAlive();
  Point getChasingPoint();
  Point getViewpoint();
  void setController(Controller controller);

  String Key_walking_speed  = "walking_speed";
  String Key_walking_speedz = "walking_speedz";
  String Key_running_speed  = "running_speed";
  String Key_running_speedz = "running_speedz";
  String Key_heavy_walking_speed  = "heavy_walking_speed";
  String Key_heavy_walking_speedz = "heavy_walking_speedz";
  String Key_heavy_running_speed  = "heavy_running_speed";
  String Key_heavy_running_speedz = "heavy_running_speedz";
  String Key_jump_height     = "jump_height";
  String Key_jump_distance   = "jump_distance";
  String Key_jump_distancez  = "jump_distancez";
  String Key_dash_height     = "dash_height";
  String Key_dash_distance   = "dash_distance";
  String Key_dash_distancez  = "dash_distancez";
  String Key_rowing_height   = "rowing_height";
  String Key_rowing_distance = "rowing_distance";
  String Key_hp_reg = "hp_reg";
  String Key_mp_reg = "mp_reg";

  class Indexer {
    private int index = -1;
    private final int[] data;

    @SafeVarargs
    public Indexer(int... data) {
      this.data = data;
    }

    public int next() {
      if (++index == data.length) {
        index = 0;
      }
      return data[index];
    }

    public int reset() {
      return data[index = 0];
    }

  }

}

// https://lf-empire.de/lf2-empire/data-changing/types/167-effect-0-characters
// http://lf2.wikia.com/wiki/Health_and_mana
