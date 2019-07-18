import java.io.*;
/*
https://www.lf-empire.de/forum/showthread.php?tid=1877
Programmed by Blue Phoenix
Explanations and help by Silva
Latest version: 14 Jan 2009
*/
public final class RawConv {
    
    public static void main(String[] args) throws IOException {
        File folder = null;
        if ((args.length > 0) && (folder = new File(args[0])).isDirectory()) {
            final byte[] encryptChar = "odBearBecauseHeIsVeryGoodSiuHungIsAGo".getBytes();
            for (File f: folder.listFiles(x -> (x.getAbsoluteFile().isFile() && x.getName().endsWith(".dat")))) {
                String newFileName = f.getName().replaceFirst("dat$", "txt");
                FileOutputStream os = new FileOutputStream(newFileName);
                FileInputStream is = new FileInputStream(f.getAbsolutePath());
                is.skip(123);
                int readBytes = 0;
                byte[] buffer = new byte[encryptChar.length];
                while ((readBytes = is.read(buffer)) > 0) {
                    for (int i = 0; i < readBytes; ++i)
                        buffer[i] -= encryptChar[i];
                    os.write(buffer, 0, readBytes);
                }
                os.close();
                is.close();
                System.out.printf("Converted: %s\n", newFileName);
            }
        } else
            System.err.println("Input: the folder containing *.dat files");
        return;
    }
    
}
