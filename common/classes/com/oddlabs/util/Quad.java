package com.oddlabs.util;

import java.io.*;
import org.lwjgl.opengl.*;

public strictfp class Quad implements Serializable {
	private final static long serialVersionUID = 1;

	private final float u1;
	private final float v1;
	private final float u2;
	private final float v2;
	private final int width;
	private final int height;

	public Quad(float u1, float v1, float u2, float v2, int width, int height) {
		this.u1 = u1;
		this.v1 = v1;
		this.u2 = u2;
		this.v2 = v2;
		this.width = width;
		this.height = height;
	}

	public void renderClipped(float x, float y, float clip_left, float clip_right, float clip_bottom, float clip_top) {
		float x1 = x;
		float x2 = x + width;
		float y1 = y;
		float y2 = y + height;
		float cleft_amount = StrictMath.max(0, clip_left - x1);
		float cright_amount = StrictMath.max(0, x2 - clip_right);
		float cbottom_amount = StrictMath.max(0, clip_bottom - y1);
		float ctop_amount = StrictMath.max(0, y2 - clip_top);
		if (ctop_amount + cbottom_amount >= height || cright_amount + cleft_amount >= width)
			return;
		x1 += cleft_amount;
		x2 -= cright_amount;
		y1 += cbottom_amount;
		y2 -= ctop_amount;
		float tex_width_scale = (u2 - u1)/width;
		float tex_height_scale = (v2 - v1)/height;
		float clipped_u1 = u1 + cleft_amount*tex_width_scale;
		float clipped_u2 = u2 - cright_amount*tex_width_scale;
		float clipped_v1 = v1 + cbottom_amount*tex_height_scale;
		float clipped_v2 = v2 - ctop_amount*tex_height_scale;
/*if (x1 != x || x2 != x + width || y1 != y || y2 != y + height)
System.out.println("x = " + x + " | y = " + y + " | clip_left = " + clip_left + " | clip_right = " + clip_right);*/
		render(x1, y1, x2, y1, x2, y2, x1, y2, clipped_u1, clipped_u2, clipped_v1, clipped_v2);
	}

	public final void render(float x, float y) {
		render(x, y, width, height);
	}

	public void render(float x, float y, int width, int height) {
		render(x, y, x + width, y, x + width, y + height, x, y + height);
	}

	public void render(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		render(x1, y1, x2, y2, x3, y3, x4, y4, u1, u2, v1, v2);
	}

	public static void render(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4,
			float u1, float u2, float v1, float v2) {
		GL11.glTexCoord2f(u1, v1);
		GL11.glVertex3f(x1, y1, 0);
		GL11.glTexCoord2f(u2, v1);
		GL11.glVertex3f(x2, y2, 0);
		GL11.glTexCoord2f(u2, v2);
		GL11.glVertex3f(x3, y3, 0);
		GL11.glTexCoord2f(u1, v2);
		GL11.glVertex3f(x4, y4, 0);
	}

	public final int getWidth() {
		return width;
	}

	public final int getHeight() {
		return height;
	}

	public final String toString() {
		return "u1 = " + u1 + " | v1 = " + v1 + " | u2 = " + u2 + " | v2 = " + v2 + " | width = " + width + " | height = " + height;
	}
}
