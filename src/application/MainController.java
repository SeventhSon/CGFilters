package application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController implements Initializable, ApplyFilterListener,
		ChangeListener, CustomFilterListener {
	@FXML
	private ImageView imageDisplay;
	@FXML
	private Parent root;

	private Image image;

	private CGService model;

	private Lock mLock;

	@FXML
	private Menu FiltersMenu;

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
	private void undo(ActionEvent e) {

	}

	@FXML
	private void redo(ActionEvent e) {

	}

	@FXML
	private void blur(ActionEvent e) {
		image = model.convolutionFilter(image, model.getBlur());
		imageDisplay.setImage(image);
	}

	@FXML
	private void gauss(ActionEvent e) {
		image = model.convolutionFilter(image, model.getGauss());
		imageDisplay.setImage(image);
	}

	@FXML
	private void edge(ActionEvent e) {
		image = model.convolutionFilter(image, model.getEdge());
		imageDisplay.setImage(image);
	}

	@FXML
	private void sharpen(ActionEvent e) {
		image = model.convolutionFilter(image, model.getSharpen());
		imageDisplay.setImage(image);
	}

	@FXML
	private void emboss(ActionEvent e) {
		image = model.convolutionFilter(image, model.getEmboss());
		imageDisplay.setImage(image);
	}

	@FXML
	private void inversion(ActionEvent e) {
		image = model.functionalFilter(image, model.getInversion());
		imageDisplay.setImage(image);
	}

	@FXML
	private void brightness(ActionEvent e) {
		openFunctionalPopup("Set brightness", "brightness");
	}

	@FXML
	private void editor(ActionEvent e) {
		FXMLLoader fxmlLoader = new FXMLLoader();
		AnchorPane editor;
		try {
			editor = (AnchorPane) fxmlLoader.load(getClass().getResource(
					"Editor.fxml").openStream());
			EditorController editorController = (EditorController) fxmlLoader
					.getController();
			Stage stage = new Stage();
			stage.setTitle("Functional filter editor");
			stage.setScene(new Scene(editor));
			stage.showAndWait();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@FXML
	private void contrast(ActionEvent e) {
		openFunctionalPopup("Set contrast", "contrast");
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

	private float openFunctionalPopup(String title, String type) {
		AnchorPane popup;
		try {
			FXMLLoader fxmlLoader = new FXMLLoader();
			popup = (AnchorPane) fxmlLoader.load(getClass().getResource(
					"PopupFunctionalView.fxml").openStream());
			PopupFunctionalController popupController = (PopupFunctionalController) fxmlLoader
					.getController();
			Stage stage = new Stage();
			stage.setTitle(title);
			stage.setScene(new Scene(popup));
			popupController.setType(type);
			stage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0f;
	}

	@Override
	public void onApplyFilter(ApplyFilterEvent applyFilterEvent) {
		if (mLock.tryLock()) {
			switch (applyFilterEvent.getType()) {
			case "contrast":
				imageDisplay
						.setImage(model.functionalFilter(image, model
								.getContrast((float) applyFilterEvent
										.getFactor() / 200)));
				break;
			case "brightness":
				imageDisplay.setImage(model.functionalFilter(image,
						model.getBrightness((float) applyFilterEvent
								.getFactor() / 100)));
				break;
			case "gamma":
				imageDisplay.setImage(model.functionalFilter(image,
						model.getGamma((float) applyFilterEvent.getFactor())));
				break;
			default:
				break;
			}
			mLock.unlock();
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		mLock = new ReentrantLock();
		model = CGService.getInstance();
		EventDispatcher.getInstance().subscribe(this,
				ApplyFilterEvent.class.toString());
		EventDispatcher.getInstance().subscribe(this,
				ChangeEvent.class.toString());
		EventDispatcher.getInstance().subscribe(this,
				CustomFilterEvent.class.toString());
	}

	@Override
	public void onStoreChange(ChangeEvent changeEvent) {
		image = imageDisplay.getImage();
	}

	@Override
	public void onUndoChange(ChangeEvent changeEvent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRedoChange(ChangeEvent changeEvent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCustomFilter(CustomFilterEvent customFilterEvent) {
		image = model.functionalFilter(image, customFilterEvent.getFilter());
		imageDisplay.setImage(image);
	}

}
