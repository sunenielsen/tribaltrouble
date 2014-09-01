package com.oddlabs.event;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

public final strictfp class SaveDeterministic extends Deterministic {
	private final static short MAX_DEFAULTS = Short.MAX_VALUE;
	
	private final ByteChannel channel;
	private final ByteBuffer buffer;

	private final ByteBufferOutputStream byte_buffer_output_stream = new ByteBufferOutputStream();

	private int total_bytes_written;
	private short num_defaults = MIN_DEFAULTS;

	public SaveDeterministic(File logging_file) {
		try {
			buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
			channel = new FileOutputStream(logging_file).getChannel();
			System.out.println("Logging to " + logging_file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public final boolean isPlayback() {
		return false;
	}

	private final void flushBuffer() {
		try {
			buffer.flip();
			while (buffer.hasRemaining()) {
				total_bytes_written += channel.write(buffer);
			}
			buffer.compact();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public final void endLog() {
		startLog(0, false);
		try {
			flushBuffer();
			channel.close();
			System.out.println("Closed log file, bytes written: " + total_bytes_written);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private final boolean startLog(int num_bytes, boolean def) {
		if (def && num_defaults < MAX_DEFAULTS) {
			num_defaults++;
			return false;
		}
		num_bytes += DEFAULTS_SIZE;
//		int stack_trace_hash = getTraceId();
//		num_bytes += 4;
		if (num_bytes > buffer.remaining())
			flushBuffer();
//		buffer.putInt(stack_trace_hash);
		buffer.putShort(num_defaults);
		num_defaults = MIN_DEFAULTS;
		return true;
	}

	protected final byte log(byte b, byte def) {
		if (startLog(1, b == def))
			buffer.put(b);
		return b;
	}

	protected final char log(char c, char def) {
		if (startLog(2, c == def))
			buffer.putChar(c);
		return c;
	}

	protected final int log(int i, int def) {
		if (startLog(4, i == def))
			buffer.putInt(i);
		return i;
	}

	protected final long log(long l, long def) {
		if (startLog(8, l == def))
			buffer.putLong(l);
		return l;
	}

	protected final float log(float f, float def) {
		if (startLog(4, f == def))
			buffer.putFloat(f);
		return f;
	}
	
	protected final Object logObject(Object o) {
		try {
			ObjectOutputStream object_output_stream = new ObjectOutputStream(byte_buffer_output_stream);
			object_output_stream.writeObject(o);
			object_output_stream.close();
			return o;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected final void logBuffer(ByteBuffer b) {
		if (startLog(0, false)) {
			while (true) {
				int saved_limit = b.limit();
				b.limit(saved_limit - Math.max(0, b.remaining() - buffer.remaining()));
				buffer.put(b);
				b.limit(saved_limit);
				if (!b.hasRemaining())
					break;
				flushBuffer();
			}
		}
	}

	public final strictfp class ByteBufferOutputStream extends OutputStream {
		public final void write(int b) throws IOException {
			log((byte)b);
		}
	}   
}
