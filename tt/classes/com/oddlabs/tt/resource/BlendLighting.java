package com.oddlabs.tt.resource;

import com.oddlabs.tt.resource.GLByteImage;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.util.GLState;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public final strictfp class BlendLighting extends BlendInfo {
	private final float r;
	private final float g;
	private final float b;
	
	public BlendLighting(GLByteImage alpha_image, float r, float g, float b) {
		super(alpha_image, Globals.COMPRESSED_LUMINANCE_FORMAT);
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public final void setup() {
		GLState.activeTexture(GL13.GL_TEXTURE1);
		GL11.glColor3f(r, b, g);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		bindAlpha();
		GLState.activeTexture(GL13.GL_TEXTURE0);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_ONE);
	}

	public final void reset() {
		GLState.activeTexture(GL13.GL_TEXTURE1);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
		GLState.activeTexture(GL13.GL_TEXTURE0);
	}
}
