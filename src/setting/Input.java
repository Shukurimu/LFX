package setting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public enum Input {
  Up    ("U"),
  Down  ("D"),
  Left  ("L"),
  Right ("R"),
  Attack("a"),
  Jump  ("j"),
  Defend("d");

  public static final String CHAR_SEPARATOR = " ";
  public static final List<String> DEFAULT_LINES = List.of(
      "NUMPAD8 NUMPAD2 NUMPAD4 NUMPAD6 NUMPAD5 NUMPAD0 ADD",
      "W X A D S TAB BACK_QUOTE",
      "UP DOWN LEFT RIGHT ENTER SHIFT CONTROL",
      "I COMMA J L K SPACE PERIOD"
  );

  public final String symbol;

  private Input(String symbol) {
    this.symbol = symbol;
  }

  public static List<String[]> getDefault() {
    List<String[]> keyArrayList = new ArrayList<>();
    for (String defaultLine : DEFAULT_LINES) {
      keyArrayList.add(defaultLine.split(CHAR_SEPARATOR));
    }
    return keyArrayList;
  }

  static List<String[]> load(BufferedReader reader) {
    List<String[]> keyArrayList = new ArrayList<>();
    for (String defaultLine : DEFAULT_LINES) {
      String line = null;
      try {
        line = reader.readLine();
      } catch (IOException ex) {
        line = defaultLine;
        ex.printStackTrace();
      }
      keyArrayList.add(line.split(CHAR_SEPARATOR));
    }
    return keyArrayList;
  }

  static boolean save(PrintWriter writer, List<String[]> keyArrayList) {
    boolean noException = true;
    for (int i = 0; i < DEFAULT_LINES.size(); ++i) {
      String line = null;
      try {
        line = String.join(CHAR_SEPARATOR, keyArrayList.get(i));
      } catch (Exception ex) {
        line = DEFAULT_LINES.get(i);
        ex.printStackTrace();
        noException = false;
      }
      writer.println(line);
    }
    return noException;
  }

}
