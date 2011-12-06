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
	private static final String OUT_FULL_TIME_COMPARISON = "data/full_time.csv";
	
	public static int[] mNumThreads = new int[] {1,2,3,4,5,6,7,8,9,10};
	
	public static void main(String[] args) {

		try {
			fullTimeComparison();
			 //timeBuildingAndWriting();
			 timeBuildingJpegInfo();
			 timeWritingData();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void combinedAnalysis() throws IOException, FileNotFoundException{
		File inDir = new File(IMG_IN_DIR);
		
		PrintWriter totalTimings = new PrintWriter(new File(OUT_FULL_TIME_COMPARISON));
		PrintWriter writeTimings = new PrintWriter(new File(OUT_WRITE_COMPRESSED_DATA_TIMINGS));
		PrintWriter colorTimings = new PrintWriter(new File(OUT_JPEG_COLOR_CONVERSION));
		PrintWriter[] timings = new PrintWriter[]{totalTimings,writeTimings,colorTimings};
		for(PrintWriter pw : timings){
			pw.print("Single,");
			for(int i = 0; i < mNumThreads.length; i++){
				pw.print(mNumThreads[i] + " Worker,");
			}
			pw.println();
		}
	
		Random r = new Random();
	
		int progress = 1;
		
		long singleTime = 0;
		
		long[][] multiTime = new long[3][mNumThreads.length];
		for(int i = 0; i <mNumThreads.length; i++){
			multiTime[0][i] = 0;
			multiTime[1][i] = 0;
			multiTime[2][i] = 0;
		}
		int count = inDir.listFiles().length;
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
			long singleStart = System.nanoTime();
			JpegEncoder ss = new JpegEncoder(current, quality,
					new BufferedOutputStreamSink());
			ss.compress();
			long singleEnd = System.nanoTime() - singleStart;
			singleTime = singleTime + (singleEnd / count);
			
			for(int i = 0; i < mNumThreads.length; i++){
				int nT = mNumThreads[i];
				JpegEncoder.NUMBER_THREADS = nT;
				JpegInfo.threadCount = nT;
			
				JpegInfo.mApproach = Approach.SingleThread;
				JpegEncoder.mDataApproach = DataApproach.MultiThread;
				JpegEncoder sm = new JpegEncoder(current, quality,
						new BufferedOutputStreamSink());
				sm.compress();
				multiTime[1][i] = multiTime[1][i] + (sm.timings.writingCompressedData / count);
				
				JpegInfo.mApproach = Approach.ColumnColorConvert;
				JpegEncoder.mDataApproach = DataApproach.SingleThread;
				JpegEncoder ms = new JpegEncoder(current, quality,
						new BufferedOutputStreamSink());
				ms.compress();
				multiTime[2][i]  = multiTime[2][i] + (sm.timings.jpegInfoColorConversion / count);
				
				JpegInfo.mApproach = Approach.SingleThread;
				JpegEncoder.mDataApproach = DataApproach.SingleThread;
				long multiStart = System.nanoTime();
				JpegEncoder mm = new JpegEncoder(current, quality,
						new BufferedOutputStreamSink());
				mm.compress();
				long multiEnd = System.nanoTime() - multiStart;
				multiTime[0][i] = multiTime[0][i] + (multiEnd / count);
			
			}
			for(PrintWriter pw: timings){
				pw.print(singleTime + ",");
			}
			for(int i = 0; i < mNumThreads.length; i++){
				totalTimings.print(multiTime[0][i] + ",");
				writeTimings.print(multiTime[1][i] + ",");
				colorTimings.print(multiTime[2][i] + ",");
			}
			
			for(PrintWriter pw: timings){
				pw.println();
				pw.flush();
				pw.close();
			}
			//timings.print(single);
			//timings.print(',');
	
			
		}
		
	}
	
	public static void fullTimeComparison() throws IOException,FileNotFoundException{
		File inDir = new File(IMG_IN_DIR);
	
		PrintWriter timings = new PrintWriter(new File(OUT_FULL_TIME_COMPARISON));
		timings.print("Single,");
		for(int i = 0; i < mNumThreads.length; i++){
			timings.print(mNumThreads[i] + " Worker,");
		}
		timings.println();
	
		Random r = new Random();
	
		int progress = 1;
		
		long singleTime = 0;
		long[] multiTime = new long[mNumThreads.length];
		for(int i = 0; i < multiTime.length; i++){
			multiTime[i] = 0;
		}
		int count = inDir.listFiles().length;
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
			long start = System.nanoTime();
			JpegEncoder e = new JpegEncoder(current, quality,
					new BufferedOutputStreamSink());
			e.compress();
			long single = System.nanoTime() - start;
			singleTime += single / count;
	
			//timings.print(single);
			//timings.print(',');
	
			JpegInfo.mApproach = Approach.ColumnColorConvert;
			JpegEncoder.mDataApproach = DataApproach.MultiThread;
			for(int i = 0; i < mNumThreads.length; i++){
				int nT = mNumThreads[i];
				JpegEncoder.NUMBER_THREADS = nT;
				JpegInfo.threadCount = nT;
				start = System.nanoTime();
				e = new JpegEncoder(current, quality,
						new BufferedOutputStreamSink());
				e.compress();
				long multi = System.nanoTime() - start;
				multiTime[i] = multiTime[i] + (multi / count);
				//timings.print(multi + ",");
			}
			//timings.println();
		}
		timings.print(singleTime + ",");
		for(long l : multiTime){
			timings.print(l + ",");
		}
		timings.println();
	
		timings.flush();
		timings.close();
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

			JpegInfo.mApproach = Approach.SingleThread;
			JpegEncoder.mDataApproach = DataApproach.SingleThread;
			
			JpegEncoder e = new JpegEncoder(current, r.nextInt(100) + 1,new FileOutputStream(outFile));
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
		timings.print("Single,");
		for(int i = 0; i < mNumThreads.length; i++){
			timings.print(mNumThreads[i] + " Worker,");
		}
		timings.println();

		Random r = new Random();

		int progress = 1;
		long singleTime = 0;
		long[] multiTime = new long[mNumThreads.length];
		for(int i = 0; i < multiTime.length; i++){
			multiTime[i] = 0;
		}
		int count = inDir.listFiles().length;
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
			singleTime += e.timings.writingCompressedData / count;

			//timings.print(e.timings.writingCompressedData);
			//timings.print(',');

			for(int i = 0; i < mNumThreads.length; i++){
				int nT = mNumThreads[i];
				JpegEncoder.NUMBER_THREADS = nT;
				JpegInfo.threadCount = nT;
				JpegInfo.mApproach = Approach.SingleThread;
				JpegEncoder.mDataApproach = DataApproach.MultiThread;
				e = new JpegEncoder(current, quality,
						new BufferedOutputStreamSink());
				e.compress();
				
				multiTime[i] = multiTime[i] + (e.timings.writingCompressedData / count);
			}
		}

		timings.print(singleTime + ",");
		for(long l : multiTime){
			timings.print(l + ",");
		}
		timings.println();
		timings.flush();
		timings.close();
	}

	public static void timeBuildingJpegInfo() throws IOException,
			FileNotFoundException {
		File inDir = new File(IMG_IN_DIR);

		PrintWriter timings = new PrintWriter(new File(OUT_JPEG_COLOR_CONVERSION));
		timings.print("Single,");
		for(int i = 0; i < mNumThreads.length; i++){
			timings.print(mNumThreads[i] + " Worker,");
		}
		timings.println();

		Random r = new Random();

		int progress = 1;
		long singleTime = 0;
		long[] multiTime = new long[mNumThreads.length];
		for(int i = 0; i < multiTime.length; i++){
			multiTime[i] = 0;
		}
		int count = inDir.listFiles().length;
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

			singleTime += e.timings.jpegInfoColorConversion /count;
			//timings.print(e.timings.jpegInfoColorConversion);
			//timings.print(',');

			for(int i = 0; i < mNumThreads.length; i++){
				int nT = mNumThreads[i];
				JpegEncoder.NUMBER_THREADS = nT;
				JpegInfo.threadCount = nT;
				JpegInfo.mApproach = Approach.ColumnColorConvert;
				JpegEncoder.mDataApproach = DataApproach.SingleThread;
				e = new JpegEncoder(current, quality,
						new BufferedOutputStreamSink());
				e.compress();
	
				multiTime[i] = multiTime[i] + (e.timings.jpegInfoColorConversion / count);
			}
		}

		timings.print(singleTime + ",");
		for(long l : multiTime){
			timings.print(l + ",");
		}
		timings.println();
		timings.flush();
		timings.close();

	}

}
