package application.ui;

import application.events.ImageEdit;
import application.services.FilterTask;
import com.google.common.eventbus.EventBus;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupController implements Initializable {
    @FXML
    private Slider slider;

    @FXML
    private TextField text;

    private FilterCallback cb;
    private FilterTask current;
    private EventBus eventBus;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        current = null;
        slider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null) {
                text.setText("");
                return;
            }
            text.setText(String.valueOf(newValue.intValue()));
            if (current != null) {
                current.cancel();
            }
            current = cb.filter(newValue.floatValue());
            current.onComplete(result -> eventBus.post(new ImageEdit(ImageEdit.Type.PREVIEW, result)));
        });
    }

    public void ok(ActionEvent e) {
        current.onComplete((result) -> {
            eventBus.post(new ImageEdit(ImageEdit.Type.COMMIT));
            Platform.runLater(() -> {
                ((Stage) slider.getScene().getWindow()).close();
            });
        });
    }

    public void init(FilterCallback cb, EventBus eventBus) {
        this.eventBus = eventBus;
        this.cb = cb;
    }
}
