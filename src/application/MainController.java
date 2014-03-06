package application;

import java.io.File;
import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController {
	@FXML
	private ImageView imageDisplay;
	@FXML
	private Parent root;

	private Image image;

	private CGService model;

	@FXML
	private Menu FiltersMenu;

	public MainController() {
		model = CGService.getInstance();
	}

	@FXML
	private void close(ActionEvent e) {
		Platform.exit();
	}

	@FXML
	private void inversion(ActionEvent e) {
		image = model.functionalFilter(image, model.getInversion());
		imageDisplay.setImage(image);
	}

	@FXML
	private void brightness(ActionEvent e) {
		image = model.functionalFilter(image, model.getBrightness(-0.15f));
		imageDisplay.setImage(image);
	}

	@FXML
	private void contrast(ActionEvent e) {
		openFunctionalPopup("Set contrast");
		image = model.functionalFilter(image, model.getContrast(-0.1f));
		imageDisplay.setImage(image);
	}

	@FXML
	private void loadImage(ActionEvent e) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select an image");
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("All Images", "*.*"),
				new FileChooser.ExtensionFilter("JPG", "*.jpg"),
				new FileChooser.ExtensionFilter("PNG", "*.png"));
		File chosen = fileChooser.showOpenDialog((Stage) root.getScene()
				.getWindow());
		if (chosen != null) {
			image = new Image("file:" + chosen.getPath());
			imageDisplay.setImage(image);
			FiltersMenu.setDisable(false);
		}
	}

	private float openFunctionalPopup(String title) {
		AnchorPane popup;
		try {
			popup = (AnchorPane) FXMLLoader.load(getClass().getResource(
					"PopupFunctionalView.fxml"));
			Stage stage = new Stage();
			stage.setTitle(title);
			stage.setScene(new Scene(popup));
			stage.showAndWait();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0f;
	}
}
