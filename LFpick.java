import java.util.ArrayList;
import java.util.Set;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Pos;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.scene.control.Label;
import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;

final class LFpick extends GridPane {
    public static final int MAXTEAMS = 5;
    public static final Image randomPick;
    public static final Image unselected;
    public static final double ICONSIZE = 180.0;
    public static final String[] heroID = new String[] { "RANDOM",
        "Template", "Deep", "John", "Henry", "Rudolf", "Louis", "Firen", "Freeze", "Dennis", "Woody", "Davis"
    };
    public static int randomHeroID() {
        return java.util.concurrent.ThreadLocalRandom.current().nextInt(1, heroID.length);
    }
    static {
        Canvas c = new Canvas(ICONSIZE, ICONSIZE);
        unselected = c.snapshot(null, null);
        javafx.scene.canvas.GraphicsContext gc = c.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0.0, 0.0, ICONSIZE, ICONSIZE);
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(null, FontWeight.BOLD, ICONSIZE - 2.0 * 15.0/* padding */));
        gc.fillText("?", ICONSIZE / 2.0, ICONSIZE / 2.0);
        randomPick = c.snapshot(null, null);
    }
    private final ArrayList<LFcard> cardArray;
    private final Timeline render;
    private int aniTime = 0;
    
    class LFcard extends VBox {
        public static final int NONE = 0b11;
        public static final int DONE = 0b10;
        public static final int HALF = 0b01;
        private final LFcontrol ctrl;
        private int phase = 0;// 0-init 1-hero 2-team 3-done
        private int index = 0;
        private int team = 0;
        private final Label infoName = new Label("PlayerX");
        private final Label infoHero = new Label("");
        private final Label infoTeam = new Label("");
        private final ImageView infoIcon = new ImageView(unselected);
        
        public LFcard(LFcontrol c) {
            ctrl = c;
            infoName.setFont(Font.font(24.0));
            infoHero.setFont(Font.font(24.0));
            infoTeam.setFont(Font.font(20.0));
            this.setSpacing(6.0);
            this.getChildren().addAll(infoName, infoIcon, infoHero, infoTeam);
            this.setAlignment(Pos.CENTER);
            this.setMinHeight(300);
        }
        
        public int readyBits() {
            return ((phase == 3) ? DONE : ((phase == 0) ? NONE : HALF));
        }
        
        public int keyInput(KeyCode c) {
            if (c == ctrl.code_a) {
                phase = Math.min(phase + 1, 3);
            } else if (c == ctrl.code_j) {
                phase = Math.max(phase - 1, 0);
            } else if (c == ctrl.code_U) {
                if (phase == 1)
                    index = 0;
            } else if (c == ctrl.code_D) {/* press down key to random */
                if (phase == 1)
                    index = randomHeroID();
            } else if (c == ctrl.code_R) {
                if (phase == 1)
                    index = (index + 1) % heroID.length;
                else if (phase == 2)
                    team = (team + 1) % MAXTEAMS;
            } else if (c == ctrl.code_L) {
                if (phase == 1)
                    index = (index - 1 + heroID.length) % heroID.length;
                else if (phase == 2)
                    team = (team - 1 + MAXTEAMS) % MAXTEAMS;
            } else
                return readyBits();
            switch (phase) {
                case 0:
                    infoIcon.setImage(unselected);
                    infoHero.setText("");
                    break;
                case 1:
                    infoHero.setText(heroID[index]);
                    infoIcon.setImage((index == 0) ? randomPick : ((LFhero)LFX.objPool.get(heroID[index])).faceImage);
                    infoIcon.setFitWidth(ICONSIZE);
                    infoIcon.setFitHeight(ICONSIZE);
                    infoTeam.setText("");
                    break;
                case 2:
                    infoTeam.setText((team == 0) ? "Independent" : ("Team" + team));
                    break;
            }
            return readyBits();
        }
        
        public LFhero makeHero() {
            return (phase == 3) ?
                ((LFhero)LFX.objPool.get(heroID[(index == 0) ? randomHeroID() : index])).makeCopyPick(ctrl, team) : null;
        }
        
        public void animate() {
            switch (phase) {
                case 1:
                    infoHero.setTextFill(((aniTime & 1) == 1) ? Color.BLACK : Color.BLUE);
                    break;
                case 2:
                    infoHero.setTextFill(Color.BLACK);
                    infoTeam.setTextFill(((aniTime & 1) == 1) ? Color.BLACK : Color.BLUE);
                    break;
                case 3:
                    infoTeam.setTextFill(Color.BLACK);
                    break;
            }
            return;
        }
        
    }
    
    public LFpick(ArrayList<LFcontrol> ctrlArray) {
        cardArray = new ArrayList<>(ctrlArray.size());
        Label head = new Label("ALL PICK");
        head.setMinHeight(80.0);
        head.setFont(Font.font(null, FontWeight.BOLD, 52.0));
        this.add(head, 0, 0, ctrlArray.size(), 1);
        // this.setHgrow(head, javafx.scene.layout.Priority.ALWAYS);
        GridPane.setHalignment(head, HPos.CENTER);
        for (int i = 0; i < ctrlArray.size(); ++i) {
            LFcard c = new LFcard(ctrlArray.get(i));
            cardArray.add(c);
            this.add(c, i, 1);
            // this.setVgrow(c, javafx.scene.layout.Priority.ALWAYS);
            // this.setHgrow(c, javafx.scene.layout.Priority.ALWAYS);
        }
        Label tail = new Label("xxxxxxxx");
        tail.setMinHeight(60.0);
        tail.setFont(Font.font(null, FontWeight.MEDIUM, 20.0));
        this.add(tail, 0, 2, ctrlArray.size(), 1);
        // this.setHgrow(tail, javafx.scene.layout.Priority.ALWAYS);
        GridPane.setHalignment(tail, HPos.RIGHT);
        
        // this.setAlignment(Pos.CENTER);
        this.setHgap(9.0);
        this.setVgap(16.0);
        // this.setPrefSize(LFmap.defaultWorldWidth, LFmap.SCENE_HEIGHT);
        
        // this.setPrefSize(javafx.scene.layout.Region.USE_COMPUTED_SIZE, javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        // this.setMinSize(LFmap.defaultWorldWidth, LFmap.SCENE_HEIGHT);
        
        render = new Timeline(new KeyFrame(new Duration(1000.0 / 8.0), e -> {
            ++aniTime;
            for (LFcard c: cardArray)
                c.animate();
        }));
        render.setCycleCount(Animation.INDEFINITE);
        render.play();
    }
    
    public Scene makeScene() {
        Scene scene = LFX.sceneBuilder(this, this.computePrefWidth(0), this.computePrefHeight(0));
        scene.setOnKeyPressed(e -> {
            KeyCode keyCode = e.getCode();
            if (keyCode == KeyCode.ESCAPE)
                javafx.application.Platform.exit();
            int ready = LFcard.NONE;
            for (LFcard c: cardArray)
                ready &= c.keyInput(keyCode);
            if (ready == LFcard.DONE) {
                render.stop();
                ArrayList<LFhero> a = new ArrayList<>(4);
                for (LFcard c: cardArray)
                    a.add(c.makeHero());
                LFX.goToLFmap(a);
            }
        });
        return scene;
    }
    
}
