package platform;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.Parent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public final class Main extends Application {

  @Override
  public void start(Stage primaryStage) {
    AbstractScreen.setPrimaryStage(primaryStage);

    final VBox guiContainer = new VBox(16.0);
    final Screen mainScreen = new AbstractScreen() {
      @Override protected Parent makeParent() { return guiContainer; }
      @Override protected void keyHandler(KeyCode keyCode) {}
    };

    Task<Void> loadTask = ResourceManager.loadTask();
    loadTask.setOnSucceeded(event -> {
      System.err.println("Succeeded");
      Screen pickingScene = new PickingScreen();
      primaryStage.setScene(pickingScene.getScene());
    });
    loadTask.setOnFailed(event -> {
      System.err.println("Failed");
      loadTask.getException().printStackTrace();
    });

    Text banner = new Text("Little Fighter X");
    banner.setEffect(new InnerShadow(6.0, 3.0, 3.0, Color.DARKORCHID));
    banner.setFill(Color.AQUA);
    banner.setFont(Font.font("Lucida Calligraphy", FontWeight.BLACK, 80));

    Text messageText = new Text("");

    Button playButton = new Button("Game Start");
    playButton.setFont(Font.font(null, FontWeight.BLACK, 32));
    playButton.setOnAction(event -> {
      playButton.setDisable(true);
      AbstractScreen.setEscapeExit();
      messageText.textProperty().bind(loadTask.messageProperty());
      primaryStage.setResizable(false);
      new Thread(loadTask).start();
    });

    Button configButton = new Button("Configuration");
    configButton.setFont(Font.font(20.0));
    configButton.setOnAction(event -> {
      mainScreen.gotoNext(new ConfigScreen(messageText.textProperty()));
    });

    guiContainer.setAlignment(Pos.CENTER);
    guiContainer.getChildren().addAll(banner, playButton, configButton, messageText);
    primaryStage.setResizable(false);
    primaryStage.setScene(mainScreen.getScene());
    primaryStage.setTitle("Little Fighter X");
    primaryStage.setX(600.0);
    primaryStage.show();
    return;
  }

  @Override
  public void stop() {
    System.out.println("[Exit]");
    javafx.application.Platform.exit();
    return;
  }

  public static void main(String[] args) {
    Application.launch(args);
    return;
  }

}
