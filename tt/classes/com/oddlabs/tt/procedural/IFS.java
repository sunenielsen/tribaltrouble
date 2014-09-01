package com.oddlabs.tt.procedural;

import java.util.Random;

import com.oddlabs.procedural.Channel;
import com.oddlabs.procedural.Layer;

public final strictfp class IFS {
	private Random random;
	public Channel channel;

	public IFS(int width, int height, int maxpoints, int seed, float[][][] transformations, float[] probabilities) {
		channel = new Channel(width, height);
		random = new Random(seed);
		
		// init image
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				channel.putPixel(x, y, 0f);
			}
		}
		
		float p = 0;
		float x_coord = 0f;
		float y_coord = 0f;
		float x_temp = 0f;
		
		// loop points
		for (int i = 0; i < maxpoints; i++) {
			p = random.nextFloat();
			x_temp = x_coord;
			
			// loop transformations
			for (int t = 0; t < transformations.length; t++) {
				if (p < probabilities[t]) {
					x_coord = (transformations[t][2][0] - transformations[t][3][0])*x_coord + (transformations[t][2][0] - transformations[t][1][0])*y_coord + transformations[t][0][0];
					y_coord = (transformations[t][2][1] - transformations[t][3][1])*x_temp + (transformations[t][2][1] - transformations[t][1][1])*y_coord + transformations[t][0][1];
					break;
				}
			}
			
			// put pixel
			if (i > 100) { // skip first 100 transformations
				channel.putPixel((int)(x_coord*width), (int)(y_coord*height), 1f);
			}
		}
	}

	public final Layer toLayer() {
			return new Layer(channel, channel, channel);
	}

	public final Channel toChannel() {
			return channel;
	}

}
