package application.graphics;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class CGUtils {
    public static int encodeRGB(int R, int G, int B) {
        return 255 << 24
                | (int) Math
                .min(Math.max(R, 0), 255) << 16
                | (int) Math
                .min(Math.max(G, 0), 255) << 8
                | (int) Math
                .min(Math.max(B, 0), 255);
    }

    public static int[] decodeRGB(int color) {
        return new int[]{(color >> 16) & 255, (color >> 8) & 255, color & 255};
    }

    public static int clamp(int x, int y, int w, int h) {
        if (x < 0)
            x = 0;
        if (x >= w)
            x = w - 1;
        if (y < 0)
            y = 0;
        if (y >= h)
            y = h - 1;
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
