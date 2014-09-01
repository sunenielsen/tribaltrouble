package com.oddlabs.tt.procedural;

import com.oddlabs.procedural.Tools;
import com.oddlabs.procedural.Layer;
import com.oddlabs.procedural.Channel;

public final strictfp class Pie {
	public Channel channel;

	public static final int CIRCLE = 1;
	public static final int FULL = 2;
	
	public Pie(int size, float fill, int filltype) {
		channel = new Channel(size, size);
		float x_coord, y_coord, radius, angle, value;
		float fade_dist = 1f/size;
		float inner_radius = 0.5f - fade_dist;
		float fade_angle = (float)StrictMath.atan(0.5f/size);
		float inner_angle = fill - fade_angle;
		for (int x = 0; x < size; x++) {
			x_coord = (x + 0.5f)/size - 0.5f;
			for (int y = 0; y < size; y++) {
				y_coord = (y + 0.5f)/size - 0.5f;
				radius = (float)StrictMath.sqrt(x_coord*x_coord + y_coord*y_coord);
				if (x_coord != 0) {
					if (x_coord > 0) {
						angle = (float)(0.5f*StrictMath.PI + StrictMath.atan(y_coord/x_coord));
					} else {
						angle = (float)(1.5f*StrictMath.PI + StrictMath.atan(y_coord/x_coord));
					}
				} else {
					if (y_coord > 0) {
						angle = (float)StrictMath.PI;
					} else {
						angle = 0f;
					}
				}
				value = (float)(0.5f*angle/StrictMath.PI);
				switch (filltype) {
					case CIRCLE:
						if (radius < inner_radius) {
							if (value < inner_angle) {
								channel.putPixel(x, y, 1f);
							} else if (value >= inner_angle && value <= fill) {
								channel.putPixel(x, y, Tools.interpolateLinear(1f, 0f, (value - inner_angle)/fade_angle));
							}
						} else if (radius >= inner_radius && radius <= 0.5f) {
							if (value < inner_angle) {
								channel.putPixel(x, y, Tools.interpolateLinear(1f, 0f, (radius - inner_radius)/fade_dist));
							} else if (value >= inner_angle && value <= fill) {
								channel.putPixel(x, y, StrictMath.min(Tools.interpolateLinear(1f, 0f, (value - inner_angle)/fade_angle), Tools.interpolateLinear(1f, 0f, (radius - inner_radius)/fade_dist)));
							}
						}
						break;
					case FULL:
							if (value < inner_angle) {
								channel.putPixel(x, y, 1f);
							} else if (value >= inner_angle && value <= fill) {
								channel.putPixel(x, y, Tools.interpolateLinear(1f, 0f, (value - inner_angle)/fade_angle));
							}
						break;
					default:
						assert false : "wrong filltype";
						break;
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
