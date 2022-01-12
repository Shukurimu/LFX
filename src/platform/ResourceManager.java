package platform;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.concurrent.Task;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import base.BaseController;
import base.Controller;
import base.InputMonitor;
import data.*;
import object.Observable;
import object.Playable;
import setting.Configure;
import util.Tuple;

class ResourceManager {
  static final List<Controller> controllerList = new ArrayList<>();
  static final Map<String, Image> portraitLibrary = new HashMap<>(32);
  static final Map<String, Tuple<Observable, List<Image>>> library = new HashMap<>(128);

  /**
   * Don't let anyone instantiate this class.
   */
  private ResourceManager() {}

  static Task<Void> loadTask() {
    return new Task<Void>() {
      @Override
      public Void call() throws Exception {
        // This method does not run on FX application thread.
        updateMessage("Controllers");
        for (String[] inputSetting : Configure.load().getInputSetting()) {
          controllerList.add(makeController(inputSetting));
        }
        updateMessage(register(Template.register()));
        return null;
      }
    };
  }

  /**
   * Makes {@code Controller} and registers keys.
   *
   * @param inputSetting an array of {@code KeyCode} String
   * @return the corresponding {@code Controller}
   */
  private static Controller makeController(String[] inputSetting) {
    Map<Controller.Input, InputMonitor> inputMap = new EnumMap<>(Controller.Input.class);
    for (Controller.Input input : Controller.Input.values()) {
      KeyCode keyCode = null;
      try {
        keyCode = KeyCode.valueOf(inputSetting[input.ordinal()]);
      } catch (IllegalArgumentException ex) {
        keyCode = KeyCode.UNDEFINED;
        System.err.println("Invalid KeyCode: " + inputSetting[input.ordinal()]);
      } catch (IndexOutOfBoundsException ex) {
        keyCode = KeyCode.UNDEFINED;
        System.err.println("Insufficient Elements");
      }
      inputMap.put(input, AbstractScreen.requestMonitor(keyCode));
    }
    return new BaseController(inputMap);
  }

  private static String register(Observable o) throws Exception {
    String identifier = o.getIdentifier();

    if (o instanceof Playable x) {
      Image portrait = new Image("file:" + x.getPortraitPath());
      portraitLibrary.put(o.getIdentifier(), portrait);
    }

    ArrayList<Image> pictureList = new ArrayList<>(512);
    for (Tuple<String, int[]> t : o.getPictureInfo()) {
      int[] cell = t.second;
      pictureList.addAll(loadImageCells(t.first, cell[0], cell[1], cell[2], cell[3]));
    }
    pictureList.trimToSize();
    library.put(o.getIdentifier(), new Tuple<>(o, pictureList));
    return identifier;
  }

  /**
   * Some Bitmaps are not loaded correctly in JavaFX.
   * You may need to convert them into standard format in advance.
   */
  static List<Image> loadImageCells(String path, int w, int h, int row, int col) throws Exception {
    Image rawImage = new Image("file:" + path);

    final int width = (int) rawImage.getWidth();
    final int height = (int) rawImage.getHeight();
    final int[] pixels = new int[width * height];

    WritablePixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();
    rawImage.getPixelReader().getPixels(0, 0, width, height, pixelFormat, pixels, 0, width);
    Arrays.setAll(pixels, i -> pixels[i] == 0xff000000 ? 0 : pixels[i]);  // transparent

    WritableImage matteImage = new WritableImage(width, height);
    matteImage.getPixelWriter().setPixels(0, 0, width, height, pixelFormat, pixels, 0, width);
    PixelReader reader = matteImage.getPixelReader();

    Image nothing = new WritableImage(w, h);
    List<Image> imageList = new ArrayList<>(w * h);
    for (int iCol = 0, y = 0; iCol < col; ++iCol, y += h + 1) {  // +1 for separating line
      for (int iRow = 0, x = 0; iRow < row; ++iRow, x += w + 1) {
        if (x + w <= width && y + h <= height) {
          imageList.add(new WritableImage(reader, x, y, w, h));
        } else {
          // IndexOutOfBounds shows nothing.
          imageList.add(nothing);
        }
      }
    }
    return imageList;
  }

  /**
   * @param width of generated {@code Image}
   * @throws IllegalStateException if snapshot() is called on a thread other than
   *                               the JavaFX Application Thread
   */
  private static Image drawSelectionRandomImage(double width) {
    Canvas canvas = new Canvas(width, width);
    GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.setFill(Color.BLACK);
    gc.fillRect(0.0, 0.0, width, width);
    gc.setFill(Color.WHITE);
    gc.setFont(Font.font(null, FontWeight.BOLD, width - 2.0 * 15.0));
    gc.setTextAlign(TextAlignment.CENTER);
    gc.fillText("?", width * 0.5, width * 0.8);
    return canvas.snapshot(null, null);
  }

}
