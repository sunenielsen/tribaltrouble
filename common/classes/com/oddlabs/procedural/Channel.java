package com.oddlabs.procedural;

import com.oddlabs.util.*;
import java.util.*;
import java.util.zip.*;
import java.nio.*;

public final strictfp class Channel {
	public float[][] pixels;
	public int width;
	public int height;
	public boolean powerof2;

	public Channel(int width, int height) {
		pixels = new float[height][width];
		this.width = width;
		this.height = height;
		this.powerof2 = Utils.isPowerOf2(width) && Utils.isPowerOf2(height);
	}

	public final Layer toLayer() {
		return new Layer(this.copy(), this.copy(), this.copy());
	}

	public final long getChecksum() {
		byte[] bytes = new byte[4];
		ByteBuffer bytebuffer = ByteBuffer.wrap(bytes);
		Checksum checksum = new CRC32();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				bytebuffer.putFloat(0, getPixel(x, y));
				checksum.update(bytes, 0, bytes.length);
			}
		}
		return checksum.getValue();
	}

	public final int getWidth() {
		return width;
	}

	public final int getHeight() {
		return height;
	}

	public final void putPixel(int x, int y, float value) {
		pixels[y][x] = value;
	}

	public final float getPixel(int x, int y) {
		return pixels[y][x];
	}

	public final float[][] getPixels() {
		return pixels;
	}

	public final void putPixelWrap(int x, int y, float value) {
		if (this.powerof2) {
			if (x < 0 || x >= width) x = (width + x) & (width - 1);
			if (y < 0 || y >= height) y = (height + y) & (height - 1);
			pixels[y][x] = value;
		} else {
			if (x < 0 || x >= width || y < 0 || y >= height) {
				pixels[(y + height) % height][(x + width) % width] = value;
			} else {
				pixels[y][x] = value;
			}
		}
	}

	public final float getPixelWrap(int x, int y) {
		if (this.powerof2) {
			if (x < 0 || x >= width) x = (width + x) & (width - 1);
			if (y < 0 || y >= height) y = (height + y) & (height - 1);
			return pixels[y][x];
		} else {
			if (x < 0 || x >= width || y < 0 || y >= height) {
				return pixels[(y + height) % height][(x + width) % width];
			} else {
				return pixels[y][x];
			}
		}
	}

	public final float getPixelSafe(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return 0f;
		} else {
			return pixels[y][x];
		}
	}

	public final void putPixelSafe(int x, int y, float value) {
		if (x >= 0 && x < width && y >= 0 && y < height) {
			pixels[y][x] = value;
		}
	}

	public final void putPixelClip(int x, int y, float value) {
		if (value < 0) pixels[y][x] = 0;
		else if (value > 1) pixels[y][x] = 1;
		else pixels[y][x] = value;
	}

	public final Channel fill(float value) {
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				pixels[y][x] = value;
		return this;
	}

	public final Channel fill(float value, float min, float max) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (pixels[y][x] >= min && pixels[y][x] <= max) {
					pixels[y][x] = value;
				}
			}
		}
		return this;
	}

	public final float findMin() {
		float min = Float.MAX_VALUE;
		float val = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				val = pixels[y][x];
				if (val < min) min = val;
			}
		}
		return min;
	}

	public final float findMax() {
		float max = Float.MIN_VALUE;
		float val = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				val = pixels[y][x];
				if (val > max) max = val;
			}
		}
		return max;
	}

	public final float[] findMinMax() {
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		float val1 = 0;
		float val2 = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x+=2) {
				val1 = pixels[y][x];
				val2 = pixels[y][x+1];
				if (val1 <= val2) {
					if (val1 < min) min = val1;
					if (val2 > max) max = val2;
				} else {
					if (val2 < min) min = val2;
					if (val1 > max) max = val1;
				}
			}
		}
		return new float[]{min, max};
	}

	public final float sum() {
		float sum = 0f;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				sum+= pixels[y][x];
			}
		}
		return sum;
	}

	public final Channel copy() {
		Channel channel = new Channel(width, height);
		float[][] new_pixels = channel.getPixels();
		for (int y = 0; y < height; y++) {
			System.arraycopy(pixels[y], 0, new_pixels[y], 0, width);
		}
		return channel;
	}

	public final Channel dynamicRange() {
		float[] minmax = findMinMax();
		float min = minmax[0];
		float max = minmax[1];
		float factor = 1f/(max - min);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, factor*(pixels[y][x] - min));
			}
		}
		return this;
	}

	public final Channel dynamicRange(float new_min, float new_max) {
		float min = findMin();
		float max = findMax();
		float inv_maxmin = 1f/(max - min);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, Tools.interpolateLinear(new_min, new_max, (pixels[y][x] - min)*inv_maxmin));
			}
		}
		return this;
	}

	public final Channel dynamicRange(float min, float max, float new_min, float new_max) {
		float val;
		float inv_maxmin = 1f/(max - min);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				val = getPixel(x, y);
				if (val >= min && val <= max) {
					putPixel(x, y, Tools.interpolateLinear(new_min, new_max, (pixels[y][x] - min)*inv_maxmin));
				} else {
					if (val < min) {
						putPixel(x, y, new_min);
					} else {
						putPixel(x, y, new_max);
					}
				}
			}
		}
		return this;
	}

	public final Channel dynamicRangeSymmetric() {
		float[] minmax = findMinMax();
		float min = minmax[0];
		float max = minmax[1];
		if (min > (1 - max)) {
			min = 1f - max;
		}
		if (max < (1 - min)) {
			max = 1f - min;
		}
		float factor = 1f/(max - min);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, factor*(pixels[y][x] - min));
			}
		}
		return this;
	}

	public final Channel clip() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixelClip(x, y, getPixel(x, y));
			}
		}
		return this;
	}

	public final Channel crop(int x_lo, int y_lo, int x_hi, int y_hi) {
		int new_width = x_hi - x_lo + 1;
		int new_height = y_hi - y_lo + 1;
		Channel channel = new Channel(new_width, new_height);
		float[][] new_pixels = channel.getPixels();
		for (int y = y_lo; y <= y_hi; y++) {
			System.arraycopy(pixels[y], x_lo, new_pixels[y - y_lo], 0, new_width);
		}
		return channel;
	}

	public final Channel cropWrap(int x_lo, int y_lo, int x_hi, int y_hi) {
		int new_width = x_hi - x_lo + 1;
		int new_height = y_hi - y_lo + 1;
		Channel channel = new Channel(new_width, new_height);
		for (int y = 0; y < new_height; y++) {
			int y_old = y + y_lo;
			for (int x = 0; x < new_width; x++) {
				int x_old = x + x_lo;
				if (x_old < 0 || x_old >= width || y_old < 0 || y_old >= height) {
					channel.putPixel(x, y, getPixelWrap(x_old, y_old));
				} else {
					channel.putPixel(x, y, getPixel(x_old, y_old));
				}
			}
		}
		return channel;
	}

	public final Channel tile(int new_width, int new_height) {
		Channel channel = new Channel(new_width, new_height);
		for (int y = 0; y < new_height; y++) {
			for (int x = 0; x < new_width; x++) {
				if (x < width && y < height) {
					channel.putPixel(x, y, getPixel(x, y));
				} else {
					channel.putPixel(x, y, getPixelWrap(x, y));
				}
			}
		}
		pixels = channel.getPixels();
		width = new_width;
		height = new_height;
		return this;
	}

	public final Channel tileDouble() {
		Channel channel = new Channel(width<<1, height<<1);
		float[][] new_pixels = channel.getPixels();
		for (int y = 0; y < height; y++) {
			System.arraycopy(pixels[y], 0, new_pixels[y], 0, width);
			System.arraycopy(pixels[y], 0, new_pixels[y], width, width);
			System.arraycopy(pixels[y], 0, new_pixels[y + height], 0, width);
			System.arraycopy(pixels[y], 0, new_pixels[y + height], width, width);
		}
		pixels = channel.getPixels();
		width = width<<1;
		height = height<<1;
		return this;
	}

	public final Channel[] quadSplit() {
		assert Utils.isPowerOf2(width) && Utils.isPowerOf2(height) : "only power of 2 sized channels";
		Channel channel1 = this.copy().crop(0, 0, (width>>1) - 1, (height>>1) - 1);
		Channel channel2 = this.copy().crop(width>>1, 0, width - 1, (height>>1) - 1);
		Channel channel3 = this.copy().crop(0, height>>1, (width>>1) - 1, height - 1);
		Channel channel4 = this.copy().crop(width>>1, height>>1, width - 1, height - 1);
		return new Channel[]{channel1, channel2, channel3, channel4};
	}

	public final Channel quadJoin(Channel channel1, Channel channel2, Channel channel3, Channel channel4) {
		assert channel1.width == channel2.width && channel2.width == channel3.width && channel3.width == channel4.width && channel1.height == channel2.height && channel2.height == channel3.height && channel3.height == channel4.height : "channels must be same size";
		assert width == channel1.width<<1 && height == channel1.height<<1 : "size mismatch";
		Channel channel = new Channel(channel1.width<<1, channel1.height<<1);
		channel.place(channel1, 0, 0);
		channel.place(channel2, channel1.width, 0);
		channel.place(channel3, 0, channel1.height);
		channel.place(channel4, channel1.width, channel1.height);
		pixels = channel.getPixels();
		return this;
	}

	public final Channel offset(int x_offset, int y_offset) {
		Channel channel = new Channel(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				channel.putPixel(x, y, getPixelWrap(x - x_offset, y - y_offset));
			}
		}
		pixels = channel.getPixels();
		return this;
	}

	public final Channel brightness(float brightness) {
		if (brightness > 1f) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					putPixelClip(x, y, brightness*getPixel(x, y));
				}
			}
		} else {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					putPixel(x, y, brightness*getPixel(x, y));
				}
			}
		}
		return this;
	}

	public final Channel multiply(float factor) {
		if (factor == 1)
			return this;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, factor*getPixel(x, y));
			}
		}
		return this;
	}

	public final Channel power(float exponent) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, (float)StrictMath.pow(getPixel(x, y), exponent));
			}
		}
		return this;
	}

	public final Channel power2() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float val = getPixel(x, y);
				putPixel(x, y, val*val);
			}
		}
		return this;
	}

	public final Channel log() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, (float)StrictMath.log(getPixel(x, y)));
			}
		}
		return this;
	}

	public final Channel add(float add) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, getPixel(x, y) + add);
			}
		}
		return this;
	}

	public final Channel addClip(float add) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixelClip(x, y, getPixel(x, y) + add);
			}
		}
		return this;
	}

	public final Channel contrast(float contrast) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixelClip(x, y, ((getPixel(x, y) - 0.5f)*contrast) + 0.5f);
			}
		}
		return this;
	}

	public final Channel gamma(float gamma) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, (float)StrictMath.pow(getPixel(x, y), 1/gamma));
			}
		}
		return this;
	}

	public final Channel gamma2() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float val = 1f - getPixel(x, y);
				putPixel(x, y, 1 - val*val);
			}
		}
		return this;
	}

	public final Channel gamma4() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float val = 1f - getPixel(x, y);
				val = val*val;
				putPixel(x, y, 1 - val*val);
			}
		}
		return this;
	}

	public final Channel gamma8() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float val = 1f - getPixel(x, y);
				val = val*val;
				val = val*val;
				putPixel(x, y, 1 - val*val);
			}
		}
		return this;
	}

	public final Channel gain(float gain) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (getPixel(x, y) < 0.5f)
					putPixel(x, y, (float)(StrictMath.pow(2 * getPixel(x, y), StrictMath.log(1 - gain)/StrictMath.log(0.5d))/2f));
				else
					putPixel(x, y, 1f - (float)(StrictMath.pow(2 - 2 * getPixel(x, y), StrictMath.log(1 - gain)/StrictMath.log(0.5d))/2f));
			}
		}
		return this;
	}

	public final Channel smoothGain() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, Tools.interpolateSmooth(0f, 1f, getPixel(x, y)));
			}
		}
		return this;
	}

	public final Channel invert() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, 1f - getPixel(x, y));
			}
		}
		return this;
	}

	public final Channel threshold(float start, float end) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float val = getPixel(x, y);
				if (val >= start && val <= end) {
					putPixel(x, y, 1f);
				} else {
					putPixel(x, y, 0f);
				}
			}
		}
		return this;
	}

	public final Channel scale(int new_width, int new_height) {
		return this.scaleLinear(new_width, new_height);
	}

	public final Channel scaleHalf() {
		int new_width = width/2;
		int new_height = height/2;
		Channel channel = new Channel(new_width, new_height);
		for (int y = 0; y < new_height; y++) {
			int y_coord_lo = y*2;
			int y_coord_hi = y_coord_lo + 1;
			for (int x = 0; x < new_width; x++) {
				int x_coord_lo = x*2;
				int x_coord_hi = x_coord_lo + 1;
				float val1 = Tools.interpolateLinear(getPixelWrap(x_coord_lo, y_coord_lo),
					getPixelWrap(x_coord_hi, y_coord_lo),
					.5f);
				float val2 = Tools.interpolateLinear(getPixelWrap(x_coord_lo, y_coord_hi),
					getPixelWrap(x_coord_hi, y_coord_hi),
					.5f);
				channel.putPixel(x, y, Tools.interpolateLinear(val1, val2, .5f));
			}
		}
		pixels = channel.getPixels();
		width = new_width;
		height = new_height;
		return this;
	}

	public final Channel scaleLinear(int new_width, int new_height) {
		if (width == new_width && height == new_height) {
			return this;
		}
		Channel channel = new Channel(new_width, new_height);
		float x_coord = 0;
		float y_coord = 0;
		float val1 = 0;
		float val2 = 0;
		float height_ratio = (float)height/new_height;
		float width_ratio = (float)width/new_width;
		for (int y = 0; y < new_height; y++) {
			y_coord = y*height_ratio - 0.5f;
			int y_coord_lo = (int)y_coord;
			int y_coord_hi = y_coord_lo + 1;
			for (int x = 0; x < new_width; x++) {
				x_coord = x*width_ratio - 0.5f;
				int x_coord_lo = (int)x_coord;
				int x_coord_hi = x_coord_lo + 1;
				float x_diff = x_coord - x_coord_lo;
				val1 = Tools.interpolateLinear(getPixelWrap(x_coord_lo, y_coord_lo),
					getPixelWrap(x_coord_hi, y_coord_lo),
					x_diff);
				val2 = Tools.interpolateLinear(getPixelWrap(x_coord_lo, y_coord_hi),
					getPixelWrap(x_coord_hi, y_coord_hi),
					x_diff);
				channel.putPixel(x, y, Tools.interpolateLinear(val1, val2, y_coord - y_coord_lo));
			}
		}
		pixels = channel.getPixels();
		width = new_width;
		height = new_height;
		return this;
	}

	public final Channel scaleCubic(int new_width, int new_height) {
		if (width == new_width && height == new_height) {
			return this;
		}
		Channel channel = new Channel(new_width, new_height);
		float x_coord, y_coord;
		float val0, val1, val2, val3;
		float height_ratio = (float)height/new_height;
		float width_ratio = (float)width/new_width;
		float x_diff, y_diff;
		int x_coord_lo, x_coord_lolo, x_coord_hi, x_coord_hihi, y_coord_lo, y_coord_lolo, y_coord_hi, y_coord_hihi;
		for (int y = 0; y < new_height; y++) {
			y_coord = y*height_ratio - 0.5f;
			y_coord_lo = (int)y_coord;
			y_coord_lolo = y_coord_lo - 1;
			y_coord_hi = y_coord_lo + 1;
			y_coord_hihi = y_coord_hi + 1;
			y_diff = y_coord - y_coord_lo;
			for (int x = 0; x < new_width; x++) {
				x_coord = x*width_ratio - 0.5f;
				x_coord_lo = (int)x_coord;
				x_coord_lolo = x_coord_lo - 1;
				x_coord_hi = x_coord_lo + 1;
				x_coord_hihi = x_coord_hi + 1;
				x_diff = x_coord - x_coord_lo;
				val0 = Tools.interpolateCubic(
					getPixelWrap(x_coord_lolo, y_coord_lolo),
					getPixelWrap(x_coord_lolo, y_coord_lo),
					getPixelWrap(x_coord_lolo, y_coord_hi),
					getPixelWrap(x_coord_lolo, y_coord_hihi),
					y_diff);
				val1 = Tools.interpolateCubic(
					getPixelWrap(x_coord_lo, y_coord_lolo),
					getPixelWrap(x_coord_lo, y_coord_lo),
					getPixelWrap(x_coord_lo, y_coord_hi),
					getPixelWrap(x_coord_lo, y_coord_hihi),
					y_diff);
				val2 = Tools.interpolateCubic(
					getPixelWrap(x_coord_hi, y_coord_lolo),
					getPixelWrap(x_coord_hi, y_coord_lo),
					getPixelWrap(x_coord_hi, y_coord_hi),
					getPixelWrap(x_coord_hi, y_coord_hihi),
					y_diff);
				val3 = Tools.interpolateCubic(
					getPixelWrap(x_coord_hihi, y_coord_lolo),
					getPixelWrap(x_coord_hihi, y_coord_lo),
					getPixelWrap(x_coord_hihi, y_coord_hi),
					getPixelWrap(x_coord_hihi, y_coord_hihi),
					y_diff);
				channel.putPixel(x, y, Tools.interpolateCubic(val0, val1, val2, val3, x_diff));
			}
		}
		pixels = channel.getPixels();
		width = new_width;
		height = new_height;
		return this;
	}

	public final Channel scaleFast(int new_width, int new_height) {
		if (width == new_width && height == new_height) {
			return this;
		}
		Channel channel = new Channel(new_width, new_height);
		int x_coord = 0;
		int y_coord = 0;
		for (int y = 0; y < new_height; y++) {
			for (int x = 0; x < new_width; x++) {
				x_coord = x*width/new_width;
				y_coord = y*height/new_height;
				channel.putPixel(x, y, getPixel(x_coord, y_coord));
			}
		}
		pixels = channel.getPixels();
		width = new_width;
		height = new_height;
		return this;
	}

	public final Channel scaleDouble() {
		assert width == height : "square images only";

		// calculate filter
		Channel filter = new Channel(width<<1, height<<1);
		for (int y = 0; y < height; y++) {
			int y_shift = y<<1;
			for (int x = 0; x < width; x++) {
				int x_shift = x<<1;
				float value = 0.25f*getPixel(x, y);
				filter.putPixel(x_shift, y_shift, value);
				filter.putPixel(x_shift + 1, y_shift, value);
				filter.putPixel(x_shift, y_shift + 1, value);
				filter.putPixel(x_shift + 1, y_shift + 1, value);
			}
		}

		// draw image
		Channel channel = new Channel(width<<1, height<<1);
		for (int y = 1; y < (height<<1) - 1; y++) {
			for (int x = 1; x < (width<<1) - 1; x++) {
				channel.putPixel(x, y, filter.getPixel(x - 1, y) + filter.getPixel(x + 1, y) + filter.getPixel(x, y - 1) + filter.getPixel(x, y + 1));
			}
		}

		// fix edges
		int max = (width<<1) - 1;
		for (int i = 0; i < max; i++) {
			channel.putPixel(0, i, filter.getPixelWrap(-1, i) + filter.getPixelWrap(1, i) + filter.getPixelWrap(0, i - 1) + filter.getPixelWrap(0, i + 1));
			channel.putPixel(i, 0, filter.getPixelWrap(i, -1) + filter.getPixelWrap(i, 1) + filter.getPixelWrap(i - 1, 0) + filter.getPixelWrap(i + 1, 0));
			channel.putPixel(max, i, filter.getPixelWrap(max - 1, i) + filter.getPixelWrap(max + 1, i) + filter.getPixelWrap(max, i - 1) + filter.getPixelWrap(max, i + 1));
			channel.putPixel(i, max, filter.getPixelWrap(i, max - 1) + filter.getPixelWrap(i, max + 1) + filter.getPixelWrap(i - 1, max) + filter.getPixelWrap(i + 1, max));
		}
		pixels = channel.getPixels();
		width = width<<1;
		height = height<<1;
		return this;
	}

	public final Channel rotate(int degrees) {
		Channel channel = null;
		int tmp = width;
		switch (degrees) {
			case 90:
				channel = new Channel(height, width);
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						channel.putPixel(y, width - x - 1, getPixel(x, y));
					}
				}
				width = height;
				height = tmp;
				break;
			case 180:
				channel = new Channel(width, height);
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						channel.putPixel(width - x - 1, height - y - 1, getPixel(x, y));
				}
				}
				break;
			case 270:
				channel = new Channel(height, width);
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						channel.putPixel(height - y - 1, x, getPixel(x, y));
					}
				}
				width = height;
				height = tmp;
				break;
			default:
				assert false: "Rotation degrees not a multiple of 90";
		}
		pixels = channel.getPixels();
		return this;
	}

	public final Channel shear(float offset) {
		Channel channel = new Channel(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				channel.putPixel(x, y, getPixelWrap((int)(x + offset*width*((float)y/height)), y));
			}
		}
		pixels = channel.getPixels();
		return this;
	}

	public final Channel sine(int frequency) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, (float)StrictMath.sin(StrictMath.PI*2*frequency*getPixel(x, y)));
			}
		}
		return this;
	}

	public final Channel xsine(int frequency) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, (float)StrictMath.sin(2*StrictMath.PI*(((float)x/width)*frequency + getPixel(x, y))));
			}
		}
		return this.dynamicRange();
	}

	public final Channel perturb(Channel channel1, Channel channel2) {
		Channel channel = new Channel(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float x_coord = x + width*channel1.getPixel(x, y);
				int x_coord_lo = (int)x_coord;
				int x_coord_hi = x_coord_lo + 1;
				float x_frac = x_coord - x_coord_lo;
				float y_coord = y + height*channel2.getPixel(x, y);
				int y_coord_lo = (int)y_coord;
				int y_coord_hi = y_coord_lo + 1;
				float y_frac = y_coord - y_coord_lo;
				float val1 = Tools.interpolateLinear(getPixelWrap(x_coord_lo, y_coord_lo), getPixelWrap(x_coord_hi, y_coord_lo), x_frac);
				float val2 = Tools.interpolateLinear(getPixelWrap(x_coord_lo, y_coord_hi), getPixelWrap(x_coord_hi, y_coord_hi), x_frac);
				channel.putPixel(x, y, Tools.interpolateLinear(val1, val2, y_frac));
			}
		}
		pixels = channel.getPixels();
		return this;
	}

	public final Channel perturb(Channel perturb, float magnitude) {
		Channel channel = new Channel(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float x_coord = x + width*magnitude*(perturb.getPixel(x, y) - 0.5f);
				int x_coord_lo = (int)x_coord;
				int x_coord_hi = x_coord_lo + 1;
				float x_frac = x_coord - x_coord_lo;
				float y_coord = y + height*magnitude*(perturb.getPixel(y, x) - 0.5f);
				int y_coord_lo = (int)y_coord;
				int y_coord_hi = y_coord_lo + 1;
				float y_frac = y_coord - y_coord_lo;
				float val1 = Tools.interpolateLinear(getPixelWrap(x_coord_lo, y_coord_lo), getPixelWrap(x_coord_hi, y_coord_lo), x_frac);
				float val2 = Tools.interpolateLinear(getPixelWrap(x_coord_lo, y_coord_hi), getPixelWrap(x_coord_hi, y_coord_hi), x_frac);
				channel.putPixel(x, y, Tools.interpolateLinear(val1, val2, y_frac));
			}
		}
		pixels = channel.getPixels();
		return this;
	}

	public final Channel flipH() {
		Channel channel = new Channel(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				channel.putPixel(x, y, getPixel(width - x - 1, y));
			}
		}
		pixels = channel.getPixels();
		return this;
	}

	public final Channel flipV() {
		float[] tmp;
		for (int y = 0; y < height>>1; y++) {
			tmp = pixels[y];
			pixels[y] = pixels[height - y - 1];
			pixels[height - y - 1] = tmp;
		}
		return this;
	}

	public final Channel smoothFast() {
		Channel filter = new Channel(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				filter.putPixel(x, y, 0.25f*getPixel(x, y));
			}
		}
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, filter.getPixelWrap(x - 1, y) + filter.getPixelWrap(x + 1, y) + filter.getPixelWrap(x, y - 1) + filter.getPixelWrap(x, y + 1));
			}
		}
		return this;
	}

	public final Channel smooth(int radius) {
		radius = StrictMath.max(1, radius);
		Channel filter = new Channel(width, height);
		float factor = 1f/((2*radius + 1)*(2*radius + 1));
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				filter.putPixel(x, y, factor*getPixel(x, y));
			}
		}
		for (int x = radius; x < width - radius; x++) {
			int y = radius;
			float sum = 0f;
			for (int i = -radius; i < radius + 1; i++) {
				for (int j = -radius; j < radius + 1; j++) {
					sum += filter.getPixel(x + j, y + i);
				}
			}
			for (y++; y < height - radius; y++) {
				for (int j = -radius; j < radius + 1; j++) {
					sum -= filter.getPixel(x + j, y - radius - 1);
					sum += filter.getPixel(x + j, y + radius);
				}
				putPixel(x, y, sum);
			}
		}
		return this;
	}

	public final Channel smoothWrap(int radius) {
		radius = StrictMath.max(1, radius);
		Channel filter = new Channel(width, height);
		float factor = 1f/((2*radius + 1)*(2*radius + 1));
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				filter.putPixel(x, y, factor*getPixel(x, y));
			}
		}
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				float sum = 0f;
				for (int i = -radius; i < radius + 1; i++) {
					for (int j = -radius; j < radius + 1; j++) {
						sum += filter.getPixelWrap(x + j, y + i);
					}
				}
				putPixel(x, y, sum);
			}
		}
		return this;
	}

	public final Channel smooth(int radius, Channel mask) {
		radius = StrictMath.max(1, radius);
		Channel filter = new Channel(width, height);
		float factor = 1f/((2*radius + 1)*(2*radius + 1));
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				filter.putPixel(x, y, factor*getPixel(x, y));
			}
		}
		for (int x = radius; x < width - radius; x++) {
			int y = radius;
			float sum = 0f;
			for (int i = -radius; i < radius + 1; i++) {
				for (int j = -radius; j < radius + 1; j++) {
					sum += filter.getPixel(x + j, y + i);
				}
			}
			for (y++; y < height - radius; y++) {
				float alpha = mask.getPixel(x, y);
				if (alpha > 0) {
					for (int j = -radius; j < radius + 1; j++) {
						sum -= filter.getPixel(x + j, y - radius - 1);
						sum += filter.getPixel(x + j, y + radius);
					}
					putPixel(x, y, alpha*sum + (1f - alpha)*getPixel(x, y));
				}
			}
		}
		return this;
	}

	public final Channel sharpen(int radius) {
		radius = StrictMath.max(1, radius);
		Channel channel = new Channel(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float value = 0;
				for (int i = -radius; i <= radius; i++) {
					for (int j = -radius; j <= radius; j++) {
						if (i == 0 && j == 0) {
							value += (2*radius + 1)*(2*radius + 1)*getPixel(x, y);
						} else {
							value -= getPixelWrap(x + i, y + j);
						}
					}
				}
				channel.putPixel(x, y, value);
			}
		}
		pixels = channel.getPixels();
		return this;
	}

	public final Channel convolution(float[][] filter, float divisor, float offset) {
		int radius = (filter[0].length - 1)/2;
		Channel channel = new Channel(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float value = 0;
				for (int i = -radius; i <= radius; i++) {
					for (int j = -radius; j <= radius; j++) {
						value += filter[i + radius][j + radius] * getPixelWrap(x + i, y + j);
					}
				}
				value = value/divisor + offset;
				channel.putPixel(x, y, value);
			}
		}
		pixels = channel.getPixels();
		return this;
	}

	public final Channel floodfill(int init_x, int init_y, float value) {
		assert init_x < width && init_x >= 0 : "x coordinate outside image";
		assert init_y < height && init_y >= 0 : "y coordinate outside image";
		float oldval = getPixel(init_x, init_y);
		boolean[][] marked = new boolean[width][height];
		marked[init_x][init_y] = true;
		List list = new java.util.LinkedList();
		list.add(new int[]{init_x, init_y});

		while (list.size() > 0) {
			int[] coords = (int[])list.remove(0);
			int x = coords[0];
			int y = coords[1];
			putPixel(x, y, value);
			if (x > 0 && getPixel(x - 1, y) == oldval && !marked[x - 1][y]) {
				marked[x - 1][y] = true;
				list.add(new int[]{x - 1, y});
			}
			if (x < width - 1 && getPixel(x + 1, y) == oldval && !marked[x + 1][y]) {
				marked[x + 1][y] = true;
				list.add(new int[]{x + 1, y});
			}
			if (y > 0 && getPixel(x, y - 1) == oldval && !marked[x][y - 1]) {
				marked[x][y - 1] = true;
				list.add(new int[]{x, y - 1});
			}
			if (y < height - 1 && getPixel(x, y + 1) == oldval && !marked[x][y + 1]) {
				marked[x][y + 1] = true;
				list.add(new int[]{x, y + 1});
			}
		}
		return this;
	}

	public final Channel largestConnected(float value) {
		Channel tmp = this.copy();
		Channel fillmap = new Channel(width, height);
		int[] fillcoords = tmp.findFirst(value);
		int max_count = 0;
		while (fillcoords[0] != -1) { // while reachable pixels remain
			int count = 0;
			int init_x = fillcoords[0];
			int init_y = fillcoords[1];
			fillmap.fill(0f);
			// flood fill
			boolean[][] marked = new boolean[width][height];
			marked[init_x][init_y] = true;
			List list = new java.util.LinkedList();
			list.add(new int[]{init_x, init_y});
			while (list.size() > 0) {
				int[] coords = (int[])list.remove(0);
				int x = coords[0];
				int y = coords[1];
				tmp.putPixel(x, y, -1f);
				fillmap.putPixel(x, y, 1f);
				count++;
				if (x > 0 && tmp.getPixel(x - 1, y) == 1f && !marked[x - 1][y]) {
					marked[x - 1][y] = true;
					list.add(new int[]{x - 1, y});
				}
				if (x < width - 1 && tmp.getPixel(x + 1, y) == 1f && !marked[x + 1][y]) {
					marked[x + 1][y] = true;
					list.add(new int[]{x + 1, y});
				}
				if (y > 0 && tmp.getPixel(x, y - 1) == 1f && !marked[x][y - 1]) {
					marked[x][y - 1] = true;
					list.add(new int[]{x, y - 1});
				}
				if (y < height - 1 && tmp.getPixel(x, y + 1) == 1f && !marked[x][y + 1]) {
					marked[x][y + 1] = true;
					list.add(new int[]{x, y + 1});
				}
			}
			if (count > max_count) {
				pixels = fillmap.copy().pixels;
				max_count = count;
			}
			fillcoords = tmp.findFirst(value);
		}
		return this;
	}

	public final float averageConnected(float value) {
		Channel tmp = this.copy();
		int[] fillcoords = tmp.findFirst(value);
		int area_count = 0;
		int area_total = 0;
		while (fillcoords[0] != -1) { // while reachable pixels remain
			area_count++;
			int count = 0;
			int init_x = fillcoords[0];
			int init_y = fillcoords[1];
			// flood fill
			boolean[][] marked = new boolean[width][height];
			marked[init_x][init_y] = true;
			List list = new java.util.LinkedList();
			list.add(new int[]{init_x, init_y});
			while (list.size() > 0) {
				int[] coords = (int[])list.remove(0);
				int x = coords[0];
				int y = coords[1];
				tmp.putPixel(x, y, -1f);
				count++;
				if (x > 0 && tmp.getPixel(x - 1, y) == 1f && !marked[x - 1][y]) {
					marked[x - 1][y] = true;
					list.add(new int[]{x - 1, y});
				}
				if (x < width - 1 && tmp.getPixel(x + 1, y) == 1f && !marked[x + 1][y]) {
					marked[x + 1][y] = true;
					list.add(new int[]{x + 1, y});
				}
				if (y > 0 && tmp.getPixel(x, y - 1) == 1f && !marked[x][y - 1]) {
					marked[x][y - 1] = true;
					list.add(new int[]{x, y - 1});
				}
				if (y < height - 1 && tmp.getPixel(x, y + 1) == 1f && !marked[x][y + 1]) {
					marked[x][y + 1] = true;
					list.add(new int[]{x, y + 1});
				}
			}
			fillcoords = tmp.findFirst(value);
			area_total += count;
		}
		if (area_count == 0) {
			return -1f;
		} else {
			return area_total/area_count;
		}
	}
	
	public final Channel squareFit(float value, int size) {
		Channel channel = new Channel(width, height);
		boolean match;
		for (int y = 0; y <= height - size; y++) {
			for (int x = 0; x <= width - size; x++) {
				match = true;
				out:
				for (int i = 0; i < size; i++) {
					for (int j = 0; j < size; j++) {
						match = match && (getPixel(x + i, y + j) == value);
						if (!match) break out;
					}
				}
				if (match) {
					channel.putPixel(x, y, value);
				}
			}
		}
		pixels = channel.copy().pixels;
		return this;
	}
	
	public final Channel boxFit(float value, int width, int height) {
		Channel channel = new Channel(this.width, this.height);
		boolean match;
		for (int y = 0; y <= this.height - height; y++) {
			for (int x = 0; x <= this.width - width; x++) {
				match = true;
				out:
				for (int i = 0; i < width; i++) {
					for (int j = 0; j < height; j++) {
						match = match && (getPixel(x + i, y + j) == value);
						if (!match) break out;
					}
				}
				if (match) {
					channel.putPixel(x, y, value);
				}
			}
		}
		pixels = channel.copy().pixels;
		return this;
	}

	public final int count(float value) {
		int count = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (getPixel(x, y) == value)
					count++;
			}
		}
		return count;
	}

	public final Channel grow(float value, int radius) {
		Channel channel = this.copy();
		for (int y = radius; y < height - radius; y++) {
			for (int x = radius; x < width - radius; x++) {
				if (getPixel(x, y) == value) {
					for (int i = -radius; i <= radius; i++) {
						for (int j = -radius; j <= radius; j++) {
							channel.putPixel(x + i, y + j, value);
						}
					}
				}
			}
		}
		pixels = channel.getPixels();
		return this;
	}
	
	public final Channel squareGrow(float value, int size) {
		Channel channel = this.copy();
		for (int y = 0; y <= height - size; y++) {
			for (int x = 0; x <= width - size; x++) {
				if (getPixel(x, y) == value) {
					for (int i = 0; i < size; i++) {
						for (int j = 0; j < size; j++) {
							channel.putPixel(x + i, y + j, value);
						}
					}
				}
			}
		}
		pixels = channel.getPixels();
		return this;
	}

	public final int[] find(int radius, int x_start, int y_start, float value) {
		if (getPixel(x_start, y_start) == value)
			return new int[]{x_start, y_start};
		int r = 1;
		while (r <= radius) {
			int x = x_start - r;
			int x2 = x_start + r;
			for (int i = 0; i < 2*r - 1; i++) {
				int y_i = y_start - r + 1 + i;
				if (getPixelWrap(x, y_i) == value)
					return new int[]{(x + width)%width, (y_i + height)%height};
				if (getPixelWrap(x2, y_i) == value)
					return new int[]{(x2 + width)%width, (y_i + height)%height};
			}
			int y = y_start - r;
			int y2 = y_start + r;
			for (int i = 0; i < 2*r + 1; i++) {
				int x_i = x_start - r + i;
				if (getPixelWrap(x_i, y) == value)
					return new int[]{(x_i + width)%width, (y + height)%height};
				if (getPixelWrap(x_i, y2) == value)
					return new int[]{(x_i + width)%width, (y2 + height)%height};
			}
			r++;
		}
		return new int[]{-1, -1};
	}
	
	public final int[] findNoWrap(int radius, int x_start, int y_start, float value) {
		if (getPixel(x_start, y_start) == value)
			return new int[]{x_start, y_start};
		int r = 1;
		while (r <= radius) {
			int x = x_start - r;
			int x2 = x_start + r;
			for (int i = 0; i < 2*r - 1; i++) {
				int y_i = y_start - r + 1 + i;
				if (x >= 0 && x < width && y_i >= 0 && y_i < height && getPixel(x, y_i) == value)
					return new int[]{x, y_i};
				if (x2 >= 0 && x2 < width && y_i >= 0 && y_i < height && getPixel(x2, y_i) == value)
					return new int[]{x2, y_i};
			}
			int y = y_start - r;
			int y2 = y_start + r;
			for (int i = 0; i < 2*r + 1; i++) {
				int x_i = x_start - r + i;
				if (x_i >= 0 && x_i < width && y >= 0 && y < height && getPixel(x_i, y) == value)
					return new int[]{x_i, y};
				if (x_i >= 0 && x_i < width && y2 >= 0 && y2 < height && getPixel(x_i, y2) == value)
					return new int[]{x_i, y2};
			}
			r++;
		}
		return new int[]{-1, -1};
	}

	public final int[] findFirst(float value) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (getPixel(x, y) == value)
					return new int[]{x, y};
			}
		}
		return new int[]{-1, -1};
	}

	public final Channel bump(Channel bumpmap, float lx, float ly, float shadow, float light, float ambient) {
		assert bumpmap.getWidth() == width && bumpmap.getHeight() == height: "bumpmap does not match channel size";
		Channel channel = new Channel(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float nx = bumpmap.getPixelWrap(x + 1, y) - bumpmap.getPixelWrap(x - 1, y);
				float ny = bumpmap.getPixelWrap(x, y + 1) - bumpmap.getPixelWrap(x, y - 1);
				float brightness = nx*lx + ny*ly;
				if (brightness >= 0) {
					channel.putPixelClip(x, y, (getPixel(x, y) + brightness*light)*(bumpmap.getPixel(x, y)*shadow + 1 - shadow));
				} else {
					channel.putPixelClip(x, y, (getPixel(x, y) + brightness*(1 - ambient))*(bumpmap.getPixel(x, y)*shadow + 1 - shadow));
				}
			}
		}
		pixels = channel.getPixels();
		return this;
	}
	
	public final Channel bumpSpecular(Channel bumpmap, float lx, float ly, float lz, float shadow, float light, int specular) {
		assert bumpmap.getWidth() == width && bumpmap.getHeight() == height: "bumpmap size does not match layer size";
		float lnorm = (float)StrictMath.sqrt(lx*lx + ly*ly + lz*lz);
		float nz = 4*(1f/StrictMath.min(width, height));
		float nzlz = nz*lz;
		float nz2 = nz*nz;
		int power = 2<<specular;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float nx = bumpmap.getPixelWrap(x + 1, y) - bumpmap.getPixelWrap(x - 1, y);
				float ny = bumpmap.getPixelWrap(x, y + 1) - bumpmap.getPixelWrap(x, y - 1);
				float brightness = nx*lx + ny*ly;
				float costheta = (brightness + nzlz)/((float)Math.sqrt(nx*nx + ny*ny + nz2)*lnorm); // can use math here, not game state affecting
				float highlight;
				if (costheta > 0) {
					highlight = (float)Math.pow(costheta, power); // can use math here, not game state affecting
				} else {
					highlight = 0;
				}
				putPixelClip(x, y, (getPixel(x, y) + highlight*light)*(bumpmap.getPixel(x, y)*shadow + 1 - shadow));
			}
		}
		return this;
	}

	public final Channel lineart() {
		Channel channel = new Channel(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				channel.putPixel(x, y, StrictMath.max(
					StrictMath.max(
						StrictMath.abs(getPixelWrap(x, y) - getPixelWrap(x - 1, y)),
						StrictMath.abs(getPixelWrap(x, y) - getPixelWrap(x + 1, y))),
					StrictMath.max(
						StrictMath.abs(getPixelWrap(x, y) - getPixelWrap(x, y - 1)),
						StrictMath.abs(getPixelWrap(x, y) - getPixelWrap(x, y + 1)))
				));
			}
		}
		pixels = channel.getPixels();
		return this;
	}

	public final Channel relativeIntensity(int radius) {
		radius = StrictMath.max(1, radius);
		Channel relint = new Channel(width, height);
		float factor = 1f/((2*radius + 1)*(2*radius + 1));
		float sum, avr;

		for (int x = 0; x < width; x++) {
			int y = 0;
			sum = 0f;
			for (int i = -radius; i < radius + 1; i++) {
				for (int j = -radius; j < radius + 1; j++) {
					sum += getPixelWrap(x + j, y + i);
				}
			}
			for (; y < height; y++) {
				if (y > 0) {
					for (int j = -radius; j < radius + 1; j++) {
						sum -= getPixelWrap(x + j, y - radius - 1);
						sum += getPixelWrap(x + j, y + radius);
					}
				}
				avr = sum*factor;
				relint.putPixel(x, y, getPixel(x, y) - avr);
			}
		}
		return relint.add(0.5f);
	}

	public final Channel relativeIntensityNormalized(int radius) {
		return this.relativeIntensity(radius).dynamicRangeSymmetric();
	}

	public final Channel[] fft() {
		assert width == height : "square images only";
		int size = width;
		assert Utils.isPowerOf2(size) : "size must be power of 2";

		// convert channel to complex number array
		float[] a = new float[size*size*2 + 1];
		int n = 1;
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				a[n] = getPixel(x, y);
				n += 2;
			}
		}

		// perform fast fourier transform
		fastFourierTransform(a, size, 1);

		// convert complex number array to channels
		n = 1;
		Channel magnitude = new Channel(size, size);
		Channel phase = new Channel(size, size);
		float real, imag;
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				real = a[n++];
				imag = a[n++];
				magnitude.putPixel(x, y, (float)Math.sqrt(real*real + imag*imag));
				if (imag == 0 && real >= 0) {
					phase.putPixel(x, y, (float)Math.PI/2f);
				} else if (imag == 0 && real < 0) {
					phase.putPixel(x, y, (float)Math.PI/-2f);
				} else {
					phase.putPixel(x, y, (float)Math.atan(real/imag));
				}
			}
		}

		// return magnitude and phase channels
		return new Channel[]{magnitude.offset(size>>1, size>>1), phase};
	}

	public final Channel fftInv(Channel magni, Channel phase) {
		assert magni.width == magni.height && phase.width == phase.height && magni.width == phase.width : "both images must be square and same size";
		int size = magni.width;
		assert Utils.isPowerOf2(size) : "size must be power of 2";

		// convert channels to complex number array
		Channel magnitude = magni.copy().offset(size>>1, size>>1);
		float mag, pha;
		float[] a = new float[size*size*2 + 1];
		int n = 1;
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				mag = magnitude.getPixel(x, y);
				pha = phase.getPixel(x, y);
				a[n++] = mag*(float)Math.cos(pha);
				a[n++] = mag*(float)Math.sin(pha);
			}
		}

		// perform fast fourier transform
		fastFourierTransform(a, size, -1);

		// convert complex number array to channel
		n = 1;
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				putPixel(x, y, a[n]);
				n += 2;
			}
		}

		// return real component channel
		return this;
	}

	private void fastFourierTransform(float[] data, int size, int isign) {
		int i1, i2, i3;
		int i2rev, i3rev, ip1, ip2, ip3, ifp1, ifp2;
		int ibit, idim, k1, k2, n, nprev, nrem, ntot;
		float tempi, tempr;
		float theta, wi, wpi, wpr, wr, wtemp;
		ntot = size*size;
		nprev = 1;
		for (idim = 2; idim >= 1; idim--) {
			n = size;
			nrem = ntot / (n * nprev);
			ip1 = nprev << 1;
			ip2 = ip1 * n;
			ip3 = ip2 * nrem;
			i2rev = 1;
			for (i2 = 1; i2 <= ip2; i2 += ip1) {
				if (i2 < i2rev) {
					for (i1 = i2; i1 <= i2 + ip1 - 2; i1 += 2) {
						for (i3 = i1; i3 <= ip3; i3 += ip2) {
							i3rev = i2rev + i3 - i2;
							tempr=data[i3]; data[i3] = (data[i3rev]); data[i3rev] = tempr;
							tempr=data[i3 + 1]; data[i3 + 1] = data[i3rev + 1]; data[i3rev + 1] = tempr;
						}
					}
				}
				ibit = ip2 >> 1;
				while (ibit >= ip1 && i2rev > ibit) {
					i2rev -= ibit;
					ibit >>= 1;
				}
				i2rev += ibit;
			}
			ifp1 = ip1;
			while (ifp1 < ip2) {
				ifp2 = ifp1 << 1;
				theta = isign * ((float)Math.PI * 2) / (ifp2 / ip1);
				wtemp = (float)Math.sin(0.5 * theta);
				wpr = -2.0f * wtemp * wtemp;
				wpi = (float)Math.sin(theta);
				wr = 1.0f;
				wi = 0.0f;
				for (i3 = 1; i3 <= ifp1; i3 += ip1) {
					for (i1 = i3; i1 <= i3 + ip1 - 2; i1 += 2) {
						for (i2 = i1; i2 <= ip3; i2 += ifp2) {
							k1 = i2;
							k2 = k1 + ifp1;
							tempr = wr * data[k2] - wi * data[k2 + 1];
							tempi = wr * data[k2 + 1] + wi * data[k2];
							data[k2] = data[k1] - tempr;
							data[k2 + 1] = data[k1 + 1] - tempi;
							data[k1] += tempr;
							data[k1 + 1] += tempi;
						}
					}
					wr = (wtemp = wr) * wpr - wi * wpi + wr;
					wi = wi * wpr + wtemp * wpi + wi;
				}
				ifp1 = ifp2;
			}
			nprev *= n;
		}
	}

	public final Channel erode(float talus, int iterations) {
		float h, h1, h2, h3, h4, d1, d2, d3, d4, max_d;
		int i, j;
		for (int iter = 0; iter < iterations; iter++) {
			for (int y = 1; y < height - 1; y++) {
				for (int x = 1; x < width - 1; x++) {
					h = getPixel(x, y);
					h1 = getPixel(x, y + 1);
					h2 = getPixel(x - 1, y);
					h3 = getPixel(x + 1, y);
					h4 = getPixel(x, y - 1);
					d1 = h - h1;
					d2 = h - h2;
					d3 = h - h3;
					d4 = h - h4;
					i = 0;
					j = 0;
					max_d = 0f;
					if (d1 > max_d) {
						max_d = d1;
						j = 1;
					}
					if (d2 > max_d) {
						max_d = d2;
						i = -1;
						j = 0;
					}
					if (d3 > max_d) {
						max_d = d3;
						i = 1;
						j = 0;
					}
					if (d4 > max_d) {
						max_d = d4;
						i = 0;
						j = -1;
					}
					if (max_d > talus) {
						continue;
					}
					max_d *= 0.5f;
					putPixel(x, y, getPixel(x, y) - max_d);
					putPixel(x + i, y + j, getPixel(x + i, y + j) + max_d);
				}
			}
		}
		return this;
	}
	
	public final Channel erodeThermal(float talus, int iterations) {
		float h, h1, h2, h3, h4, d1, d2, d3, d4, max_d;
		int i, j;
		for (int iter = 0; iter < iterations; iter++) {
			for (int y = 1; y < height - 1; y++) {
				for (int x = 1; x < width - 1; x++) {
					h = getPixel(x, y);
					h1 = getPixel(x, y + 1);
					h2 = getPixel(x - 1, y);
					h3 = getPixel(x + 1, y);
					h4 = getPixel(x, y - 1);
					d1 = h - h1;
					d2 = h - h2;
					d3 = h - h3;
					d4 = h - h4;
					i = 0;
					j = 0;
					max_d = 0f;
					if (d1 > max_d) {
						max_d = d1;
						j = 1;
					}
					if (d2 > max_d) {
						max_d = d2;
						i = -1;
						j = 0;
					}
					if (d3 > max_d) {
						max_d = d3;
						i = 1;
						j = 0;
					}
					if (d4 > max_d) {
						max_d = d4;
						i = 0;
						j = -1;
					}
					if (max_d < talus) {
						continue;
					}
					max_d *= 0.5f;
					putPixel(x, y, getPixel(x, y) - max_d);
					putPixel(x + i, y + j, getPixel(x + i, y + j) + max_d);
				}
			}
		}
		return this;
	}

	public final Channel place(Channel sprite, int x_offset, int y_offset) {
		for (int y = y_offset; y < y_offset + sprite.getHeight(); y++) {
			for (int x = x_offset; x < x_offset + sprite.getWidth(); x++) {
				putPixelWrap(x, y, sprite.getPixelWrap(x - x_offset, y - y_offset));
			}
		}
		return this;
	}

	public final Channel place(Channel sprite, Channel alpha, int x_offset, int y_offset) {
		float alpha_val;
		for (int y = y_offset; y < y_offset + sprite.getHeight(); y++) {
			for (int x = x_offset; x < x_offset + sprite.getWidth(); x++) {
				alpha_val = alpha.getPixel(x - x_offset, y - y_offset);
				putPixelWrap(x, y, alpha_val*sprite.getPixelWrap(x - x_offset, y - y_offset) + (1 - alpha_val)*getPixelWrap(x, y));
			}
		}
		return this;
	}

	public final Channel placeBrightest(Channel sprite, int x_offset, int y_offset) {
		for (int y = y_offset; y < y_offset + sprite.getHeight(); y++) {
			for (int x = x_offset; x < x_offset + sprite.getWidth(); x++) {
				putPixelWrap(x, y, StrictMath.max(getPixelWrap(x, y), sprite.getPixelWrap(x - x_offset, y - y_offset)));
			}
		}
		return this;
	}

	public final Channel placeDarkest(Channel sprite, int x_offset, int y_offset) {
		for (int y = y_offset; y < y_offset + sprite.getHeight(); y++) {
			for (int x = x_offset; x < x_offset + sprite.getWidth(); x++) {
				putPixelWrap(x, y, StrictMath.min(getPixelWrap(x, y), sprite.getPixelWrap(x - x_offset, y - y_offset)));
			}
		}
		return this;
	}

	public final Channel abs() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, 2f*StrictMath.abs(getPixel(x, y) - 0.5f));
			}
		}
		return this;
	}


	public final Channel channelBlend(Channel channel, float alpha) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, alpha*channel.getPixel(x, y) + (1 - alpha)*getPixel(x, y));
			}
		}
		return this;
	}

	public final Channel channelBlend(Channel channel, Channel alpha) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float alpha_val = alpha.getPixel(x, y);
				putPixel(x, y, alpha_val*channel.getPixel(x, y) + (1 - alpha_val)*getPixel(x, y));
			}
		}
		return this;
	}

	public final Channel channelAdd(Channel channel) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixelClip(x, y, getPixel(x, y) + channel.getPixel(x, y));
			}
		}
		return this;
	}

	public final Channel channelAddNoClip(Channel channel) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, getPixel(x, y) + channel.getPixel(x, y));
			}
		}
		return this;
	}

	public final Channel channelSubtract(Channel channel) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixelClip(x, y, getPixel(x, y) - channel.getPixel(x, y));
			}
		}
		return this;
	}

	public final Channel channelSubtractNoClip(Channel channel) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, getPixel(x, y) - channel.getPixel(x, y));
			}
		}
		return this;
	}

	public final Channel channelAverage(Channel channel) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, (getPixel(x, y) + channel.getPixel(x, y))/2f);
			}
		}
		return this;
	}

	public final Channel channelMultiply(Channel channel) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, getPixel(x, y) * channel.getPixel(x, y));
			}
		}
		return this;
	}

	public final Channel channelDivide(Channel channel) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, getPixel(x, y) / channel.getPixel(x, y));
			}
		}
		return this;
	}

	public final Channel channelDifference(Channel channel) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, StrictMath.abs(getPixel(x, y) - channel.getPixel(x, y)));
			}
		}
		return this;
	}

	public final Channel channelDarkest(Channel channel) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, StrictMath.min(getPixel(x, y), channel.getPixel(x, y)));
			}
		}
		return this;
	}

	public final Channel channelBrightest(Channel channel) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				putPixel(x, y, StrictMath.max(getPixel(x, y), channel.getPixel(x, y)));
			}
		}
		return this;
	}

}
