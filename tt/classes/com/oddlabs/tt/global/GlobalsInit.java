package com.oddlabs.tt.global;

import org.lwjgl.opengl.GL11;

import com.oddlabs.tt.util.GLUtils;

// Class that initializes all static data in Globals - must be called once before anything else
public final strictfp class GlobalsInit {
	public final static void init() {
		initTextureSize();
	}

	private final static int bestTextureSize(int size) {
		int texture_max = Globals.MIN_TEXTURE_SIZE;
		int texture_min = texture_max;
		for (; texture_max < Globals.MAX_TEXTURE_SIZE; texture_min = texture_max, texture_max <<= 1)
			if (texture_max >= size) break;
		if (size + texture_min*(Globals.TEXTURE_WEIGHT - 1.0f) - texture_max*Globals.TEXTURE_WEIGHT > 0)
			return texture_max;
		else
			return texture_min;
	}

	private final static void initTextureSize() {
		int tex_pow;
		int max_size = GLUtils.getGLInteger(GL11.GL_MAX_TEXTURE_SIZE);
		System.out.println("Maximum texture size " + max_size);
		Globals.MAX_TEXTURE_SIZE = max_size;
		for (tex_pow = 0; max_size != 1; max_size >>= 1, tex_pow++);
		Globals.MAX_TEXTURE_POWER = tex_pow;

		Globals.TEXTURE_SIZES = new int[Globals.MAX_TEXTURE_SIZE];
		Globals.TEXTURE_SPLITS = new byte[Globals.MAX_TEXTURE_SIZE];
		Globals.BEST_SIZES = new int[Globals.MAX_TEXTURE_SIZE];

		for (int i = 0; i < Globals.MAX_TEXTURE_SIZE; i++)
			Globals.BEST_SIZES[i] = bestTextureSize(i);

		int result;
		byte num_splits;
		for (int i = 0; i < Globals.MAX_TEXTURE_SIZE; i++) {
			result = 0;
			num_splits = 0;
			while (result < i) {
				result += Globals.BEST_SIZES[i - result];
				num_splits++;
			}
			Globals.TEXTURE_SIZES[i] = result;
			Globals.TEXTURE_SPLITS[i] = num_splits;
		}
	}
}
