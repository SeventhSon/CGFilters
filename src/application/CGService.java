package application;

import java.util.ArrayList;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class CGService {

	private ArrayList<Image> mChangeList;

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
		mChangeList = new ArrayList<>();
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

		float slope = factor >= 0 ? factor * 20 : 1 / (-factor * 20);
		for (int i = 0; i < 256; i++) {
			contarr[i] = (int) ((i - 128) * slope + 128);
			if (contarr[i] < 0)
				contarr[i] = 0;
			else if (contarr[i] > 255)
				contarr[i] = 255;
		}
		return contarr;
	}

	public void storeChange(Image img) {
		mChangeList.add(img);
		mCurrentChange++;
	}

	public Image undo() {
		mChangeList.get(mCurrentChange);
		return null;
	}

	public Image redo() {
		return null;
	}
}
