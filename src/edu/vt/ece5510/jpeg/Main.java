package edu.vt.ece5510.jpeg;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Main {

	private static final String DATA_OUT_DIR = "data/out/";
	private static final String DATA_IN_FILE = "data/summary.txt";

	public static void main(String[] args) {

		BufferedImage current = null;
		try {
			current = ImageIO.read(new File("dice.png"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JpegEncoder en = null;
		try {
			en = new JpegEncoder(current, 5, new FileOutputStream(
					"dice_low.jpg"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		en.compress();
		
		/*
		
		try {
			doIt();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

	public static void doIt() throws IOException, FileNotFoundException {
		ArrayList<File> inFiles = new ArrayList<File>();

		FileInputStream dataIn = new FileInputStream(new File(DATA_IN_FILE));
		InputStreamReader foo = new InputStreamReader(dataIn);
		BufferedReader dataInBuffer = new BufferedReader(foo);

		String line;
		while (null != (line = dataInBuffer.readLine())) {
			BufferedImage current = ImageIO.read(new File(line));
			JpegEncoder e = new JpegEncoder(current, 5, new FileOutputStream(
					"dice_low.jpg"));

			// start timing
			// modify this to write everything into memory, and then return that
			// memory so that the FileIO does not get included in the timing
			// information. In fact, the test can likely avoid writing output files at all? 
			e.compress();
		}

	}
}
