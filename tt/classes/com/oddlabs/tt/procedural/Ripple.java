package com.oddlabs.tt.procedural;

import com.oddlabs.procedural.Channel;
import com.oddlabs.procedural.Layer;

public final strictfp class Ripple {
	public Channel channel;

	public Ripple(int width, int height, float point_x, float point_y, float factor) {

		// create image
		channel = new Channel(width, height);

		// fill in pixelvalues
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				float x_coord = (float)x/width;
				float y_coord = (float)y/height;
				
				float dx = StrictMath.abs(x_coord - point_x);
				float dy = StrictMath.abs(y_coord - point_y);
				float dx1 = 1 + dx;
				float dy1 = 1 + dy;
				float dx2 = dx;
				float dy2 = dy1;
				float dx3 = 1 - dx;
				float dy3 = dy1;
				float dx4 = dx1;
				float dy4 = dy;
				float dx5 = dx3;
				float dy5 = dy;
				float dx6 = dx1;
				float dy6 = 1 - dy;
				float dx7 = dx;
				float dy7 = dy6;
				float dx8 = dx3;
				float dy8 = dy6;

				float dist = (float)StrictMath.sqrt(dx*dx + dy*dy);
				float dist1 = (float)StrictMath.sqrt(dx1*dx1 + dy1*dy1);
				float dist2 = (float)StrictMath.sqrt(dx2*dx2 + dy2*dy2);
				float dist3 = (float)StrictMath.sqrt(dx3*dx3 + dy3*dy3);
				float dist4 = (float)StrictMath.sqrt(dx4*dx4 + dy4*dy4);
				float dist5 = (float)StrictMath.sqrt(dx5*dx5 + dy5*dy5);
				float dist6 = (float)StrictMath.sqrt(dx6*dx6 + dy6*dy6);
				float dist7 = (float)StrictMath.sqrt(dx7*dx7 + dy7*dy7);
				float dist8 = (float)StrictMath.sqrt(dx8*dx8 + dy8*dy8);
				
				if (dist > 1) dist = 1f;
				if (dist1 > 1) dist1 = 1f;
				if (dist2 > 1) dist2 = 1f;
				if (dist3 > 1) dist3 = 1f;
				if (dist4 > 1) dist4 = 1f;
				if (dist5 > 1) dist5 = 1f;
				if (dist6 > 1) dist6 = 1f;
				if (dist7 > 1) dist7 = 1f;
				if (dist8 > 1) dist8 = 1f;
				
				float value = (float)(StrictMath.cos(factor*dist)*(-dist + 1)
									+ StrictMath.cos(factor*dist1)*(-dist1 + 1)
									+ StrictMath.cos(factor*dist2)*(-dist2 + 1)
									+ StrictMath.cos(factor*dist3)*(-dist3 + 1)
									+ StrictMath.cos(factor*dist4)*(-dist4 + 1)
									+ StrictMath.cos(factor*dist5)*(-dist5 + 1)
									+ StrictMath.cos(factor*dist6)*(-dist6 + 1)
									+ StrictMath.cos(factor*dist7)*(-dist7 + 1)
									+ StrictMath.cos(factor*dist8)*(-dist8 + 1));
				//if (value < 0) {
				//	value = -(float)StrictMath.sqrt(-value);
				//} else {
				//	value = (float)StrictMath.sqrt(value);
				//}
				channel.putPixel(x, y, value);
			}
		}

		// normalize image
		channel.dynamicRange();

	}

	public final Layer toLayer() {
		return new Layer(channel, channel, channel);
	}

	public final Channel toChannel() {
		return channel;
	}

}
