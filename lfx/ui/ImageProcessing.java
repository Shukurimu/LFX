
    x1 = px - owner.currFrame.centerR + itr.x;
    x2 = x1 + itr.w;
  } else {
    x2 = px + owner.currFrame.centerR - itr.x;
    x1 = x2 - itr.w;
  }
  y1 = owner.py - owner.currFrame.centerY + itr.y;
  public final boolean updateFXnode() {
    if (objectExist) {
      fxNode.setX(px - (faceRight ? currFrame.centerR : currFrame.centerL));
      fxNode.setY(pz + py - currFrame.centerY);
      fxNode.setViewOrder(pz);
      fxNode.setImage(faceRight ? currFrame.image1 : currFrame.image2);
      return true;
    } else {
      /* for JavaFx ObservableList */
      fxNode.setVisible(false);
      fxNode = null;
      return false;
    }
  }

  public static Image loadImage(String path) {
    Image src = null;
    try {
      src = new Image(LFobject.class.getResource(path).openStream());
    } catch (Exception ouch) {
      ouch.printStackTrace();
      System.err.println("Exception in loading " + path);
    }
    return src;
  }

  /* some Bitmap are not loaded correctly in JavaFX, you should pre-convert it to standard format */
  protected final void loadImageCells(String path, int w, int h, int row, int col) {
    Image origin = loadImage(path);
    final int ow = (int)origin.getWidth();
    final int oh = (int)origin.getHeight();
    final int ln = ow * oh;
    int[] pixels = new int[ln];
    /* row values might be out of bound */
    int realRow = Math.min(row, (ow + 1) / (w + 1));

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

    /* programmically mirror images */
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
    /* index out of bound showing nothing */
    WritableImage nothing = new WritableImage(w, h);

    PixelReader reader1 = buff1.getPixelReader();
    PixelReader reader2 = buff2.getPixelReader();
    for (int i = 0; i < col; ++i) {
      for (int j = 0; j < row; ++j) {
        /* plus one for 1px seperating line */
        final int x = j * (w + 1);
        final int y = i * (h + 1);
        if (x + w < ow && y + h < oh) {
          picture1.add(new WritableImage(reader1, x, y, w, h));
          picture2.add(new WritableImage(reader2, x, y, w, h));
        } else {
          picture1.add(nothing);
          picture2.add(nothing);
        }
      }
    }
    return;
  }
