package com.oddlabs.tt.procedural;

import com.oddlabs.tt.resource.*;
import com.oddlabs.tt.render.*;
import com.oddlabs.tt.global.*;
import com.oddlabs.procedural.Layer;
import com.oddlabs.procedural.Channel;
import com.oddlabs.procedural.Tools;

import org.lwjgl.opengl.*;

public final strictfp class GeneratorSonic extends TextureGenerator {
	private static final int TEXTURE_SIZE = 128;

	public final Texture[] generate() {
		Channel sonic_alpha = new Channel(TEXTURE_SIZE>>1, TEXTURE_SIZE>>1);
		
		float x_coord;
		float y_coord;
		float radius;
		
		for (int y = 0; y < TEXTURE_SIZE>>1; y++) {
			y_coord = .5f - (y + .5f)/TEXTURE_SIZE;
			for (int x = 0; x < TEXTURE_SIZE>>1; x++) {
				x_coord = .5f - (x + .5f)/TEXTURE_SIZE;
				radius = 4f*(x_coord*x_coord*x_coord + y_coord*y_coord*y_coord);
				if (radius < .5f) {
					sonic_alpha.putPixel(x, y, Tools.interpolateLinear(1f, 0f, (float)Math.sqrt(2f*radius))); // can use Math here; not gamestate affecting
				}
			}
		}
		Channel sonic_alpha_final = new Channel(TEXTURE_SIZE, TEXTURE_SIZE);
		sonic_alpha_final.quadJoin(sonic_alpha, sonic_alpha.copy().rotate(270), sonic_alpha.copy().rotate(90), sonic_alpha.copy().rotate(180));
		
		Channel sonic_color = new Channel(TEXTURE_SIZE, TEXTURE_SIZE).fill(.05f);
		//Channel sonic_color = new Channel(TEXTURE_SIZE, TEXTURE_SIZE).fill(.1f); // screenshot fix
		Layer sonic = new Layer(sonic_color, sonic_color, sonic_color, sonic_alpha_final);
		GLIntImage sonic_img = new GLIntImage(sonic);
		if (Landscape.DEBUG) sonic_img.saveAsPNG("generator_sonic");
		Texture[] textures = new Texture[1];
		textures[0] = new Texture(sonic_img.createMipMaps(), Globals.COMPRESSED_RGBA_FORMAT, GL11.GL_LINEAR_MIPMAP_NEAREST, GL11.GL_LINEAR, GL11.GL_REPEAT, GL11.GL_REPEAT);
		return textures;
	}

	public final int hashCode() {
		return TEXTURE_SIZE + 3;
	}
}
