package application.graphics;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;

public class GaussianBlur implements Filter {
    private Kernel kernel;
    private float size;

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

    public WritableImage applyTo(Image image) {
        int sWidth = (int) image.getWidth();
        int sHeight = (int) image.getHeight();
        int w = kernel.getWidth();
        int h = kernel.getHeight();
        int[] pixelData = new int[(int) (sWidth * sHeight)];
        int[] resultPixelData = new int[(int) (sWidth * sHeight)];
        image.getPixelReader().getPixels(0, 0, sWidth, sHeight,
                WritablePixelFormat.getIntArgbInstance(), pixelData, 0, sWidth);
        WritableImage result = CGUtils.copyImage(image);
        for (int y = 0; y < sHeight; y++)
            for (int x = 0; x < sWidth; x++) {
                int sumR = 0, sumG = 0, sumB = 0;
                for (int i = 0; i < h; i++)
                    for (int j = 0; j < w; j++) {
                        int px = pixelData[CGUtils.clamp(x + (j - w / 2), y
                                + (i - h / 2), sWidth, sHeight)];
                        int[] colors = CGUtils.decodeRGB(px);
                        float factor = kernel.get(i, j);
                        sumR += factor * colors[0];
                        sumG += factor * colors[1];
                        sumB += factor * colors[2];
                    }
                resultPixelData[y * sWidth + x] = CGUtils.encodeRGB(sumR, sumG, sumB);
            }
        result.getPixelWriter().setPixels(0, 0, sWidth, sHeight, WritablePixelFormat.getIntArgbInstance(),
                resultPixelData, 0, sWidth);
        return result;
    }

    public static Kernel generateKernel(float radius) {
        int r = (int) Math.ceil(radius);
        int rows = r * 2 + 1;
        float[] matrix = new float[rows];
        float sigma = radius / 3;
        float sigma22 = 2 * sigma * sigma;
        float sigmaPi2 = 2 * (float) Math.PI * sigma;
        float sqrtSigmaPi2 = (float) Math.sqrt(sigmaPi2);
        float radius2 = radius * radius;
        float total = 0;
        int index = 0;
        for (int row = -r; row <= r; row++) {
            float distance = row * row;
            if (distance > radius2)
                matrix[index] = 0;
            else
                matrix[index] = (float) Math.exp(-(distance) / sigma22) / sqrtSigmaPi2;
            total += matrix[index];
            index++;
        }
        for (int i = 0; i < rows; i++)
            matrix[i] /= total;

        return new Kernel(rows, 1, matrix);
    }
}
