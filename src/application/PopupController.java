package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class PopupController implements Initializable {
	@FXML
	private Slider slider;

	@FXML
	private TextField text;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		slider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(
					ObservableValue<? extends Number> observableValue,
					Number oldValue, Number newValue) {
				if (newValue == null) {
					text.setText("");
					return;
				}
				text.setText(Math.round(newValue.intValue()) + "");
			}
		});
	}

}
