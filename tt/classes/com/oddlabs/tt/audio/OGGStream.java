package com.oddlabs.tt.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import com.jcraft.jogg.*;
import com.jcraft.jorbis.*;

import com.oddlabs.tt.resource.NativeResource;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.util.ByteBufferOutputStream;

public final strictfp class OGGStream {
	private final URL file;
	private int data_size = 8192;
	private final byte[] data_buffer = new byte[data_size];
	private byte[] sync_buffer;
	private final SyncState sync_state = new SyncState();
	private final StreamState stream_state = new StreamState();
	private final Page page = new Page();
	private final Packet packet = new Packet();
	private final Info info = new Info();
	private final Comment comment = new Comment();
	private final DspState dsp_state = new DspState();
	private final Block block = new Block(dsp_state);
	private final InputStream input;
	private final float[][][] outer_pcm = new float[1][][];

	private int[] indices;
	private boolean eos = false;

	public OGGStream(URL file) {
		this.file = file;
		sync_state.init();
		try {
			input = file.openStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		getHeaders();
		prepareStream();
	}

	private final void getHeaders() {
		readFromInput();

		if (sync_state.pageout(page) != 1) {
//			if (bytes < 4096)
//				break;
			throw new RuntimeException("Invalid ogg stream");
		}

		stream_state.init(page.serialno());
		info.init();
		comment.init();
		if (stream_state.pagein(page) < 0)
			throw new RuntimeException("Error reading fist page of ogg stream");

		if (stream_state.packetout(packet) != 1)
			throw new RuntimeException("Error reading initial header packet");

		if (info.synthesis_headerin(comment, packet) < 0)
			throw new RuntimeException("Invalid ogg stream");

		int i = 0;
		while (i < 2) {
			while (i < 2) {
				int result = sync_state.pageout(page);
				if (result == 0)
					break;

				if (result == 1) {
					stream_state.pagein(page);
					while (i < 2) {
						result = stream_state.packetout(packet);
						if (result == 0)
							break;
						if (result == -1)
							throw new RuntimeException("Corrupt header");

						info.synthesis_headerin(comment,  packet);
						i++;
					}
				}
			}
			int bytes = readFromInput();
			if (bytes == 0 && i < 2)
				throw new RuntimeException("Vorbis headers not found");

		}
	}

	public final int getChannels() {
		return info.channels;
	}

	public final int getRate() {
		return info.rate;
	}

	private final void prepareStream() {
		data_size = 4096/info.channels;
		dsp_state.synthesis_init(info);
		block.init(dsp_state);
		indices = new int[info.channels];
	}

	public final int read(ByteBufferOutputStream output) {
		int written = 0;
		while (!eos) {
			int result = sync_state.pageout(page);
			if (result == 0) {
				if (readFromInput() == 0) {
					return written;
				}
				continue;
				//break;
			}
			if (result == -1) {
				throw new RuntimeException("Corrupt bitstream in " + file.getFile());
			} else {
				stream_state.pagein(page);
				while (true) {
					result = stream_state.packetout(packet);

					if (result == 0) {
						break;
					} else if (result != -1) {
						int samples;
						if (block.synthesis(packet) == 0) {
							dsp_state.synthesis_blockin(block);
						}

						while ((samples = dsp_state.synthesis_pcmout(outer_pcm, indices)) > 0) {
							float[][] pcm = outer_pcm[0];
							int count = (samples < data_size ? samples : data_size);

							for (int i = 0; i < info.channels; i++) {
								int ptr = i*2;
								int mono = indices[i];
								for (int j = 0; j < count; j++) {
									int val = (int)(pcm[i][mono + j]*32767.0);
									if (val > 32767) {
										val = 32767;
									}
									if (val < -32768) {
										val = -32768;
									}
									if (val < 0)
										val = val | 0x8000;
									if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
										data_buffer[ptr] = (byte)(val);
										data_buffer[ptr + 1] = (byte)(val >>> 8);
									} else {
										data_buffer[ptr + 1] = (byte)(val);
										data_buffer[ptr] = (byte)(val >>> 8);
									}
									ptr += 2*info.channels;
								}
							}

							output.write(data_buffer, 0, 2*info.channels*count);
							written += 2*info.channels*count;
							dsp_state.synthesis_read(count);
						}
					}
				}
				if (page.eos() != 0) {
					eos = true;
					stream_state.clear();
					block.clear();
					dsp_state.clear();
					info.clear();
					sync_state.clear();
				}
			}
			break;
		}
		return written;
	}

	private final int readFromInput() {
		int index = sync_state.buffer(4096);
		int bytes;
		sync_buffer = sync_state.data;
		try {
			bytes = input.read(sync_buffer, index, sync_buffer.length - index);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (bytes == -1)
			return 0;
		sync_state.wrote(bytes);
		return bytes;
	}
}
