package com.oddlabs.tt.procedural;

import java.util.Random;

import com.oddlabs.procedural.Channel;
import com.oddlabs.procedural.Layer;
import com.oddlabs.procedural.Tools;

public final strictfp class Bricks {
	private Random random;
	public Channel channel;
	
	public static final int BUMP = 1;
	public static final int COLOR = 2;

	public Bricks(int width, int height, int bricks, int layers, float x_mortar, float y_mortar, float stagger, float randomness, long seed, int type) {

		random = new Random(seed);
		float[][] cells = new float[bricks][layers];
		for (int i = 0; i < bricks; i++) {
			for (int j = 0; j < layers; j++) {
				cells[i][j] = random.nextFloat();
			}
		}

		channel = new Channel(width, height);
		float cellwidth = (float)width/bricks;
		float cellheight = (float)height/layers;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int x_cell = (int)(((float)x/width)*bricks);
				int y_cell = (int)(((float)y/height)*layers);
				float x_coord = (x - cellwidth*(int)(x/cellwidth))/cellwidth + y_cell*stagger + randomness*(cells[0][y_cell] - 0.5f);
				float x_coord_mod = x_coord;
				if (x_coord < 0 || x_coord > 1) {
					x_coord_mod = Tools.modulo(x_coord, 1f);
				}
				float y_coord = (y - cellheight*(int)(y/cellheight))/cellheight;
				if (x_coord_mod < x_mortar || x_coord_mod > 1-x_mortar || y_coord < y_mortar || y_coord > 1-y_mortar) {
					channel.putPixel(x, y, 0f);
				} else {
					switch (type) {
						case BUMP:
							channel.putPixel(x, y, 1f);
							break;
						case COLOR:
							if (x_coord < 0 || x_coord > 1) {
								channel.putPixel(x, y, cells[Tools.modulo(x_cell + StrictMath.round(x_coord - x_coord_mod), bricks)][y_cell]);
							} else {
								channel.putPixel(x, y, cells[x_cell][y_cell]);
							}
							break;
						default:
							assert false: "incorrect coloring type";
					}
				}
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
