package lfx.util;

import java.util.List;

public final class Const {
  public static final List<String> KEY_SYMBOLS = List.of("U", "D", "L", "R", "a", "j", "d");
  public static final int KEY_NUM = KEY_SYMBOLS.size();

  public static final List<String> TEAM_NAMES = List.of(
      "Independent", "Team1", "Team2", "Team3", "Team4"
  );
  public static final int TEAM_NUM = TEAM_NAMES.size();

  public static final String CONFIG_PATH = "setting.txt";
  public static final List<String> DEFAULT_PLAYER_NAME = List.of(
      "Player1", "Player2", "Player3", "Player4"
  );
  public static final int PLAYER_NUM = DEFAULT_PLAYER_NAME.size();

  public static final double CONFIG_BUTTON_WIDTH = 120;
  public static final double DEFAULT_FPS = 30.0;
  public static final double CANVAS_WIDTH = 794 / PLAYER_NUM;
  public static final double CANVAS_HEIGHT = 60;
  public static final double TEXTLABEL_HEIGHT = 20;
  public static final double WINDOW_WIDTH = 794;
  public static final double WINDOW_HEIGHT = (550 - 128) + CANVAS_HEIGHT + TEXTLABEL_HEIGHT * 2;
  public static final double PORTRAIT_SIZE = 180;
  public static final long VALID_KEY_INTERVAL = 200L;

  private Const() {}

}
