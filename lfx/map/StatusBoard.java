package lfx.map;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.List;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.text.TextAlignment;
import lfx.object.Hero;
import lfx.util.Const;
import lfx.util.Tuple;
import lfx.util.Viewer;

public class StatusBoard extends Canvas {
  private static final double PADDING = 5.0;
  private static final double ICON_SIZE = Const.CANVAS_HEIGHT - PADDING * 2.0;
  private static final double BAR_BEGIN = ICON_SIZE + PADDING * 2.0;
  private static final double BAR_WIDTH = Const.CANVAS_WIDTH - BAR_BEGIN - PADDING - 1.0;
  private static final double BAR_HEIGHT = 15.0;
  private static final double BAR_MARGIN = (Const.CANVAS_HEIGHT - BAR_HEIGHT * 2.0) / 3.0;
  private static final double TEXT_X = BAR_BEGIN + BAR_WIDTH - 1.0;
  private static final double HP_FILL_Y = BAR_MARGIN;
  private static final double MP_FILL_Y = BAR_MARGIN * 2.0 + BAR_HEIGHT;
  private static final double HP_TEXT_Y = HP_FILL_Y + BAR_HEIGHT / 2.0;
  private static final double MP_TEXT_Y = MP_FILL_Y + BAR_HEIGHT / 2.0;
  private static final Color BACKGROUND_COLOR = Color.ROYALBLUE;
  private static final Color CONTAINER_COLOR = BACKGROUND_COLOR.darker();
  private static final LinearGradient HP_BAR_BASE;
  private static final LinearGradient HP_BAR_CURR;
  private static final LinearGradient MP_BAR_BASE;
  private static final LinearGradient MP_BAR_CURR;
  private static final LinearGradient BAR_3D_LOOK;
  private final Viewer viewer = new Viewer();
  private final Hero hero;
  private final GraphicsContext gc;

  static {
    BiFunction<List<Tuple<Double, Color>>, UnaryOperator<Color>, List<Stop>> buildStops =
        (inputs, op) -> {
          List<Stop> result = new ArrayList<>();
          inputs.forEach(info -> result.add(new Stop(info.first, op.apply(info.second))));
          return result;
        };

    List<Tuple<Double, Color>> hpInfoList = List.of(
        new Tuple<>(0.0, Color.CRIMSON),
        new Tuple<>(0.4, Color.DARKORANGE),
        new Tuple<>(0.7, Color.GOLD),
        new Tuple<>(1.0, Color.MEDIUMSPRINGGREEN)
    );
    HP_BAR_CURR = new LinearGradient(
        BAR_BEGIN, HP_FILL_Y, BAR_BEGIN + BAR_WIDTH, HP_FILL_Y + BAR_HEIGHT,
        false, CycleMethod.NO_CYCLE, buildStops.apply(hpInfoList, c -> c.brighter()));
    HP_BAR_BASE = new LinearGradient(
        BAR_BEGIN, HP_FILL_Y, BAR_BEGIN + BAR_WIDTH, HP_FILL_Y + BAR_HEIGHT,
        false, CycleMethod.NO_CYCLE, buildStops.apply(hpInfoList, c -> c.darker()));

    List<Tuple<Double, Color>> mpInfoList = List.of(
        new Tuple<>(0.0, Color.DARKORCHID),
        new Tuple<>(1.0, Color.AQUA)
    );
    MP_BAR_CURR = new LinearGradient(
        BAR_BEGIN, MP_FILL_Y, BAR_BEGIN + BAR_WIDTH, MP_FILL_Y + BAR_HEIGHT,
        false, CycleMethod.NO_CYCLE, buildStops.apply(mpInfoList, c -> c.brighter()));
    MP_BAR_BASE = new LinearGradient(
        BAR_BEGIN, MP_FILL_Y, BAR_BEGIN + BAR_WIDTH, MP_FILL_Y + BAR_HEIGHT,
        false, CycleMethod.NO_CYCLE, buildStops.apply(mpInfoList, c -> c.darker()));

    List<Tuple<Double, Color>> viewInfoList = List.of(
        new Tuple<>(0.0, Color.color(0.4, 0.4, 0.4, 0.5)),
        new Tuple<>(0.2, Color.color(0.6, 0.6, 0.6, 0.5)),
        new Tuple<>(1.0, Color.color(0.0, 0.0, 0.0, 0.5))
    );
    BAR_3D_LOOK = new LinearGradient(
        0, 0, 0, 1, true, CycleMethod.NO_CYCLE, buildStops.apply(viewInfoList, c -> c));
  }

  public StatusBoard(Hero hero, Image icon) {
    super(Const.CANVAS_WIDTH, Const.CANVAS_HEIGHT);
    this.hero = hero;
    gc = this.getGraphicsContext2D();
    gc.drawImage(icon, PADDING, PADDING, ICON_SIZE, ICON_SIZE);
    gc.setFill(BACKGROUND_COLOR);
    gc.fillRect(0, 0, Const.CANVAS_WIDTH, Const.CANVAS_HEIGHT);
    gc.setLineWidth(1.0);
    gc.setFill(Color.BLACK);
    gc.strokeRect(PADDING - 1.0, PADDING - 1.0, ICON_SIZE + 2.0, ICON_SIZE + 2.0);
    gc.strokeRect(BAR_BEGIN - 1.0, HP_FILL_Y - 1.0, BAR_WIDTH + 2.0, BAR_HEIGHT + 2.0);
    gc.strokeRect(BAR_BEGIN - 1.0, MP_FILL_Y - 1.0, BAR_WIDTH + 2.0, BAR_HEIGHT + 2.0);
    gc.setTextBaseline(VPos.CENTER);
    gc.setTextAlign(TextAlignment.RIGHT);
  }

  public void draw() {
    hero.updateViewer(viewer);
    gc.setFill(CONTAINER_COLOR);
    gc.fillRect(BAR_BEGIN, HP_FILL_Y, BAR_WIDTH, BAR_HEIGHT);
    gc.setFill(HP_BAR_BASE);
    gc.fillRect(BAR_BEGIN, HP_FILL_Y, viewer.hp2ndRatio, BAR_HEIGHT);
    gc.setFill(HP_BAR_CURR);
    gc.fillRect(BAR_BEGIN, HP_FILL_Y, viewer.hpRatio, BAR_HEIGHT);
    gc.setFill(MP_BAR_BASE);
    gc.fillRect(BAR_BEGIN, MP_FILL_Y, BAR_WIDTH, BAR_HEIGHT);
    gc.setFill(MP_BAR_CURR);
    gc.fillRect(BAR_BEGIN, MP_FILL_Y, viewer.mpRatio, BAR_HEIGHT);
    gc.setGlobalBlendMode(BlendMode.HARD_LIGHT);
    gc.setFill(BAR_3D_LOOK);
    gc.fillRect(BAR_BEGIN, HP_FILL_Y, viewer.hpRatio, BAR_HEIGHT);
    gc.fillRect(BAR_BEGIN, MP_FILL_Y, viewer.mpRatio, BAR_HEIGHT);
    gc.setGlobalBlendMode(BlendMode.SRC_OVER);
    // gc.strokeText(String.format("%.0f", hero.hp), TEXT_X, HP_TEXT_Y);
    // gc.strokeText(String.format("%.0f", hero.mp), TEXT_X, MP_TEXT_Y);
    return;
  }

}
