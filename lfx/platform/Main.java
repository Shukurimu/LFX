package lfx.platform;

import java.util.function.Consumer;
import javafx.application.Application;
import javafx.application.Platform;
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
    ResourceLoadingTask loadingTask =
        new ResourceLoadingTask((Scene scene) -> primaryStage.setScene(scene));

    Text banner = new Text("Little Fighter X");
    banner.setEffect(new InnerShadow(6.0, 3.0, 3.0, Color.DARKORCHID));
    banner.setFill(Color.AQUA);
    banner.setFont(Font.font("Lucida Calligraphy", FontWeight.BLACK, 80));

    Text author = new Text("(._.)");

    Text messageText = new Text("");
    messageText.textProperty().bind(loadingTask.messageProperty());

    Button playButton = new Button("Game Start");
    playButton.setFont(Font.font(null, FontWeight.BLACK, 32));
    playButton.setOnAction(event -> {
      primaryStage.setResizable(false);
      playButton.setVisible(false);
      new Thread(loadingTask).start();
    });

    VBox guiContainer = new VBox(16.0, banner, author, playButton, messageText);
    guiContainer.setAlignment(Pos.CENTER);  // defaults to Pos.TOP_LEFT

    Scene startScene = new Scene(guiContainer, GuiScene.WINDOW_WIDTH, GuiScene.WINDOW_HEIGHT);
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
