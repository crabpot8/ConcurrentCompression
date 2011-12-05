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
import edu.vt.ece5510.jpeg.JpegEncoder.WriteTimings;

public class Main {

	private static final String IMG_OUT_DIR = "data/out/";
	private static final String IMG_IN_DIR = "data/in/";
	private static final String OUT_JPEG_COLOR_CONVERSION = "data/color_conversion_timings.csv";
	private static final String OUT_WRITE_COMPRESSED_DATA_TIMINGS = "data/write_compressed_data_timings.csv";
	private static final String OUT_BUILDING_AND_WRITING = "data/building_and_writing.csv";

	public static void main(String[] args) {

		try {
			 timeBuildingAndWriting();
			//timeBuildingJpegInfo();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void timeBuildingAndWriting() throws IOException,
			FileNotFoundException {
		File inDir = new File(IMG_IN_DIR);

		PrintWriter timings = new PrintWriter(new File(OUT_BUILDING_AND_WRITING));
		timings.println("bjpeg,bdct,bhuff,wall,whead,wcd,weoi,mtime");
		PrintWriter writeTimings = new PrintWriter(new File(
				OUT_WRITE_COMPRESSED_DATA_TIMINGS));
		writeTimings
				.println("setup,blockTime,generateDCT,forwardDCT,quantizeDCT,huffman");

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

			WriteTimings w = e.writeTimings;
			writeTimings.print(w.setup);
			writeTimings.print(',');
			writeTimings.print(w.blockTime);
			writeTimings.print(',');
			writeTimings.print(w.generateDCT);
			writeTimings.print(',');
			writeTimings.print(w.forwardDCT);
			writeTimings.print(',');
			writeTimings.print(w.quantizeDCT);
			writeTimings.print(',');
			writeTimings.print(w.huffman);
			writeTimings.println();
		}

		timings.flush();
		timings.close();
		writeTimings.flush();
		writeTimings.close();

	}

	public static void timeBuildingJpegInfo() throws IOException,
			FileNotFoundException {
		File inDir = new File(IMG_IN_DIR);

		PrintWriter timings = new PrintWriter(new File(OUT_JPEG_COLOR_CONVERSION));
		timings.println("Single,Multi");

		Random r = new Random();

		int progress = 1;
		for (File currImg : inDir.listFiles()) {
			System.gc();
			//System.out.print(".");
			System.out.println(currImg.getName());
			if (progress++ % 80 == 0)
				System.out.println("");

			BufferedImage current = ImageIO.read(currImg);
			if (current == null)
				continue;

			int quality = r.nextInt(100) + 1;
			JpegInfo.mApproach = Approach.SingleThread;
			JpegEncoder e = new JpegEncoder(current, quality,
					new BufferedOutputStreamSink());
			e.compress();

			timings.print(e.timings.jpegInfoColorConversion);
			timings.print(',');

			JpegInfo.mApproach = Approach.ThreadPerComponent;
			e = new JpegEncoder(current, quality,
					new BufferedOutputStreamSink());
			e.compress();

			timings.println(e.timings.jpegInfoColorConversion);
		}

		timings.flush();
		timings.close();

	}

}
