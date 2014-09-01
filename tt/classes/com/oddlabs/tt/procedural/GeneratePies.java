package com.oddlabs.tt.procedural;

import com.oddlabs.procedural.Channel;
import com.oddlabs.procedural.Layer;
import com.oddlabs.tt.resource.GLIntImage;

public final strictfp class GeneratePies {

	public final static void main(String[] args) {
		int dia = 20;
		Channel channel;
		Channel alpha = new Pie(dia, 1f, Pie.CIRCLE).toChannel().channelBrightest(new Pie(dia, 1f, Pie.CIRCLE).toChannel().flipH());
		Layer layer;
		Layer canvas = new Layer(416, 64);
		canvas.addAlpha();
		int x_offset = 0;
		int y_offset = 0;
		float fill, r, g;
		float bg_alpha = 1f;
		for (int degrees = 5; degrees < 370; degrees += 15) {
			fill = degrees/360f;
			r = 1f - fill*fill*fill;
			g = 1f - (1f - fill)*(1f - fill)*(1f - fill);
			channel = new Pie(dia, fill, Pie.FULL).toChannel().invert();
			layer = new Layer(channel.copy().dynamicRange(r, 1f), channel.copy().dynamicRange(g, 1f), channel.copy(), channel.copy().invert().addClip(bg_alpha).channelMultiply(alpha));
			if (degrees < 10) {
				new GLIntImage(layer).saveAsPNG("pie00" + degrees);
			} else if (degrees < 100) {
				new GLIntImage(layer).saveAsPNG("pie0" + degrees);
			} else {
				new GLIntImage(layer).saveAsPNG("pie" + degrees);
			}
			System.out.println("degrees: " + degrees + " - fill %:" + fill);
			canvas.place(layer, x_offset, y_offset);
			x_offset += 32;
			if (x_offset > 384) {
				x_offset = 0;
				y_offset += 32;
			}
		}
		new GLIntImage(canvas).saveAsPNG("pies");
	}
}
