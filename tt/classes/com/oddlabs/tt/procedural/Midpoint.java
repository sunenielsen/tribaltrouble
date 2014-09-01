package com.oddlabs.tt.procedural;

import java.util.Random;

import com.oddlabs.procedural.Channel;
import com.oddlabs.procedural.Layer;
import com.oddlabs.util.Utils;

public final strictfp class Midpoint {

	private Random random;
	public Channel channel;

	public Midpoint(int size, int base_freq, float pers, long seed) {
		assert Utils.isPowerOf2(size) : "size must be power of 2";
		int iterations = Utils.powerOf2Log2(size);
		base_freq = StrictMath.max(base_freq, 0);
		base_freq = StrictMath.min(base_freq, iterations);
		random = new Random(seed);
		channel = new Channel(size, size);

		if (base_freq > 0) {
			int block_size = size>>base_freq;
			for (int x_block = 0; x_block < (1<<base_freq); x_block++) {
				for (int y_block = 0; y_block < (1<<base_freq); y_block++) {
					int x = x_block*block_size;
					int y = y_block*block_size;
					channel.putPixel(x, y, random.nextFloat());
				}
			}
		}

		float v1, v2, v3, v4, v5, v6, v7, v8, v9;
		int x_block, y_block, x, y;

		for (int i = base_freq; i < iterations; i++) {
			int block_size = size>>i;
			int block_size_half = size>>(i + 1);
			float amp = (float)StrictMath.pow(pers, i - base_freq);
			float amp_half = 0.5f*amp;
			// calculate center midpoints
			if (i < 2) {
				for (x_block = 0, x = 0; x_block < (1<<i); x_block++) {
					for (y_block = 0, y = 0; y_block < (1<<i); y_block++) {
						v1 = channel.getPixel(x, y);
						v2 = channel.getPixel((x + block_size) % size, y);
						v3 = channel.getPixel(x, (y + block_size) % size);
						v4 = channel.getPixel((x + block_size) % size, (y + block_size) % size);
						v5 = 0.25f*(v1 + v2 + v3 + v4) + random.nextFloat()*amp - amp_half;
						channel.putPixel(x + block_size_half, y + block_size_half, v5);
						y+= block_size;
					}
					x+= block_size;
				}
			} else {
				// safe blocks
				for (x_block = 1, x = block_size; x_block < (1<<i) - 1; x_block++) {
					for (y_block = 1, y = block_size; y_block < (1<<i) - 1; y_block++) {
						v1 = channel.getPixel(x, y);
						v2 = channel.getPixel(x + block_size, y);
						v3 = channel.getPixel(x, y + block_size);
						v4 = channel.getPixel(x + block_size, y + block_size);
						v5 = 0.25f*(v1 + v2 + v3 + v4) + random.nextFloat()*amp - amp_half;
						channel.putPixel(x + block_size_half, y + block_size_half, v5);
						y+= block_size;
					}
					x+= block_size;
				}
				// left and right edge blocks
				for (x_block = 0; x_block < (1<<i); x_block+= (1<<i) - 1) {
					x = x_block*block_size;
					for (y_block = 0, y = 0; y_block < (1<<i); y_block++) {
						v1 = channel.getPixel(x, y);
						v2 = channel.getPixel((x + block_size) % size, y);
						v3 = channel.getPixel(x, (y + block_size) % size);
						v4 = channel.getPixel((x + block_size) % size, (y + block_size) % size);
						v5 = 0.25f*(v1 + v2 + v3 + v4) + random.nextFloat()*amp - amp_half;
						channel.putPixel(x + block_size_half, y + block_size_half, v5);
						y+= block_size;
					}
				}
				// top and bottom edge blocks
				for (x_block = 1, x = block_size; x_block < (1<<i) - 1; x_block++) {
					for (y_block = 0; y_block < (1<<i); y_block+= (1<<i) - 1) {
						y = y_block*block_size;
						v1 = channel.getPixel(x, y);
						v2 = channel.getPixel((x + block_size) % size, y);
						v3 = channel.getPixel(x, (y + block_size) % size);
						v4 = channel.getPixel((x + block_size) % size, (y + block_size) % size);
						v5 = 0.25f*(v1 + v2 + v3 + v4) + random.nextFloat()*amp - amp_half;
						channel.putPixel(x + block_size_half, y + block_size_half, v5);
					}
					x+= block_size;
				}
			}
			// calculate left and bottom edge midpoints
			if (i < 2) {
				for (x_block = 0, x = 0; x_block < (1<<i); x_block++) {
					for (y_block = 0, y = 0; y_block < (1<<i); y_block++) {
						v1 = channel.getPixel(x, y);
						v5 = channel.getPixel(x + block_size_half, y + block_size_half);
						v2 = channel.getPixel((x + block_size) % size, y);
						v3 = channel.getPixel(x, (y + block_size) % size);
						v6 = channel.getPixel(((x - block_size_half) + size) % size, (y + block_size_half) % size);
						v7 = channel.getPixel((x + block_size_half) % size, ((y - block_size_half) + size) % size);
						v8 = 0.25f*(v1 + v3 + v5 + v6) + random.nextFloat()*amp - amp_half;
						v9 = 0.25f*(v1 + v2 + v5 + v7) + random.nextFloat()*amp - amp_half;
						channel.putPixel(x, y + block_size_half, v8);
						channel.putPixel(x + block_size_half, y, v9);
						y+= block_size;
					}
					x+= block_size;
				}
			} else {
				// safe blocks
				for (x_block = 1, x = block_size; x_block < (1<<i) - 1; x_block++) {
					for (y_block = 1, y = block_size; y_block < (1<<i) - 1; y_block++) {
						v1 = channel.getPixel(x, y);
						v5 = channel.getPixel(x + block_size_half, y + block_size_half);
						v2 = channel.getPixel(x + block_size, y);
						v3 = channel.getPixel(x, y + block_size);
						v6 = channel.getPixel(x - block_size_half, y + block_size_half);
						v7 = channel.getPixel(x + block_size_half, y - block_size_half);
						v8 = 0.25f*(v1 + v3 + v5 + v6) + random.nextFloat()*amp - amp_half;
						v9 = 0.25f*(v1 + v2 + v5 + v7) + random.nextFloat()*amp - amp_half;
						channel.putPixel(x, y + block_size_half, v8);
						channel.putPixel(x + block_size_half, y, v9);
						y+= block_size;
					}
					x+= block_size;
				}
				// left and right edge blocks
				for (x_block = 0; x_block < (1<<i); x_block+= (1<<i) - 1) {
					x = x_block*block_size;
					for (y_block = 0, y = 0; y_block < (1<<i); y_block++) {
						v1 = channel.getPixel(x, y);
						v5 = channel.getPixel(x + block_size_half, y + block_size_half);
						v2 = channel.getPixel((x + block_size) % size, y);
						v3 = channel.getPixel(x, (y + block_size) % size);
						v6 = channel.getPixel(((x - block_size_half) + size) % size, (y + block_size_half) % size);
						v7 = channel.getPixel((x + block_size_half) % size, ((y - block_size_half) + size) % size);
						v8 = 0.25f*(v1 + v3 + v5 + v6) + random.nextFloat()*amp - amp_half;
						v9 = 0.25f*(v1 + v2 + v5 + v7) + random.nextFloat()*amp - amp_half;
						channel.putPixel(x, y + block_size_half, v8);
						channel.putPixel(x + block_size_half, y, v9);
						y+= block_size;
					}
				}
				// top and bottom edge blocks
				for (x_block = 1, x = block_size; x_block < (1<<i) - 1; x_block++) {
					for (y_block = 0; y_block < (1<<i); y_block+= (1<<i) - 1) {
						y = y_block*block_size;
						v1 = channel.getPixel(x, y);
						v5 = channel.getPixel(x + block_size_half, y + block_size_half);
						v2 = channel.getPixel((x + block_size) % size, y);
						v3 = channel.getPixel(x, (y + block_size) % size);
						v6 = channel.getPixel(((x - block_size_half) + size) % size, (y + block_size_half) % size);
						v7 = channel.getPixel((x + block_size_half) % size, ((y - block_size_half) + size) % size);
						v8 = 0.25f*(v1 + v3 + v5 + v6) + random.nextFloat()*amp - amp_half;
						v9 = 0.25f*(v1 + v2 + v5 + v7) + random.nextFloat()*amp - amp_half;
						channel.putPixel(x, y + block_size_half, v8);
						channel.putPixel(x + block_size_half, y, v9);
					}
					x+= block_size;
				}
			}
		}
		channel.dynamicRange();
	}

	public final Layer toLayer() {
		return new Layer(channel, channel.copy(), channel.copy());
	}

	public final Channel toChannel() {
		return channel;
	}

}
