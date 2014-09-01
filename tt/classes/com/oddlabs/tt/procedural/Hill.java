package com.oddlabs.tt.procedural;

import com.oddlabs.procedural.Layer;
import com.oddlabs.procedural.Channel;

public final strictfp class Hill {
	private Channel channel;
	private int size;

	public static final int CIRCLE = 1;
	public static final int OVAL = 2;
	public static final int SQUARE = 3;

	public Hill(int size, int shape) {
		this.size = size;
		switch (shape) {
			case CIRCLE:
				circle();
				break;
			case OVAL:
				oval();
				break;
			case SQUARE:
				square();
				break;
			default:
				assert false : "illegal shape";
				break;
		}
	}
	
	private final void circle() {
		Channel quarter = new Channel(size>>1, size>>1);
		for (int y = 0; y < size>>1; y++) {
			float y_coord = (float)y/size;
			float dy = y_coord - 0.5f;
			for (int x = 0; x < size>>1; x++) {
				float x_coord = (float)x/size;
				float dx = x_coord - 0.5f;
				float dist = StrictMath.min(1f, 4*(dx*dx + dy*dy));
				quarter.putPixel(x, y, 1f - dist);
			}
		}
		channel = new Channel(size, size);
		channel.quadJoin(quarter, quarter.copy().rotate(270), quarter.copy().rotate(90), quarter.copy().rotate(180));
	}
	
	private final void oval() {
		Channel quarter = new Channel(size>>1, size>>1);
		for (int y = 0; y < size>>1; y++) {
			float y_coord = (float)y/size;
			float dy = StrictMath.abs(y_coord - 0.5f);
			for (int x = 0; x < size>>1; x++) {
				float x_coord = (float)x/size;
				float dx = StrictMath.abs(x_coord - 0.5f);
				float dist = StrictMath.min(1f, 8*(dx*dx*dx + dy*dy*dy));
				quarter.putPixel(x, y, 1f - dist);
			}
		}
		channel = new Channel(size, size);
		channel.quadJoin(quarter, quarter.copy().rotate(270), quarter.copy().rotate(90), quarter.copy().rotate(180));
	}
	
	private final void square() {
		Channel quarter = new Channel(size>>1, size>>1);
		for (int y = 0; y < size>>1; y++) {
			float y_coord = (float)y/size;
			float dy = y_coord - 0.5f;
			for (int x = 0; x < size>>1; x++) {
				float x_coord = (float)x/size;
				float dx = x_coord - 0.5f;
				float dx2 = dx*dx;
				float dy2 = dy*dy;
				float dist = StrictMath.min(1f, 16*(dx2*dx2 + dy2*dy2));
				quarter.putPixel(x, y, 1f - dist);
			}
		}
		channel = new Channel(size, size);
		channel.quadJoin(quarter, quarter.copy().rotate(270), quarter.copy().rotate(90), quarter.copy().rotate(180));
	}

	public final Layer toLayer() {
		return new Layer(channel, channel.copy(), channel.copy());
	}

	public final Channel toChannel() {
		return channel;
	}

}
