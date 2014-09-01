package com.oddlabs.imageutil;

import java.awt.image.*;
import java.awt.Graphics2D;
import java.io.File;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.imageio.ImageIO;
import gr.zdimensions.jsquish.Squish;
import org.lwjgl.opengl.EXTTextureCompressionS3TC;
import org.lwjgl.opengl.GL11;

import com.oddlabs.util.Utils;
import com.oddlabs.util.Image;
import com.oddlabs.util.DXTImage;
import com.oddlabs.procedural.Channel;
import com.oddlabs.procedural.Layer;

import com.sixlegs.image.png.*;

public final class Convert {
	private static String current_ext;
	
	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.out.println("Usage: Convert <infile> <operations> <outfile>");
			System.exit(1);
		}
		File infile = new File(args[0]);
		File outfile = new File(args[args.length - 1]);
		List args_list = new ArrayList();
		for (int i = 1; i < args.length - 1; i++)
			args_list.add(args[i]);
		System.out.println("Converting " + infile);
		Layer[] images = new Layer[]{loadFile(infile)};
		images = processOperations(args_list.iterator(), images);
		if (outfile.exists()) {
			if (outfile.isDirectory()) {
				String infilename = infile.getName();
				int dot_index = infilename.lastIndexOf(".");
				outfile = new File(outfile, infilename.substring(0, dot_index));
			}
		} else {
			File parent = outfile.getParentFile();
			if (parent != null)
				parent.mkdirs();
		}
		if (current_ext != null && !outfile.getName().endsWith(current_ext)) {
			outfile = new File(outfile.getParentFile(), outfile.getName() + "." + current_ext);
		}
System.out.println("outfile = " + outfile);
		save(outfile, images);
	}

	private static Layer[] processOperations(Iterator args, Layer[] images) {
		while (args.hasNext()) {
			String op = (String)args.next();
			images = processOperation(op, args, images);
		}
		return images;
	}

	private static Layer[] processOperation(String op, Iterator args, Layer[] images) {
		if (op.equals("-mipmaps")) {
			if (images.length != 1)
				throw new IllegalArgumentException("Can only create mipmaps from one image, not " + images.length);
			List mipmaps = new ArrayList();
			Layer last_mipmap = images[0];
			int mip_width = last_mipmap.getWidth();
			int mip_height = last_mipmap.getHeight();
			mipmaps.add(last_mipmap);
			while (mip_width > 1 && mip_height > 1) {
				mip_width /=2;
				mip_height /= 2;
				Layer mipmap = last_mipmap.copy();
				mipmap.scaleHalf();
				mipmaps.add(mipmap);
				last_mipmap = mipmap;
			}
			images = (Layer[])mipmaps.toArray(new Layer[0]);
		} else if (op.equals("-half")) {
			for (int i = 0; i < images.length; i++)
				images[i].scaleHalf();
		} else if (op.equals("-format")) {
			current_ext = (String)args.next();
		} else if (op.equals("-flip")) {
			for (int i = 0; i < images.length; i++)
				images[i].flipV();
		} else if (op.equals("-gamma")) {
			String gamma_str = (String)args.next();
			float gamma = Float.parseFloat(gamma_str);
			for (int i = 0; i < images.length; i++)
				images[i].gamma(gamma);
		} else
			throw new IllegalArgumentException("Unknown operation: " + op);
		return images;
	}

	private static Layer loadFile(File file) throws IOException {
		PngImage image = new PngImage(file.getPath());
		int width = image.getWidth();
		int height = image.getHeight();
//		int channels = image.getRaster().getNumBands() <= 3 ? 3 : 4;
		assert image.getColorType() == PngImage.COLOR_TYPE_RGB || image.getColorType() == PngImage.COLOR_TYPE_RGB_ALPHA;
		int channels = image.getColorType() == PngImage.COLOR_TYPE_RGB ? 3 : 4;
//		final byte[] bytes = getImageData(image);
		int[] ints = new int[width*height];
		try {
			boolean success = new PixelGrabber(image, 0, 0, width, height, ints, 0, width).grabPixels();
			assert success;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		byte[] bytes = new byte[width*height*4];
		int index = 0;
		for (int i = 0; i < ints.length; i++) {
			byte a = (byte)((ints[i] >> 24) & 0xff);
			byte r = (byte)((ints[i] >> 16) & 0xff);
			byte g = (byte)((ints[i] >>  8) & 0xff);
			byte b = (byte)((ints[i]	  ) & 0xff);
			bytes[index++] = r;
			bytes[index++] = g;
			bytes[index++] = b;
			bytes[index++] = a;
		}
		Layer image_layer = new Layer(width, height);
		if (channels == 4)
			image_layer.a = new Channel(width, height);
		image_layer.loadFromBytes(bytes);
		return image_layer;
	}

	private static void saveImage(File file, Layer[] images) throws IOException {
		if (images.length != 1)
			throw new IllegalArgumentException("Can't save more than 1 image in .image format");
		byte[] bytes = images[0].convertToBytes();
		Image image = new Image(images[0].getWidth(), images[0].getHeight(), ByteBuffer.wrap(bytes));
		image.write(file);
	}
	
	private static void saveDxtn(File file, Layer[] images) throws IOException {
		int internal_format;
		Squish.CompressionType type;
		if (images[0].a == null) {
			internal_format = EXTTextureCompressionS3TC.GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
			type = Squish.CompressionType.DXT1;
		} else {
			internal_format = EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
			type = Squish.CompressionType.DXT5;
		}
		byte[][] mipmap_bytes = new byte[images.length][];
		for (int i = 0; i < mipmap_bytes.length; i++) {
//images[i].saveAsPNG(new File(file.getParentFile(), images[i].getWidth() + "x" + images[i].getHeight() + "-" + file.getName() + ".png"));
			byte[] mipmap = images[i].convertToBytes();
			mipmap_bytes[i] = Squish.compressImage(mipmap, images[i].getWidth(), images[i].getHeight(), null, type, Squish.CompressionMethod.CLUSTER_FIT);
/*System.out.println("Decompressing............");
			byte[] decompressed = Squish.decompressImage(null, images[i].getWidth(), images[i].getHeight(), mipmap_bytes[i], type);
System.out.println("Done");*/
		}
		new DXTImage((short)images[0].getWidth(),(short)images[0].getHeight(), internal_format, mipmap_bytes).write(file);
	}

	private static void save(File file, Layer[] images) throws IOException {
		if (file.getName().endsWith(".dxtn")) {
			saveDxtn(file, images);
		} else if (file.getName().endsWith(".image")) {
			saveImage(file, images);
		} else
			throw new IllegalArgumentException("unknown extension: " + file);
	}

/*	
		String filename = new File(infile).getName();
		filename = filename.substring(0, filename.lastIndexOf("."));
		System.out.println("Converting " + infile);
		BufferedImage image = ImageIO.read(new File(infile));
		int width = image.getWidth();
		int height = image.getHeight();
		int channels = image.getRaster().getNumBands() <= 3 ? 3 : 4;
		final byte[] bytes = getImageData(image);
		Utils.flip(bytes, width*4, height);
		Layer image_layer = new Layer(width, height);
		if (channels == 4)
			image_layer.a = new Channel(width, height);
		image_layer.loadFromBytes(bytes);
image_layer.saveAsPNG(filename + "." + width + "x" + height + "p");
		image_layer.gamma(Layer.INV_GAMMA_EXPONENT);
		List mipmaps = new ArrayList();
		final byte[] blocks = compressImage(channels, width, height, bytes, Squish.CompressionMethod.CLUSTER_FIT);
		mipmaps.add(blocks);
		int mip_width = width;
		int mip_height = height;
		Layer mipmap_layer = image_layer;
		while (mip_width > 1 && mip_height > 1) {
			mip_width /=2;
			mip_height /= 2;
			mipmap_layer.scaleHalf();
			Layer mipmap_layer_export = mipmap_layer.copy();
			mipmap_layer_export.gamma(Layer.GAMMA_EXPONENT);
			byte[] mipmap = mipmap_layer_export.convertToBytes();
mipmap_layer.saveAsPNG(filename + "." + mip_width + "x" + mip_height + "p");
			byte[] mipmap_blocks = compressImage(channels, mip_width, mip_height, mipmap, Squish.CompressionMethod.CLUSTER_FIT);
			mipmaps.add(mipmap_blocks);
		}
		String outfile = dstdir + File.separatorChar + filename + ".dxtn";
		int internal_format;
		if (channels == 3) {
			internal_format = EXTTextureCompressionS3TC.GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
		} else {
			internal_format = EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
		}
		new DXTImage((short)width,(short)height, internal_format, (byte[][])mipmaps.toArray(new byte[][]{})).write(new File(outfile));
	}
*/
	private static byte[] getImageData(BufferedImage image) {

		final int type = image.getType();

		if ( type != BufferedImage.TYPE_3BYTE_BGR && type != BufferedImage.TYPE_4BYTE_ABGR ) {
			// Bored to do it right, let Java2D do the conversion for us
			final BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getRaster().getNumBands() <= 3 ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_4BYTE_ABGR);
			final Graphics2D g2 = newImage.createGraphics();

			g2.drawImage(image, null, 0, 0);

			image = newImage;
		}

		final Raster raster = image.getRaster();
		byte[] data = ((DataBufferByte)raster.getDataBuffer()).getData();

		if ( raster.getNumBands() == 3 ) {
			final byte[] bytes = new byte[image.getWidth() * image.getHeight() * 4];

			for ( int i = 0, j = 0; i < data.length; ) {
				final byte b = data[i++];
				final byte g = data[i++];
				final byte r = data[i++];

				bytes[j++] = r;
				bytes[j++] = g;
				bytes[j++] = b;
				bytes[j++] = (byte)0xFF;
			}

			data = bytes;
		} else {
			for ( int i = 0; i < data.length; ) {
				final byte a = data[i + 0];
				final byte b = data[i + 1];
				final byte g = data[i + 2];
				final byte r = data[i + 3];

				data[i + 0] = r;
				data[i + 1] = g;
				data[i + 2] = b;
				data[i + 3] = a;

				i += 4;
			}
		}

		return data;
	}
}
