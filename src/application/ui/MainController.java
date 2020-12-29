package application.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import application.services.CGService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController implements Initializable {
    @FXML
    private ImageView imageView;
    @FXML
    private Parent root;
    @FXML
    private Menu FiltersMenu;

    private CGService cgService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cgService = new CGService(imageView);
        imageView.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
//                        double scaleY = image.getHeight()
//                                / imageDisplay.fitHeightProperty()
//                                .doubleValue();
//                        scaleY = 1;
                        //select
                    }
                });
    }

    @FXML
    private void close(ActionEvent e) {
        Platform.exit();
    }

    @FXML
    private void save(ActionEvent e) {

    }

    @FXML
    private void saveAs(ActionEvent e) {

    }

    @FXML
    private void loadImage(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"));
        File chosen = fileChooser.showOpenDialog((Stage) root.getScene().getWindow());
        if (chosen != null) {
            Image inputImage = new Image(chosen.toURI().toString());
            WritableImage image = new WritableImage(inputImage.getPixelReader(),
                    (int) inputImage.getWidth(), (int) inputImage.getHeight());
            cgService.setImage(image);
            FiltersMenu.setDisable(false);
        }
    }

    private float openFilterConfiguration(String title, FilterCallback cb) {
        AnchorPane popup;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            popup = fxmlLoader.load(getClass().getResource("PopupView.fxml").openStream());
            PopupController popupController = fxmlLoader
                    .getController();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(popup));
            popupController.init(cgService, cb);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0f;
    }

    public void gauss(ActionEvent actionEvent) {
        openFilterConfiguration("Apply gaussian blur...", (size) -> cgService.blur(size));
    }

    public void unsharpen(ActionEvent actionEvent) {
        openFilterConfiguration("Apply gaussian sharpening...", (size) -> cgService.sharpen(size));
    }

    public void undo(ActionEvent actionEvent) {
    }

    public void redo(ActionEvent actionEvent) {
    }
}
