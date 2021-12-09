package setting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.List;

public class Configure {
  public static final String CONFIG_PATH = "setting.txt";
  public static final Charset CHARSET = Charset.forName("utf-8");

  private List<String[]> keyboardSetting;

  private Configure() {
    // empty
  }

  public static Configure load() {
    Configure configure = new Configure();
    try (FileReader fileReader = new FileReader(CONFIG_PATH, CHARSET);
         BufferedReader reader = new BufferedReader(fileReader)) {
      configure.keyboardSetting = Input.load(reader);
    } catch (Exception expection) {
      expection.printStackTrace();
      configure.keyboardSetting = Input.getDefault();
    }
    return configure;
  }

  public List<String[]> getKeyboardSetting() {
    return keyboardSetting;
  }

  public void setKeyboardSetting(List<String[]> newKeyboardSetting) {
    keyboardSetting = newKeyboardSetting;
    return;
  }

  public String save() {
    try (PrintWriter writer = new PrintWriter(CONFIG_PATH, CHARSET)) {
      Input.save(writer, keyboardSetting);
      return "Saved";
    } catch (Exception expection) {
      expection.printStackTrace();
      return "Failed";
    }
  }

}
