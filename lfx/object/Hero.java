package lfx.object;

import lfx.base.Controller;
import lfx.base.Viewer;
import lfx.component.Wpoint;
import lfx.object.Observable;
import lfx.object.Playable;
import lfx.util.Const;
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

  @Override Hero makeClone(int teamId, boolean faceRight);
  Wpoint getWpoint();
  boolean isAlive();
  Point getChasingPoint();
  Point getViewpoint();
  void updateViewer(Viewer viewer);
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

  int ACT_STANDING = 0;
  int ACT_WALKING = 5;
  int ACT_RUNNING = 9;
  int ACT_HEAVY_WALK = 12;
  int ACT_HEAVY_RUN = 16;
  int ACT_HEAVY_STOP_RUN = 19;
  int ACT_WEAPON_ATK1 = 20;
  int ACT_WEAPON_ATK2 = 25;
  int ACT_JUMP_WEAPON_ATK = 30;
  int ACT_RUN_WEAPON_ATK = 35;
  int ACT_DASH_WEAPON_ATK = 40;
  int ACT_LIGHT_WEAPON_THROW = 45;
  int ACT_HEAVY_WEAPON_THROW = 50;
  int ACT_SKY_WEAPON_THROW = 52;
  int ACT_DRINK = 55;
  int ACT_PUNCH1 = 60;
  int ACT_PUNCH2 = 65;
  int ACT_SUPER_PUNCH = 70;
  int ACT_JUMP_ATK = 80;
  int ACT_RUN_ATK = 85;
  int ACT_DASH_ATK = 90;
  int ACT_DASH_DEF = 95;
  int ACT_FLIP1 = 100;
  int ACT_ROLLING = 102;
  int ACT_FLIP2 = 108;
  int ACT_DEFEND = 110;
  int ACT_DEFEND_HIT = 111;
  int ACT_BROKEN_DEF = 112;
  int ACT_PICK_LIGHT = 115;
  int ACT_PICK_HEAVY = 116;
  int ACT_CATCH = 120;
  int ACT_CAUGHT = 130;
  int ACT_FORWARD_FALL1 = 180;
  int ACT_FORWARD_FALL2 = 181;
  int ACT_FORWARD_FALL3 = 182;
  int ACT_FORWARD_FALL4 = 183;
  int ACT_FORWARD_FALL5 = 184;
  int ACT_FORWARD_FALLR = 185;
  int ACT_BACKWARD_FALL1 = 186;
  int ACT_BACKWARD_FALL2 = 187;
  int ACT_BACKWARD_FALL3 = 188;
  int ACT_BACKWARD_FALL4 = 189;
  int ACT_BACKWARD_FALL5 = 190;
  int ACT_BACKWARD_FALLR = 191;
  int ACT_ICE = 200;
  int ACT_UPWARD_FIRE = 203;
  int ACT_DOWNWARD_FIRE = 205;
  int ACT_TIRED = 207;
  int ACT_JUMP = 210;
  int ACT_JUMPAIR = 212;  // gain jumping force
  int ACT_DASH1 = 213;
  int ACT_DASH2 = 214;  // reverse
  int ACT_CROUCH1 = 215;  // jump and flip landing
  int ACT_STOPRUN = 218;
  int ACT_CROUCH2 = 219;
  int ACT_INJURE1 = 220;
  int ACT_FRONTHURT = 221;
  int ACT_INJURE2 = 222;
  int ACT_BACKHURT = 223;
  int ACT_INJURE3 = 224;
  int ACT_DOP = 226;
  int ACT_LYING1 = 230;
  int ACT_LYING2 = 231;
  int ACT_THROW_LYING_MAN = 232;
  int ACT_TRANSFORM_FAIL = Const.DEF;
  int ACT_TRANSFORM_BACK = 245;

}

// https://lf-empire.de/lf2-empire/data-changing/types/167-effect-0-characters
// http://lf2.wikia.com/wiki/Health_and_mana
