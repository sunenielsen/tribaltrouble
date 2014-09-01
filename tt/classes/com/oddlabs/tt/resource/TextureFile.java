package com.oddlabs.tt.resource;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.render.Texture;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.util.Image;
import com.oddlabs.util.DXTImage;
import com.oddlabs.util.Utils;

public final strictfp class TextureFile extends File {
	private final int internal_format;
	private final int min_filter;
	private final int mag_filter;
	private final int wrap_s;
	private final int wrap_t;
	private final int base_fadeout_level;
	private final int max_mipmap_level;
	private final float fadeout_factor;
	private final boolean max_alpha;
	private final boolean is_dxt;

	public TextureFile(String location) {
		this(location, Globals.COMPRESSED_RGBA_FORMAT);
	}

	public TextureFile(String location, int internal_format) {
		this(location, internal_format, GL11.GL_LINEAR_MIPMAP_NEAREST, GL11.GL_LINEAR, GL11.GL_REPEAT, GL11.GL_REPEAT);
	}

	public TextureFile(String location, int internal_format, int min_filter, int mag_filter, int wrap_s, int wrap_t) {
		this(location, internal_format, min_filter, mag_filter, wrap_s, wrap_t, Globals.NO_MIPMAP_CUTOFF, 10000, 1.0f);
	}

	public TextureFile(String location, int internal_format, int min_filter, int mag_filter, int wrap_s, int wrap_t, int max_mipmap_level, int base_fadeout_level, float fadeout_factor) {
		this(location, internal_format, min_filter, mag_filter, wrap_s, wrap_t, max_mipmap_level, base_fadeout_level, fadeout_factor, false);
	}
	
	public TextureFile(String location, int internal_format, int min_filter, int mag_filter, int wrap_s, int wrap_t, int max_mipmap_level, int base_fadeout_level, float fadeout_factor, boolean max_alpha) {
		super(locateTexture(location));
		this.is_dxt = locateDXT(location) != null;
		this.internal_format = internal_format;
		this.min_filter = min_filter;
		this.mag_filter = mag_filter;
		this.wrap_s = wrap_s;
		this.wrap_t = wrap_t;
		this.base_fadeout_level = base_fadeout_level;
		this.max_mipmap_level = max_mipmap_level;
		this.fadeout_factor = fadeout_factor;
		this.max_alpha = max_alpha;
	}

	private static URL locate(String location_with_ext) {
		URL url_classpath = Utils.class.getResource(location_with_ext);
		if (url_classpath != null)
			return url_classpath;
		try {
			java.io.File file =  new java.io.File(com.oddlabs.tt.util.Utils.getInstallDir(), location_with_ext);
			if (file.exists())
				return file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	private final static URL locateDXT(String location) {
		return locate(location + ".dxtn");
	}

	private final static URL locateTexture(String location) {
		URL url = locateDXT(location);
		if (url != null)
			return url;

/*		String location_jpg = location + ".jpg";
		URL url_jpg = Utils.class.getResource(location_jpg);
		if (url_jpg != null)
			return url_jpg;*/

		url = locate(location + ".image");
		if (url != null)
			return url;

		throw new RuntimeException(location);
	}

	private final BufferedImage readFile(URL url) {
		try {
			return ImageIO.read(url);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public final boolean isDXTImage() {
		return is_dxt;
	}

	public final DXTImage getDXTImage() {
		try {
			return DXTImage.read(getURL());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public final GLImage getImage() {
		GLImage img;
/*		URL url_jpg = Utils.class.getResource(loc + ".jpg");
		if (url_jpg != null) {
			BufferedImage bi_rgb = readFile(url_jpg);
			int components = bi_rgb.getColorModel().getNumComponents();
			assert components == 1 || components == 3;
			int width = bi_rgb.getWidth();
			int height = bi_rgb.getHeight();
			byte[] data_rgb = (byte[]) bi_rgb.getRaster().getDataElements(0, 0, width, height, null);
			
			URL url_png_alpha = Utils.class.getResource(loc + "_a.png");
			byte[] data_a;
			if (url_png_alpha != null) {
				BufferedImage bi_a = readFile(url_png_alpha);
				assert bi_a.getColorModel().getNumComponents() == 1;
				assert width == bi_a.getWidth() && height == bi_a.getHeight();
				data_a = (byte[]) bi_a.getRaster().getDataElements(0, 0, width, height, null);
			} else
				data_a = null;

			ByteBuffer buf = ByteBuffer.allocateDirect(4*width*height);
			IntBuffer int_buf = buf.asIntBuffer();
			for (int i = 0; i < int_buf.capacity(); i++) {
				int r = data_rgb[i*components] & 0xff;
				int g;
				int b;
				if (components == 3) {
					g = data_rgb[i*3 + 1] & 0xff;
					b = data_rgb[i*3 + 2] & 0xff;
				} else {
					g = r;
					b = r;
				}
				
				int a;
				if (data_a != null)
					a = data_a[i] & 0xff;
				else
					a = 0xff;
				int pixel = (r << 24) | (g << 16) | (b << 8) | a;
				int_buf.put(i, pixel);
			}
			buf.rewind();
			img = new GLIntImage(width, height, buf, GL11.GL_RGBA);
		} else {*/
			Image image = Image.read(getURL());
			img = new GLIntImage(image.getWidth(), image.getHeight(), image.getPixels(), GL11.GL_RGBA);
//		}
		return img;
	}
	
	public final Object newInstance() {
		return new Texture(this);
	}

	public final boolean equals(Object o) {
		if (!(o instanceof TextureFile))
			return false;
		TextureFile other = (TextureFile)o;
		if (internal_format != other.internal_format || min_filter != other.min_filter || mag_filter != other.mag_filter || 
			max_mipmap_level != other.max_mipmap_level ||
			wrap_s != other.wrap_s || wrap_t != other.wrap_t || base_fadeout_level != other.base_fadeout_level || fadeout_factor != other.fadeout_factor)
			return false;
		return super.equals(o);
	}

	public final int getInternalFormat() {
		return internal_format;
	}

	public final int getMinFilter() {
		return min_filter;
	}

	public final int getMagFilter() {
		return mag_filter;
	}

	public final int getWrapS() {
		return wrap_s;
	}

	public final int getWrapT() {
		return wrap_t;
	}

	public final int getBaseFadeoutLevel() {
		return base_fadeout_level;
	}

	public final int getMaxMipmapLevel() {
		return max_mipmap_level;
	}

	public final float getFadeoutFactor() {
		return fadeout_factor;
	}

	public final boolean hasMaxAlpha() {
		return max_alpha;
	}
}
