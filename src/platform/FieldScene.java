package platform;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import base.Controller;
import base.Vector;
import ecosystem.BaseField;
import ecosystem.Hero;
import ecosystem.Observable;

public class FieldScene extends BaseField {
  private static final System.Logger logger = System.getLogger("");

  private static class ObjectView extends VBox {
    final Observable object;
    final List<Image> pictureList;
    final ImageView picture = new ImageView();

    ObjectView(Observable object, List<Image> pictureList) {
      this.object = object;
      this.pictureList = pictureList;
      getChildren().add(picture);
      setRotationAxis(Rotate.Y_AXIS);
    }

    /**
     * Updates GUI view of the binding object.
     * A child with a lower viewOrder will be in front of a child with a higher viewOrder.
     */
    void update() {
      setVisible(object.exists());
      Image image = pictureList.get(object.getImageIndex());
      Vector coordinate = object.getSceneCoordinate();
      if (object.isFaceRight()) {
        setRotate(0.0);
        setTranslateX(coordinate.x());
      } else {
        setRotate(180.0);
        setTranslateX(coordinate.x() - image.getWidth());
      }
      setTranslateY(coordinate.y() - coordinate.z());
      picture.setImage(image);
      return;
    }

  }

  private final SubScene scene;
  private final List<Node> objectViewList;
  private final List<Observable> spectatingObjects = new ArrayList<>();
  private final Camera camera = new PerspectiveCamera(true);
  private final DoubleProperty cameraPosition = camera.translateXProperty();
  private final Translate mouseTranslation =
      new Translate(WIDTH_DIV2, -FIELD_HEIGHT, DEFAULT_DISTANCE);

  public FieldScene(double width, double top, double bottom) {
    super(width, top, bottom);
    Group root = new Group();
    objectViewList = root.getChildren();
    scene = new SubScene(root, FIELD_WIDTH, FIELD_HEIGHT);
    scene.setOnMousePressed(this::handlePressed);
    scene.setOnMouseDragged(this::handleDragged);
    scene.setOnScroll(this::handleScroll);
    scene.setPickOnBounds(true);
    scene.setCamera(camera);
    camera.setFarClip(6000);
    camera.getTransforms().add(mouseTranslation);
  }

  private void addNode(Observable object) {
    List<Image> pictureList = ResourceManager.getPictureList(object.getIdentifier());
    objectViewList.add(new ObjectView(object, pictureList));
    return;
  }

  public void addPlayer(Hero o, Controller controller, int teamId) {
    logger.log(Level.INFO, o);
    o.initTerrain(this);
    o.setController(controller);
    o.setProperty(teamId == 0 ? requestIndependentTeamId() : teamId, random.nextBoolean());
    emplace(o);
    addNode(o);
    spectatingObjects.add(o);
    return;
  }

  @Override
  public void stepOneFrame() {
    super.stepOneFrame();
    objectViewList.removeIf(v -> !v.isVisible());
    pendingList.forEach(this::addNode);
    objectViewList.forEach(v -> ((ObjectView) v).update());
    double pos = calcCameraPos(spectatingObjects, cameraPosition.get());
    cameraPosition.set(pos);
    return;
  }

  public SubScene getScene() {
    return scene;
  }

  public DoubleProperty getCameraXProperty() {
    return cameraPosition;
  }

  // ==================== Mouse ====================

  private static final double FAREST_DISTANCE = -1600.0;
  private static final double NEAREST_DISTANCE = -200.0;
  private static final double DEFAULT_DISTANCE = -900.0;
  private static final double SCROLL_MULTIPLIER = 0.8;

  private double mouseStartX = 0.0;
  private double mouseStartY = 0.0;
  private double translationStartX = 0.0;
  private double translationStartY = 0.0;

  private void handlePressed(MouseEvent event) {
    mouseStartX = event.getScreenX();
    mouseStartY = event.getScreenY();
    translationStartX = mouseTranslation.getX();
    translationStartY = mouseTranslation.getY();
  }

  private void handleDragged(MouseEvent event) {
    double deltaX = mouseStartX - event.getScreenX();
    mouseTranslation.setX(translationStartX + deltaX);
    double deltaY = mouseStartY - event.getScreenY();
    mouseTranslation.setY(translationStartY + deltaY);
    logger.log(Level.INFO, mouseTranslation);
  }

  private void handleScroll(ScrollEvent event) {
    double newDistance = mouseTranslation.getZ() + event.getDeltaY() * SCROLL_MULTIPLIER;
    newDistance = Math.max(FAREST_DISTANCE, Math.min(NEAREST_DISTANCE, newDistance));
    mouseTranslation.setZ(newDistance);
    logger.log(Level.INFO, mouseTranslation);
  }

}
