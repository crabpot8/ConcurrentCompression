package edu.vt.ece5510.jpeg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.imageio.ImageIO;

import edu.vt.ece5510.jpeg.JpegEncoder.Timings;
import edu.vt.ece5510.jpeg.JpegInfo.Approach;

public class Main {

	private static final String IMG_OUT_DIR = "data/out/";
	private static final String IMG_IN_DIR = "data/in/";
	private static final String TIMING_OUT_FILE = "data/timings.csv";

	public static void main(String[] args) {

		try {
			// timeBuildingAndWriting();
			timeBuildingJpegInfo();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void timeBuildingAndWriting() throws IOException,
			FileNotFoundException {
		File inDir = new File(IMG_IN_DIR);

		PrintWriter timings = new PrintWriter(new File(TIMING_OUT_FILE));
		timings.println("bjpeg,bdct,bhuff,wall,whead,wcd,weoi,mtime");

		Random r = new Random();
		String line;
		for (File currImg : inDir.listFiles()) {
			line = currImg.getName();
			BufferedImage current = ImageIO.read(currImg);
			if (current == null)
				continue;
			String outFile = IMG_OUT_DIR
					+ line.substring(0, line.lastIndexOf(".")) + ".jpg";

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
			timings.println(t.jpegInfoColorConversion);
		}

		timings.flush();
		timings.close();

	}

	public static void timeBuildingJpegInfo() throws IOException,
			FileNotFoundException {
		File inDir = new File(IMG_IN_DIR);

		PrintWriter timings = new PrintWriter(new File(TIMING_OUT_FILE));
		timings.println("Single,Multi");

		Random r = new Random();
		String line;
		int progress = 1;
		for (File currImg : inDir.listFiles()) {
			System.gc();
			System.out.print(".");
			if (progress++ % 80 == 0)
				System.out.println("");
			line = currImg.getName();
			BufferedImage current = ImageIO.read(currImg);
			if (current == null)
				continue;

			int quality = r.nextInt(100) + 1;
			JpegInfo.mApproach = Approach.SingleThread;
			JpegEncoder e = new JpegEncoder(current, quality, new BufferedOutputStreamSink());
			e.compress();

			timings.print(e.timings.jpegInfoColorConversion);
			timings.print(',');

			JpegInfo.mApproach = Approach.ThreadPerComponent;
			e = new JpegEncoder(current, quality, new BufferedOutputStreamSink());
			e.compress();

			timings.println(e.timings.jpegInfoColorConversion);
		}

		timings.flush();
		timings.close();

	}

}
