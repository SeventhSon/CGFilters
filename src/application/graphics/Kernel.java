package application.graphics;

public class Kernel {
    private float[] matrix;
    private int width;
    private int height;

    public Kernel(int height, int width, float[] matrix) {
        this.matrix = matrix;
        this.width = width;
        this.height = height;
    }

    public float get(int row, int col) {
        return matrix[row * width + col];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
