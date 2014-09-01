package com.oddlabs.util;

import java.nio.ShortBuffer;

public final strictfp class IndexListACMR {
	public static float computeACMR(short[] indices, int fifo_size) {
		return computeACMR(ShortBuffer.wrap(indices), fifo_size);
	}

	public static float computeACMR(ShortBuffer indices, int fifo_size) {
		int misses = 0;
		short[] fifo = new short[fifo_size];
		int current_fifo_size = 0;
outer:
		for (int i = 0; i < indices.remaining(); i++) {
			short index = indices.get(indices.position() + i);
			for (int j = 0; j < current_fifo_size; j++)
				if (fifo[j] == index)
					continue outer;
			if (current_fifo_size == fifo.length) {
				for (int j = 1; j < current_fifo_size; j++)
					fifo[j - 1] = fifo[j];
			} else
				current_fifo_size++;
			fifo[current_fifo_size - 1] = index;
			misses++;
		}
		return (float)misses*3/indices.remaining();
	}
}
