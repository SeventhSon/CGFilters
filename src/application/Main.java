package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = (BorderPane) FXMLLoader.load(getClass()
					.getResource("MainView.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(
					getClass().getResource("application.css").toExternalForm());
			ImageView imgv = (ImageView) root.lookup("#imageDisplay");
			MenuBar mb = (MenuBar) root.lookup("#menu");
			imgv.fitWidthProperty().bind(root.widthProperty());
			imgv.fitHeightProperty().bind(
					root.heightProperty().subtract(mb.getMaxHeight()));
			imgv.setImage(new Image(
					"file:/home/guru/workspace/CGFilters/images/loading2.gif"));
			primaryStage.setMinHeight(300);
			primaryStage.setMinWidth(400);
			primaryStage.setScene(scene);
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					Platform.exit();
				}
			});
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
