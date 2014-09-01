package com.oddlabs.tt.resource;

import com.oddlabs.tt.render.Texture;

import org.lwjgl.opengl.GL11;

public abstract strictfp class BlendInfo {
	private final Texture alpha_map;

	private final Texture createAlphaMap(GLByteImage alpha_image, int format) {
		return new Texture(new GLByteImage[]{alpha_image}, format, GL11.GL_LINEAR,
										  GL11.GL_LINEAR, GL11.GL_REPEAT, GL11.GL_REPEAT);
	}

	protected BlendInfo(GLByteImage alpha_image, int format) {
		alpha_map = createAlphaMap(alpha_image, format);
	}

/*	public void delete() {
		alpha_map.delete();
	}
*/
	protected final void bindAlpha() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, alpha_map.getHandle());
	}

	public abstract void setup();
	public abstract void reset();
}
