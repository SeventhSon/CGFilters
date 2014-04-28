package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;

public class CGService {

	private Image[] mChangeList;

	private int mCurrentChange = -1;
	
	private int SIZE = 0;

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

	public WritableImage getNewImage(int width, int height) {
		WritableImage wi = new WritableImage(width, height);
		return wi;
	}
	
	public void setSize(int i){
		SIZE = i;
	}

	public void circle(WritableImage source, int x1, int y1, int x2, int y2) {
		if (source == null)
			return;
		PixelWriter pw = source.getPixelWriter();
		int argb = 255 << 24 | 0 << 16 | 0 << 8 | 0;
		int R = (int) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
		int d = 1 - R;
		int x = 0;
		int y = R;
		while (y > x) {
			drawBrush(x + x1, y + y1, SIZE, argb, pw);
			drawBrush(y + x1, x + y1, SIZE, argb, pw);
			drawBrush(-x + x1, y + y1, SIZE, argb, pw);
			drawBrush(-y + x1, x + y1, SIZE, argb, pw);
			drawBrush(-x + x1, -y + y1, SIZE, argb, pw);
			drawBrush(-y + x1, -x + y1, SIZE, argb, pw);
			drawBrush(x + x1, -y + y1, SIZE, argb, pw);
			drawBrush(y + x1, -x + y1, SIZE, argb, pw);
			if (d < 0) // move to E
				d += 2 * x + 3;
			else // move to SE
			{
				d += 2 * x - 2 * y + 5;
				--y;
			}
			++x;
		}
	}

	public void line(WritableImage source, int x1, int y1, int x2, int y2) {
		if (source == null)
			return;
		PixelWriter pw = source.getPixelWriter();

		int argb = 255 << 24 | 0 << 16 | 0 << 8 | 0;
		
		int sx = x1 < x2 ? 1 : -1;
		int sy = y1 < y2 ? 1 : -1;
		
		int dx = Math.abs(x2 - x1);
		int dy = Math.abs(y2 - y1);
		
		if (dy < dx ) {
			int dE = 2 * dy;
			int dNE = 2 * (dy - dx);
			int d = 2 * dy - dx;
			while ((x1 - x2)*sx < 0) {
				drawBrush(x1, y1, SIZE, argb, pw);
				drawBrush(x2, y2, SIZE, argb, pw);

				x1 += sx;
				x2 -= sx;
				if (d < 0)
					d += dE;
				else {
					d += dNE;
					y1 += sy;
					y2 -= sy;
				}
			}
		} else {
			int dE = 2 * dx;
			int dNE = 2 * (dx - dy);
			int d = 2 * dx - dy;
			while ((y1 - y2)*sy < 0 ) {
				drawBrush(x1, y1, SIZE, argb, pw);
				drawBrush(x2, y2, SIZE, argb, pw);

				y1 += sy;
				y2 -= sy;
				if (d < 0)
					d += dE;
				else {
					d += dNE;
					x1 += sx;
					x2 -= sx;
				}
			}
		}
	}
	
	private void drawBrush(int x, int y, int size, int argb, PixelWriter pw){
		pw.setArgb(x, y, argb);
		for(int i=1;i<size+1;i++){
			pw.setArgb(x, y-i, argb);
			pw.setArgb(x, y+i, argb);
		}
	}

	public WritableImage convolutionFilter(Image source, float[][] kernel) {
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

	public WritableImage thresholdingFilter(Image source, int[] lookupArray) {
		PixelReader pr = source.getPixelReader();
		if (pr == null)
			return null;
		WritableImage wi = new WritableImage((int) source.getWidth(),
				(int) source.getHeight());
		PixelWriter pw = wi.getPixelWriter();
		for (int y = 0; y < source.getHeight(); y++) {
			for (int x = 0; x < source.getWidth(); x++) {
				int argb = pr.getArgb(x, y);
				argb = (int) (0.299 * ((argb >> 16) & 255) + 0.587
						* ((argb >> 8) & 255) + 0.114 * (argb & 255));
				argb = lookupArray[argb];
				pw.setArgb(x, y, 255 << 24 | argb << 16 | argb << 8 | argb);
			}
		}
		return wi;
	}

	public WritableImage functionalFilter(Image source, int[] lookupArray) {
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

	public int[] getGamma(float gamma) {
		gamma = gamma / 100;
		int[] gammarr = new int[256];
		for (int i = 0; i < 256; i++) {
			gammarr[i] = (int) (Math.pow((float) i / 255, gamma) * 255);
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

	public int[] getAverageDither(int k) {
		int[] thresharr = new int[256];
		int div = 256 / k;
		int x = 0, z;
		for (int i = 0; i < 256; i++) {
			if (i > div) {
				System.out.println("========" + div);
				div += 256 / k;
				x++;
			}
			z = (int) (x / (k - 1.0f) * 255.0f);
			thresharr[i] = z > 255 ? 255 : z;
			System.out.println(i + ": (" + x + ") " + thresharr[i]);
		}
		return thresharr;
	}

	public int[] getThresholding() {
		int[] thresharr = new int[256];
		for (int i = 0; i < 256; i++)
			// thresharr[i] = i < 128 ? 0 : 0<<24|255<<16|255<<8|255;
			thresharr[i] = i < 128 ? 0 : 255;
		return thresharr;
	}

	public WritableImage kmeansQuantization(Image source, int k) {

		boolean isChanged = true;
		Random rand = new Random();

		int sWidth = (int) source.getWidth();
		int sHeight = (int) source.getHeight();

		int[] pixelData = new int[(int) (sWidth * sHeight)];

		source.getPixelReader().getPixels(0, 0, sWidth, sHeight,
				WritablePixelFormat.getIntArgbInstance(), pixelData, 0, sWidth);
		WritableImage wi = new WritableImage((int) sWidth, (int) sHeight);

		int[] argbPalette = new int[k];
		for (int i = 0; i < k; i++)
			argbPalette[i] = pixelData[rand.nextInt(pixelData.length)];

		int[] pixelsToGroups = new int[pixelData.length];

		int it = 0;
		while (isChanged && it < 50) {
			isChanged = false;
			int[][] sumsCounts = new int[k][4];
			for (int i = 0; i < pixelData.length; i++) {
				int minIndex = -1;
				int min = 99999999;
				for (int j = 0; j < argbPalette.length; j++) {
					if (min > distance(argbPalette[j], pixelData[i])) {
						min = distance(argbPalette[j], pixelData[i]);
						minIndex = j;
					}
				}
				if (pixelsToGroups[i] != minIndex) {
					pixelsToGroups[i] = minIndex;
					isChanged = true;
				}
				sumsCounts[minIndex][0] += (pixelData[i] >> 16) & 255;
				sumsCounts[minIndex][1] += (pixelData[i] >> 8) & 255;
				sumsCounts[minIndex][2] += pixelData[i] & 255;
				sumsCounts[minIndex][3]++;
			}
			for (int i = 0; i < argbPalette.length; i++)
				if (sumsCounts[i][3] != 0)
					argbPalette[i] = (255 << 24)
							| ((sumsCounts[i][0] / sumsCounts[i][3]) << 16)
							| ((sumsCounts[i][1] / sumsCounts[i][3]) << 8)
							| (sumsCounts[i][2] / sumsCounts[i][3]);
			it++;
		}
		System.out.println("Iterations " + it);
		for (int i = 0; i < pixelData.length; i++)
			pixelData[i] = argbPalette[pixelsToGroups[i]];

		wi.getPixelWriter().setPixels(0, 0, sWidth, sHeight,
				WritablePixelFormat.getIntArgbInstance(), pixelData, 0, sWidth);
		return wi;
	}

	public int distance(int color1, int color2) {
		int x1 = color1 >> 16 & 255;
		int y1 = color1 >> 8 & 255;
		int z1 = color1 & 255;

		int x2 = color2 >> 16 & 255;
		int y2 = color2 >> 8 & 255;
		int z2 = color2 & 255;

		return (int) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)
				+ (z1 - z2) * (z1 - z2));
	}
}
