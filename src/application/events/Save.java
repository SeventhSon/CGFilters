package application.events;

import java.io.File;

public class Save {
    private File file;

    public Save(File file) {
        this.file = file;
    }

    public Save() {

    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
