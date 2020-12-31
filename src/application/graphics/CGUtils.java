package application.graphics;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class CGUtils {
    public enum ClampingType {CLIP, WRAP}

    public static int encodeARGB(int A, int R, int G, int B) {
        return Math.min(Math.max(A, 0), 0xff) << 24
                | Math.min(Math.max(R, 0), 0xff) << 16
                | Math.min(Math.max(G, 0), 0xff) << 8
                | Math.min(Math.max(B, 0), 0xff);
    }

    public static int[] decodeARGB(int color) {
        return new int[]{(color >> 24) & 0xff, (color >> 16) & 0xff, (color >> 8) & 0xff, color & 0xff};
    }

    public static int clamp(int x, int y, int w, int h, ClampingType type) {
        if (type == ClampingType.CLIP) {
            if (x < 0)
                x = 0;
            else if (x >= w)
                x = w - 1;
            if (y < 0)
                y = 0;
            else if (y >= h)
                y = h - 1;
        } else {
            x = (x + w) % w;
            y = (y + h) % h;
        }
        return y * w + x;
    }

    public static WritableImage copyImage(Image image) {
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, color);
            }
        }
        return writableImage;
    }
}
