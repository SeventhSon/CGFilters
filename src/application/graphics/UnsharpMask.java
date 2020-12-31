package application.graphics;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;

public class UnsharpMask extends GaussianBlur {
    private float amount = 0.5f;
    private int threshold = 1;

    public UnsharpMask(float size, float amount, int threshold) {
        super(size);
        this.amount = amount;
        this.threshold = threshold;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public WritableImage applyTo(Image image, Rectangle2D region) {
        int sWidth = (int) image.getWidth();
        int sHeight = (int) image.getHeight();
        int kernelWidth = kernel.getWidth();
        int kernelHeight = kernel.getHeight();
        int[] pixelData = new int[(int) (sWidth * sHeight)];
        int[] resultPixelData = new int[(int) (sWidth * sHeight)];
        image.getPixelReader().getPixels(0, 0, sWidth, sHeight, WritablePixelFormat.getIntArgbInstance(),
                pixelData, 0, sWidth);
        WritableImage result = CGUtils.copyImage(image);

        int x1 = (int) region.getMinX();
        int y1 = (int) region.getMinY();
        int x2 = (int) (x1 + region.getWidth());
        int y2 = (int) (y1 + region.getHeight());
        if (x1 < 0)
            x1 = 0;
        if (y1 < 0)
            y1 = 0;
        if (x2 > sWidth)
            x2 = sWidth - 1;
        if (y2 > sHeight)
            y2 = sHeight - 1;


        convolve(pixelData, resultPixelData, x1, y1, x2, y2, sHeight, sWidth, kernelHeight, kernelWidth, false);
        convolve(resultPixelData, pixelData, x1, y1, x2, y2, sHeight, sWidth, kernelHeight, kernelWidth, true);

        image.getPixelReader().getPixels(0, 0, sWidth, sHeight, WritablePixelFormat.getIntArgbInstance(),
                resultPixelData, 0, sWidth);

        int index = 0;
        for (int y = 0; y < sHeight; y++) {
            for (int x = 0; x < sWidth; x++) {
                int[] maskARGB = CGUtils.decodeARGB(pixelData[index]);
                int[] srcARGB = CGUtils.decodeARGB(resultPixelData[index]);

                int r = srcARGB[1], g = srcARGB[2], b = srcARGB[3];
                if (Math.abs(srcARGB[1] - maskARGB[1]) >= threshold)
                    r = (int) (amount * (srcARGB[1] - maskARGB[1]) + srcARGB[1]);
                if (Math.abs(srcARGB[2] - maskARGB[2]) >= threshold)
                    g = (int) (amount * (srcARGB[2] - maskARGB[2]) + srcARGB[2]);
                if (Math.abs(srcARGB[3] - maskARGB[3]) >= threshold)
                    b = (int) (amount * (srcARGB[3] - maskARGB[3]) + srcARGB[3]);

                resultPixelData[index] = CGUtils.encodeARGB(srcARGB[0], r, g, b);
                index++;
            }
        }
        result.getPixelWriter().setPixels(0, 0, sWidth, sHeight, WritablePixelFormat.getIntArgbInstance(),
                resultPixelData, 0, sWidth);
        return result;
    }
}
