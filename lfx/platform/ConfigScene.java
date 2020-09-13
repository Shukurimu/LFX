package lfx.platform;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.stage.Stage;
import lfx.setting.Keyboard;

public class ConfigScene extends Application implements GuiScene {
  public static final String CONFIG_PATH = "setting.txt";
  public static final Color FOCUSED_TEXT_FILL = Color.DODGERBLUE;
  public static final Color UNFOCUSED_TEXT_FILL = Color.BLACK;
  public static final Charset CHARSET = Charset.forName("utf-8");

  private final GridPane pane;
  private final List<ToggleButton> keyButtonList;
  private ToggleButton focusing = null;

  public ConfigScene() {
    pane = null;
    keyButtonList = null;
  }

  public ConfigScene(List<String[]> keyArrayList, Consumer<String> finishFunction) {
    pane = new GridPane();
    for (int index : new int[] { 1, 2, 3, 4 }) {
      pane.add(makeNameText("Player " + index), index, 0);
    }
    for (Keyboard keyboard : Keyboard.values()) {
      pane.add(makeNameText(keyboard.name()), 0, keyboard.ordinal() + 1);
    }

    keyButtonList = new ArrayList<>(28);
    for (int colIndex = 1; colIndex <= 4; ++colIndex) {
      for (int rowIndex = 1; rowIndex <= 7; ++rowIndex) {
        ToggleButton button = new ToggleButton();
        button.setPrefSize(CONFIG_BUTTON_WIDTH, 30);
        button.setOnAction(this::clickBindingHandler);
        keyButtonList.add(button);
        pane.add(button, colIndex, rowIndex);
      }
    }
    setButtonText(keyArrayList);

    pane.add(makeActionButton("Default", event -> setButtonText(Keyboard.loadDefault())), 2, 12);
    pane.add(makeActionButton("Save", event -> finishFunction.accept(this.save())), 3, 12);
    pane.add(makeActionButton("Cancel", event -> finishFunction.accept("Cancelled")), 4, 12);
    pane.setAlignment(Pos.CENTER);
    pane.setHgap(9.0);
    pane.setVgap(6.0);
    pane.getChildren().forEach(fxNode -> fxNode.setFocusTraversable(false));
  }

  List<String[]> getButtonText() {
    List<String[]> keyArrayList = new ArrayList<>(4);
    for (int i = 0; i < 4; ++i) {
      String[] keyArray = new String[7];
      for (int j = 0; j < 7; ++j) {
        keyArray[j] = keyButtonList.get(i * 7 + j).getText();
      }
      keyArrayList.add(keyArray);
    }
    return keyArrayList;
  }

  void setButtonText(List<String[]> textArrayList) {
    int index = 0;
    for (String[] textArray : textArrayList) {
      for (String text : textArray) {
        keyButtonList.get(index).setText(text);
        ++index;
      }
    }
    return;
  }

  static Text makeNameText(String text) {
    Text node = new Text(text);
    GridPane.setHalignment(node, HPos.CENTER);
    return node;
  }

  static Button makeActionButton(String text, EventHandler<ActionEvent> handler) {
    Button button = new Button(text);
    button.setOnAction(handler);
    button.setPrefWidth(CONFIG_BUTTON_WIDTH);
    button.setFont(Font.font(null, FontWeight.BLACK, 16));
    return button;
  }

  private void keyPressHandler(KeyEvent event) {
    event.consume();
    if (focusing != null && focusing.isSelected()) {
      KeyCode code = event.getCode();
      if (code == KeyCode.ESCAPE) {
        // cancel
      } else if (code == KeyCode.UNDEFINED || code.isFunctionKey() || code.isMediaKey()) {
        focusing.setText(KeyCode.UNDEFINED.name());
      } else {
        focusing.setText(code.name());
      }
      focusing.setTextFill(UNFOCUSED_TEXT_FILL);
      focusing.setSelected(false);
    }
    return;
  }

  private void clickBindingHandler(ActionEvent event) {
    focusing = (ToggleButton) event.getSource();
    for (ToggleButton button : keyButtonList) {
      if (button == focusing) {
        button.setTextFill(FOCUSED_TEXT_FILL);
        button.setSelected(true);
      } else {
        button.setTextFill(UNFOCUSED_TEXT_FILL);
        button.setSelected(false);
      }
    }
    return;
  }

  @Override
  public Scene makeScene(Consumer<Scene> sceneChanger) {
    Scene scene = new Scene(pane, WINDOW_WIDTH, WINDOW_HEIGHT);
    scene.setOnKeyPressed(this::keyPressHandler);
    return scene;
  }

  static List<String[]> load() {
    List<String> fileLines = new ArrayList<>();
    try (FileReader fileReader = new FileReader(CONFIG_PATH, CHARSET);
         BufferedReader reader = new BufferedReader(fileReader)) {
      return Keyboard.load(reader);
    } catch (Exception ex) {
      System.err.println("Exception happened while loading settings.");
    }
    return Keyboard.load(null);
  }

  private String save() {
    try (PrintWriter writer = new PrintWriter(CONFIG_PATH, CHARSET)) {
      Keyboard.save(writer, getButtonText());
      return "Saved";
    } catch (Exception ex) {
      System.err.println("Exception happened while saving settings.");
      return "Failed";
    }
  }

  @Override
  public void start(Stage primaryStage) {
    ConfigScene configScene = new ConfigScene(load(), (String result) -> {
      System.out.println(result);
      Platform.exit();
    });
    primaryStage.setScene(configScene.makeScene(null));
    primaryStage.setTitle("Little Fighter X - Setting");
    primaryStage.setX(600.0);
    primaryStage.show();
    return;
  }

  public static void main(String[] args) {
    Application.launch(args);
    return;
  }

}
