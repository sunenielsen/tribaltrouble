package com.oddlabs.tt.procedural;

import org.lwjgl.opengl.GL11;

import com.oddlabs.procedural.Channel;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.render.Texture;
import com.oddlabs.tt.resource.GLByteImage;
import com.oddlabs.tt.resource.GLIntImage;

public final strictfp class GeneratorClouds extends TextureGenerator {
	private static final int TEXTURE_SIZE = 256;

	public static final int INNER = 0;
	public static final int OUTER = 1;

	private final int terrain_type;

	public GeneratorClouds(int terrain_type) {
		this.terrain_type = terrain_type;
	}

	public final Texture[] generate() {
		int seed = Globals.LANDSCAPE_SEED;
		Channel clouds1 = new Midpoint(TEXTURE_SIZE, 3, 0.55f, seed).toChannel();
		Channel clouds2 = new Midpoint(TEXTURE_SIZE, 2, 0.4f, seed).toChannel();
		
		switch (terrain_type) {
			case Landscape.NATIVE:
				clouds1.dynamicRange(0.5f, 1f, 0f, 1f).gamma(0.75f).brightness(0.5f);
				clouds2.dynamicRange(0.25f, 1f, 0f, 1f).gamma(0.5f).brightness(0.33f);
				break;
			case Landscape.VIKING:
				clouds1.dynamicRange(0.5f, 1f, 0f, 0.75f);
				clouds2.dynamicRange(0.5f, 1f, 0f, 0.75f);
				break;
			default:
				assert false : "illegal terrain_type";
				break;
		}
		
		if (Landscape.DEBUG) new GLIntImage(clouds1.toLayer()).saveAsPNG("generator_clouds_1");
		if (Landscape.DEBUG) new GLIntImage(clouds2.toLayer()).saveAsPNG("generator_clouds_2");
		GLByteImage cloud_images[] = new GLByteImage[] {new GLByteImage(clouds1, GL11.GL_LUMINANCE), new GLByteImage(clouds2, GL11.GL_LUMINANCE)};
		Texture[] textures = new Texture[cloud_images.length];
		for (int i = 0; i < cloud_images.length; i++) {
			textures[i] = new Texture(cloud_images[i].createMipMaps(), Globals.COMPRESSED_LUMINANCE_FORMAT, GL11.GL_LINEAR_MIPMAP_NEAREST, GL11.GL_LINEAR, GL11.GL_REPEAT, GL11.GL_REPEAT);
		}
		return textures;
	}
	
	public final int hashCode() {
		return TEXTURE_SIZE;
	}

	public boolean equals(Object o) {
		return super.equals(o) && ((GeneratorClouds)o).terrain_type == terrain_type;
	}
}
