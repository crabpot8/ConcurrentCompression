package edu.vt.ece5510.jpeg;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;

public class BufferedOutputStreamSink extends BufferedOutputStream {

	public BufferedOutputStreamSink() {
		this(new PipedOutputStream());
	}

	public BufferedOutputStreamSink(OutputStream out) {
		super(out);
	}
	
	@Override
	public void write(byte[] b) {	
	}
	
	@Override
	public void write(int b) {
	}

	@Override
	public void write(byte[] b, int offset, int len ) {
	}
	
	@Override
	public void flush() {
	}
	
	@Override
	public void close() {
	}

}
