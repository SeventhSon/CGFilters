package application.services;

import application.graphics.Filter;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public class FilterTask {
    private Filter filter;
    private Image image;
    private Image result;
    private boolean canceled;
    private ReadyCallback cb;

    public FilterTask(Filter filter, Image image) {
        this.filter = filter;
        this.image = image;
        this.canceled = false;
    }

    public Filter getFilter() {
        return filter;
    }

    public synchronized void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Image getImage() {
        return image;
    }

    public synchronized void setImage(Image image) {
        this.image = image;
    }

    public synchronized void setResult(WritableImage result) {
        this.result = result;
        if(cb!=null)
            cb.result(this.result);
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void cancel() {
        if (!canceled)
            this.canceled = true;
    }

    public void onReady(ReadyCallback cb){
        this.cb = cb;
    }
}
