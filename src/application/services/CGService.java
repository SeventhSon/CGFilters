package application.services;

import application.events.ImageEdit;
import application.events.ImageLoaded;
import application.events.Save;
import application.events.Selection;
import application.graphics.CGUtils;
import application.graphics.GaussianBlur;
import application.graphics.UnsharpMask;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CGService {

    private BlockingQueue<FilterTask> taskQueue;
    private ImageView view;
    private Image currentImage;
    private File openFile;
    private Image preview;
    private final int WORKER_COUNT = 4;
    private Selection selection;

    public CGService(ImageView view) {

        this.view = view;
        taskQueue = new LinkedBlockingQueue<>();
        for (int i = 0; i < WORKER_COUNT; i++) {
            Thread worker = new Thread(() -> {
                while (true) {
                    try {
                        FilterTask task = taskQueue.take();
                        if (!task.isCanceled())
                            task.run();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            worker.setDaemon(true);
            worker.start();
        }
    }

    public void setImage(Image image) {
        Platform.runLater(() -> {
            view.setImage(image);
            currentImage = image;
        });
    }

    public void setPreview(Image image) {
        preview = image;
        Platform.runLater(() -> {
            view.setImage(image);
        });
    }

    private void scheduleTask(FilterTask task) {
        taskQueue.removeIf((filterTask -> filterTask.isCanceled()));
        taskQueue.add(task);
    }

    public FilterTask blur(float strength) {
        FilterTask task = new FilterTask((image) -> {
            GaussianBlur blur = new GaussianBlur(strength, CGUtils.ClampingType.CLIP);
            WritableImage preview;
            if (selection != null) {
                Rectangle2D region = selection.getArea();
                preview = blur.applyTo(image, region);
            }
            else
                preview = blur.applyTo(image);
            return preview;
        }, CGUtils.copyImage(currentImage));
        scheduleTask(task);
        return task;
    }

    public FilterTask sharpen(float strength) {
        FilterTask task = new FilterTask((image) -> {
            UnsharpMask unsharpMask = new UnsharpMask(strength / 2, 1.5f, 1);
            WritableImage preview;
            if (selection != null) {
                Rectangle2D region = selection.getArea();
                preview = unsharpMask.applyTo(image, region);
            }
            else
                preview = unsharpMask.applyTo(image);
            return preview;
        }, CGUtils.copyImage(currentImage));
        scheduleTask(task);
        return task;
    }

    @Subscribe
    public void onImageEdit(ImageEdit event) {
        switch (event.getType()) {
            case COMMIT -> {
                currentImage = preview;
            }
            case PREVIEW -> {
                setPreview(event.getImage());
            }
            case ROLLBACK -> {
                setImage(currentImage);
            }
        }
    }

    @Subscribe
    public void onImageLoad(ImageLoaded event) {
        setImage(event.getImage());
        openFile = event.getFile();
    }

    @Subscribe
    public void onSave(Save event) {
        BufferedImage bImage = SwingFXUtils.fromFXImage(currentImage, null);
        try {
            ImageIO.write(bImage, "png", event.getFile() != null ? event.getFile() : openFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Subscribe
    private void onSelectionChange(Selection selection) {
        if (selection.getType() == Selection.SelectionType.CLEAR)
            this.selection = null;
        else if (selection.getType() == Selection.SelectionType.SET)
            this.selection = selection;
    }
}