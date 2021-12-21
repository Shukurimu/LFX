package platform;

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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import setting.Configure;
import setting.Input;

public class ConfigScene extends Application implements GuiScene {
  public static final Color FOCUSED_TEXT_FILL = Color.DODGERBLUE;
  public static final Color UNFOCUSED_TEXT_FILL = Color.BLACK;

  private final GridPane pane;
  private final Configure configure;
  private final List<Button> keyButtonList;
  private Button focusing = null;

  public ConfigScene() {
    pane = null;
    configure = null;
    keyButtonList = null;
  }

  public ConfigScene(Consumer<String> finishFunction) {
    pane = new GridPane();
    for (int index : new int[] { 1, 2, 3, 4 }) {
      pane.add(makeNameText("Player " + index), index, 0);
    }
    for (Input input : Input.values()) {
      pane.add(makeNameText(input.name()), 0, input.ordinal() + 1);
    }

    keyButtonList = new ArrayList<>(28);
    for (int colIndex = 1; colIndex <= 4; ++colIndex) {
      for (int rowIndex = 1; rowIndex <= 7; ++rowIndex) {
        Button button = new Button();
        button.setPrefSize(CONFIG_BUTTON_WIDTH, 30);
        button.setOnAction(this::clickBindingHandler);
        keyButtonList.add(button);
        pane.add(button, colIndex, rowIndex);
      }
    }

    configure = Configure.load();
    setButtonText(configure.getKeyboardSetting());

    pane.add(makeActionButton("Default", event -> {
      if (focusing != null) {
        focusing.setTextFill(UNFOCUSED_TEXT_FILL);
        focusing = null;
      }
      setButtonText(Input.getDefault());
    }), 2, 12);
    pane.add(makeActionButton("Save", event -> {
      configure.setKeyboardSetting(getButtonText());
      finishFunction.accept(configure.save());
    }), 3, 12);
    pane.add(makeActionButton("Cancel", event -> {
      finishFunction.accept("Cancelled");
    }), 4, 12);
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
    if (focusing != null) {
      KeyCode code = event.getCode();
      if (code == KeyCode.ESCAPE) {
        // cancel
      } else if (code.isFunctionKey() || code.isMediaKey()) {
        focusing.setText(KeyCode.UNDEFINED.name());
      } else {
        focusing.setText(code.name());
      }
      focusing.setTextFill(UNFOCUSED_TEXT_FILL);
      focusing = null;
    }
    return;
  }

  private void clickBindingHandler(ActionEvent event) {
    keyButtonList.forEach(b -> b.setTextFill(UNFOCUSED_TEXT_FILL));
    focusing = (Button) event.getSource();
    focusing.setTextFill(FOCUSED_TEXT_FILL);
    return;
  }

  @Override
  public Scene makeScene(Consumer<Scene> sceneChanger) {
    Scene scene = new Scene(pane, WINDOW_WIDTH, WINDOW_HEIGHT);
    scene.setOnKeyPressed(this::keyPressHandler);
    return scene;
  }

  @Override
  public void start(Stage primaryStage) {
    ConfigScene configScene = new ConfigScene((String result) -> {
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
