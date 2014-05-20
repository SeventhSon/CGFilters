package application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController implements Initializable, ApplyFilterListener,
		ChangeListener, CustomFilterListener {
	@FXML
	private ImageView imageDisplay;
	@FXML
	private Parent root;

	private WritableImage image;

	private CGService model;

	private Lock mLock;

	private int mX = -1, mY = -1;

	@FXML
	private Menu FiltersMenu;

	private String mMode = "Select";

	@FXML
	private void setMode(ActionEvent e) {
		Button offender = (Button) e.getSource();
		mMode = offender.getText();
		mX = -1;
		System.out.println(offender.getText());
	}

	@FXML
	private void setSize(ActionEvent e) {
		TextField offender = (TextField) e.getSource();
		model.setSize(Integer.parseInt(offender.getText()));
	}

	@FXML
	private void newImage(ActionEvent e) {
		image = model.getNewImage(1366, 768);
		imageDisplay.setImage(image);
		FiltersMenu.setDisable(false);
		imageDisplay.autosize();
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
	private void undo(ActionEvent e) {

	}

	@FXML
	private void redo(ActionEvent e) {

	}

	@FXML
	private void kmeans(ActionEvent e) {
		image = model.kmeansQuantization(image, 16);
		imageDisplay.setImage(image);
	}

	@FXML
	private void gamma(ActionEvent e) {
		openFunctionalPopup("Set gamma correction", "gamma", 0.01, 300, 100);
	}

	@FXML
	private void averageDither(ActionEvent e) {
		openFunctionalPopup("Set average dithering", "averagedither", 2, 32, 2);
	}

	@FXML
	private void thresholding(ActionEvent e) {
		image = model.thresholdingFilter(image, model.getThresholding());
		imageDisplay.setImage(image);
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
			Image loadee = new Image("file:" + chosen.getPath());
			image = new WritableImage(loadee.getPixelReader(),
					(int) loadee.getWidth(), (int) loadee.getHeight());
			imageDisplay.setImage(image);
			FiltersMenu.setDisable(false);
		}
		imageDisplay.autosize();
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

	private float openFunctionalPopup(String title, String type, double min,
			double max, double avg) {
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
			popupController.setRange(min, max, avg);
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
			case "averagedither":
				imageDisplay.setImage(model.thresholdingFilter(image, model
						.getAverageDither((int) applyFilterEvent.getFactor())));
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
		imageDisplay.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						double scaleY = image.getHeight()
								/ imageDisplay.fitHeightProperty()
										.doubleValue();
						scaleY = 1;
						if (mX == -1) {
							if (!mMode.equals("Select")) {
								mX = (int) (e.getX() * scaleY);
								mY = (int) (e.getY() * scaleY);
								if (mMode.equals("Fill")) {
									model.fill(image, mX, mY);
									mX = -1;
								}
							}else
								model.scanline(image, (int)e.getX(), (int)e.getY());
						} else {
							if (mMode.equals("Line")) {
								model.line(image, mX, mY,
										(int) (e.getX() * scaleY),
										(int) (e.getY() * scaleY));
							} else if (mMode.equals("Circle")) {
								model.circle(image, mX, mY,
										(int) (e.getX() * scaleY),
										(int) (e.getY() * scaleY));
							}
							mX = -1;
							//imageDisplay.setImage(model.msaa(image));
							imageDisplay.setImage(image);
						}
					}
				});
	}

	@Override
	public void onStoreChange(ChangeEvent changeEvent) {
		image = (WritableImage) imageDisplay.getImage();
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
