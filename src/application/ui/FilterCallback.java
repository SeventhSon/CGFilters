package application.ui;

import application.services.FilterTask;
import javafx.scene.image.Image;

public interface FilterCallback {
    FilterTask filter(float size);
}
