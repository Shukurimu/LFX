package platform;

import java.util.List;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import object.Playable;

public class StatusBoard extends Canvas {
  public static final double CANVAS_WIDTH = 794 / 4;
  public static final double CANVAS_HEIGHT = 60;
  private static final double PADDING = 5.0;
  private static final double ICON_SIZE = CANVAS_HEIGHT - PADDING * 2.0;
  private static final double BAR_BEGIN = ICON_SIZE + PADDING * 2.0;
  private static final double BAR_WIDTH = CANVAS_WIDTH - BAR_BEGIN - PADDING - 1.0;
  private static final double BAR_HEIGHT = 15.0;
  private static final double BAR_MARGIN = (CANVAS_HEIGHT - BAR_HEIGHT * 2.0) / 3.0;
  private static final double TEXT_X = BAR_BEGIN + BAR_WIDTH - 1.0;
  private static final double HP_FILL_Y = BAR_MARGIN;
  private static final double MP_FILL_Y = BAR_MARGIN * 2.0 + BAR_HEIGHT;
  private static final double HP_TEXT_Y = HP_FILL_Y + BAR_HEIGHT / 2.0;
  private static final double MP_TEXT_Y = MP_FILL_Y + BAR_HEIGHT / 2.0;
  private static final Color BACKGROUND_COLOR = Color.CORNFLOWERBLUE;
  private static final Color CONTAINER_COLOR = BACKGROUND_COLOR.darker();
  private static final LinearGradient HP_BAR_BASE;
  private static final LinearGradient HP_BAR_CURR;
  private static final LinearGradient MP_BAR_BASE;
  private static final LinearGradient MP_BAR_CURR;
  private static final LinearGradient BAR_3D_LOOK;

  private final Playable target;
  private final double[] status = new double[6];
  private final GraphicsContext gc;

  static {
    List<Stop> HP_STOPS = List.of(
        new Stop(0.0, Color.CRIMSON),
        new Stop(0.4, Color.DARKORANGE),
        new Stop(0.7, Color.GOLD),
        new Stop(1.0, Color.MEDIUMSPRINGGREEN)
    );
    HP_BAR_CURR = new LinearGradient(
        BAR_BEGIN, HP_FILL_Y, BAR_BEGIN + BAR_WIDTH, HP_FILL_Y + BAR_HEIGHT,
        false, CycleMethod.NO_CYCLE,
        HP_STOPS.stream().map(s -> new Stop(s.getOffset(), s.getColor().brighter())).toList());
    HP_BAR_BASE = new LinearGradient(
        BAR_BEGIN, HP_FILL_Y, BAR_BEGIN + BAR_WIDTH, HP_FILL_Y + BAR_HEIGHT,
        false, CycleMethod.NO_CYCLE,
        HP_STOPS.stream().map(s -> new Stop(s.getOffset(), s.getColor().darker())).toList());

    List<Stop> MP_STOPS = List.of(
        new Stop(0.0, Color.DARKORCHID),
        new Stop(1.0, Color.AQUA)
    );
    MP_BAR_CURR = new LinearGradient(
        BAR_BEGIN, MP_FILL_Y, BAR_BEGIN + BAR_WIDTH, MP_FILL_Y + BAR_HEIGHT,
        false, CycleMethod.NO_CYCLE,
        MP_STOPS.stream().map(s -> new Stop(s.getOffset(), s.getColor().brighter())).toList());
    MP_BAR_BASE = new LinearGradient(
        BAR_BEGIN, MP_FILL_Y, BAR_BEGIN + BAR_WIDTH, MP_FILL_Y + BAR_HEIGHT,
        false, CycleMethod.NO_CYCLE,
        MP_STOPS.stream().map(s -> new Stop(s.getOffset(), s.getColor().darker())).toList());

    BAR_3D_LOOK = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, List.of(
        new Stop(0.0, Color.color(0.4, 0.4, 0.4, 0.5)),
        new Stop(0.2, Color.color(0.6, 0.6, 0.6, 0.5)),
        new Stop(1.0, Color.color(0.0, 0.0, 0.0, 0.5))
    ));
  }

  private StatusBoard(Playable target) {
    super(CANVAS_WIDTH, CANVAS_HEIGHT);
    this.target = target;
    gc = this.getGraphicsContext2D();
  }

  private void initialize(Image portrait) {
    gc.setFill(BACKGROUND_COLOR);
    gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    gc.drawImage(portrait, PADDING, PADDING, ICON_SIZE, ICON_SIZE);
    gc.setLineWidth(1.0);
    gc.setFill(Color.BLACK);
    gc.strokeRect(PADDING - 1.0, PADDING - 1.0, ICON_SIZE + 2.0, ICON_SIZE + 2.0);
    gc.strokeRect(BAR_BEGIN - 1.0, HP_FILL_Y - 1.0, BAR_WIDTH + 2.0, BAR_HEIGHT + 2.0);
    gc.strokeRect(BAR_BEGIN - 1.0, MP_FILL_Y - 1.0, BAR_WIDTH + 2.0, BAR_HEIGHT + 2.0);
    gc.setFont(Font.font("Verdana"));
    gc.setStroke(Color.MAROON);
    gc.setTextBaseline(VPos.CENTER);
    gc.setTextAlign(TextAlignment.RIGHT);
    return;
  }

  public static StatusBoard of(Playable target, Image portrait) {
    StatusBoard s = new StatusBoard(target);
    s.initialize(portrait);
    return s;
  }

  public static StatusBoard ofEmpty() {
    StatusBoard s = new StatusBoard(Playable.SELECTION_IDLE) {
      @Override public void update() {}
    };
    s.initialize(null);
    return s;
  }

  public void update() {
    target.fillStatus(status);
    double hp2Length = status[0] / status[1] * BAR_WIDTH;
    double hp1Length = status[2] / status[3] * BAR_WIDTH;
    double mp0Length = status[4] / status[5] * BAR_WIDTH;
    gc.setFill(CONTAINER_COLOR);
    gc.fillRect(BAR_BEGIN, HP_FILL_Y, BAR_WIDTH, BAR_HEIGHT);
    gc.setFill(HP_BAR_BASE);
    gc.fillRect(BAR_BEGIN, HP_FILL_Y, hp2Length, BAR_HEIGHT);
    gc.setFill(HP_BAR_CURR);
    gc.fillRect(BAR_BEGIN, HP_FILL_Y, hp1Length, BAR_HEIGHT);
    gc.setFill(MP_BAR_BASE);
    gc.fillRect(BAR_BEGIN, MP_FILL_Y, BAR_WIDTH, BAR_HEIGHT);
    gc.setFill(MP_BAR_CURR);
    gc.fillRect(BAR_BEGIN, MP_FILL_Y, mp0Length, BAR_HEIGHT);
    gc.setGlobalBlendMode(BlendMode.HARD_LIGHT);
    gc.setFill(BAR_3D_LOOK);
    gc.fillRect(BAR_BEGIN, HP_FILL_Y, hp1Length, BAR_HEIGHT);
    gc.fillRect(BAR_BEGIN, MP_FILL_Y, mp0Length, BAR_HEIGHT);
    gc.setGlobalBlendMode(BlendMode.SRC_OVER);
    gc.strokeText(String.format("%.0f", status[2]), TEXT_X, HP_TEXT_Y);
    gc.strokeText(String.format("%.0f", status[4]), TEXT_X, MP_TEXT_Y);
    return;
  }

}
