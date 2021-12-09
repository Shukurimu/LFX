package map;

import java.util.List;
import javafx.scene.Node;

public class Background {
  public final double width = 900;
  public final double top = 200;
  public final double bottom = 400;
  public final List<Node> elementList;

  public Background() {
    // Currently support native background only.
    elementList = List.of(
        (new Layer("NativeBase", 0, 0, width)).pic,
        (new Layer("NativePath", 0, 0, width)).pic
    );
  }

}
