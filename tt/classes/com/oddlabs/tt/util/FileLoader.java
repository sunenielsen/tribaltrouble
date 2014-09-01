package com.oddlabs.tt.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import com.oddlabs.tt.event.LocalEventQueue;

public final strictfp class FileLoader implements FileLoaderInterface {
	private final FileLoaderListener listener;
	private final ReadableByteChannel file_channel;
	private final ByteBuffer buffer;

	public FileLoader(File file, FileLoaderListener listener, int num_bytes) {
		this.buffer = ByteBuffer.allocate(num_bytes);
		this.listener = listener;
		IOException exception;
		ReadableByteChannel tmp_channel;
		try {
			tmp_channel = new FileInputStream(file).getChannel();
			exception = null;
		} catch (IOException e) {
			tmp_channel = null;
			exception = e;
		}
		this.file_channel = tmp_channel;
		if (LocalEventQueue.getQueue().getDeterministic().log(exception != null))
			error((IOException)LocalEventQueue.getQueue().getDeterministic().log(exception));
		else
			newFile(file, LocalEventQueue.getQueue().getDeterministic().log(file.length()));
	}

	public final void newFile(File file, long length) {
		listener.newFile(file, length);
	}
	
	public final void load() {
		if (LocalEventQueue.getQueue().getDeterministic().log(file_channel == null || !file_channel.isOpen()))
			return;
		buffer.clear();
		IOException exception;
		boolean eof;
		try {
			int num_bytes_read;
			do {
				num_bytes_read = file_channel.read(buffer);
			} while (num_bytes_read != -1 && buffer.hasRemaining());
			eof = num_bytes_read == -1;
			if (eof)
				file_channel.close();
			exception = null;
		} catch (IOException e) {
			exception = e;
			eof = true;
		}
		if (LocalEventQueue.getQueue().getDeterministic().log(exception != null))
			error((IOException)LocalEventQueue.getQueue().getDeterministic().log(exception));
		else
			data((byte[])LocalEventQueue.getQueue().getDeterministic().log(buffer.array()),
					LocalEventQueue.getQueue().getDeterministic().log(buffer.position()),
					LocalEventQueue.getQueue().getDeterministic().log(eof));
	}

	public final void data(byte[] data, int num_bytes_read, boolean eof) {
		listener.data(data, num_bytes_read, eof);
	}
	
	public final void error(IOException e) {
		close();
		listener.error(e);
	}

	public final void close() {
		if (file_channel != null) {
			try {
				file_channel.close();
			} catch (IOException e) {
				//ignore
			}
		}
	}
}
