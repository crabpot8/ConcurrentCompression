package edu.vt.ece5510.jpeg;

import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.util.ArrayList;

/*
 * JpegInfo - Given an image, sets default information about it and divides it
 * into its constituant components, downsizing those that need to be.
 */
class JpegInfo {

	// the following are set as the default
	static final int Precision = 8;
	static final int NumberOfComponents = 3;
	String comment;
	private Image imageobj;

	int imageHeight;
	int imageWidth;
	int BlockWidth[];
	int BlockHeight[];

	Object Components[];

	int[] CompID = { 1, 2, 3 };

	int[] horizSampleFactor = { 1, 1, 1 };
	int[] vertSampleFactor = { 1, 1, 1 };

	int[] quantizeTableNumbers = { 0, 1, 1 };

	int[] DCtableNumber = { 0, 1, 1 };
	int[] ACtableNumber = { 0, 1, 1 };

	private boolean[] lastColumnIsDummy = { false, false, false };
	private boolean[] lastRowIsDummy = { false, false, false };

	int Ss = 0;
	int Se = 63;
	int Ah = 0;
	int Al = 0;

	private int compWidth[], compHeight[];
	private int MaxHsampFactor;
	private int MaxVsampFactor;

	public JpegInfo(Image image) {
		Components = new Object[NumberOfComponents];
		compWidth = new int[NumberOfComponents];
		compHeight = new int[NumberOfComponents];
		BlockWidth = new int[NumberOfComponents];
		BlockHeight = new int[NumberOfComponents];
		imageobj = image;
		imageWidth = image.getWidth(null);
		imageHeight = image.getHeight(null);
		comment = "JPEG Encoder Copyright 1998, James R. Weeks and BioElectroMech.  ";
		getYCCArray();
	}

	public void setComment(String comment) {
		comment.concat(comment);
	}

	public String getComment() {
		return comment;
	}

	/*
	 * This method creates and fills three arrays, Y, Cb, and Cr using the input image.
	 */
	private void getYCCArray() {
		int values[] = new int[imageWidth * imageHeight];
		int r, g, b, y, x;
		// In order to minimize the chance that grabPixels will throw an exception
		// it may be necessary to grab some pixels every few scanlines and process
		// those before going for more. The time expense may be prohibitive.
		// However, for a situation where memory overhead is a concern, this may be the only choice.
		PixelGrabber grabber = new PixelGrabber(imageobj.getSource(), 0, 0, imageWidth, imageHeight, values, 0, imageWidth);
		MaxHsampFactor = 1;
		MaxVsampFactor = 1;
		for (y = 0; y < NumberOfComponents; y++) {
			MaxHsampFactor = Math.max(MaxHsampFactor, horizSampleFactor[y]);
			MaxVsampFactor = Math.max(MaxVsampFactor, vertSampleFactor[y]);
		}
		for (y = 0; y < NumberOfComponents; y++) {
		compWidth[y] = (((imageWidth % 8 != 0) ? ((int) Math
					.ceil(imageWidth / 8.0)) * 8 : imageWidth) / MaxHsampFactor)
					* horizSampleFactor[y];
			if (compWidth[y] != ((imageWidth / MaxHsampFactor) * horizSampleFactor[y])) {
				lastColumnIsDummy[y] = true;
			}
			// results in a multiple of 8 for compWidth; this will make the rest of the program fail for the unlikely
			// event that someone tries to compress an 16 x 16 pixel image which would of course be worse than pointless
			BlockWidth[y] = (int) Math.ceil(compWidth[y] / 8.0);
			compHeight[y] = (((imageHeight % 8 != 0) ? ((int) Math
					.ceil(imageHeight / 8.0)) * 8 : imageHeight) / MaxVsampFactor)
					* vertSampleFactor[y];
			if (compHeight[y] != ((imageHeight / MaxVsampFactor) * vertSampleFactor[y])) {
				lastRowIsDummy[y] = true;
			}
			BlockHeight[y] = (int) Math.ceil(compHeight[y] / 8.0);
		}
		try {
			grabber.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//method 1
		float Y[][] = new float[compHeight[0]][compWidth[0]];
		float Cr1[][] = new float[compHeight[0]][compWidth[0]];
		float Cb1[][] = new float[compHeight[0]][compWidth[0]];
		
		ArrayList<Thread> threads = new ArrayList<Thread>();
		for (y = 0; y < imageHeight; ++y) {
			for (x = 0; x < imageWidth; ++x) {
				Thread t = new Thread(new Method1(values, x, y, Y, Cb1, Cr1));
				threads.add(t);
				t.start();
			}
		}
		for(Thread t : threads){
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		Components[0] = Y;
		Components[1] = Cb1;
		Components[2] = Cr1;
		/*float Y[][] = new float[compHeight[0]][compWidth[0]];
		float Cr1[][] = new float[compHeight[0]][compWidth[0]];
		float Cb1[][] = new float[compHeight[0]][compWidth[0]];
		int index = 0;

		for (y = 0; y < imageHeight; ++y) {
			for (x = 0; x < imageWidth; ++x) {
				r = ((values[index] >> 16) & 0xff);
				g = ((values[index] >> 8) & 0xff);
				b = (values[index] & 0xff);

				// Color Conversion
				Y[y][x] = (float) ((0.299 * r + 0.587 * g + 0.114 * b));
				Cb1[y][x] = 128 + (float) ((-0.16874 * r - 0.33126 * g + 0.5 * b));
				Cr1[y][x] = 128 + (float) ((0.5 * r - 0.41869 * g - 0.08131 * b));
				index++;
			}
		}*/

		/*Components[0] = Y;
		Components[1] = Cb1;
		Components[2] = Cr1;*/
	}
	
	public class Method1 implements Runnable {
			
		int[] values;
		public int x;
		public int y;
		public float[][] Cy,Cb,Cr;
		
		public Method1(int[] val, int x_, int y_, float[][] cy, float[][] cb, float[][] cr){
			values = val;
			x = x_;
			y = y_;
			Cy = cy;
			Cb = cb;
			Cr = cr;
		}

		@Override
		public void run() {
			int index = y * imageWidth + x;
			int r = ((values[index] >> 16) & 0xff);
			int g = ((values[index] >> 8) & 0xff);
			int b = (values[index] & 0xff);

			// Color Conversion
			Cy[y][x] = (float) ((0.299 * r + 0.587 * g + 0.114 * b));
			Cb[y][x] = 128 + (float) ((-0.16874 * r - 0.33126 * g + 0.5 * b));
			Cr[y][x] = 128 + (float) ((0.5 * r - 0.41869 * g - 0.08131 * b));
			
		}
	}
	
public class Method2Y implements Runnable{
		
	int[] values;
	float[][] Y;
	
		public Method2Y(int[] val,float[][] mat){
			values = val;
			Y = mat;
		}
		
		public void run(){
			for (int y = 0; y < imageHeight; ++y) {
				for (int x = 0; x < imageWidth; ++x) {
					int index = y * imageWidth + x;
					int r = ((values[index] >> 16) & 0xff);
					int g = ((values[index] >> 8) & 0xff);
					int b = (values[index] & 0xff);	
					Y[y][x] = (float) ((0.299 * r + 0.587 * g + 0.114 * b));
				}
			}
		}
	}

public class Method2B implements Runnable{
	
	int[] values;
	float[][] Cb;
	
	public Method2B(int[] val,float[][] mat){
		values = val;
		Cb= mat;
	}
	
	public void run(){
		for (int y = 0; y < imageHeight; ++y) {
			for (int x = 0; x < imageWidth; ++x) {
				int index = y * imageWidth + x;
				int r = ((values[index] >> 16) & 0xff);
				int g = ((values[index] >> 8) & 0xff);
				int b = (values[index] & 0xff);	
				Cb[y][x] = 128 + (float) ((-0.16874 * r - 0.33126 * g + 0.5 * b));
			}
		}
	}
}

public class Method2R implements Runnable{
	
	int[] values;
	float[][] Cr;
	
	public Method2R(int[] val,float[][] mat){
		values = val;
		Cr = mat;
	}
	
	public void run(){
		for (int y = 0; y < imageHeight; ++y) {
			for (int x = 0; x < imageWidth; ++x) {
				int index = y * imageWidth + x;
				int r = ((values[index] >> 16) & 0xff);
				int g = ((values[index] >> 8) & 0xff);
				int b = (values[index] & 0xff);	
				Cr[y][x] = 128 + (float) ((0.5 * r - 0.41869 * g - 0.08131 * b));
			}
		}
	}
}

}