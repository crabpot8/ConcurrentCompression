package edu.vt.ece5510.jpeg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.imageio.ImageIO;

import edu.vt.ece5510.jpeg.JpegEncoder.DataApproach;
import edu.vt.ece5510.jpeg.JpegEncoder.Timings;
import edu.vt.ece5510.jpeg.JpegInfo.Approach;

public class Main {

	private static final String IMG_OUT_DIR = "data/out/";
	private static final String IMG_IN_DIR = "data/in/";
	private static final String OUT_JPEG_COLOR_CONVERSION = "data/color_conversion_timings.csv";
	private static final String OUT_WRITE_COMPRESSED_DATA_TIMINGS = "data/write_compressed_data_timings.csv";
	private static final String OUT_BUILDING_AND_WRITING = "data/building_and_writing.csv";
	
	public static void main(String[] args) {

		try {
			 //timeBuildingAndWriting();
			 //timeBuildingJpegInfo();
			 timeWritingData();
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
			String outFile = IMG_OUT_DIR
					+ line.substring(0, line.lastIndexOf(".")) + ".jpg";

			JpegEncoder e = new JpegEncoder(current, r.nextInt(100) + 1,
					new FileOutputStream(outFile));

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
	
	public static void timeWritingData() throws IOException,
		FileNotFoundException{
		File inDir = new File(IMG_IN_DIR);

		PrintWriter timings = new PrintWriter(new File(OUT_WRITE_COMPRESSED_DATA_TIMINGS));
		timings.println("Single,Multi");

		Random r = new Random();

		int progress = 1;
		for (File currImg : inDir.listFiles()) {
			System.gc();
			System.out.print(".");
			if (progress++ % 80 == 0)
				System.out.println("");

			BufferedImage current = ImageIO.read(currImg);
			if (current == null)
				continue;

			int quality = r.nextInt(100) + 1;
			JpegInfo.mApproach = Approach.SingleThread;
			JpegEncoder.mDataApproach = DataApproach.SingleThread;
			JpegEncoder e = new JpegEncoder(current, quality,
					new BufferedOutputStreamSink());
			
			e.compress();

			timings.print(e.timings.writingCompressedData);
			timings.print(',');

			JpegInfo.mApproach = Approach.SingleThread;
			JpegEncoder.mDataApproach = DataApproach.MultiThread;
			e = new JpegEncoder(current, quality,
					new BufferedOutputStreamSink());
			e.compress();

			timings.println(e.timings.writingCompressedData);
		}

		timings.flush();
		timings.close();
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
			System.out.print(".");
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

			JpegInfo.mApproach = Approach.ColumnColorConvert;
			e = new JpegEncoder(current, quality,
					new BufferedOutputStreamSink());
			e.compress();

			timings.println(e.timings.jpegInfoColorConversion);
		}

		timings.flush();
		timings.close();

	}

}
