package lfx.platform;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import lfx.platform.ConfigScene;
import lfx.platform.Engine;
import lfx.util.Controller;

public final class Main extends Application {
  Consumer<Scene> sceneChanger = null;

  @Override
  public void start(Stage primaryStage) {
    sceneChanger = (Scene scene) -> primaryStage.setScene(scene);
    VBox vbox = new VBox(8.0);
    vbox.setAlignment(Pos.CENTER);
    Scene startScene = new Scene(vbox, Engine.WINDOW_WIDTH, Engine.WINDOW_HEIGHT);

    Text banner = new Text("Little Fighter X");
    banner.setEffect(new InnerShadow(6.0, 3.0, 3.0, Color.DARKORCHID));
    banner.setFill(Color.AQUA);
    banner.setFont(Font.font("Lucida Calligraphy", FontWeight.BLACK, 80));

    Text author = new Text("(._.)");
    author.setVisible(false);

    Text messageText = new Text("");

    Button playButton = new Button("Game Start");

    playButton.setFont(Font.font(null, FontWeight.BLACK, 32));
    Button configButton = new Button("Control Settings");
    configButton.setFont(Font.font(null, FontWeight.EXTRA_BOLD, 24));

    playButton.setOnAction(event -> {
      primaryStage.setResizable(false);
      playButton.setVisible(false);
      configButton.setVisible(false);
      LoadingTask task = new LoadingTask(messageText);
      (new Thread(task)).start();
    });

    Consumer<String> configSceneBridge = (String result) -> {
      messageText.setText(result);
      primaryStage.setScene(startScene);
    };
    configButton.setOnAction(event -> {
      ConfigScene scene = new ConfigScene(configSceneBridge);
      primaryStage.setScene(scene.makeScene());
    });

    vbox.getChildren().addAll(banner, author, play, messageText, sett);
    // primaryStage.setResizable(false);
    primaryStage.setScene(startScene);
    primaryStage.setTitle("Little Fighter X");
    primaryStage.setX(600.0);
    primaryStage.show();
    return;
  }

  @Override
  public void stop() {
    System.out.println("[Exit]");
    System.exit(0);
    return;
  }

  private class LoadingTask extends Task<Void> {
    List<Controller> controllerList = new ArrayList<>();

    public LoadingTask(Text text) {
      text.textProperty().bind(this.messageProperty());
    }

    @Override
    public Void call() {
      System.out.println("isFxApplicationThread: " + Platform.isFxApplicationThread());
      for (String[] stringArray : ConfigScene.loadControlStringArrayList()) {
        controllerList.add(new KeyboardControl(stringArray));
      }
      return null;
    }

    @Override
    protected void succeeded() {
      super.succeeded();
      sceneChanger.accept(new PickingScene(sceneChanger, controllerList));
      return;
    }

  }

  public static Scene sceneBuilder(Parent parent, double cpw, double cph) {
    double width = 1600;
    double height = 900;
    parent.getTransforms().setAll(new Scale(width / cpw, height / cph));
    return new Scene(parent, width, height, Color.BLACK);
  }

  public static void main(String[] args) {
    Application.launch(args);
    return;
  }

}
