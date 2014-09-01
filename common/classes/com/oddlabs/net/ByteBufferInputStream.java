package com.oddlabs.net;

import java.io.InputStream;
import java.nio.ByteBuffer;

public final strictfp class ByteBufferInputStream extends InputStream {
	private final ByteBuffer buffer;

	public ByteBufferInputStream(byte[] array) {
		buffer = ByteBuffer.wrap(array);
	}
	
	public final ByteBuffer buffer() {
		return buffer;
	}

	public final int available() {
		return buffer.remaining();
	}

	public final int read(byte[] bytes, int offset, int length) {
		if (available() == 0)
			return -1;
		length = StrictMath.min(length, available());
		buffer.get(bytes, offset, length);
		return length;
	}
	
	public final int read() {
		if (available() > 0) {
			int b = buffer.get();
			return b & 0xff;
		} else
			return -1;
	}
}
