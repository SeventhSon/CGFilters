package application;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
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

	public void setSize(int i) {
		SIZE = i;
	}

	public void scanline(WritableImage source, int x1, int y1) {
		PixelWriter pw = source.getPixelWriter();
		int argb = 255 << 24 | 0 << 16 | 0 << 8 | 0;

		Point[] vertices = new Point[] { new Point(0, -50), new Point(14, -20),
				new Point(47, -15), new Point(23, 7), new Point(29, 40),
				new Point(-23, 7), new Point(-47, -15), new Point(-14, -20) };

		for (Point point : vertices) {
			point.x += x1;
			point.y += y1;
		}
		int y = Integer.MAX_VALUE;
		for (int i = 0; i < vertices.length; i++)
			y = y > vertices[i].y ? vertices[i].y : y;
		HashMap<Integer, Edge> edgeTable = constructEdgeTable(vertices);
		LinkedList<Edge> activeEdgeTable = new LinkedList<Edge>();

		while (!edgeTable.isEmpty() || !activeEdgeTable.isEmpty()) {
			Edge bucket = edgeTable.remove(y);
			if (bucket != null) {
				System.out.print(y);
				while (bucket.next != null) {
					System.out.print(" Edge(" + bucket.m + " " + bucket.x + " "
							+ bucket.ymin + " " + bucket.ymax + ") ");
					activeEdgeTable.add(bucket);
					bucket = bucket.next;
				}
				System.out.print(" Edge(" + bucket.m + " " + bucket.x + " "
						+ bucket.ymin + " " + bucket.ymax + ") \n");
				activeEdgeTable.add(bucket);
			}

			Collections.sort(activeEdgeTable, new EdgeComparator2());

			boolean paint = false;
			int minx = 0;

			for (int i = 0; i < activeEdgeTable.size(); i++) {
				Edge edge = activeEdgeTable.get(i);
				if (edge.ymax == y) {
					activeEdgeTable.remove(edge);
					i--;
					System.out.println("removing" + edge.m + " " + edge.x + " "
							+ edge.ymin + " " + edge.ymax + " " + y);
				} else {
					if (paint) {
						System.out.println("from " + minx + " to " + edge.x);
						for (int x = minx; x < edge.x; x++) {
							pw.setArgb(x, y, argb);
						}
						paint = false;
					} else {
						paint = true;
					}
					minx = (int) edge.x;
					edge.x += edge.m;
				}
			}
			y++;
		}
	}

	public HashMap<Integer, Edge> constructEdgeTable(Point[] vertices) {
		HashMap<Integer, Edge> EdgeTable = new HashMap<Integer, Edge>();
		int ymin, ymax, x;
		float m;
		for (int i = 1; i < vertices.length; i++) {
			if (vertices[i - 1].y < vertices[i].y) {
				ymin = vertices[i - 1].y;
				x = vertices[i - 1].x;
				ymax = vertices[i].y;
			} else {
				ymin = vertices[i].y;
				ymax = vertices[i - 1].y;
				x = vertices[i].x;
			}
			m = vertices[i].y - vertices[i - 1].y != 0 ? 1.0f
					* (vertices[i].x - vertices[i - 1].x)
					/ (vertices[i].y - vertices[i - 1].y) : 0;
			if (EdgeTable.containsKey(ymin)) {
				Edge bucket = EdgeTable.get(ymin);
				while (bucket.next != null)
					bucket = bucket.next;
				bucket.next = new Edge(ymin, ymax, x, m);
			} else {
				EdgeTable.put(ymin, new Edge(ymin, ymax, x, m));
			}
			System.out.println("Insert Edge(" + m + " " + x + " " + ymin + " "
					+ ymax + ") ");

		}
		if (vertices[vertices.length - 1].y <= vertices[0].y) {
			ymin = vertices[vertices.length - 1].y;
			x = vertices[vertices.length - 1].x;
			ymax = vertices[0].y;
		} else {
			ymin = vertices[0].y;
			ymax = vertices[vertices.length - 1].y;
			x = vertices[0].x;
		}
		m = vertices[0].y - vertices[vertices.length - 1].y != 0 ? 1.0f
				* (vertices[0].x - vertices[vertices.length - 1].x)
				/ (vertices[0].y - vertices[vertices.length - 1].y) : 0;
		if (EdgeTable.containsKey(ymin)) {
			Edge bucket = EdgeTable.get(ymin);
			while (bucket.next != null)
				bucket = bucket.next;
			bucket.next = new Edge(ymin, ymax, x, m);
		} else {
			EdgeTable.put(ymin, new Edge(ymin, ymax, x, m));
		}
		System.out.println("Insert Edge(" + m + " " + x + " " + ymin + " "
				+ ymax + ") ");

		return EdgeTable;
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
		drawBrush(x + x1, y + y1, SIZE, argb, pw);
		drawBrush(y + x1, x + y1, SIZE, argb, pw);
		drawBrush(-x + x1, y + y1, SIZE, argb, pw);
		drawBrush(-y + x1, x + y1, SIZE, argb, pw);
		drawBrush(-x + x1, -y + y1, SIZE, argb, pw);
		drawBrush(-y + x1, -x + y1, SIZE, argb, pw);
		drawBrush(x + x1, -y + y1, SIZE, argb, pw);
		drawBrush(y + x1, -x + y1, SIZE, argb, pw);
	}

	public Image msaa(WritableImage source) {
		WritableImage out = new WritableImage((int) source.getWidth() / 2,
				(int) source.getHeight() / 2);
		PixelReader pr = source.getPixelReader();
		PixelWriter pw = out.getPixelWriter();
		int s1, s2, s3, s4;
		for (int i = 0; i < out.getHeight(); i++)
			for (int j = 0; j < out.getWidth(); j++) {
				s1 = pr.getArgb(j * 2, i * 2);
				s2 = pr.getArgb(j * 2, i * 2 + 1);
				s3 = pr.getArgb(j * 2 + 1, i * 2);
				s4 = pr.getArgb(j * 2 + 1, i * 2 + 1);
				int r = (s1 >> 16) & 255 + (s2 >> 16) & 255 + (s3 >> 16) & 255
						+ (s4 >> 16) & 255;
				int g = (s1 >> 8) & 255 + (s2 >> 8) & 255 + (s3 >> 8) & 255
						+ (s4 >> 8) & 255;
				int b = s1 & 255 + s2 & 255 + s3 & 255 + s4 & 255;

				int argb = 255 << 24 | (r / 4) << 16 | (g / 4) << 8 | (b / 4);
				pw.setArgb(j, i, argb);
			}
		return out;
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

		if (dy < dx) {
			int dE = 2 * dy;
			int dNE = 2 * (dy - dx);
			int d = 2 * dy - dx;
			while ((x1 - x2) * sx < 0) {
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
			while ((y1 - y2) * sy < 0) {
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
		drawBrush(x1, y1, SIZE, argb, pw);
		drawBrush(x2, y2, SIZE, argb, pw);
	}

	private void drawBrush(int x, int y, int size, int argb, PixelWriter pw) {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				pw.setArgb(x + j - size / 2, y + i - size / 2, argb);
			}
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

	public void fill(WritableImage image, int mX, int mY) {
		PixelReader pr = image.getPixelReader();
		PixelWriter pw = image.getPixelWriter();
		Queue<Point2D> points = new LinkedList<Point2D>();
		int old = pr.getArgb(mX, mY);
		int fillColor = 255 << 24 | 255 << 16 | 255 << 8 | 255;
		points.add(new Point2D(mX, mY));

		while (!points.isEmpty()) {
			Point2D cur = points.poll();
			if (cur.getX() < 0 | cur.getY() < 0
					| cur.getX() >= image.getWidth()
					| cur.getY() >= image.getHeight())
				continue;
			if (pr.getArgb((int) cur.getX(), (int) cur.getY()) == old) {
				pw.setArgb((int) cur.getX(), (int) cur.getY(), fillColor);
				points.add(new Point2D(cur.getX() + 1, cur.getY()));
				points.add(new Point2D(cur.getX() - 1, cur.getY()));
				points.add(new Point2D(cur.getX(), cur.getY() - 1));
				points.add(new Point2D(cur.getX(), cur.getY() + 1));
			}
		}
	}
}

class Edge {
	int ymin;
	int ymax;
	float x;
	float m;
	Edge next;

	public Edge(int ymin, int ymax, int x, float m) {
		this.ymin = ymin;
		this.ymax = ymax;
		this.m = m;
		this.x = x;
		this.next = null;
	}

}

class Point {
	int x, y;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
}