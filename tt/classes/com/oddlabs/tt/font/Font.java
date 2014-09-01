package com.oddlabs.tt.font;

import com.oddlabs.util.Quad;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.resource.Resources;
import com.oddlabs.tt.resource.TextureFile;
import com.oddlabs.tt.render.Texture;
import com.oddlabs.util.FontInfo;

import org.lwjgl.opengl.GL11;

public final strictfp class Font {
	private final Quad[] key_array;
	private final Texture texture;
	private final int x_border;
	private final int y_border;
	private final int height;

	public Font(FontInfo font_info) {
		this.key_array = font_info.getKeyMap();
		TextureFile file = new TextureFile(font_info.getTextureName(),
										   GL11.GL_RGBA,
										   GL11.GL_LINEAR,
										   GL11.GL_LINEAR,
										   GL11.GL_REPEAT,
										   GL11.GL_REPEAT);
		this.texture = (Texture)Resources.findResource(file);
		this.x_border = font_info.getBorderX();
		this.y_border = font_info.getBorderY();
		this.height = font_info.getHeight();
	}

	public final Quad getQuad(char c) {
		return key_array[c];
	}

	public final void setup() {
		GL11.glEnd();
		setupQuads();
	}

	public final void setupQuads() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getHandle());
		GL11.glBegin(GL11.GL_QUADS);
	}

	public final void resetQuads() {
		GL11.glEnd();
	}

	public final void reset() {
		resetQuads();
		Skin.getSkin().bindTexture();
		GL11.glBegin(GL11.GL_QUADS);
	}

	public final int getXBorder() {
		return x_border;
	}

	public final int getYBorder() {
		return y_border;
	}

	public final int getHeight() {
		return height;
	}

	public final char getWidestChar(CharSequence text) {
		assert text.length() > 0: "Empty CharSequence";

		int widest = 0;
		char widest_char = ' ';
		for (int i = 0; i < text.length(); i++) {
			Quad quad = getQuad(text.charAt(i));
			if (quad != null) {
				int width = quad.getWidth() - x_border;
				if (widest < width) {
					widest = width;
					widest_char = text.charAt(i);
				}
			}
		}
		return widest_char;
	}

	public final int getWidth(CharSequence text) {
		if (text.length() == 0)
			return 0;
		int width = 0;
		for (int i = 0; i < text.length(); i++) {
			Quad quad = getQuad(text.charAt(i));
			if (quad != null)
				width += quad.getWidth() - x_border;
		}
		return width + x_border;
	}
}
