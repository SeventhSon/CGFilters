package application.ui;

import java.net.URL;
import java.util.ResourceBundle;

import application.services.CGService;
import application.services.FilterTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PopupController implements Initializable {
    @FXML
    private Slider slider;

    @FXML
    private TextField text;

    private CGService service;
    private FilterCallback cb;
    private FilterTask current;

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
        });
    }

    public void ok(ActionEvent e) {
        current.onReady((result) -> {
            Platform.runLater(() -> ((Stage) slider.getScene().getWindow()).close());
        });
    }

    public void init(CGService service, FilterCallback cb) {
        this.cb = cb;
        this.service = service;
    }
}
