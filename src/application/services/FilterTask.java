package application.services;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public class FilterTask {
    private Work work;
    private WritableImage image;
    private WritableImage result;
    private boolean canceled;
    private CompleteCallback cb;

    public interface Work {
        WritableImage execute(Image image);
    }

    public FilterTask(Work work, WritableImage image) {
        this.work = work;
        this.image = image;
        this.canceled = false;
    }

    public void run() {
        this.result = work.execute(image);
        if (cb != null && !canceled)
            cb.result(this.result);
    }

    public synchronized boolean isCanceled() {
        return canceled;
    }

    public synchronized void cancel() {
        if (!canceled)
            this.canceled = true;
    }

    public void onComplete(CompleteCallback cb) {
        this.cb = cb;
        if (this.result!=null)
            cb.result(this.result);
    }
}
