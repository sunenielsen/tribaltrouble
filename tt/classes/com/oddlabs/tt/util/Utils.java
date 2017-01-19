package com.oddlabs.tt.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.lwjgl.BufferUtils;

import com.oddlabs.tt.global.Globals;

public final strictfp class Utils {
	private static final ByteBuffer sqrtByteBuf = BufferUtils.createByteBuffer(4);
	private static final IntBuffer sqrtIntBuf = sqrtByteBuf.asIntBuffer();
	private static final FloatBuffer sqrtFloatBuf = sqrtByteBuf.asFloatBuffer();

	private final static Object[] empty_object_array = new Object[0];

	public final static String getBundleString(ResourceBundle bundle, String key) {
		return getBundleString(bundle, key, empty_object_array);
	}
	
	public final static String getBundleString(ResourceBundle bundle, String key, Object[] object_array) {
		return MessageFormat.format(bundle.getString(key), object_array);
	}
 
	public final static File getInstallDir() {
		return new File(System.getProperty("user.dir"));
	}

	public final static FloatBuffer toBuffer(float[] floats) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(floats.length);
		buffer.put(floats);
		buffer.rewind();
		return buffer;
	}

	public final static ShortBuffer toBuffer(short[] shorts) {
		ShortBuffer buffer = BufferUtils.createShortBuffer(shorts.length);
		buffer.put(shorts);
		buffer.rewind();
		return buffer;
	}

	public final static IntBuffer toBuffer(int[] ints) {
		IntBuffer buffer = BufferUtils.createIntBuffer(ints.length);
		buffer.put(ints);
		buffer.rewind();
		return buffer;
	}

	public final static void saveAsBMP(String filename, ByteBuffer pixel_data, int width, int height) {
		long before = System.currentTimeMillis();
		int pad = 4 - (width*3)%4;
		if (pad == 4)
			pad = 0;
		int size = (width*3 + pad)*height + 54;
		ByteBuffer buffer = ByteBuffer.allocate(size);

		//write BMP header
		buffer.put((byte)0x42);							 // signature, must be 4D42 hex
		buffer.put((byte)0x4D);							 // ...
		buffer.put((byte)(size & 0x000000ff));		// size of BMP file in bytes
		buffer.put((byte)((size & 0x0000ff00)>>8));   // ...
		buffer.put((byte)((size & 0x00ff0000)>>16));  // ...
		buffer.put((byte)((size & 0xff000000)>>24));  // ...
		buffer.put((byte)0);								// reserved, must be zero
		buffer.put((byte)0);								// reserved, must be zero
		buffer.put((byte)0);								// reserved, must be zero
		buffer.put((byte)0);								// reserved, must be zero
		buffer.put((byte)54);							   // offset to start of image data in bytes
		buffer.put((byte)0);								// ...
		buffer.put((byte)0);								// ...
		buffer.put((byte)0);								// ...
		buffer.put((byte)40);							   // size of BITMAPINFOHEADER structure, must be 40
		buffer.put((byte)0);								// ...
		buffer.put((byte)0);								// ...
		buffer.put((byte)0);								// ...
		buffer.put((byte)(width & 0x000000ff));	   // image width in pixels
		buffer.put((byte)((width & 0x0000ff00)>>8));  // ...
		buffer.put((byte)((width & 0x00ff0000)>>16)); // ...
		buffer.put((byte)((width & 0xff000000)>>24)); // ...
		buffer.put((byte)(height & 0x000000ff));	  // image width in pixels
		buffer.put((byte)((height & 0x0000ff00)>>8)); // ...
		buffer.put((byte)((height & 0x00ff0000)>>16));// ...
		buffer.put((byte)((height & 0xff000000)>>24));// ...
		buffer.put((byte)1);								// number of planes in the image, must be 1
		buffer.put((byte)0);								// ...
		buffer.put((byte)24);		   // number of bits per pixel (1, 4, 8, or 24)
		buffer.put((byte)0);								// ...
		buffer.put((byte)0);								// compression type (0=none, 1=RLE-8, 2=RLE-4)
		buffer.put((byte)0);								// ...
		buffer.put((byte)0);								// ...
		buffer.put((byte)0);								// ...
		buffer.put((byte)((size - 54) & 0x000000ff));		// size of image data in bytes (including padding)
		buffer.put((byte)(((size - 54) & 0x0000ff00)>>8));   // ...
		buffer.put((byte)(((size - 54) & 0x00ff0000)>>16));  // ...
		buffer.put((byte)(((size - 54) & 0xff000000)>>24));  // ...
		buffer.put((byte)0);								// horizontal resolution in pixels per meter (unreliable)
		buffer.put((byte)0);								// ...
		buffer.put((byte)0);								// ...
		buffer.put((byte)0);								// ...
		buffer.put((byte)0);								// vertical resolution in pixels per meter (unreliable)
		buffer.put((byte)0);								// ...
		buffer.put((byte)0);								// ...
		buffer.put((byte)0);								// ...
		buffer.put((byte)0);								// number of colors in image, or zero
		buffer.put((byte)0);								// ...
		buffer.put((byte)0);								// ...
		buffer.put((byte)0);								// ...
		buffer.put((byte)0);								// number of important colors, or zero
		buffer.put((byte)0);								// ...
		buffer.put((byte)0);								// ...
		buffer.put((byte)0);								// ...

		pixel_data.rewind();
		IntBuffer int_pixel_data = pixel_data.asIntBuffer();
		//write BMP image data
		for (int y = height - 1; y >= 0; y--) {
			for (int x = 0; x < width; x++) {
				int pixel = int_pixel_data.get(y*width + x);
				byte r = (byte)((pixel >> 24) & 0xff);
				byte g = (byte)((pixel >> 16) & 0xff);
				byte b = (byte)((pixel >> 8) & 0xff);
				buffer.put(b);
				buffer.put(g);
				buffer.put(r);
			}
			for (int i = 0; i < pad; i++) {
				buffer.put((byte)0);
			}

		}
		buffer.rewind();
		File image_file = new File(filename);
		try {
			FileOutputStream fout = new FileOutputStream(image_file);
			fout.write(buffer.array());
			fout.flush();
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		long after = System.currentTimeMillis();
		System.out.println("File " + filename + " saved in " + (after - before) + " milliseconds");
	}

	public final static void saveAsTGA(String filename, ByteBuffer pixel_data, int width, int height) {
		long before = System.currentTimeMillis();
		try {
			FileOutputStream fout = new FileOutputStream(filename + ".tga");

			//write TGA header
			fout.write(0); //ID length, 0 because no image id field
			fout.write(0); //no color map
			fout.write(2); //image type (24 bit RGB, uncompressed)
			fout.write(0); //color map origin, ignore because no color map
			fout.write(0); //color map origin, ignore because no color map
			fout.write(0); //color map origin, ignore because no color map
			fout.write(0); //color map length, ignore because no color map
			fout.write(0); //color map entry size, ignore because no color map
			fout.write(0); //x origin
			fout.write(0); //x origin
			fout.write(0); //x origin
			fout.write(0); //y origin
			short s = (short)width;
			fout.write((byte)(s & 0x00ff));	  //image width low byte
			fout.write((byte)((s & 0xff00)>>8)); //image width high byte
			s = (short)height;
			fout.write((byte)(s & 0x00ff));	  //image height low byte
			fout.write((byte)((s & 0xff00)>>8)); //image height high byte
			fout.write(32); //bpp
			fout.write(0); //description bits

			pixel_data.rewind();
			//write TGA image data
			fout.getChannel().write(pixel_data);

			fout.flush();
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		long after = System.currentTimeMillis();
		System.out.println("File " + filename + " saved in " + (after - before) + " milliseconds");
	}

	public final static int numTextureSplits(int size) {
		return (size >> Globals.MAX_TEXTURE_POWER) + Globals.TEXTURE_SPLITS[size & (Globals.MAX_TEXTURE_SIZE - 1)];
	}

	public final static int toTextureSize(int size) {
		return (size & ~(Globals.MAX_TEXTURE_SIZE - 1)) + Globals.TEXTURE_SIZES[size & (Globals.MAX_TEXTURE_SIZE - 1)];
	}

	public final static int roundToTextureSize(int size) {
		assert size <= Globals.MAX_TEXTURE_SIZE;
		int tex_size = 1;
		while (tex_size < size)
			tex_size <<= 1;
		return tex_size;
	}

	public final static int bestTextureSize(int size) {
		if (size >= Globals.MAX_TEXTURE_SIZE)
			return Globals.MAX_TEXTURE_SIZE;
		return Globals.BEST_SIZES[size];
	}

	// int = 0xAARRGGBB A=alpha R=red G=green B=blue
	public final static float intToRed(int color) {
		int red = (color >> 16) & 0xff;
		return ((float)red)/255;
	}

	public final static float intToGreen(int color) {
		int green = (color >> 8) & 0xff;
		return ((float)green)/255;
	}

	public final static float intToBlue(int color) {
		int blue = color & 0xff;
		return ((float)blue)/255;
	}

	public final static float invsqrt(float x) {
		float xhalf = 0.5f * x;
		sqrtFloatBuf.put(0, x);
		int i = sqrtIntBuf.get(0);
		i = 0x5f375a86 - (i >> 1);
		sqrtIntBuf.put(0, i);
		x = sqrtFloatBuf.get(0);
		x *= (1.5f - xhalf * x * x); // This line may be duplicated for more accuracy.
		return x;
	}

	public final static void storeMatrixInArray(StrictMatrix4f m, float[][] a) {
		a[0][0] = m.m00;
		a[0][1] = m.m01;
		a[0][2] = m.m02;
		a[0][3] = m.m03;
		a[1][0] = m.m10;
		a[1][1] = m.m11;
		a[1][2] = m.m12;
		a[1][3] = m.m13;
		a[2][0] = m.m20;
		a[2][1] = m.m21;
		a[2][2] = m.m22;
		a[2][3] = m.m23;
		a[3][0] = m.m30;
		a[3][1] = m.m31;
		a[3][2] = m.m32;
		a[3][3] = m.m33;
	}
}
