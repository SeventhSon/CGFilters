package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PopupFunctionalController implements Initializable {
	@FXML
	private Slider slider;

	@FXML
	private TextField text;
	
	private String mType;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		slider.valueProperty().addListener(new ChangeListener<Number>() {
			EventDispatcher eventBus = EventDispatcher.getInstance();
			@Override
			public void changed(
					ObservableValue<? extends Number> observableValue,
					Number oldValue, Number newValue) {
				if (newValue == null) {
					text.setText("");
					return;
				}
				text.setText(Math.round(newValue.intValue()) + "");
				eventBus.publish(new ApplyFilterEvent(mType, newValue.intValue()));
			}
		});
	}
	
	public void ok(ActionEvent e){
		EventDispatcher.getInstance().publish(new ChangeEvent("Store"));
		((Stage)slider.getScene().getWindow()).close();
	}

	public void setType(String type) {
		mType = type; 
	}

}
