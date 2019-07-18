import java.util.ArrayList;
import java.util.HashMap;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.layout.VBox;
import javafx.scene.effect.InnerShadow;
import javafx.scene.control.Button;
import javafx.concurrent.Task;
import javafx.scene.transform.Scale;
// javac -sourcepath src -cp classes -d classes
public final class LFX extends Application {
	public static final String settingFileName = "setting.txt";
	
	public static final HashMap<String, LFobject> objPool = new HashMap<>(40);
	public static long systemTime = 0L;
	public static LFmap currMap = null;
	public static final ArrayList<LFcontrol> ctrlArray = new ArrayList<>(LFsetting.PLAYER_NUM);
	
	private static double sceneWidth = 0;
	private static double sceneHeight = 0;
	private static Stage primaryStage = null;
	private Scene startScene = null;
	
	@Override
	public void init() {
		LFsetting.load(ctrlArray, settingFileName);
		return;
	}
	
	@Override
	public void start(Stage stage) {
		primaryStage = stage;
		
		VBox vbox = new VBox(8.0);
		vbox.setAlignment(javafx.geometry.Pos.CENTER);
		
		Text banner = new Text("Little Fighter X");
		banner.setEffect(new InnerShadow(6.0, 3.0, 3.0, Color.DARKORCHID));
		banner.setFill(Color.AQUA);
		banner.setFont(Font.font("Lucida Calligraphy", FontWeight.BLACK, 80));
		
		Text author = new Text("");
		author.setVisible(false);
		
		Button play = new Button("Game Start");
		play.setFont(Font.font(null, FontWeight.BLACK, 32));
		
		Button sett = new Button("Control Settings");
		sett.setFont(Font.font(null, FontWeight.EXTRA_BOLD, 24));
		sett.setOnAction(e -> primaryStage.setScene((new LFsetting(this, ctrlArray)).makeScene()));
		
		Text loadingText = new Text("Loading...");
		loadingText.setVisible(false);
		vbox.getChildren().addAll(banner, author, play, loadingText, sett);
		
		play.setOnAction(e -> {
			// /*
			primaryStage.setResizable(false);
			sceneWidth = startScene.getWidth();
			sceneHeight = startScene.getHeight();
			primaryStage.setTitle(String.format("Little Fighter JavaFX [%.0fx%.0f]", sceneWidth, sceneHeight));
			// */
			play.setVisible(false);
			sett.setVisible(false);
			loadingText.setVisible(true);
			LFload x = new LFload(loadingText, objPool);
			(new Thread(x)).start();
		});
		
		startScene = new Scene(vbox, LFmap.defaultWorldWidth, LFmap.SCENE_HEIGHT);
		startScene.setOnKeyPressed(e -> {
			if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE)
				javafx.application.Platform.exit();
		});
		
		// primaryStage.setResizable(false);
		primaryStage.setScene(startScene);
		primaryStage.setTitle("Little Fighter JavaFX");
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
	
	/* scene scaling */
	public static Scene sceneBuilder(Parent p, double cpw, double cph) {
		p.getTransforms().setAll(new Scale(sceneWidth / cpw, sceneHeight / cph));
		return new Scene(p, sceneWidth, sceneHeight, Color.BLACK);
		// return new Scene(p, Color.BLACK);
	}
	
	class LFload extends Task<Void> {
		private final HashMap<String, LFobject> pool;
		private long current = 0L;
		
		public LFload(Text text, HashMap<String, LFobject> p) {
			pool = p;
			text.textProperty().bind(this.messageProperty());
		}
		
		private void buildId(LFobject o) {
			pool.put(o.identifier, o);
			// this.updateProgress(++current, 30L);
			this.updateMessage("Loading... " + o.identifier);
			return;
		}
		
		@Override
		public Void call() {
			System.out.println("isFxApplicationThread: " + javafx.application.Platform.isFxApplicationThread());
			// /* like data.txt
			buildId(Data_Template.singleton);
			buildId(Data_Deep.singleton);
			buildId(Data_John.singleton);
			buildId(Data_Henry.singleton);
			buildId(Data_Rudolf.singleton);
			buildId(Data_Louis.singleton);
			buildId(Data_Firen.singleton);
			buildId(Data_Freeze.singleton);
			buildId(Data_Dennis.singleton);
			buildId(Data_Woody.singleton);
			buildId(Data_Davis.singleton);
			
			buildId(Data_Baseball.singleton);
			buildId(Data_Icesword.singleton);
			buildId(Data_Milk.singleton);
			buildId(Data_Stone.singleton);
			
			buildId(Data_Deepball.singleton);
			buildId(Data_Johnball.singleton);
			buildId(Data_Johnbiscuit.singleton);
			buildId(Data_Henrywind.singleton);
			buildId(Data_Henryarrow1.singleton);
			buildId(Data_Henryarrow2.singleton);
			buildId(Data_Rudolfweapon.singleton);
			buildId(Data_Louisarmour1.singleton);
			buildId(Data_Louisarmour2.singleton);
			buildId(Data_Firenball.singleton);
			buildId(Data_Firenflame.singleton);
			buildId(Data_Freezeball.singleton);
			buildId(Data_Freezecolumn.singleton);
			buildId(Data_Dennisball.singleton);
			buildId(Data_Dennischase.singleton);
			buildId(Data_Woodyball.singleton);
			buildId(Data_Davisball.singleton);
			// */
			return null;
		}
		
		@Override
		protected void succeeded() {
			super.succeeded();
			goToLFpick();
			return;
		}
		
	}
	
	public void backFromLFsetting(ArrayList<LFcontrol> ca) {
		if (ca != null) {
			ctrlArray.clear();
			ctrlArray.addAll(ca);
			LFsetting.save(ctrlArray, settingFileName);
		}
		primaryStage.setScene(startScene);
		return;
	}
	
	/* this method may be called several times */
	public static void goToLFpick() {
		currMap = null;
		primaryStage.setScene((new LFpick(ctrlArray)).makeScene());
		return;
	}
	
	/* this method may be called several times */
	public static void goToLFmap(ArrayList<LFhero> a) {
		currMap = new LFmap();
		int barPosition = 0;
		for (LFhero h: a) {
			if (h != null)
				currMap.pickedHero(h, barPosition);
			++barPosition;
		}
		primaryStage.setScene(currMap.makeScene());
		return;
	}
	
	public static void main(String[] args) {
		Application.launch(args);
		return;
	}
	
}
