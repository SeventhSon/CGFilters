package application;

import application.services.CGService;
import application.ui.MainController;
import com.google.common.eventbus.EventBus;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            BorderPane root = fxmlLoader.load(getClass().getResource("ui/MainView.fxml").openStream());
            ImageView imageView = (ImageView) root.lookup("#imageView");
            Canvas canvas = (Canvas) root.lookup("#uiCanvas");

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("ui/application.css").toExternalForm());
//            canvas.widthProperty().bind(imageView.fitWidthProperty());

            primaryStage.setMinHeight(400);
            primaryStage.setMinWidth(500);
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(event -> Platform.exit());

            CGService cgService = new CGService(imageView);
            EventBus eventBus = new EventBus();
            eventBus.register(cgService);

            MainController controller = fxmlLoader.getController();
            controller.init(eventBus, cgService);
            eventBus.register(controller);

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
