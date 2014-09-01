package com.oddlabs.tt.util;

import java.io.OutputStream;
import java.io.IOException;

public final strictfp class LoggerOutputStream extends OutputStream {
	private final OutputStream[] streams;
	
	public LoggerOutputStream(OutputStream[] streams) {
		this.streams = streams;
	}

	public final void write(byte[] bytes, int offset, int length) throws IOException {
		for (int i = 0; i < streams.length; i++)
			streams[i].write(bytes, offset, length);
	}

	public final void write(int b) throws IOException {
		for (int i = 0; i < streams.length; i++)
			streams[i].write(b);
	}
}
