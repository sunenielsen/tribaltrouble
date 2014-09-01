package com.oddlabs.tt.procedural;

import com.oddlabs.procedural.Tools;
import com.oddlabs.procedural.Layer;
import com.oddlabs.procedural.Channel;

public final strictfp class Gradient {
	public Channel channel;
	
	public static final int HORIZONTAL = 1;
	public static final int VERTICAL = 2;
	
	public static final int LINEAR = 1;
	public static final int SMOOTH = 2;
	public static final int POLYNOMIAL = 3;

	public Gradient(int width, int height, float[][] gradient_list, int orientation, int interpolation) {
		channel = new Channel(width, height);
		float x_coord = 0;
		int index = 0;
		int index_max = gradient_list.length - 1;
		float value = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				x_coord = (float)x/width;
				if (x_coord >= gradient_list[index][0] && index < index_max) index++;
				if (x_coord < gradient_list[0][0]) {
					value = gradient_list[0][1];
				} else {
					if (x_coord >= gradient_list[index_max][0]) {
						value = gradient_list[index_max][1];
					} else {
						switch (interpolation) {
							case LINEAR:
								value = Tools.interpolateLinear(gradient_list[index - 1][1], gradient_list[index][1], (x_coord - gradient_list[index - 1][0])/(gradient_list[index][0] - gradient_list[index - 1][0]));
								break;
							case SMOOTH:
								value = Tools.interpolateSmooth(gradient_list[index - 1][1], gradient_list[index][1], (x_coord - gradient_list[index - 1][0])/(gradient_list[index][0] - gradient_list[index - 1][0]));
								break;
							default:
								assert false: "incorrect interpolation method";
						}
					}
				}
				switch (orientation) {
					case HORIZONTAL:
						channel.putPixel(x, y, value);
						break;
					case VERTICAL:
						channel.putPixel(y, x, value);
						break;
					default:
						assert false: "incorrect orientation";
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
