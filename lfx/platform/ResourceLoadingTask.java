package lfx.platform;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import lfx.base.Controller;
import lfx.data.*;

class ResourceLoadingTask extends Task<Void> {
  private Consumer<Scene> sceneChanger;
  private List<Controller> controllerList = new ArrayList<>(8);

  ResourceLoadingTask(Consumer<Scene> sceneChanger) {
    this.sceneChanger = sceneChanger;
  }

  @Override
  public Void call() {
    // This method does not run on FX application thread.
    int index = 0;
    for (String[] stringArray : ConfigScene.load()) {
      controllerList.add(new KeyboardController(stringArray));
      this.updateMessage("Load Controller " + ++index);
    }
    this.updateMessage("Load " + Template.of().getIdentifier());
    return null;
  }

  @Override
  protected void succeeded() {
    super.succeeded();
    GuiScene scene = new PickingScene(controllerList);
    sceneChanger.accept(scene.makeScene(sceneChanger));
    return;
  }

}
