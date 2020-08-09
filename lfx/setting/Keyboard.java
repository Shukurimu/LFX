package lfx.setting;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Keyboard {
  public static final List<String> NAMES = List.of(
      "Up", "Down", "Left", "Right", "Attack", "Jump", "Defend"
  );
  static final List<String> DEFAULT_LINES = List.of(
      "NUMPAD8 NUMPAD2 NUMPAD4 NUMPAD6 NUMPAD5 NUMPAD0 ADD",
      "W X A D S TAB BACK_QUOTE",
      "UP DOWN LEFT RIGHT ENTER SHIFT CONTROL",
      "I COMMA J L K SPACE PERIOD"
  );
  static final String CHAR_SEPARATOR = " ";

  private Keyboard() {
    // No Instantiation
  }

  public static List<String[]> loadDefault() {
    List<String[]> keyArrayList = new ArrayList<>();
    for (String defaultLine : DEFAULT_LINES) {
      keyArrayList.add(defaultLine.split(CHAR_SEPARATOR));
    }
    return keyArrayList;
  }

  public static List<String[]> load(BufferedReader reader) {
    List<String[]> keyArrayList = new ArrayList<>();
    for (String defaultLine : DEFAULT_LINES) {
      String line = null;
      try {
        line = reader.readLine();
      } catch (java.io.IOException ex) {
        line = defaultLine;
      }
      keyArrayList.add(line.split(CHAR_SEPARATOR));
    }
    return keyArrayList;
  }

  public static boolean save(PrintWriter writer, List<String[]> keyArrayList) {
    boolean noException = true;
    for (ListIterator<String> it = DEFAULT_LINES.listIterator(); it.hasNext(); ) {
      String defaultLine = it.next();
      String line = null;
      try {
        line = String.join(CHAR_SEPARATOR, keyArrayList.get(it.previousIndex()));
      } catch (Exception ex) {
        line = defaultLine;
        noException = false;
      }
      writer.println(line);
    }
    return noException;
  }

}
