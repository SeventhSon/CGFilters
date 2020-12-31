package application.events;

import javafx.scene.image.WritableImage;

public class ImageEdit {
    public enum Type {UNDO, REDO, COMMIT, PREVIEW, ROLLBACK}

    private Type type;
    private WritableImage image;

    public ImageEdit(Type type) {
        this.type = type;
    }

    public ImageEdit(Type type, WritableImage image) {
        this.type = type;
        this.image = image;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public WritableImage getImage() {
        return image;
    }

    public void setImage(WritableImage image) {
        this.image = image;
    }
}
