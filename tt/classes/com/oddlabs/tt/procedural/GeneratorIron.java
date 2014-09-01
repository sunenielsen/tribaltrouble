package com.oddlabs.tt.procedural;

import com.oddlabs.tt.resource.*;
import com.oddlabs.tt.render.*;
import com.oddlabs.tt.global.*;
import com.oddlabs.procedural.Layer;
import com.oddlabs.procedural.Channel;

import org.lwjgl.opengl.*;

public final strictfp class GeneratorIron extends TextureGenerator {
	private static final int TEXTURE_SIZE = 128;

	public final Texture[] generate() {
		int seed = Globals.LANDSCAPE_SEED;
		
		Channel rock_bump = new Voronoi(TEXTURE_SIZE, 8, 8, 1, 1f, seed).getDistance(-1f, 0f, 0f).multiply(.49f);
		rock_bump.channelAdd(new Voronoi(TEXTURE_SIZE, 8, 8, 1, 1f, seed+1).getDistance(-1f, 0f, 0f).multiply(.49f));
		Channel noise = new Midpoint(TEXTURE_SIZE, 7, .4f, seed).toChannel();
		Channel noise2 = new Midpoint(TEXTURE_SIZE, 3, .45f, seed).toChannel();
		rock_bump.channelAdd(noise.copy().multiply(.02f));
		Layer rock = noise.copy().dynamicRange(.5f, .75f).toLayer();
		Layer rust = new Layer(new Channel(TEXTURE_SIZE, TEXTURE_SIZE).fill(.4f), new Channel(TEXTURE_SIZE, TEXTURE_SIZE).fill(.15f), new Channel(TEXTURE_SIZE, TEXTURE_SIZE).fill(0f), noise2.copy().gamma2().invert());
		rock.layerBlend(rust);
		rock.bump(rock_bump, TEXTURE_SIZE/256f, 0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f).gamma(.5f).dynamicRange(0f, .75f);
		rock.bumpSpecular(rock_bump, 1f, 0f, 1f, 0f, .1f, .1f, .1f, 1);
		Layer stain = new Layer(noise2.copy().multiply(.4f), noise2.copy().multiply(.15f), noise2.copy().multiply(0f), noise2.copy().gamma(.75f).rotate(90));
		stain.bump(noise2.copy(), 8f, 0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f);
		rock.layerBlend(stain);
		
		if (Landscape.DEBUG) new GLIntImage(rock).saveAsPNG("generator_iron");
		Texture[] textures = new Texture[1];
		textures[0] = new Texture(new GLIntImage(rock).createMipMaps(), GL11.GL_RGB, GL11.GL_LINEAR_MIPMAP_NEAREST, GL11.GL_LINEAR, GL11.GL_REPEAT, GL11.GL_REPEAT);
		return textures;
	}
	
	public final int hashCode() {
		return TEXTURE_SIZE;
	}
}
