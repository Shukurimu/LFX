package lfx.util;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import lfx.util.Tuple;

public final class Resource {
  static final WritablePixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();

  private Resource() {}

  public static Image loadImage(String path) {
    try {
      return new Image(path);
    } catch (Exception expection) {
      expection.printStackTrace();
    }
    System.err.printf("Exception in loading [%s].%n", path);
    return null;
  }

  /**
   * Some Bitmaps are not loaded correctly in JavaFX.
   * You may need to convert them into standard format in advance.
   */
  public static List<Tuple<Image, Image>> loadImageCells(
      String path, int w, int h, int row, int col) {
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

    Tuple<Image, Image> nothingTuple = Tuple.of(new WritableImage(w, h));
    List<Tuple<Image, Image>> pictureList = new ArrayList<>(w * h);
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
          pictureList.add(new Tuple<>(normal, mirror));
        } else {
          // index-out-of-bound shows nothing
          pictureList.add(nothingTuple);
        }
      }
    }
    return pictureList;
  }

}
