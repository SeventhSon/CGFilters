package application;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;

public class CGService {

	private Image[] mChangeList;

	private int mCurrentChange = -1;

	private static volatile CGService instance = null;

	public static CGService getInstance() {
		if (instance == null) {
			synchronized (CGService.class) {
				if (instance == null) {
					instance = new CGService();
				}
			}
		}
		return instance;
	}

	private CGService() {
		// mChangeList = new Image[256];
	}

	public Image convolutionFilter(Image source, float[][] kernel) {
		int sWidth = (int) source.getWidth();
		int sHeight = (int) source.getHeight();
		int w = 3;
		int h = 3;
		float factor = kernel[3][0];
		float bias = kernel[3][1];
		int[] pixelData = new int[(int) (sWidth * sHeight)];
		int[] resultPixelData = new int[(int) (sWidth * sHeight)];
		source.getPixelReader().getPixels(0, 0, sWidth, sHeight,
				WritablePixelFormat.getIntArgbInstance(), pixelData, 0, sWidth);
		WritableImage wi = new WritableImage((int) sWidth, (int) sHeight);
		for (int y = 0; y < sHeight; y++)
			for (int x = 0; x < sWidth; x++) {
				int sumR = 0, sumG = 0, sumB = 0;
				for (int i = 0; i < h; i++)
					for (int j = 0; j < w; j++) {
						int px = pixelData[convertCoords(x + (j - w / 2), y
								+ (i - h / 2), sWidth, sHeight)];
						float kr = kernel[i][j];
						sumR += kr * ((px >> 16) & 255);
						sumG += kr * ((px >> 8) & 255);
						sumB += kr * (px & 255);
					}
				resultPixelData[y * sWidth + x] = 255 << 24
						| (int) Math
								.min(Math.max(sumR / factor + bias, 0), 255) << 16
						| (int) Math
								.min(Math.max(sumG / factor + bias, 0), 255) << 8
						| (int) Math
								.min(Math.max(sumB / factor + bias, 0), 255);
			}
		wi.getPixelWriter().setPixels(0, 0, sWidth, sHeight,
				WritablePixelFormat.getIntArgbInstance(), resultPixelData, 0,
				sWidth);
		return wi;
	}

	private int convertCoords(int x, int y, int w, int h) {
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

	public Image functionalFilter(Image source, int[] lookupArray) {
		PixelReader pr = source.getPixelReader();
		if (pr == null)
			return null;
		WritableImage wi = new WritableImage((int) source.getWidth(),
				(int) source.getHeight());
		PixelWriter pw = wi.getPixelWriter();
		for (int y = 0; y < source.getHeight(); y++) {
			for (int x = 0; x < source.getWidth(); x++) {
				int argb = pr.getArgb(x, y);
				pw.setArgb(x, y, 255 << 24
						| lookupArray[(argb >> 16) & 255] << 16
						| lookupArray[(argb >> 8) & 255] << 8
						| lookupArray[argb & 255]);
			}
		}
		return wi;
	}

	public int[] getInversion() {
		int[] invarr = new int[256];
		for (int i = 0; i < 256; i++)
			invarr[i] = 255 - i;
		return invarr;
	}

	public int[] getGamma(float factor) {
		float gamma = (factor + 100 / 200) * 2.2f;
		int[] gammarr = new int[256];
		for (int i = 0; i < 256; i++) {
			gammarr[i] = (int) Math.pow(i, gamma);
			if (gammarr[i] > 255)
				gammarr[i] = 255;
		}
		return gammarr;
	}

	public int[] getBrightness(float factor) {
		int[] brarr = new int[256];
		int offset = (int) (factor * 255);
		int x;
		for (int i = 0; i < 256; i++) {
			x = offset + i;
			if (x > 255)
				x = 255;
			else if (x < 0)
				x = 0;
			brarr[i] = x;
		}
		return brarr;
	}

	public int[] getContrast(float factor) {
		int[] contarr = new int[256];

		float slope = factor * 25;
		if (slope < 1)
			slope = 1;
		if (factor < 0)
			slope = 1 + factor;
		for (int i = 0; i < 256; i++) {
			contarr[i] = (int) ((i - 128) * slope + 128);
			if (contarr[i] < 0)
				contarr[i] = 0;
			else if (contarr[i] > 255)
				contarr[i] = 255;
		}
		return contarr;
	}

	public synchronized void storeChange(Image img) {
		mChangeList[++mCurrentChange] = img;
		for (int i = mCurrentChange + 1; i < mChangeList.length
				&& mChangeList[i] != null; i++)
			mChangeList[i] = null;
	}

	public synchronized Image undo() {
		return mChangeList[--mCurrentChange];
	}

	public synchronized Image redo() {
		if (mChangeList[mCurrentChange + 1] != null) {
			return mChangeList[++mCurrentChange];
		}
		return null;
	}

	public float[][] getBlur() {
		return new float[][] { { 1.0f, 1f, 1.0f }, { 1f, 1f, 1f },
				{ 1.0f, 1f, 1.0f }, { 9f, 0 } };
	}
	
	public float[][] getGauss() {
		return new float[][] { { 0.0f, 1f, 0.0f }, { 1f, 4f, 1f },
				{ 0.0f, 1f, 0.0f }, { 8f, 0 } };
	}
	
	public float[][] getSharpen() {
		return new float[][] { { 0.0f, -1f, 0.0f }, { -1f, 5f, -1f },
				{ 0.0f, -1f, 0.0f }, { 1f, 0 } };
	}

	public float[][] getEmboss() {
		return new float[][] { { -1.0f, -1f, 0.0f }, { -1f, 1f, 1f },
				{ 0.0f, 1f, 1.0f }, { 1f, 0 } };
	}

	public float[][] getEdge() {
		return new float[][] { { -1.0f, 0f, 0.0f }, { 0f, 1f, 0f },
				{ 0.0f, 0f, 0.0f }, { 1f, 0 } };
	}
}
