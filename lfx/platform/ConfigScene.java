package lfx.platform;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import lfx.util.Const;

class ConfigScene extends GridPane implements EventHandler<KeyEvent> {
  public static final int PLAYER_NUM = 4;
  public static final double BUTTON_WIDTH = 120;
  public static final String IDLE_STRING = "Click on a button to set key.";
  public static final String WAITING_STRING = "Press ESC to cancel.";
  public static final Color FOCUSED_TEXT_FILL = Color.DODGERBLUE;
  public static final Color UNFOCUSED_TEXT_FILL = Color.BLACK;
  public static final Charset CHARSET = Charset.forName("utf-8");

  private Text tooltipsText = new Text(IDLE_STRING);
  private ToggleButton focusing = null;
  private ArrayList<ToggleButton> listeningList = new ArrayList<>();
  private EventHandler<ActionEvent> clickAction = (ActionEvent event) -> {
    tooltipsText.setText(WAITING_STRING);
    /** Allow keys with the ability of clicking (SPACE) or traversing (arrows) can be bound. */
    this.requestFocus();
    focusing = (ToggleButton) event.getSource();
    for (ToggleButton button : listeningList) {
      if (button == focusing) {
        button.setTextFill(FOCUSED_TEXT_FILL);
        button.setSelected(true);
      } else {
        button.setTextFill(UNFOCUSED_TEXT_FILL);
        button.setSelected(false);
      }
    }
    return;
  };

  public ConfigScene(Consumer<String> mainSceneBridge) {
    for (int i = 0, index = 1; i < Const.PLAYER_NUM; ++i, ++index) {
      this.add(new Text("Player" + index), index, 0);
    }
    for (int i = 0, index = 1; i < Const.KEY_NUM; ++i, ++index) {
      this.add(new Text(Const.KEY_NAMES.get(i)), 0, index);
    }
    this.getChildren().forEach(text -> GridPane.setHalignment(text, HPos.CENTER));

    List<String[]> controlStringArrayList = loadControlStringArrayList();
    int colIndex = 0;
    for (String[] controlStringArray: controlStringArrayList) {
      ++colIndex;
      int rowIndex = 0;
      for (String keyString: controlStringArray) {
        ToggleButton button = new ToggleButton(keyString);
        button.setPrefSize(BUTTON_WIDTH, 30);
        button.setOnAction(clickAction);
        listeningList.add(button);
        this.add(button, colIndex, ++rowIndex);
      }
    }

    GridPane.setHalignment(tooltipsText, HPos.RIGHT);
    this.add(tooltipsText, 0, 9, Const.PLAYER_NUM + 1, 1);
    this.setAlignment(Pos.CENTER);
    this.setHgap(9.0);
    this.setVgap(6.0);

    this.add(makeActionButton("Update", event -> mainSceneBridge.accept(this.save())),
             Const.PLAYER_NUM - 1, 12);
    this.add(makeActionButton("Cancel", event -> mainSceneBridge.accept("Cancelled")),
             Const.PLAYER_NUM - 0, 12);
  }

  private Button makeActionButton(String text, EventHandler<ActionEvent> handler) {
    Button button = new Button(text);
    button.setOnAction(handler);
    button.setPrefWidth(BUTTON_WIDTH);
    button.setFont(Font.font(null, FontWeight.BLACK, 16));
    return button;
  }

  @Override
  public void handle(KeyEvent event) {
    event.consume();
    if (focusing != null && focusing.isSelected()) {
      KeyCode code = event.getCode();
      if (code == KeyCode.UNDEFINED || code.isFunctionKey() || code.isMediaKey()) {
        tooltipsText.setText("Invalid KeyCode: " + code);
      } else if (code == KeyCode.ESCAPE) {
        tooltipsText.setText(IDLE_STRING);
      } else {
        tooltipsText.setText(IDLE_STRING);
        focusing.setText(code.toString());
      }
      focusing.setTextFill(UNFOCUSED_TEXT_FILL);
      focusing.setSelected(false);
    }
    return;
  }

  public Scene makeScene() {
    Scene scene = new Scene(this, Const.WINDOW_WIDTH, Const.WINDOW_HEIGHT);
    scene.setOnKeyPressed(this);
    this.requestFocus();
    return scene;
  }

  private static List<String> load() {
    List<String> fileLines = new ArrayList<>();
    try (FileReader fileReader = new FileReader(Const.CONFIG_PATH, CHARSET);
         BufferedReader reader = new BufferedReader(fileReader)) {
      String line;
      while ((line = reader.readLine()) != null)
        fileLines.add(line);
    } catch (Exception ex) {
      System.err.println("Exception happened while loading settings.");
    }
    return fileLines;
  }

  public static List<String[]> loadControlStringArrayList() {
    List<String> fileLines = load();
    List<String[]> result = new ArrayList<>(Const.PLAYER_NUM);
    for (int i = 0; i < Const.PLAYER_NUM; ++i) {
      String stringLine = null;
      try {
        stringLine = fileLines.get(i);
      } catch (Exception ex) {
        stringLine = Const.DEFAULT_KEY_SETTING.get(i);
        System.err.println("Exception happened while parsing settings.");
      } finally {
        result.add(stringLine.split(Const.CONFIG_SEPARATOR));
      }
    }
    return result;
  }

  private List<String> dumpsControlLines() {
    List<String> controlLines = new ArrayList<>(Const.PLAYER_NUM);
    for (int i = 0, index = 0; i < Const.PLAYER_NUM; ++i) {
      List<String> controlString = new ArrayList<>();
      for (int j = 0; j < Const.KEY_NUM; ++j, ++index) {
        controlString.add(listeningList.get(index).getText());
      }
      controlLines.add(String.join(Const.CONFIG_SEPARATOR, controlString));
    }
    return controlLines;
  }

  private String save() {
    List<String> controlLines = dumpsControlLines();
    try (PrintWriter writer = new PrintWriter(Const.CONFIG_PATH, CHARSET)) {
      controlLines.forEach(writer::println);
      return "Saved";
    } catch (Exception ex) {
      System.err.println("Exception happened while saving settings.");
      return "Failed";
    }
  }

}
