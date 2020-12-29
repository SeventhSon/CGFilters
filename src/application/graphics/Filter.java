package application.graphics;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public interface Filter {
    WritableImage applyTo(Image image);

    float getSize();

    void setSize(float size);
}
