package com.oddlabs.tt.procedural;

import com.oddlabs.tt.resource.*;
import com.oddlabs.tt.render.*;
import com.oddlabs.tt.global.*;
import com.oddlabs.procedural.Layer;
import com.oddlabs.procedural.Channel;

import org.lwjgl.opengl.*;

public final strictfp class GeneratorOcean extends TextureGenerator {
	private static final int TEXTURE_SIZE = 256;
	
	private final int terrain_type;

	public GeneratorOcean(int terrain_type) {
		this.terrain_type = terrain_type;
	}
	
	public final Texture[] generate() {
		int seed = Globals.LANDSCAPE_SEED + 1;
		
		// water1
		Channel perlin2 = new Perlin(TEXTURE_SIZE, TEXTURE_SIZE, 4, 4, 0.5f, 1, seed, Perlin.CUBIC, Perlin.NORMAL).toChannel();
		Channel perlin4 = new Perlin(TEXTURE_SIZE, TEXTURE_SIZE, 4, 4, 0.5f, 3, seed, Perlin.CUBIC, Perlin.NORMAL).toChannel();
		Channel perlin32 = new Perlin(TEXTURE_SIZE, TEXTURE_SIZE, 32, 32, 0.5f, 2, seed, Perlin.CUBIC, Perlin.NORMAL).toChannel();
		Channel perlin64 = new Perlin(TEXTURE_SIZE, TEXTURE_SIZE, 64, 64, 0.5f, 2, seed, Perlin.CUBIC, Perlin.NORMAL).toChannel();
		Channel noise32 = perlin32.copy().abs().dynamicRange().gamma8().gamma2().invert().perturb(perlin4.copy().rotate(90), 0.05f).perturb(perlin2, 0.05f);
		Channel noise64 = perlin64.copy().abs().dynamicRange().gamma2().invert().channelMultiply(perlin32.copy().rotate(90).contrast(2f)).perturb(perlin4.copy().rotate(180), 0.05f).perturb(perlin2.copy().rotate(90), 0.05f);
		Channel highlight = noise32.channelBrightest(noise64);
		Layer water1 = new Layer(new Channel(TEXTURE_SIZE, TEXTURE_SIZE), perlin4.copy().dynamicRange(0.5f, 0.8f).rotate(180), perlin4.copy().rotate(270).dynamicRange(0.6f, 0.9f));
		water1.layerAdd(highlight.multiply(0.2f).toLayer());
		water1.addAlpha();
		water1.a.fill(0.5f);
		if (terrain_type == Landscape.VIKING) {
			water1.multiply(0.4f);
			water1.a.addClip(0.1f);
		}
		
		// water2
		Channel voronoi12 = new Voronoi(TEXTURE_SIZE, 12, 12, 1, 1f, seed).getDistance(1f, 0f, 0f);
		Channel voronoi24 = new Voronoi(TEXTURE_SIZE, 24, 24, 1, 1f, seed).getDistance(1f, 0f, 0f);
		Channel perturb = new Midpoint(TEXTURE_SIZE, 4, 0.4f, 42).toChannel();
		voronoi12.channelAverage(voronoi24);
		voronoi12.perturb(perturb, 0.025f);
		Layer water2 = new Layer(voronoi12.copy(), voronoi12.copy(), voronoi12.copy(), voronoi12.copy().gamma(1.5f));
		water2.bump(voronoi12, 3.5f, 0f, 0.5f, 0.5f, 0.8f, 1f, 0f, 0f, 0f);
		
		switch (terrain_type) {
			case Landscape.NATIVE:
				water2.r.dynamicRange(0f, 0.4f);
				water2.g.dynamicRange(0.6f, 1f);
				water2.b.dynamicRange(0.9f, 1f);
				break;
			case Landscape.VIKING:
				water2.r.dynamicRange(0.5f, 1f);
				water2.g.dynamicRange(0.7f, 1f);
				water2.b.dynamicRange(0.8f, 1f);
				water2.a.gamma(0.5f).dynamicRange(0f, 0.2f);
				break;
			default:
				assert false : "illegal terrain_type";
				break;
		}
		
		if (Landscape.DEBUG) new GLIntImage(water1).saveAsPNG("generator_water_1");
		if (Landscape.DEBUG) new GLIntImage(water2).saveAsPNG("generator_water_2");
		Texture[] textures = new Texture[2];
		textures[0] = new Texture(new GLImage[]{new GLIntImage(water1)}, GL11.GL_RGBA, GL11.GL_LINEAR, GL11.GL_LINEAR, GL11.GL_REPEAT, GL11.GL_REPEAT);
		textures[1] = new Texture(new GLImage[]{new GLIntImage(water2)}, GL11.GL_RGBA, GL11.GL_LINEAR, GL11.GL_LINEAR, GL11.GL_REPEAT, GL11.GL_REPEAT);
		return textures;
	}
	
	public final int hashCode() {
		return TEXTURE_SIZE;
	}

	public boolean equals(Object o) {
		return super.equals(o) && ((GeneratorOcean)o).terrain_type == terrain_type;
	}
}
