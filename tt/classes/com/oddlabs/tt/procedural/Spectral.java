package com.oddlabs.tt.procedural;

import java.util.Random;

import com.oddlabs.procedural.Channel;
import com.oddlabs.procedural.Layer;
import com.oddlabs.procedural.Tools;
import com.oddlabs.util.Utils;

public final strictfp class Spectral {

	// interpolation methods
	public static final int LINEAR = 1;
	public static final int SMOOTH = 2;
	public static final int CUBIC = 3;
	public static final int AUTO = 4;

	private Random random;
	public Channel channel;
	public Channel[] noise_channels;

	public Spectral(int size, int base_frequency, int octaves, float persistence, long seed, int interpolation) {
		assert Utils.isPowerOf2(size) : "size must be power of 2";
		assert Utils.isPowerOf2(base_frequency) : "base_frequency must be power of 2";
		generateOctaves(base_frequency, octaves, seed);
		mergeOctaves(size, persistence, octaves, interpolation);
		channel.dynamicRange();
	}

	// generate noise octaves
	private final void generateOctaves(int base_frequency, int octaves, long seed) {
		base_frequency = StrictMath.max(2, base_frequency);
		noise_channels = new Channel[octaves];
		random = new Random(seed);
		for (int i = 0; i < octaves; i++) {
			int size_noise = base_frequency*(1 << i);
			noise_channels[i] = new Channel(size_noise, size_noise);
			for (int y = 0; y < size_noise; y++) {
				for (int x = 0; x < size_noise; x++) {
					noise_channels[i].putPixel(x, y, random.nextFloat());
				}
			}
		}
	}

	// interpolate and sum octave channels
	private final void mergeOctaves(int size, float persistence, int octaves, int interpolation) {
		channel = new Channel(size, size);
		int method_threshold = 0;
		if (interpolation == SMOOTH) {
			method_threshold = noise_channels.length;
		} else if (interpolation == AUTO) {
			while (size>>3 > (int)Math.pow(2, method_threshold))
				method_threshold++;
		}

		for (int i = 0; i < octaves; i++) {
			Channel octave = noise_channels[i];
			float amplitude = (float)StrictMath.pow(persistence,i);
			float size_ratio = (float)octave.width/size;
			int block_size = size/octave.width;
			int blocks = octave.width;
			int y_block_lo, y_block_hi, x_block_lo, x_block_hi, x_pixel, y_pixel;
			float val, val1, val2;

			if (size == octave.width) { // no interpolation needed when octave matches image size
				for (y_pixel = 0; y_pixel < size; y_pixel++) {
					for (x_pixel = 0; x_pixel < size; x_pixel++) {
						channel.putPixel(x_pixel, y_pixel, channel.getPixel(x_pixel, y_pixel) + octave.getPixel(x_pixel, y_pixel)*amplitude);
					}
				}
			} else if (interpolation != CUBIC && i >= method_threshold) { // interpolate linear
				float y_incr1, y_incr2, x_incr;
				for (y_block_lo = 0; y_block_lo < blocks; y_block_lo++) {
					y_block_hi = (y_block_lo + 1) % octave.width;
					for (x_block_lo = 0; x_block_lo < blocks; x_block_lo++) {
						x_block_hi = (x_block_lo + 1) % octave.width;
						y_incr1 = (octave.getPixel(x_block_lo, y_block_hi) - octave.getPixel(x_block_lo, y_block_lo)) / block_size;
						y_incr2 = (octave.getPixel(x_block_hi, y_block_hi) - octave.getPixel(x_block_hi, y_block_lo)) / block_size;
						val1 = octave.getPixel(x_block_lo, y_block_lo) - 0.5f*y_incr1;
						val2 = octave.getPixel(x_block_hi, y_block_lo) - 0.5f*y_incr2;
						for (int y = 0; y < block_size; y++) {
							y_pixel = y + y_block_lo*block_size;
							val1 += y_incr1;
							val2 += y_incr2;
							x_incr = (val2 - val1) / block_size;
							val = val1 - 0.5f*x_incr;
							for (int x = 0; x < block_size; x++) {
								x_pixel = x + x_block_lo*block_size;
								val += x_incr;
								channel.putPixel(x_pixel, y_pixel, channel.getPixel(x_pixel, y_pixel) + val*amplitude);
							}
						}
					}
				}
			} else if (interpolation != CUBIC) { // interpolate smooth
				float y_coord, x_coord, y_diff, x_diff;
				for (y_block_lo = 0; y_block_lo < blocks; y_block_lo++) {
					y_block_hi = (y_block_lo + 1) % octave.width;
					for (int y = 0; y < block_size; y++) {
						y_pixel = y + y_block_lo*block_size;
						y_coord = y_pixel*size_ratio;
						y_diff = y_coord - y_block_lo;
						for (x_block_lo = 0; x_block_lo < blocks; x_block_lo++) {
							x_block_hi = (x_block_lo + 1) % octave.width;
							val1 = Tools.interpolateSmooth(octave.getPixel(x_block_lo, y_block_lo), octave.getPixel(x_block_lo, y_block_hi), y_diff);
							val2 = Tools.interpolateSmooth(octave.getPixel(x_block_hi, y_block_lo), octave.getPixel(x_block_hi, y_block_hi), y_diff);
							for (int x = 0; x < block_size; x++) {
								x_pixel = x + x_block_lo*block_size;
								x_coord = x_pixel*size_ratio;
								x_diff = x_coord - x_block_lo;
								val = Tools.interpolateSmooth(val1, val2, x_diff);
								channel.putPixel(x_pixel, y_pixel, channel.getPixel(x_pixel, y_pixel) + val*amplitude);
							}
						}
					}
				}
			} else { // interpolate cubic
				float y_coord, x_coord, y_diff, x_diff, val0, val3;
				int y_block_lolo, y_block_hihi, x_block_lolo, x_block_hihi;
				for (y_block_lo = 0; y_block_lo < blocks; y_block_lo++) {
					y_block_hi = y_block_lo + 1;
					y_block_lolo = y_block_lo - 1;
					y_block_hihi = y_block_hi + 1;
					for (int y = 0; y < block_size; y++) {
						y_pixel = y + y_block_lo*block_size;
						y_coord = y_pixel*size_ratio;
						y_diff = y_coord - y_block_lo;
						for (x_block_lo = 0; x_block_lo < blocks; x_block_lo++) {
							x_block_hi = x_block_lo + 1;
							x_block_lolo = x_block_lo - 1;
							x_block_hihi = x_block_hi + 1;
							val0 = Tools.interpolateCubic(
								octave.getPixelWrap(x_block_lolo, y_block_lolo),
								octave.getPixelWrap(x_block_lolo, y_block_lo),
								octave.getPixelWrap(x_block_lolo, y_block_hi),
								octave.getPixelWrap(x_block_lolo, y_block_hihi),
								y_diff);
							val1 = Tools.interpolateCubic(
								octave.getPixelWrap(x_block_lo, y_block_lolo),
								octave.getPixelWrap(x_block_lo, y_block_lo),
								octave.getPixelWrap(x_block_lo, y_block_hi),
								octave.getPixelWrap(x_block_lo, y_block_hihi),
								y_diff);
							val2 = Tools.interpolateCubic(
								octave.getPixelWrap(x_block_hi, y_block_lolo),
								octave.getPixelWrap(x_block_hi, y_block_lo),
								octave.getPixelWrap(x_block_hi, y_block_hi),
								octave.getPixelWrap(x_block_hi, y_block_hihi),
								y_diff);
							val3 = Tools.interpolateCubic(
								octave.getPixelWrap(x_block_hihi, y_block_lolo),
								octave.getPixelWrap(x_block_hihi, y_block_lo),
								octave.getPixelWrap(x_block_hihi, y_block_hi),
								octave.getPixelWrap(x_block_hihi, y_block_hihi),
								y_diff);
							for (int x = 0; x < block_size; x++) {
								x_pixel = x + x_block_lo*block_size;
								x_coord = x_pixel*size_ratio;
								x_diff = x_coord - x_block_lo;
								val = Tools.interpolateCubic(val0, val1, val2, val3, x_diff);
								channel.putPixelWrap(x_pixel, y_pixel, channel.getPixelWrap(x_pixel, y_pixel) + val*amplitude);
							}
						}
					}
				}
			}
		}
	}

	public final Layer toLayer() {
		return new Layer(channel, channel.copy(), channel.copy());
	}

	public final Channel toChannel() {
		return channel;
	}

}
