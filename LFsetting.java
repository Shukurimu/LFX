import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.geometry.HPos;
import java.nio.file.Files;
import java.io.PrintWriter;
import java.io.File;

final class LFsetting extends GridPane implements EventHandler<KeyEvent> {
    public static final double BUTTON_WIDTH = 120;
    public static final String defaultText = "Click on the button and Press a key, or ESC to cancel.";
    public static final String[] defaultSetting = {
        "NUMPAD8 NUMPAD2 NUMPAD4 NUMPAD6 NUMPAD5 NUMPAD0 ADD",
        "W X A D S TAB BACK_QUOTE",
        "UP DOWN LEFT RIGHT ENTER SHIFT CONTROL",
        "I COMMA J L K SPACE PERIOD"
    };
    public static final int PLAYER_NUM = defaultSetting.length;
    
    private Text tooltipsText = new Text(defaultText);
    private ToggleButton current = null;
    private ArrayList<ToggleButton> buttonArray = new ArrayList<>(PLAYER_NUM * 7);
    private EventHandler<ActionEvent> clickAction = (ActionEvent event) -> {
        /* allow keys with the ability of
           clicking (e.g., SPACE) or
           traversing (e.g., arrows) can be bound */
        this.requestFocus();
        tooltipsText.setText(defaultText);
        current = (ToggleButton)event.getSource();
        for (ToggleButton b: buttonArray) {
            if (b == current) {
                b.setTextFill(Color.DODGERBLUE);
                b.setSelected(true);
            } else {
                b.setTextFill(Color.BLACK);
                b.setSelected(false);
            }
        }
        return;
    };
    
    public LFsetting(final LFX lfx, ArrayList<LFcontrol> ctrlArray) {
        this.add(new Text("Player1"), 1, 0);
        this.add(new Text("Player2"), 2, 0);
        this.add(new Text("Player3"), 3, 0);
        this.add(new Text("Player4"), 4, 0);
        this.add(new Text("Up"), 0, 1);
        this.add(new Text("Down"), 0, 2);
        this.add(new Text("Left"), 0, 3);
        this.add(new Text("Right"), 0, 4);
        this.add(new Text("Attack"), 0, 5);
        this.add(new Text("Jump"), 0, 6);
        this.add(new Text("Defend"), 0, 7);
        this.getChildren().forEach(c -> GridPane.setHalignment(c, HPos.CENTER));
        
        int colIndex = 0;
        for (LFcontrol c: ctrlArray) {
            ++colIndex;
            int rowIndex = 0;
            for (String s: c.asStringArray()) {
                ToggleButton tb = new ToggleButton(s);
                tb.setPrefSize(BUTTON_WIDTH, 30);
                tb.setOnAction(clickAction);
                // tb.setFocusTraversable(false);
                buttonArray.add(tb);
                this.add(tb, colIndex, ++rowIndex);
            }
        }
        
        GridPane.setHalignment(tooltipsText, HPos.RIGHT);
        this.add(tooltipsText, 0, 9, PLAYER_NUM + 1, 1);
        this.setAlignment(Pos.CENTER);
        this.setHgap(9.0);
        this.setVgap(6.0);
        
        Button cfButton = new Button("Confirm");
        cfButton.setOnAction(e -> lfx.backFromLFsetting(makeCtrlArray()));
        cfButton.setPrefWidth(BUTTON_WIDTH);
        cfButton.setFont(Font.font(null, FontWeight.BLACK, 16));
        this.add(cfButton, PLAYER_NUM - 1, 12);
        Button ccButton = new Button("Cancel");
        ccButton.setOnAction(e -> lfx.backFromLFsetting(null));
        ccButton.setPrefWidth(BUTTON_WIDTH);
        ccButton.setFont(Font.font(null, FontWeight.BLACK, 16));
        this.add(ccButton, PLAYER_NUM - 0, 12);
    }
    
    @Override
    public void handle(KeyEvent event) {
        event.consume();
        if (current != null && current.isSelected()) {
            final KeyCode c = event.getCode();
            boolean valid = (c != KeyCode.ESCAPE);
            if ((c == KeyCode.UNDEFINED) || c.isFunctionKey() || c.isMediaKey()) {
                tooltipsText.setText("Invalid KeyCode: " + c);
                valid = false;
            }
            if (valid)
                current.setText(c.toString());
            current.setTextFill(Color.BLACK);
            current.setSelected(false);
        }
        return;
    }
    
    public Scene makeScene() {
        Scene scene = new Scene(this, LFmap.defaultWorldWidth, LFmap.SCENE_HEIGHT);
        scene.setOnKeyPressed(this);
        this.requestFocus();
        return scene;
    }
    
    public ArrayList<LFcontrol> makeCtrlArray() {
        ArrayList<LFcontrol> ctrlArray = new ArrayList<>(PLAYER_NUM);
        for (int i = 0; i < PLAYER_NUM; ++i) {
            String codeString = buttonArray.get(i * 7).getText();
            for (int j = 1; j < 7; ++j)
                codeString += " " + buttonArray.get(i * 7 + j).getText();
            ctrlArray.add(new LFcontrol(codeString));
        }
        return ctrlArray;
    }
    
    public static void load(ArrayList<LFcontrol> ctrlArray, String path) {
        List<String> content = null;
        try {
            content = Files.readAllLines((new File(path)).toPath());
        } catch (Exception ex) {}
        if (content == null || content.size() != PLAYER_NUM)
            content = Arrays.asList(defaultSetting);
        for (String s: content)
            ctrlArray.add(new LFcontrol(s));
        return;
    }
    
    public static void save(ArrayList<LFcontrol> ctrlArray, String path) {
        try (PrintWriter pw = new PrintWriter(path, "utf-8")) {
            ctrlArray.forEach(pw::println);
        } catch (Exception ex) {}
        return;
    }
    
}
