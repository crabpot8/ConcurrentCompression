// Retrieved this file from 
// http://www.java2s.com/Code/Java/2D-Graphics-GUI/Performsajpegcompressionofanimage.htm

// Copyright (C) 1998, James R. Weeks and BioElectroMech.
// Visit BioElectroMech at www.obrador.com.  Email James@obrador.com.

// This software is based in part on the work of the Independent JPEG Group.
// See license.txt for details about the allowed used of this software.
// See IJGreadme.txt for details about the Independent JPEG Group's license.

// The following is the above-referenced license.txt
//The JpegEncoder and its associated classes are Copyright (c) 1998, James R.
//Weeks and BioElectroMech.  This software is based in part on the work of the
//Independent JPEG Group.
//
//Redistribution and use in source and binary forms, with or without
//modification, are permitted provided that the following conditions are met:
//
//1. Redistributions of source code must retain the above copyright notice, this
//list of conditions, all files included with the source code, and the following
//disclaimer.
//2. Redistributions in binary form must reproduce the above copyright notice,
//this list of conditions and the following disclaimer in the documentation
//and/or other materials provided with the distribution.
//
//THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY
//EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
//WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
//DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
//(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
//LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
//ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
//(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package edu.vt.ece5510.jpeg;

import java.awt.Image;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

// Version 1.0a
// Copyright (C) 1998, James R. Weeks and BioElectroMech.
// Visit BioElectroMech at www.obrador.com. Email James@obrador.com.

// See license.txt for details about the allowed used of this software.
// This software is based in part on the work of the Independent JPEG Group.
// See IJGreadme.txt for details about the Independent JPEG Group's license.

// This encoder is inspired by the Java Jpeg encoder by Florian Raemy,
// studwww.eurecom.fr/~raemy.
// It borrows a great deal of code and structure from the Independent
// Jpeg Group's Jpeg 6a library, Copyright Thomas G. Lane.
// See license.txt for details.

/*
 * JpegEncoder - The JPEG main program which performs a jpeg compression of an
 * image.
 */
class JpegEncoder {

	private BufferedOutputStream mOutStream;

	private JpegInfo mJpegInfo;

	private Huffman mHuffman;

	private DCT mDCT;

	/**
	 * 0 to 100 and from bad image quality, high compression to good image
	 * quality low compression
	 */
	private int mQuality;

	private Image mImage;

	// Converts zigzag order to natural order
	private int[] jpegNaturalOrder = { 0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32,
			25, 18, 11, 4, 5, 12, 19, 26, 33, 40, 48, 41, 34, 27, 20, 13, 6, 7,
			14, 21, 28, 35, 42, 49, 56, 57, 50, 43, 36, 29, 22, 15, 23, 30, 37,
			44, 51, 58, 59, 52, 45, 38, 31, 39, 46, 53, 60, 61, 54, 47, 55, 62,
			63, };

	/**
	 * 
	 * @param image
	 *            An image that should be compressed into jpeg. For proper speed
	 *            tests this image should be fully loaded from disk / network
	 * @param quality
	 *            A 0..100 low to high quality parameter. Low quality means high
	 *            compression / low filesize, and high quality means low
	 *            compression / big filesize
	 * @param out
	 *            The OutputStream the resulting data should be written to.
	 */
	public JpegEncoder(Image image, int quality, OutputStream out) {
		mQuality = quality;
		mImage = image;
		mOutStream = new BufferedOutputStream(out);
	}

	public void compress() {
		long start = System.nanoTime();
		mJpegInfo = new JpegInfo(mImage);
		timings.buildingJpegInfo = System.nanoTime() - start;
		timings.jpegInfoColorConversion = mJpegInfo.methodtime;
		start = System.nanoTime();
		mDCT = new DCT(mQuality);
		timings.buildingDCT = System.nanoTime() - start;
		start = System.nanoTime();
		mHuffman = new Huffman(mJpegInfo.imageWidth, mJpegInfo.imageHeight);
		timings.buildingHuffman = System.nanoTime() - start;
		start = System.nanoTime();

		writeHeaders(mOutStream);
		try {
			mOutStream.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		timings.writingHeaders = System.nanoTime() - start;
		start = System.nanoTime();

		writeCompressedData(mOutStream);
		try {
			mOutStream.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		timings.writingCompressedData = System.nanoTime() - start;
		start = System.nanoTime();

		writeEOI(mOutStream);
		try {
			mOutStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		timings.writingEOI = System.nanoTime() - start;
		timings.writingAll = timings.writingCompressedData + timings.writingEOI
				+ timings.writingHeaders;
		try {
			mOutStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeCompressedData(BufferedOutputStream outStream) {

		/*
		 * This method controls the compression of the image. Starting at the
		 * upper left of the image, it compresses 8x8 blocks of data until the
		 * entire image has been compressed.
		 */

		// Setup the block width and height
		int MinBlockWidth, MinBlockHeight, currComponent;

		MinBlockWidth = ((mJpegInfo.imageWidth % 8 != 0) ? (int) (Math
				.floor(mJpegInfo.imageWidth / 8.0) + 1) * 8
				: mJpegInfo.imageWidth);
		MinBlockHeight = ((mJpegInfo.imageHeight % 8 != 0) ? (int) (Math
				.floor(mJpegInfo.imageHeight / 8.0) + 1) * 8
				: mJpegInfo.imageHeight);
		for (currComponent = 0; currComponent < JpegInfo.NumberOfComponents; currComponent++) {
			MinBlockWidth = Math.min(MinBlockWidth,
					mJpegInfo.BlockWidth[currComponent]);
			MinBlockHeight = Math.min(MinBlockHeight,
					mJpegInfo.BlockHeight[currComponent]);
		}

		float dctArray1[][] = new float[8][8];
		double dctArray2[][] = new double[8][8];
		int dctArray3[] = new int[8 * 8];
		int lastDCvalue[] = new int[JpegInfo.NumberOfComponents];

		// Iterate the grid of blocks
		for (int blockRow = 0; blockRow < MinBlockHeight; blockRow++) {
			for (int blockCol = 0; blockCol < MinBlockWidth; blockCol++) {
				int xpos = blockCol * 8;
				int ypos = blockRow * 8;

				// Iterate over all components
				for (currComponent = 0; currComponent < JpegInfo.NumberOfComponents; currComponent++) {

					float[][] componentArray = (float[][]) mJpegInfo.Components[currComponent];

					for (int a = 0; a < 8; a++)
						for (int b = 0; b < 8; b++)
							dctArray1[a][b] = componentArray[ypos + a][xpos + b];

					dctArray2 = mDCT.forwardDCT(dctArray1);
					dctArray3 = mDCT.quantizeBlock(dctArray2,
							mJpegInfo.quantizeTableNumbers[currComponent]);
					mHuffman.HuffmanBlockEncoder(outStream, dctArray3,
							lastDCvalue[currComponent],
							mJpegInfo.DCtableNumber[currComponent],
							mJpegInfo.ACtableNumber[currComponent]);
					lastDCvalue[currComponent] = dctArray3[0];
				}
			}
		}
		mHuffman.flushBuffer(outStream);
	}

	private void writeEOI(BufferedOutputStream out) {
		byte[] EOI = { (byte) 0xFF, (byte) 0xD9 };
		writeArray(EOI, out);
	}

	private void writeHeaders(BufferedOutputStream out) {
		int i, j, index, offset, length;
		int tempArray[];

		// the SOI marker
		byte[] SOI = { (byte) 0xFF, (byte) 0xD8 };
		writeArray(SOI, out);

		// The order of the following headers is quiet inconsequential.
		// the JFIF header
		byte JFIF[] = new byte[18];
		JFIF[0] = (byte) 0xff;
		JFIF[1] = (byte) 0xe0;
		JFIF[2] = (byte) 0x00;
		JFIF[3] = (byte) 0x10;
		JFIF[4] = (byte) 0x4a;
		JFIF[5] = (byte) 0x46;
		JFIF[6] = (byte) 0x49;
		JFIF[7] = (byte) 0x46;
		JFIF[8] = (byte) 0x00;
		JFIF[9] = (byte) 0x01;
		JFIF[10] = (byte) 0x00;
		JFIF[11] = (byte) 0x00;
		JFIF[12] = (byte) 0x00;
		JFIF[13] = (byte) 0x01;
		JFIF[14] = (byte) 0x00;
		JFIF[15] = (byte) 0x01;
		JFIF[16] = (byte) 0x00;
		JFIF[17] = (byte) 0x00;
		writeArray(JFIF, out);

		// comment Header
		String comment = "";
		comment = mJpegInfo.getComment();
		length = comment.length();
		byte COM[] = new byte[length + 4];
		COM[0] = (byte) 0xFF;
		COM[1] = (byte) 0xFE;
		COM[2] = (byte) ((length >> 8) & 0xFF);
		COM[3] = (byte) (length & 0xFF);
		java.lang.System.arraycopy(mJpegInfo.comment.getBytes(), 0, COM, 4,
				mJpegInfo.comment.length());
		writeArray(COM, out);

		// The DQT header
		// 0 is the luminance index and 1 is the chrominance index
		byte DQT[] = new byte[134];
		DQT[0] = (byte) 0xFF;
		DQT[1] = (byte) 0xDB;
		DQT[2] = (byte) 0x00;
		DQT[3] = (byte) 0x84;
		offset = 4;
		for (i = 0; i < 2; i++) {
			DQT[offset++] = (byte) ((0 << 4) + i);
			tempArray = (int[]) mDCT.quantum[i];
			for (j = 0; j < 64; j++) {
				DQT[offset++] = (byte) tempArray[jpegNaturalOrder[j]];
			}
		}
		writeArray(DQT, out);

		// Start of Frame Header
		byte SOF[] = new byte[19];
		SOF[0] = (byte) 0xFF;
		SOF[1] = (byte) 0xC0;
		SOF[2] = (byte) 0x00;
		SOF[3] = (byte) 17;
		SOF[4] = (byte) JpegInfo.Precision;
		SOF[5] = (byte) ((mJpegInfo.imageHeight >> 8) & 0xFF);
		SOF[6] = (byte) ((mJpegInfo.imageHeight) & 0xFF);
		SOF[7] = (byte) ((mJpegInfo.imageWidth >> 8) & 0xFF);
		SOF[8] = (byte) ((mJpegInfo.imageWidth) & 0xFF);
		SOF[9] = (byte) JpegInfo.NumberOfComponents;
		index = 10;
		for (i = 0; i < SOF[9]; i++) {
			SOF[index++] = (byte) mJpegInfo.CompID[i];
			SOF[index++] = (byte) ((mJpegInfo.horizSampleFactor[i] << 4) + mJpegInfo.vertSampleFactor[i]);
			SOF[index++] = (byte) mJpegInfo.quantizeTableNumbers[i];
		}
		writeArray(SOF, out);

		// The DHT (Define Huffman Table) Header
		byte DHT1[], DHT2[], DHT3[], DHT4[];
		int bytes, temp, oldindex, intermediateindex;
		length = 2;
		index = 4;
		oldindex = 4;
		DHT1 = new byte[17];
		DHT4 = new byte[4];
		DHT4[0] = (byte) 0xFF;
		DHT4[1] = (byte) 0xC4;
		for (i = 0; i < 4; i++) {
			bytes = 0;
			DHT1[index++ - oldindex] = (byte) ((int[]) mHuffman.bits
					.elementAt(i))[0];
			for (j = 1; j < 17; j++) {
				temp = ((int[]) mHuffman.bits.elementAt(i))[j];
				DHT1[index++ - oldindex] = (byte) temp;
				bytes += temp;
			}
			intermediateindex = index;
			DHT2 = new byte[bytes];
			for (j = 0; j < bytes; j++) {
				DHT2[index++ - intermediateindex] = (byte) ((int[]) mHuffman.val
						.elementAt(i))[j];
			}
			DHT3 = new byte[index];
			java.lang.System.arraycopy(DHT4, 0, DHT3, 0, oldindex);
			java.lang.System.arraycopy(DHT1, 0, DHT3, oldindex, 17);
			java.lang.System.arraycopy(DHT2, 0, DHT3, oldindex + 17, bytes);
			DHT4 = DHT3;
			oldindex = index;
		}
		DHT4[2] = (byte) (((index - 2) >> 8) & 0xFF);
		DHT4[3] = (byte) ((index - 2) & 0xFF);
		writeArray(DHT4, out);

		// Start of Scan Header
		byte SOS[] = new byte[14];
		SOS[0] = (byte) 0xFF;
		SOS[1] = (byte) 0xDA;
		SOS[2] = (byte) 0x00;
		SOS[3] = (byte) 12;
		SOS[4] = (byte) JpegInfo.NumberOfComponents;
		index = 5;
		for (i = 0; i < SOS[4]; i++) {
			SOS[index++] = (byte) mJpegInfo.CompID[i];
			SOS[index++] = (byte) ((mJpegInfo.DCtableNumber[i] << 4) + mJpegInfo.ACtableNumber[i]);
		}
		SOS[index++] = (byte) mJpegInfo.Ss;
		SOS[index++] = (byte) mJpegInfo.Se;
		SOS[index++] = (byte) ((mJpegInfo.Ah << 4) + mJpegInfo.Al);
		writeArray(SOS, out);

	}

	private static void writeArray(byte[] data, BufferedOutputStream out) {
		try {
			out.write(data);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public Timings timings = new Timings();

	public class Timings {
		long buildingJpegInfo;
		long buildingDCT;
		long buildingHuffman;
		long writingAll;
		long writingHeaders;
		long writingCompressedData;
		long writingEOI;
		long jpegInfoColorConversion;
	}
}