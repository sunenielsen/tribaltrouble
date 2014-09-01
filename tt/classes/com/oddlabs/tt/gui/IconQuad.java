package com.oddlabs.tt.gui;

import com.oddlabs.tt.render.Texture;
import com.oddlabs.util.Quad;

import org.lwjgl.opengl.*;

public final strictfp class IconQuad extends Quad {
	private final static long serialVersionUID = 1;

	private final Texture texture;

	public IconQuad(float u1, float v1, float u2, float v2, int width, int height, Texture texture) {
		super(u1, v1, u2, v2, width, height);
		this.texture = texture;
	}

	public final void render(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		GL11.glEnd();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getHandle());
//		Icons.getIcons().bindTexture();
		GL11.glBegin(GL11.GL_QUADS);
		super.render(x1, y1, x2, y2, x3, y3, x4, y4);
		GL11.glEnd();
		Skin.getSkin().bindTexture();
		GL11.glBegin(GL11.GL_QUADS);
	}
}
