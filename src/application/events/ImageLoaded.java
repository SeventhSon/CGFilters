package application.events;

import javafx.scene.image.WritableImage;

import java.io.File;

public class ImageLoaded {
    private WritableImage image;
    private File file;

    public ImageLoaded(WritableImage image, File file) {
        this.image = image;
        this.file = file;
    }

    public WritableImage getImage() {
        return image;
    }

    public void setImage(WritableImage image) {
        this.image = image;
    }

    public File getFile() {
        return file;
    }
}
