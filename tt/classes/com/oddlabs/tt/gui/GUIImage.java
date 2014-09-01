package com.oddlabs.tt.gui;

import org.lwjgl.opengl.GL11;

import com.oddlabs.tt.render.Texture;
import com.oddlabs.tt.resource.Resources;
import com.oddlabs.tt.resource.TextureFile;

public final strictfp class GUIImage extends GUIObject {
	private final float u1;
	private final float v1;
	private final float u2;
	private final float v2;
	private final Texture texture;

	public GUIImage(int width, int height, float u1, float v1, float u2, float v2, String texture_name) {
		this(width, height, u1, v1, u2, v2, (Texture)Resources.findResource(new TextureFile(texture_name, GL11.GL_RGBA, GL11.GL_LINEAR, GL11.GL_LINEAR, GL11.GL_REPEAT, GL11.GL_REPEAT)));
	}

	public GUIImage(int width, int height, float u1, float v1, float u2, float v2, Texture texture) {
		this.u1 = u1;
		this.v1 = v1;
		this.u2 = u2;
		this.v2 = v2;
		this.texture = texture;
		if (width < 0 || height < 0)
			setDim(texture.getWidth(), texture.getHeight());
		else
			setDim(width, height);
		setCanFocus(false);
	}

	protected final void renderGeometry() {
		int width = getWidth();
		int height = getHeight();

		GL11.glEnd();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getHandle());
		GL11.glBegin(GL11.GL_QUADS);

		GL11.glTexCoord2f(u1, v1);
		GL11.glVertex3f(0, 0, 0);
		GL11.glTexCoord2f(u2, v1);
		GL11.glVertex3f(width, 0, 0);
		GL11.glTexCoord2f(u2, v2);
		GL11.glVertex3f(width, height, 0);
		GL11.glTexCoord2f(u1, v2);
		GL11.glVertex3f(0, height, 0);

		GL11.glEnd();
		Skin.getSkin().bindTexture();
		GL11.glBegin(GL11.GL_QUADS);
	}
}

