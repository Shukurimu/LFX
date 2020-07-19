package lfx.util;

import java.util.List;

public final class Const {
  public static final int FRAME_NUM = 400;
  public static final int DEF = 1357920468;
  public static final int TBA = 1357924680;
  public static final int NOP = 1357986420;
  public static final int LAG = 3;
  public static final int DV_550 = 550;
  public static final int ZWIDTH = 12;

  public static final List<String> KEY_SYMBOLS = List.of("U", "D", "L", "R", "a", "j", "d");
  public static final int KEY_NUM = KEY_SYMBOLS.size();
  public static final List<String> KEY_NAMES = List.of(
      "Up", "Down", "Left", "Right", "Attack", "Jump", "Defend"
  );

  public static final List<String> TEAM_NAMES = List.of(
      "Independent", "Team1", "Team2", "Team3", "Team4"
  );
  public static final int TEAM_NUM = TEAM_NAMES.size();

  public static final String CONFIG_PATH = "setting.txt";
  public static final String CONFIG_SEPARATOR = " ";
  public static final List<String> DEFAULT_KEY_SETTING = List.of(
      "NUMPAD8 NUMPAD2 NUMPAD4 NUMPAD6 NUMPAD5 NUMPAD0 ADD",
      "W X A D S TAB BACK_QUOTE",
      "UP DOWN LEFT RIGHT ENTER SHIFT CONTROL",
      "I COMMA J L K SPACE PERIOD"
  );
  public static final List<String> DEFAULT_PLAYER_NAME = List.of(
      "Player1", "Player2", "Player3", "Player4"
  );
  public static final int PLAYER_NUM = DEFAULT_PLAYER_NAME.size();

  public static final double CONFIG_BUTTON_WIDTH = 120;
  /** Adjustment of pz while being held for rendering order. */
  public static final double Z_OFFSET = 1e-3;
  public static final double DEFAULT_FPS = 30.0;
  public static final double FIELD_WIDTH = 794;
  public static final double FIELD_HEIGHT = 550 - 128;
  public static final double WIDTH_DIV2 = FIELD_WIDTH / 2.0;
  public static final double WIDTH_DIV24 = FIELD_WIDTH / 24.0;
  public static final double CANVAS_WIDTH = FIELD_WIDTH / PLAYER_NUM;
  public static final double CANVAS_HEIGHT = 60;
  public static final double TEXTLABEL_HEIGHT = 20;
  public static final double WINDOW_WIDTH = FIELD_WIDTH;
  public static final double WINDOW_HEIGHT = FIELD_HEIGHT + CANVAS_HEIGHT + TEXTLABEL_HEIGHT * 2;
  public static final double PORTRAIT_SIZE = 180;
  public static final double CAMERA_SPEED_FACTOR = 1.0 / 18.0;
  public static final double CAMERA_SPEED_THRESHOLD = 0.05;
  public static final long VALID_KEY_INTERVAL = 200L;

  private Const() {}

}
