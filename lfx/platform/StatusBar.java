import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;

public class StatusBar extends Canvas {
  public static final double padding = 5.0;
  public static final double iconSize = 50.0;
  public static final double canvasWidth = LFmap.defaultWorldWidth / LFsetting.PLAYER_NUM;
  public static final double canvasHeight = iconSize + padding * 2.0;
  public static final double barStart = iconSize + padding * 2.0;
  public static final double barWidth = canvasWidth - barStart - padding - 1.0;
  public static final double barHeight = 15.0;
  public static final double barmargin = (canvasHeight - barHeight * 2.0) / 3.0;
  public static final double hpY = barmargin;
  public static final double mpY = barmargin * 2.0 + barHeight;
  public static final double hpTextY = hpY + barHeight / 2.0;
  public static final double mpTextY = mpY + barHeight / 2.0;
  public static final double textPos = barStart + barWidth - 1.0;
  public static final  Color canvasBackgroundColor = Color.ROYALBLUE;
  public static final  Color canvasBackgroundColorDarker = canvasBackgroundColor.darker();
  public static final  Color[] hpBarColor = { Color.CRIMSON, Color.DARKORANGE, Color.GOLD, Color.MEDIUMSPRINGGREEN };
  public static final double[] hpBarIndex = { 0.0, 0.4, 0.7, 1.0 };
  public static final  Color[] mpBarColors = { Color.DARKORCHID, Color.AQUA };
  public static final double[] mpBarIndexs = { 0.0, 1.0 };
  public static final LinearGradient hpBarBase =
      new LinearGradient(barStart, hpY, barStart + barWidth, hpY + barHeight, false, CycleMethod.NO_CYCLE, new Stop[] {
          new Stop(hpBarIndex[0], hpBarColor[0].darker()),
          new Stop(hpBarIndex[1], hpBarColor[1].darker()),
          new Stop(hpBarIndex[2], hpBarColor[2].darker()),
          new Stop(hpBarIndex[3], hpBarColor[3].darker())
      });
  public static final LinearGradient hpBarCurr =
      new LinearGradient(barStart, hpY, barStart + barWidth, hpY + barHeight, false, CycleMethod.NO_CYCLE, new Stop[] {
          new Stop(hpBarIndex[0], hpBarColor[0].brighter()),
          new Stop(hpBarIndex[1], hpBarColor[1].brighter()),
          new Stop(hpBarIndex[2], hpBarColor[2].brighter()),
          new Stop(hpBarIndex[3], hpBarColor[3].brighter())
      });
  public static final LinearGradient bar3dLook =
      new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop[] {
          new Stop(0.0, Color.color(0.4, 0.4, 0.4, 0.5)),
          new Stop(0.2, Color.color(0.6, 0.6, 0.6, 0.5)),
          new Stop(1.0, Color.color(0.0, 0.0, 0.0, 0.5))
      });
  public static final LinearGradient mpBarBase =
      new LinearGradient(barStart, mpY, barStart + barWidth, mpY + barHeight, false, CycleMethod.NO_CYCLE, new Stop[] {
          new Stop(mpBarIndexs[0], mpBarColors[0].darker()),
          new Stop(mpBarIndexs[1], mpBarColors[1].darker())
      });
  public static final LinearGradient mpBarCurr =
      new LinearGradient(barStart, mpY, barStart + barWidth, mpY + barHeight, false, CycleMethod.NO_CYCLE, new Stop[] {
          new Stop(mpBarIndexs[0], mpBarColors[0].brighter()),
          new Stop(mpBarIndexs[1], mpBarColors[1].brighter())
      });
  
  private final GraphicsContext gc;
  private final double[] values = new double[4];
  public LFhero hero = null;
  
  public static ArrayList<LFstatus> buildEmpty() {
      ArrayList<LFstatus> temp = new ArrayList<>(LFsetting.PLAYER_NUM);
      for (int i = 0; i < LFsetting.PLAYER_NUM; ++i)
          temp.add(new LFstatus());
      return temp;
  }
  
  public LFstatus() {
      super(canvasWidth, canvasHeight);
      gc = this.getGraphicsContext2D();
      gc.setFill(canvasBackgroundColor);
      gc.fillRect(0, 0, canvasWidth, canvasHeight);
      gc.setLineWidth(1.0);
      gc.setFill(Color.BLACK);
      gc.strokeRect(padding - 1.0, padding - 1.0, iconSize + 2.0, iconSize + 2.0);
      gc.strokeRect(barStart - 1.0, hpY - 1.0, barWidth + 2.0, barHeight + 2.0);
      gc.strokeRect(barStart - 1.0, mpY - 1.0, barWidth + 2.0, barHeight + 2.0);
      gc.setTextBaseline(javafx.geometry.VPos.CENTER);
      gc.setTextAlign(javafx.scene.text.TextAlignment.RIGHT);
  }
  
  public void setHero(LFhero hh) {
      hero = hh;
      gc.drawImage(hero.faceImage, padding, padding, iconSize, iconSize);
  }
  
  public void draw() {
      hero.getHealth(values);
      gc.setFill(canvasBackgroundColorDarker);
      gc.fillRect(barStart, hpY, barWidth, barHeight);
      double hp2prop = values[0] * barWidth;
      gc.setFill(hpBarBase);
      gc.fillRect(barStart, hpY, hp2prop, barHeight);
      double hp1prop = values[1] * barWidth;
      gc.setFill(hpBarCurr);
      gc.fillRect(barStart, hpY, hp1prop, barHeight);
      /* mp has no bound */
      gc.setFill(mpBarBase);
      gc.fillRect(barStart, mpY, barWidth, barHeight);
      double mp1prop = values[2] * barWidth;
      gc.setFill(mpBarCurr);
      gc.fillRect(barStart, mpY, mp1prop, barHeight);
      /* make 3d-looks bar */
      gc.setGlobalBlendMode(BlendMode.HARD_LIGHT);
      gc.setFill(bar3dLook);
      gc.fillRect(barStart, hpY, hp1prop, barHeight);
      gc.fillRect(barStart, mpY, mp1prop, barHeight);
      gc.setGlobalBlendMode(BlendMode.SRC_OVER);
      /* display real value */
      // gc.strokeText(String.format("%.0f", hero.hp), textPos, hpTextY);
      // gc.strokeText(String.format("%.0f", hero.mp), textPos, mpTextY);
      return;
  }
  
}
