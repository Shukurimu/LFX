package lfx.util;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class ImageCell {
  static final WritablePixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();
  public static final ImageCell SELECTION_IDLE_IMAGE;
  public static final ImageCell SELECTION_RANDOM_IMAGE;

  public final Image normal;
  public final Image mirror;

  private ImageCell(Image normal, Image mirror) {
    this.normal = normal;
    this.mirror = mirror;
  }

  private ImageCell(Image single) {
    this.normal = this.mirror = single;
  }

  public Image get(boolean faceRight) {
    return faceRight ? normal : mirror;
  }

  public Image get() {
    return normal;
  }

  private static Image loadImage(String path) {
    try {
      return new Image(path);
    } catch (Exception expection) {
      expection.printStackTrace();
    }
    System.err.printf("Exception in loading [%s].%n", path);
    return null;
  }

  public static ImageCell loadPortrait(String path) {
    return new ImageCell(loadImage(path));
  }

  /**
   * Some Bitmaps are not loaded correctly in JavaFX.
   * You may need to convert them into standard format in advance.
   */
  public static List<ImageCell> loadImageCells(String path, int w, int h, int row, int col) {
    Image rawImage = loadImage(path);
    if (rawImage.isError()) {
      return List.of();
    }
    final int width = (int) rawImage.getWidth();
    final int height = (int) rawImage.getHeight();
    final int[] pixels = new int[width * height];
    rawImage.getPixelReader().getPixels(0, 0, width, height, pixelFormat, pixels, 0, width);
    Arrays.setAll(pixels, i -> pixels[i] == 0xff000000 ? 0 : pixels[i]);

    WritableImage matteImage = new WritableImage(width, height);
    matteImage.getPixelWriter().setPixels(0, 0, width, height, pixelFormat, pixels, 0, width);
    PixelReader reader = matteImage.getPixelReader();

    ImageCell nothing = new ImageCell(new WritableImage(w, h));
    List<ImageCell> pictureList = new ArrayList<>(w * h);
    final int[] buffer = new int[w * h];
    for (int y = 0; y < col; y += h + 1) {  // +1 for separating line
      for (int x = 0; x < row; x += w + 1) {
        if (x + w <= width && y + h <= height) {
          Image normal = new WritableImage(reader, x, y, w, h);
          // programmically mirror image
          reader.getPixels(x, y, w, h, pixelFormat, buffer, 0, w);
          for (int i = 0; i < h; ++i) {
            for (int l = i * w, r = l + w - 1; l < r; ++l, --r) {
              int value = buffer[l];
              buffer[l] = buffer[r];
              buffer[r] = value;
            }
          }
          WritableImage mirror = new WritableImage(w, h);
          mirror.getPixelWriter().setPixels(0, 0, w, h, pixelFormat, buffer, 0, w);
          pictureList.add(new ImageCell(normal, mirror));
        } else {
          // index-out-of-bound shows nothing
          pictureList.add(nothing);
        }
      }
    }
    return pictureList;
  }

  static {
    double width = 180.0;
    Canvas canvas = new Canvas(width, width);
    GraphicsContext gc = canvas.getGraphicsContext2D();
    SELECTION_IDLE_IMAGE = new ImageCell(canvas.snapshot(null, null));

    gc.setFill(Color.BLACK);
    gc.fillRect(0.0, 0.0, width, width);
    gc.setFill(Color.WHITE);
    gc.setFont(Font.font(null, FontWeight.BOLD, width - 2.0 * 15.0));
    gc.setTextAlign(TextAlignment.CENTER);
    gc.fillText("?", width / 2.0, width / 2.0);
    SELECTION_RANDOM_IMAGE = new ImageCell(canvas.snapshot(null, null));
  }

}
