package com.oddlabs.tt.procedural;

import com.oddlabs.tt.resource.*;
import com.oddlabs.tt.render.*;
import com.oddlabs.tt.global.*;
import com.oddlabs.procedural.Layer;
import com.oddlabs.procedural.Channel;

import org.lwjgl.opengl.*;

public final strictfp class GeneratorPoison extends TextureGenerator {
	private static final int TEXTURE_SIZE = 128;

	public final Texture[] generate() {
		int seed = Globals.LANDSCAPE_SEED;
		
		/*
		Channel voronoi = new Voronoi(TEXTURE_SIZE, 4, 4, 1, 1f, 42).getDistance(-1f, 0f, 0f);
		Channel poison_alpha = new Ring(TEXTURE_SIZE, TEXTURE_SIZE, new float[][] {{0f, 1f},{0.5f, 0f}}, Ring.SMOOTH).toChannel().gamma(1.5f);
		Channel poison_color = new Channel(TEXTURE_SIZE, TEXTURE_SIZE).fill(0.5f);
		Channel poison_bump = voronoi.gamma(0.25f).smooth(3).smooth(1).dynamicRange(0.925f, 1f).channelMultiply(poison_alpha);
		poison_color.bump(poison_bump, 0f, -4f, 0f, 1f, 0f);
		*/
		
		Channel blob = new Hill(TEXTURE_SIZE, Hill.CIRCLE).toChannel();
		Channel noise = new Midpoint(TEXTURE_SIZE, 2, .45f, seed).toChannel();
		Channel poison_alpha = noise.copy().channelMultiply(blob.copy().dynamicRange(.25f, 1f)).channelSubtract(blob.copy().invert().brightness(.25f));
		
		Layer poison = new Layer(noise.copy().rotate(90).dynamicRange(.5f, 1f), new Channel(TEXTURE_SIZE, TEXTURE_SIZE).fill(1f), new Channel(TEXTURE_SIZE, TEXTURE_SIZE), poison_alpha.brightness(.75f));
		GLIntImage poison_img = new GLIntImage(poison);
		if (Landscape.DEBUG) poison_img.saveAsPNG("generator_poison");
		Texture[] textures = new Texture[1];
		textures[0] = new Texture(poison_img.createMipMaps(), Globals.COMPRESSED_RGBA_FORMAT, GL11.GL_LINEAR_MIPMAP_NEAREST, GL11.GL_LINEAR, GL11.GL_CLAMP, GL11.GL_CLAMP);
		return textures;
	}

	public final int hashCode() {
		return TEXTURE_SIZE + 398;
	}
}
