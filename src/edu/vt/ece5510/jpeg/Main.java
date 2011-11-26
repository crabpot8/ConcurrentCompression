package edu.vt.ece5510.jpeg;

import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.PixelGrabber;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Main {

	public static void main(String[] args) {
		BufferedImage img = null;

		try {
			img = ImageIO.read(new File("dice.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			JpegEncoder e = new JpegEncoder(img, 5, new FileOutputStream("dice_low.jpg"));
			e.compress();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		/*
		int w = img.getWidth();
		int h = img.getHeight();
		int[] pixels = new int[w * h];
		PixelGrabber grabber = new PixelGrabber(img, 0, 0, w, h, pixels, 0, w);
		try {
			grabber.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		DataBufferInt buffer = new DataBufferInt(pixels, w * h);

		int[] bitmasks = new int[] { 0xff0000, 0xff00, 0xff, 0xff000000 };
		SinglePixelPackedSampleModel sampleModel = new SinglePixelPackedSampleModel(
				DataBuffer.TYPE_INT, w, h, bitmasks);

		int[] greens = new int[w * h];
		sampleModel.getSamples(0, 0, w, h, 1, greens, buffer);

		
		/*/
	}


	
}
