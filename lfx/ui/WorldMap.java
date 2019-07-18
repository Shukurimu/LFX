import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.EnumMap;
import java.util.Random;
import javafx.scene.input.KeyCode;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.geometry.HPos;
import javafx.collections.ObservableList;
import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.util.Duration;

final class LFmap extends GridPane implements EventHandler<ActionEvent> {
    public static final int defaultWorldWidth = 794;
    public static final int defaultWorldHeight = 550 - 128;
    public static final int TEXTLABEL_HEIGHT = 20;
    public static final int weaponAdditionalBound = 40;
    public static final double SCENE_HEIGHT = defaultWorldHeight + LFstatus.canvasHeight + TEXTLABEL_HEIGHT * 2;
    public static final double CAMERA_SPEED_FACTOR = 1.0 / 18.0;
    public static final double CAMERA_SPEED_THRESHOLD = 0.05;
    public static final double WINDOW_WIDTH_DIV2  = defaultWorldWidth / 2.0;
    public static final double WINDOW_WIDTH_DIV24 = defaultWorldWidth / 24.0;
    public final double xwidth;
    public final double xwidthl;
    public final double xwidthr;
    public final double zboundT;
    public final double zboundB;
    public final double scrollBound;
    public int mapTime = 0;
    public double  gravity = 1.7;
    public double friction = 1.0;
    public double weaponDropRate = 1.0 / (30.0 * 7/* per second */);
    private List<LFobject> objs = new ArrayList<>(8);
    private ArrayList<LFobject> pending = new ArrayList<>(10);
    private ArrayList<LFstatus> healthBars = LFstatus.buildEmpty();
    private ArrayList<LFhero> visionTrace = new ArrayList<>();
    private Label btmText1 = new Label();
    private Label btmText2 = new Label();
    private Label midText1 = new Label();
    private Label midText2 = new Label();
    private ObservableList<Node> mapObjectList = null;
    private ScrollPane scrollPane = null;
    private ArrayList<LFlayer> layerElements = new ArrayList<>(10);
    
    private final Random mapRandom = new Random();
    private final EnumMap<KeyCode, LFkeyrecord> keyStatus = new EnumMap<KeyCode, LFkeyrecord>(KeyCode.class);
    private final Timeline render = new Timeline(new KeyFrame(new Duration(1000.0 / 30.0), this));
    private boolean unlimitedMode = true;
    private double cameraPosition = 0.0;
    
    
    
    public Scene makeScene() {
        Scene scene = LFX.sceneBuilder(this, this.computePrefWidth(0), this.computePrefHeight(0));
        scene.setOnKeyReleased(e -> keyStatus.getOrDefault(e.getCode(), LFkeyrecord.guard).setReleased());
        scene.setOnKeyPressed(e -> {
            LFkeyrecord rec = keyStatus.get(e.getCode());
            if (rec != null)
                rec.setPressed();
            else switch (e.getCode()) {
                case F1:
                    if (render.getCurrentRate() == 0.0)
                        render.play();
                    else
                        render.stop();
                    break;
                case F2:
                    if (render.getCurrentRate() == 0.0)
                        this.handle(null);
                    else
                        render.stop();
                    break;
                case F4:
                    render.stop();
                    LFX.goToLFpick();
                    break;
                case F5:
                    if (render.getCurrentRate() != 0.0)
                        render.setRate((render.getRate() == 1.0) ? 2.0 : 1.0);
                    break;
                case F6:
                    unlimitedMode ^= true;
                    break;
                case F7:
                    revive();
                    break;
                case F8:
                    weaponDropAll();
                    break;
                case F9:
                    weaponDestroy();
                    break;
                case ESCAPE:
                    render.stop();
                    javafx.application.Platform.exit();
                    break;
            }
            return;
        });
        render.setCycleCount(Animation.INDEFINITE);
        render.play();
        return scene;
    }
    
    /* original background */
    public LFmap() {
        this.setBackground(null);
        xwidth  = 900;
        xwidthl =      0 - weaponAdditionalBound;
        xwidthr = xwidth + weaponAdditionalBound;
        zboundT = 200;/* should be set >= 1.0 */
        zboundB = 400;
        cameraPosition = xwidth / 2.0;
        scrollBound = xwidth - defaultWorldWidth;
        
        for (int i = 0; i < healthBars.size(); ++i)
            this.add(healthBars.get(i), i, 0);
        
        midText1.setTextFill(Color.AQUA);
        midText1.setMinHeight(TEXTLABEL_HEIGHT);
        GridPane.setHalignment(midText1, HPos.LEFT);
        this.add(midText1, 0, 1, 2, 1);
        
        midText2.setTextFill(Color.VIOLET);
        midText2.setMinHeight(TEXTLABEL_HEIGHT);
        GridPane.setHalignment(midText2, HPos.RIGHT);
        this.add(midText2, 2, 1, 2, 1);
        
        
        layerElements.add(new LFlayer("originBg1", 0, 0, (int)xwidth));
        layerElements.add(new LFlayer("originBg2", 0, 0, (int)xwidth));
        
        Pane objectLayer = new Pane();
        mapObjectList = objectLayer.getChildren();
        
        Pane wrapper = new Pane();
        wrapper.setMaxSize(xwidth, defaultWorldHeight);
        wrapper.setMinSize(xwidth, defaultWorldHeight);
        ObservableList<Node> wrapperChildren = wrapper.getChildren();
        layerElements.forEach(e -> wrapperChildren.add(e.pic));
        wrapperChildren.add(objectLayer);
        
        scrollPane = new ScrollPane(wrapper);
        scrollPane.setMaxSize(defaultWorldWidth, defaultWorldHeight);
        scrollPane.setMinSize(defaultWorldWidth, defaultWorldHeight);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHmax(scrollBound);
        scrollPane.setVmax(0.0);
        this.add(scrollPane, 0, 2, 4, 1);
        
        btmText1.setTextFill(Color.GOLD);
        btmText1.setMinHeight(TEXTLABEL_HEIGHT);
        GridPane.setHalignment(btmText1, HPos.LEFT);
        this.add(btmText1, 0, 3, 2, 1);
        
        btmText2.setTextFill(Color.LIME);
        btmText2.setMinHeight(TEXTLABEL_HEIGHT);
        GridPane.setHalignment(btmText2, HPos.RIGHT);
        this.add(btmText2, 2, 3, 2, 1);
    }
    
    public double randomZ() {
        return mapRandom.nextDouble() * (zboundB - zboundT) + zboundT;
    }
    
    /* choose a hero on map
       pass nearest==null to choose randomly (independent to distance) */
    public LFobject chooseHero(LFobject x, boolean chooseTeam, Boolean nearest) {
        ArrayList<LFobject> temp = new ArrayList<>();
        for (LFobject o: objs) {
            if (o instanceof LFhero && o != x)
                if ((x.teamID == o.teamID) == chooseTeam)
                    temp.add(o);
        }
        if (temp.isEmpty())
            return null;
        if (nearest == null)
            return temp.get(mapRandom.nextInt(temp.size()));
        LFobject choice = null;
        if (nearest) {
            double distance = Double.MAX_VALUE;
            for (LFobject o: temp) {
                double d = Math.abs(o.px - x.px);
                if (distance > d) {
                    distance = d;
                    choice = o;
                }
            }
        } else {
            double distance = Double.MIN_VALUE;
            for (LFobject o: temp) {
                double d = Math.abs(o.px - x.px);
                if (distance < d) {
                    distance = d;
                    choice = o;
                }
            }
        }
        return choice;
    }
    
    public boolean isUnlimitedMode() {
        return unlimitedMode;
    }
    
    public void pickedHero(LFhero h, int bp) {
        h.ctrlRegister(keyStatus);
        addHero(h, 0, bp);
        return;
    }
    
    public void addHero(LFhero h, int act, int barPosition) {
        h.initialization(xwidth * 0.3 + mapRandom.nextDouble() * defaultWorldWidth, 0, randomZ(), act);
        objs.add(h);
        mapObjectList.add(h.getNode());
        if (barPosition >= 0 && barPosition < LFsetting.PLAYER_NUM)
            healthBars.get(barPosition).setHero(h);
        visionTrace.add(h);
        return;
    }
    
    public void transform(LFobject source, LFobject target) {
        if (source instanceof LFhero) {
            for (LFstatus s: healthBars) {
                if (s.hero == source) {
                    s.hero = (LFhero)target;
                    break;
                }
            }
        }
        /* 2nd transform */
        source.statusOverwrite((LFhero)target);
        target.getNode().setVisible(true);
        pending.add(target);
        return;
    }
    
    public void weaponDropAll() {
        for (LFobject o: LFX.objPool.values()) {
            if (o instanceof LFweapon) {
                LFweapon newWp = (LFweapon)o.makeCopy(true, 0);
                newWp.initialization(100.0 + mapRandom.nextDouble() * (xwidth - 200.0), -500, randomZ(), 0);
                objs.add(newWp);
                mapObjectList.add(newWp.getNode());
            }
        }
        return;
    }
    
    public void weaponDestroy() {
        for (LFobject o: objs) {
            if (o instanceof LFweapon)
                o.hp = 0.0;
        }
        return;
    }
    
    /* temporarily add to a pending list to avoid ConcurrentModificationException */
    public void spawnObject(List<LFobject> os) {
        synchronized (pending) {
            pending.addAll(os);
        }
        return;
    }
    
    /* see LFblast for details */
    public void spawnObject(List<LFobject> os, LFobject reflector, int span) {
        if (reflector != null) {
            for (LFobject o: os)
                reflector.vrest.put(o, Math.max(mapTime + span, reflector.vrest.getOrDefault(o, 0)));
        }
        spawnObject(os);
        return;
    }
    
    public double applyFriction(double v) {
        if (v >= 0.0) {
            v -= friction;
            return (v >= 0.0) ? v : 0.0;
        } else {
            v += friction;
            return (v >= 0.0) ? 0.0 : v;
        }
    }
    
    public void revive() {
        for (LFobject o: objs)
            o.revive();
        return;
    }
    
    /* this method is invoked every TimeUnit */
    @Override
    public void handle(ActionEvent event) {
        LFX.systemTime = System.currentTimeMillis();
        ++mapTime;
        
        // if (mapRandom.nextDouble() < weaponDropRate) {
            // TODO: drop a weapon
        // }
        objs.parallelStream().filter(o ->  (o instanceof LFhero))
            .forEach(o1 -> objs.forEach(o2 -> o1.checkItr(o2, this)));
        objs.parallelStream().filter(o -> !(o instanceof LFhero))
            .forEach(o1 -> objs.forEach(o2 -> o1.checkItr(o2, this)));
        
        objs.parallelStream().forEach(o -> o.updateStatus(this));
        objs.removeIf(o -> !o.updateFXnode());
        
        for (LFobject o: pending) {
            o.updateStatus(this);
            o.updateFXnode();
            objs.add(o);
            mapObjectList.add(o.getNode());
        }
        pending.clear();
        
        for (LFstatus z: healthBars) {
            if (z.hero != null)
                z.draw();
        }
        
        /* camera moving policy is modified from F.LF */
        double pos = 0.0;
        int facing = 0;
        for (LFhero h: visionTrace) {
            pos += h.px;
            facing += h.faceRight ? 1 : -1;
        }
        pos = facing * WINDOW_WIDTH_DIV24 + pos / visionTrace.size() - WINDOW_WIDTH_DIV2;
        pos = Math.min(scrollBound, Math.max(0.0, pos));
        double speed = (pos - cameraPosition) * CAMERA_SPEED_FACTOR;
        cameraPosition = (Math.abs(speed) < CAMERA_SPEED_THRESHOLD) ? pos : (cameraPosition + speed);
        scrollPane.setHvalue(cameraPosition);
        
        midText1.setText("MapTime: " + mapTime);
        midText2.setText(unlimitedMode ? "[F6] Unlimited Mode" : "");
        btmText1.setText("FxNode: " + mapObjectList.size());
        btmText2.setText(Thread.currentThread().getName());
        /* javafx.application.Platform.isFxApplicationThread() */
        return;
    }
    
    final class LFlayer {
        public final Node pic;
        public final int x, y;
        public final int width;
        
        public LFlayer(String path, int xx, int yy, int w) {
            x = xx;
            y = yy;
            width = w;
            switch (path) {
                case "originBg1":
                    pic = new Rectangle(width, LFmap.defaultWorldHeight,
                        new LinearGradient(0f, 1f, 1f, 0f, true, CycleMethod.NO_CYCLE, new Stop[] {
                            new Stop(0.00, Color.web("#f8bd55")),
                            new Stop(0.14, Color.web("#c0fe56")),
                            new Stop(0.28, Color.web("#5dfbc1")),
                            new Stop(0.43, Color.web("#64c2f8")),
                            new Stop(0.57, Color.web("#be4af7")),
                            new Stop(0.71, Color.web("#ed5fc2")),
                            new Stop(0.85, Color.web("#ef504c")),
                            new Stop(1.00, Color.web("#f2660f"))
                        })
                    );
                    pic.setLayoutY(0.0);
                    break;
                case "originBg2":
                    pic = new Rectangle(width, (zboundB - zboundT) + 12, new Color(0.0, 0.0, 0.0, 0.3));
                    pic.setLayoutY(zboundT - 7);
                    break;
                default:
                    pic = null;
            }
            pic.setLayoutX(0.0);
            
        }
        
    }
    
}
