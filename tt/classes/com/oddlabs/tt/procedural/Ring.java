package com.oddlabs.tt.procedural;

import com.oddlabs.procedural.Layer;
import com.oddlabs.procedural.Channel;
import com.oddlabs.procedural.Tools;

public final strictfp class Ring {
	public Channel channel;
	public Channel channelfinal;

	public static final int LINEAR = 1;
	public static final int SMOOTH = 2;

	public Ring(int width, int height, float[][] gradient_list, int interpolation) {
		channel = new Channel(width>>1, height>>1);
		float x_coord;
		float y_coord;
		float radius;
		int index;
		int index_max = gradient_list.length - 1;
		float value = 0;
		
		for (int x = 0; x < width>>1; x++) {
			for (int y = 0; y < height>>1; y++) {
				x_coord = 0.5f - (x + 0.5f)/width;
				y_coord = 0.5f - (y + 0.5f)/height;
				radius = (float)Math.sqrt(x_coord*x_coord + y_coord*y_coord); // can use math here, not game state affecting
				index = 0;
				
				while (radius >= gradient_list[index][0] && index < index_max) {
					index++;
				}
				
				if (radius < gradient_list[0][0]) {
					value = gradient_list[0][1];
				} else {
					if (radius >= gradient_list[index_max][0]) {
						value = gradient_list[index_max][1];
					} else {
						switch (interpolation) {
							case LINEAR:
								value = Tools.interpolateLinear(gradient_list[index - 1][1], gradient_list[index][1], (radius - gradient_list[index - 1][0])/(gradient_list[index][0] - gradient_list[index - 1][0]));
								break;
							case SMOOTH:
								value = Tools.interpolateSmooth(gradient_list[index - 1][1], gradient_list[index][1], (radius - gradient_list[index - 1][0])/(gradient_list[index][0] - gradient_list[index - 1][0]));
								break;
							default:
								assert false: "incorrect interpolation method";
						}
					}
				}
				channel.putPixel(x, y, value);
			}
		}
		
		channelfinal = new Channel(width, height);
		channelfinal.quadJoin(channel, channel.copy().rotate(270), channel.copy().rotate(90), channel.copy().rotate(180));
	}

	public final Layer toLayer() {
		return new Layer(channelfinal, channelfinal.copy(), channelfinal.copy());
	}

	public final Channel toChannel() {
		return channelfinal;
	}

}
