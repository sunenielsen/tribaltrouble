package com.oddlabs.util;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.net.URL;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTTextureCompressionS3TC;

public final class DXTImage {
	private final static int INITIAL_BUFFER_SIZE = 100000;
	private static byte[] scratch_buffer = new byte[INITIAL_BUFFER_SIZE];
	private final short width;
	private final short height;
	private final int internal_format;
	private final ByteBuffer mipmaps;

	private final static ByteBuffer convertToByteBuffer(int internal_format, int width, int height, byte[][] mipmaps) {
		int size = 0;
		int mipmap_width = width;
		int mipmap_height = height;
		for (int i = 0; i < mipmaps.length; i++) {
			size += mipmaps[i].length;
			assert mipmaps[i].length == getMipMapSize(internal_format, mipmap_width, mipmap_height);
			mipmap_width /= 2;
			mipmap_height /= 2;
		}
		ByteBuffer buffer = BufferUtils.createByteBuffer(size);
		for (int i = 0; i < mipmaps.length; i++)
			buffer.put(mipmaps[i]);
		buffer.flip();
		return buffer;
	}

	public DXTImage(short width, short height, int internal_format, byte[][] mipmaps) {
		this(width, height, internal_format, convertToByteBuffer(internal_format, width, height, mipmaps));
	}
	
	public DXTImage(short width, short height, int internal_format, ByteBuffer mipmaps) {
		this.width = width;
		this.height = height;
		this.internal_format = internal_format;
		this.mipmaps = mipmaps;
		position(0);
	}

	public final short getWidth() {
		return width;
	}

	public final short getHeight() {
		return height;
	}

	public final int getInternalFormat() {
		return internal_format;
	}

	public final ByteBuffer getMipMap() {
		return mipmaps;
	}

	private final static int getMipMapSize(int internal_format, int width, int height) {
		int blocksize = 16;
		if (internal_format == EXTTextureCompressionS3TC.GL_COMPRESSED_RGB_S3TC_DXT1_EXT)
			blocksize = 8;
		return ((width + 3)/4) * ((height + 3)/4) * blocksize;
	}

	public final int getNumMipMaps() {
		int size = mipmaps.capacity();
		int mipmap_width = width;
		int mipmap_height = height;
		int num_mipmaps = 0;
		while (size > 0) {
			int mipmap_size = getMipMapSize(internal_format, mipmap_width, mipmap_height);
			size -= mipmap_size;
			mipmap_width /= 2;
			mipmap_height /= 2;
			num_mipmaps++;
		}
		assert size == 0;
		return num_mipmaps;
	}

	public final int getWidth(int mipmap_index) {
		return width >>> mipmap_index;
	}

	public final int getHeight(int mipmap_index) {
		return height >>> mipmap_index;
	}

	public final void position(int mipmap_index) {
		mipmaps.clear();
		int mipmap_width = width;
		int mipmap_height = height;
		int position = 0;
		while (mipmap_index > 0) {
			position += getMipMapSize(internal_format, mipmap_width, mipmap_height);
			mipmap_width /= 2;
			mipmap_height /= 2;
			mipmap_index--;
		}
		mipmaps.position(position);
		position += getMipMapSize(internal_format, mipmap_width, mipmap_height);
		mipmaps.limit(position);
	}

	public static DXTImage read(URL url) throws IOException {
		InputStream in = new BufferedInputStream(url.openStream());
		int index = 0;
		int bytes_read;
		while ((bytes_read = in.read(scratch_buffer, index, scratch_buffer.length - index)) != -1) {
			index += bytes_read;
			if (index == scratch_buffer.length) {
				byte[] new_scratch_buffer = new byte[scratch_buffer.length*2];
				System.arraycopy(scratch_buffer, 0, new_scratch_buffer, 0, scratch_buffer.length);
				scratch_buffer = new_scratch_buffer;
			}
		}
		ByteBuffer header = ByteBuffer.wrap(scratch_buffer);
		short width = header.getShort();
		short height = header.getShort();
		int internal_format = header.getInt();
		int data_length = index - header.position();
		ByteBuffer buffer = BufferUtils.createByteBuffer(data_length);
		buffer.put(scratch_buffer, header.position(), data_length);
		buffer.flip();
		return new DXTImage(width, height, internal_format, buffer);
	}

	public void write(File file) throws IOException {
		WritableByteChannel out = new FileOutputStream(file).getChannel();
		ByteBuffer header = ByteBuffer.allocate(2 + 2 + 4);
		header.putShort(width).putShort(height).putInt(internal_format);
		header.flip();
		writeContents(out, header);
		int old_position = mipmaps.position();
		int old_limit = mipmaps.limit();
		mipmaps.clear();
		writeContents(out, mipmaps);
		mipmaps.position(old_position);
		mipmaps.limit(old_limit);
		out.close();
	}

	private static void writeContents(WritableByteChannel out, ByteBuffer data) throws IOException {
		while (data.hasRemaining())
			out.write(data);
	}
}
