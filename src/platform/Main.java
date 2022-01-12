package platform;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public final class Main extends Application {

  @Override
  public void start(Stage primaryStage) {
    AbstractScreen.sceneChanger = (Scene scene) -> primaryStage.setScene(scene);

    Task<Void> loadTask = ResourceManager.loadTask();
    loadTask.setOnSucceeded(event -> {
      System.err.println("Succeeded");
      Screen pickingScene = new PickingScene();
      primaryStage.setScene(pickingScene.makeScene());
    });
    loadTask.setOnFailed(event -> {
      System.err.println("Failed");
      loadTask.getException().printStackTrace();
    });

    Text banner = new Text("Little Fighter X");
    banner.setEffect(new InnerShadow(6.0, 3.0, 3.0, Color.DARKORCHID));
    banner.setFill(Color.AQUA);
    banner.setFont(Font.font("Lucida Calligraphy", FontWeight.BLACK, 80));

    Text author = new Text("(._.)");

    Text messageText = new Text("");
    messageText.textProperty().bind(loadTask.messageProperty());

    Button playButton = new Button("Game Start");
    playButton.setFont(Font.font(null, FontWeight.BLACK, 32));
    playButton.setOnAction(event -> {
      primaryStage.setResizable(false);
      playButton.setVisible(false);
      new Thread(loadTask).start();
    });

    VBox guiContainer = new VBox(16.0, banner, author, playButton, messageText);
    guiContainer.setAlignment(Pos.CENTER);  // defaults to Pos.TOP_LEFT

    Scene startScene = new Scene(guiContainer, AbstractScreen.WINDOW_WIDTH, AbstractScreen.WINDOW_HEIGHT);
    primaryStage.setResizable(false);
    primaryStage.setScene(startScene);
    primaryStage.setTitle("Little Fighter X");
    primaryStage.setX(600.0);
    primaryStage.show();
    return;
  }

  @Override
  public void stop() {
    System.out.println("[Exit]");
    Platform.exit();
    return;
  }

  public static void main(String[] args) {
    Application.launch(args);
    return;
  }

}
