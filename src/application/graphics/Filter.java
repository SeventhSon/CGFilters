package application.graphics;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public interface Filter {
    WritableImage applyTo(Image image);

    WritableImage applyTo(Image image, Rectangle2D region);

    float getSize();

    void setSize(float size);
}
