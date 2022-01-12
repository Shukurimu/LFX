package setting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Configure {
  static final String CONFIG_PATH = "setting.txt";
  static final Charset CHARSET = Charset.forName("utf-8");
  static final String CHAR_SEPARATOR = " ";
  static final List<String> DEFAULT_LINES = List.of(
      "NUMPAD8 NUMPAD2 NUMPAD4 NUMPAD6 NUMPAD5 NUMPAD0 ADD",
      "W X A D S TAB BACK_QUOTE",
      "UP DOWN LEFT RIGHT ENTER SHIFT CONTROL",
      "I COMMA J L K SPACE PERIOD"
  );

  private List<String[]> inputSetting;

  private Configure(List<String[]> inputSetting) {
    this.inputSetting = inputSetting;
  }

  public static Configure load() {
    List<String[]> inputSetting;
    try (FileReader fileReader = new FileReader(CONFIG_PATH, CHARSET);
         BufferedReader reader = new BufferedReader(fileReader)) {
      inputSetting = loadInputSetting(reader);
    } catch (Exception expection) {
      inputSetting = defaultInputSetting();
      expection.printStackTrace();
    }
    return new Configure(inputSetting);
  }

  public List<String[]> getInputSetting() {
    return inputSetting;
  }

  public void setInputSetting(List<String[]> newKeyboardSetting) {
    inputSetting = newKeyboardSetting;
    return;
  }

  public String save() {
    try (PrintWriter writer = new PrintWriter(CONFIG_PATH, CHARSET)) {
      saveInputSetting(writer, inputSetting);
      return "Saved";
    } catch (Exception expection) {
      expection.printStackTrace();
      return "Failed";
    }
  }

  public static List<String[]> defaultInputSetting() {
    List<String[]> keyArrayList = new ArrayList<>();
    for (String defaultLine : DEFAULT_LINES) {
      keyArrayList.add(defaultLine.split(CHAR_SEPARATOR));
    }
    return keyArrayList;
  }

  private static List<String[]> loadInputSetting(BufferedReader reader) {
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

  private static boolean saveInputSetting(PrintWriter writer, List<String[]> keyArrayList) {
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
