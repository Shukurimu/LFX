/**
 * https://www.lf-empire.de/forum/showthread.php?tid=1877
 * Programmed by Blue Phoenix
 * Explanations and help by Silva
 * Latest version: 14 Jan 2009
 */

import java.io.*;

public final class RawDatConverter {
  static final byte[] encryptChar = "odBearBecauseHeIsVeryGoodSiuHungIsAGo".getBytes();

  public static void main(String[] args) throws IOException {
    File folder = args.length == 0 ? null : new File(args[0]);
    if (folder == null || !folder.isDirectory()) {
      System.err.println("Input: the path of folder containing *.dat files.");
      return;
    }
    for (File file: folder.listFiles(
         x -> (x.getAbsoluteFile().isFile() && x.getName().endsWith(".dat")))) {
      String rawFileName = file.getName();
      String newFileName = rawFileName.replaceFirst("dat$", "txt");
      FileInputStream is = new FileInputStream(file.getAbsolutePath());
      FileOutputStream os = new FileOutputStream(newFileName);
      is.skip(123);
      int readBytes = 0;
      byte[] buffer = new byte[encryptChar.length];
      while ((readBytes = is.read(buffer)) > 0) {
        for (int i = 0; i < readBytes; ++i) {
          buffer[i] -= encryptChar[i];
        }
        os.write(buffer, 0, readBytes);
      }
      os.close();
      is.close();
      System.out.printf("Convert: %s -> %s%n", rawFileName, newFileName);
    }
    return;
  }

}
