package lfx.platform;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public interface Graphical {
  private static final Image PRESS_TO_JOIN_IMAGE;
  private static final Image RANDOMLY_PICK_IMAGE;
  static {
    final double size = 180.0;
    Canvas canvas = new Canvas(size, size);
    PRESS_TO_JOIN_IMAGE = canvas.snapshot(null, null);
    GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.setFill(Color.BLACK);
    gc.fillRect(0.0, 0.0, size, size);
    gc.setTextBaseline(VPos.CENTER);
    gc.setTextAlign(TextAlignment.CENTER);
    gc.setFill(Color.WHITE);
    gc.setFont(Font.font(null, FontWeight.BOLD, size - 2.0 * 15.0));
    gc.fillText("?", size / 2.0, size / 2.0);
    RANDOMLY_PICK_IMAGE = c.snapshot(null, null);
  }

  public static String convertTeamId(int teamId) {
    return teamId > Global.MAX_TEAMS ? "Independent" : ("Team " + teamId);
  }

  public Image getFaceImage();

  public default String getDisplayingIdentifier() {
    return "";
  }

  public default String getDisplayingTeamId() {
    return "";
  }

  public static final Graphical PRESS_TO_JOIN = new Graphical() {
    @Override public Image getFaceImage() {
      return PRESS_TO_JOIN_IMAGE;
    }
  };

  public static final Graphical RANDOMLY_PICK = new Graphical() {
    @Override public Image getFaceImage() {
      return RANDOMLY_PICK_IMAGE;
    }
  };

  // The followings are utilities.

  public static void updateFXnode(Node node) {
    node.setX(px - (faceRight ? currFrame.centerR : currFrame.centerL));
    node.setY(pz + py - currFrame.centerY);
    node.setViewOrder(pz);
    node.setImage(faceRight ? currFrame.image1 : currFrame.image2);
    return;
  }

  public static Image loadImage(String path) {
    Image image = null;
    try {
      image = new Image(LFX.class.getResource(path).openStream());
    } catch (Exception expection) {
      expection.printStackTrace();
      System.err.println("Exception in loading " + path);
    }
    return image;
  }

  /** Some Bitmap are not loaded correctly in JavaFX,
      you may need to pre-convert it into standard format. */
  public static Tuple<List<Image>, List<Image>> loadImageCells(
      String path, int w, int h, int row, int col) {
    Image origin = loadImage(path);
    final int ow = (int)origin.getWidth();
    final int oh = (int)origin.getHeight();
    final int ln = ow * oh;
    int[] pixels = new int[ln];
    int realRow = Math.min(row, (ow + 1) / (w + 1));  // row values can be out of bound

    if (origin.isError()) {
      // (new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,
      // "Resource Error: " + path + "\n" + origin.getException())).showAndWait();
      System.out.println("Resource Error: " + path + "\n" + origin.getException());
      javafx.application.Platform.exit();
      return;
    }
    PixelReader reader = origin.getPixelReader();
    reader.getPixels(0, 0, ow, oh, PixelFormat.getIntArgbPreInstance(), pixels, 0, ow);
    for (int i = 0; i < ln; ++i) {
      if (pixels[i] == 0xff000000)
        pixels[i] = 0;
    }
    WritableImage buff1 = new WritableImage(ow, oh);
    buff1.getPixelWriter().setPixels(0, 0, ow, oh, PixelFormat.getIntArgbPreInstance(), pixels, 0, ow);

    /** Programmically mirror the image. */
    for (int r = 0; r < oh; ++r) {
      if ((r + 1) % (h + 1) == 0)
        continue;
      final int s = r * ow;
      for (int c = 0; c < realRow; ++c) {
        final int x = s + c * (w + 1);
        for (int i = x, j = x + w - 1; i < j; ++i, --j) {
          int ptemp = pixels[i];
          pixels[i] = pixels[j];
          pixels[j] = ptemp;
        }
      }
    }
    WritableImage buff2 = new WritableImage(ow, oh);
    buff2.getPixelWriter().setPixels(0, 0, ow, oh, PixelFormat.getIntArgbPreInstance(), pixels, 0, ow);

    ArrayList<Image> pictureList1 = new ArrayList<>(w * h);
    ArrayList<Image> pictureList2 = new ArrayList<>(w * h);
    WritableImage nothing = new WritableImage(w, h);
    PixelReader reader1 = buff1.getPixelReader();
    PixelReader reader2 = buff2.getPixelReader();
    for (int i = 0; i < col; ++i) {
      for (int j = 0; j < row; ++j) {
        // +1px for separating line
        final int x = j * (w + 1);
        final int y = i * (h + 1);
        if (x + w < ow && y + h < oh) {
          pictureList1.add(new WritableImage(reader1, x, y, w, h));
          pictureList2.add(new WritableImage(reader2, x, y, w, h));
        } else {
          // index-out-of-bound shows nothing.
          pictureList1.add(nothing);
          pictureList2.add(nothing);
        }
      }
    }
    return new Tuple<>(pictureList1, pictureList2);
  }
}
