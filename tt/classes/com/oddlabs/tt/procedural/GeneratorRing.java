package com.oddlabs.tt.procedural;

import org.lwjgl.opengl.GL11;

import com.oddlabs.procedural.Channel;
import com.oddlabs.procedural.Layer;
import com.oddlabs.tt.render.Texture;
import com.oddlabs.tt.resource.GLImage;
import com.oddlabs.tt.resource.GLIntImage;

import java.util.Arrays;

public final strictfp class GeneratorRing extends TextureGenerator {
	private final int size;
	private final float[][] ring_parms;

	public GeneratorRing(int size, float[][] ring_parms) {
		this.size = size;
		this.ring_parms = ring_parms;
	}

	public final Texture[] generate() {
		Channel channel_ring = new Ring(size, size, ring_parms, Ring.LINEAR).toChannel();
		Channel channel_white = new Channel(size, size).fill(1f);
		Layer layer = new Layer(channel_white.copy(), channel_white.copy(), channel_white.copy(), channel_ring);
		Texture[] textures = new Texture[1];
		textures[0] = new Texture(new GLImage[]{new GLIntImage(layer)}, GL11.GL_RGBA, GL11.GL_LINEAR, GL11.GL_LINEAR, GL11.GL_CLAMP, GL11.GL_CLAMP);
		return textures;
	}
	
	public final int hashCode() {
		return size*ring_parms.hashCode();
	}

	private static boolean equals(float[][] a1, float[][] a2) {
		if (a1.length != a2.length)
			return false;
		for (int i = 0; i < a1.length; i++)
			if (!Arrays.equals(a1[i], a2[i]))
				return false;
		return true;
	}

	public final boolean equals(Object o) {
		if (!super.equals(o))
			return false;
		GeneratorRing other = (GeneratorRing)o;
		return size == other.size && equals(ring_parms, other.ring_parms);
	}
}
