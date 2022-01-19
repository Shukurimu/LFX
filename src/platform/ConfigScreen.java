package platform;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import base.Controller;

public class ConfigScreen extends AbstractScreen {
  private static final Color FOCUSED_TEXT_FILL = Color.DODGERBLUE;
  private static final Color UNFOCUSED_TEXT_FILL = Color.BLACK;
  private static final double CONFIG_BUTTON_WIDTH = 120;

  private final StringProperty message;
  private final List<Button> keyButtonList = new ArrayList<>(28);
  private Button focusing = null;

  ConfigScreen(StringProperty message) {
    this.message = message;
  }

  @Override
  protected GridPane makeParent() {
    GridPane pane = new GridPane();
    for (int index : new int[] { 1, 2, 3, 4 }) {
      pane.add(makeNameText("Player " + index), index, 0);
    }
    for (Controller.Input input : Controller.Input.values()) {
      pane.add(makeNameText(input.name()), 0, input.ordinal() + 1);
    }

    for (int colIndex = 1; colIndex <= 4; ++colIndex) {
      for (int rowIndex = 1; rowIndex <= 7; ++rowIndex) {
        Button button = new Button();
        button.setPrefSize(CONFIG_BUTTON_WIDTH, 30);
        button.setOnAction(this::clickBindingHandler);
        keyButtonList.add(button);
        pane.add(button, colIndex, rowIndex);
      }
    }

    Configuration configuration = Configuration.load();
    setButtonText(configuration.getInputSetting());

    pane.add(makeActionButton("Default", event -> {
      if (focusing != null) {
        focusing.setTextFill(UNFOCUSED_TEXT_FILL);
        focusing = null;
      }
      setButtonText(Configuration.defaultInputSetting());
    }), 2, 12);
    pane.add(makeActionButton("Save", event -> {
      configuration.setInputSetting(getButtonText());
      message.set(configuration.save());
      gotoPrevious();
    }), 3, 12);
    pane.add(makeActionButton("Cancel", event -> {
      message.set("Cancelled");
      gotoPrevious();
    }), 4, 12);
    pane.setAlignment(Pos.CENTER);
    pane.setHgap(9.0);
    pane.setVgap(6.0);
    pane.getChildren().forEach(fxNode -> fxNode.setFocusTraversable(false));
    return pane;
  }

  private List<String[]> getButtonText() {
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

  private void setButtonText(List<String[]> textArrayList) {
    int index = 0;
    for (String[] textArray : textArrayList) {
      for (String text : textArray) {
        keyButtonList.get(index).setText(text);
        ++index;
      }
    }
    return;
  }

  private Text makeNameText(String text) {
    Text node = new Text(text);
    GridPane.setHalignment(node, HPos.CENTER);
    return node;
  }

  private Button makeActionButton(String text, EventHandler<ActionEvent> handler) {
    Button button = new Button(text);
    button.setOnAction(handler);
    button.setPrefWidth(CONFIG_BUTTON_WIDTH);
    button.setFont(Font.font("Georgia", FontWeight.BLACK, 16));
    return button;
  }

  @Override
  protected void keyHandler(KeyCode keyCode) {
    if (focusing == null) {
      return;
    }
    if (keyCode == KeyCode.ESCAPE) {
      // cancel
    } else if (keyCode.isFunctionKey() || keyCode.isMediaKey()) {
      focusing.setText(KeyCode.UNDEFINED.name());
    } else {
      focusing.setText(keyCode.name());
    }
    focusing.setTextFill(UNFOCUSED_TEXT_FILL);
    focusing = null;
    return;
  }

  private void clickBindingHandler(ActionEvent event) {
    keyButtonList.forEach(b -> b.setTextFill(UNFOCUSED_TEXT_FILL));
    focusing = (Button) event.getSource();
    focusing.setTextFill(FOCUSED_TEXT_FILL);
    return;
  }

}
