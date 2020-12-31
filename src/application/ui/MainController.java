package application.ui;

import application.events.ImageEdit;
import application.events.ImageLoaded;
import application.events.Save;
import application.events.Selection;
import application.services.CGService;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private Canvas uiCanvas;
    @FXML
    private Parent root;
    @FXML
    private Menu filtersMenu;
    @FXML
    private MenuItem saveItem;
    @FXML
    private MenuItem saveAsItem;

    private EventBus eventBus;
    private CGService cgService;
    private GraphicsContext gCtx;
    private Point2D start, stop;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gCtx = uiCanvas.getGraphicsContext2D();

        uiCanvas.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                start = null;
                stop = null;
                eventBus.post(new Selection());
            }
        });

        uiCanvas.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                start = new Point2D(event.getX(), event.getY());
            }
        });

        uiCanvas.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY && start != null) {
                Point2D current = new Point2D(event.getX(), event.getY());
                Selection selection = new Selection(start, current);
                drawSelectionBox(selection.getArea(), true);
            }
        });

        uiCanvas.addEventFilter(MouseEvent.MOUSE_RELEASED,(event -> {
            stop = new Point2D(event.getX(), event.getY());
            if(event.getButton() == MouseButton.PRIMARY && start!=null && stop!=null) {
                Selection selection = new Selection(start, stop);
                eventBus.post(selection);
                start = null;
                stop = null;
            }
        }));
    }

    private void drawSelectionBox(Rectangle2D area, boolean isPreview) {
        gCtx.clearRect(0, 0, uiCanvas.getWidth(), uiCanvas.getHeight());
        Color black = new Color(0, 0, 0, 0.9);
        Color lightBlue = new Color(0, 0.349019608, 0.617254902, 0.3);
        if (isPreview) {
            gCtx.setFill(lightBlue);
            gCtx.fillRect(area.getMinX(), area.getMinY(), area.getWidth(), area.getHeight());
        }
        gCtx.setStroke(black);
        gCtx.strokeRect(area.getMinX(), area.getMinY(), area.getWidth(), area.getHeight());

    }

    @Subscribe
    public void onSelectionChange(Selection selection) {
        if (selection.getType() == Selection.SelectionType.CLEAR)
            gCtx.clearRect(0, 0, uiCanvas.getWidth(), uiCanvas.getHeight());
        else if (selection.getType() == Selection.SelectionType.SET)
            drawSelectionBox(selection.getArea(), false);
    }

    @FXML
    private void close(ActionEvent e) {
        Platform.exit();
    }

    @FXML
    private void saveAs(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save image as...");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"));
        File chosen = fileChooser.showSaveDialog(root.getScene().getWindow());
        if (chosen != null) {
            eventBus.post(new Save(chosen));
        }
    }

    @FXML
    private void save(ActionEvent e) {
        eventBus.post(new Save());
    }

    @FXML
    private void loadImage(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"));
        File chosen = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (chosen != null) {
            Image inputImage = new Image(chosen.toURI().toString());
            WritableImage image = new WritableImage(inputImage.getPixelReader(),
                    (int) inputImage.getWidth(), (int) inputImage.getHeight());
            filtersMenu.setDisable(false);
            uiCanvas.setWidth(image.getWidth());
            uiCanvas.setHeight(image.getHeight());
            eventBus.post(new ImageLoaded(image, chosen));
        }
    }

    private void openFilterConfiguration(String title, FilterCallback cb) {
        AnchorPane popup;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            popup = fxmlLoader.load(getClass().getResource("PopupView.fxml").openStream());
            PopupController popupController = fxmlLoader.getController();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(popup));
            popupController.init(cb, eventBus);
            stage.setOnCloseRequest((e) -> {
                eventBus.post(new ImageEdit(ImageEdit.Type.ROLLBACK));
            });
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void gauss(ActionEvent actionEvent) {
        openFilterConfiguration("Apply gaussian blur...", (strength) -> cgService.blur(strength));
    }

    public void sharpen(ActionEvent actionEvent) {
        openFilterConfiguration("Apply unsharp mask...", (strength) -> cgService.sharpen(strength));
    }

    public void undo(ActionEvent actionEvent) {
        eventBus.post(new ImageEdit(ImageEdit.Type.UNDO));
    }

    public void redo(ActionEvent actionEvent) {
        eventBus.post(new ImageEdit(ImageEdit.Type.REDO));
    }

    public void init(EventBus eventBus, CGService cgService) {
        this.eventBus = eventBus;
        this.cgService = cgService;
    }

    @Subscribe
    public void onImageLoad(ImageLoaded event) {
        if (event.getImage() != null) {
            saveItem.setDisable(false);
            saveAsItem.setDisable(false);
        } else {
            saveItem.setDisable(true);
            saveAsItem.setDisable(true);
        }

    }
}
