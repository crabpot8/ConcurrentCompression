package edu.vt.ece5510.jpeg;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

import edu.vt.ece5510.jpeg.JpegEncoder.Timings;

public class Main {

	private static final String IMG_OUT_DIR = "data/out/";
	private static final String IMG_IN_FILE = "data/summary.txt";
	private static final String TIMING_OUT_FILE = "data/timings.csv";

	public static void main(String[] args) {

		try {
			doIt();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void doIt() throws IOException, FileNotFoundException {
		FileInputStream dataIn = new FileInputStream(new File(IMG_IN_FILE));
		InputStreamReader foo = new InputStreamReader(dataIn);
		BufferedReader dataInBuffer = new BufferedReader(foo);

		PrintWriter timings = new PrintWriter(new File(TIMING_OUT_FILE));
		timings.println("bjpeg,bdct,bhuff,wall,whead,wcd,weoi,mtime");

		Random r = new Random();
		String line;
		while (null != (line = dataInBuffer.readLine())) {
			System.out.println("loop");
			BufferedImage current = ImageIO.read(new File(line));
			int dirIndex = line.lastIndexOf('/') + 1;
			String outFile = IMG_OUT_DIR
					+ line.substring(dirIndex, line.lastIndexOf(".")) + ".jpg";

			JpegEncoder e = new JpegEncoder(current, r.nextInt(100) + 1,
					new FileOutputStream(outFile));

			// modify this to write everything into memory, and then return that
			// memory so that the FileIO does not get included in the timing
			// information. In fact, the test can likely avoid writing output
			// files at all?
			e.compress();

			Timings t = e.timings;

			timings.print(t.buildingJpegInfo);
			timings.print(',');
			timings.print(t.buildingDCT);
			timings.print(',');
			timings.print(t.buildingHuffman);
			timings.print(',');
			timings.print(t.writingAll);
			timings.print(',');
			timings.print(t.writingHeaders);
			timings.print(',');
			timings.print(t.writingCompressedData);
			timings.print(',');
			timings.print(t.writingEOI);
			timings.print(',');
			timings.println(t.jpegInfoMethod);
		}

		timings.flush();
		timings.close();

	}
}
