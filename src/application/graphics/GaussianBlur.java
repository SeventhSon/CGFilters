package application.graphics;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import org.apache.commons.math3.distribution.NormalDistribution;

public class GaussianBlur implements Filter {
    private CGUtils.ClampingType clamp;
    protected Kernel kernel;
    private float size;

    public GaussianBlur(float size, CGUtils.ClampingType clamp) {
        setSize(size);
        this.clamp = clamp;
    }

    public GaussianBlur(float size) {
        setSize(size);
    }

    public void setSize(float size) {
        this.size = size;
        this.kernel = GaussianBlur.generateKernel(size);
    }

    public float getSize() {
        return this.size;
    }

    public Kernel getKernel() {
        return kernel;
    }

    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    public void convolve(int[] pixelData, int[] resultPixelData, int x1, int y1, int x2, int y2, int h, int w, int kh, int kw, boolean transpose) {
        for (int y = y1; y < y2; y++)
            for (int x = x1; x < x2; x++) {
                int sumA = 0, sumR = 0, sumG = 0, sumB = 0;
                for (int i = 0; i < kh; i++)
                    for (int j = 0; j < kw; j++) {
                        int index;
                        if (transpose)
                            index = CGUtils.clamp(x + (j - kw / 2), y + (i - kh / 2), w, h, clamp);
                        else
                            index = CGUtils.clamp(x + (i - kh / 2), y + (j - kw / 2), w, h, clamp);
                        int px = pixelData[index];
                        int[] colors = CGUtils.decodeARGB(px);
                        float factor = kernel.get(i, j);
                        sumA += factor * colors[0];
                        sumR += factor * colors[1];
                        sumG += factor * colors[2];
                        sumB += factor * colors[3];
                    }
                sumA += 1;
                sumR += 1;
                sumG += 1;
                sumB += 1;
                resultPixelData[y * w + x] = CGUtils.encodeARGB(sumA, sumR, sumG, sumB);
            }
    }

    public WritableImage applyTo(Image image) {
        int sWidth = (int) image.getWidth();
        int sHeight = (int) image.getHeight();
        Rectangle2D region = new Rectangle2D(0, 0, sWidth, sHeight);
        return applyTo(image, region);
    }

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

        result.getPixelWriter().setPixels(0, 0, sWidth, sHeight, WritablePixelFormat.getIntArgbInstance(),
                pixelData, 0, sWidth);
        return result;
    }

    public static Kernel generateKernel(float radius) {
        int r = (int) Math.ceil(radius);
        int rows = r * 2 + 1;
        float[] matrix = new float[rows];
        int index = 0;
        NormalDistribution dist = new NormalDistribution(0.0, 1.0);
        double total = 0;
        double step = 6.0 / (rows - 1.0);
        for (double i = -3.0; i <= 3.0; i += step) {
            matrix[index] = (float) dist.density(i);
            total += matrix[index];
            index++;
        }
        for (int i = 0; i < rows; i++)
            matrix[i] /= total;
        return new Kernel(rows, 1, matrix);
    }
}
