package application.services;

import application.graphics.CGUtils;
import application.graphics.Filter;
import application.graphics.GaussianBlur;
import application.graphics.Unsharpen;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CGService {

    private BlockingQueue<FilterTask> taskQueue;
    private ImageView view;
    private Image currentImage;

    public CGService(ImageView view) {
        this.view = view;
        taskQueue = new LinkedBlockingQueue<>();
        Thread worker = new Thread(() -> {
            while (true) {
                try {
                    FilterTask task = taskQueue.take();
                    Filter filter = task.getFilter();
                    Image image = task.getImage();
                    task.setResult(filter.applyTo(image));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    public void setImage(Image image) {
        Platform.runLater(() -> {
            view.setImage(image);
            currentImage = image;
        });
    }

    public void setPreview(Image image) {
        Platform.runLater(() -> {
            view.setImage(image);
        });
    }

    private void scheduleTask(FilterTask task){
        taskQueue.removeIf((filterTask -> filterTask.isCanceled()));
        taskQueue.add(task);
    }

    public FilterTask blur(float size) {
        FilterTask task = new FilterTask(new GaussianBlur(size), CGUtils.copyImage(currentImage));
        scheduleTask(task);
        return task;
    }

    public FilterTask sharpen(float size) {
        FilterTask task = new FilterTask(new Unsharpen(size), CGUtils.copyImage(currentImage));
        scheduleTask(task);
        return task;
    }
}