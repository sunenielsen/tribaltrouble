package com.oddlabs.tt.util;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.render.Texture;
import com.oddlabs.tt.resource.GLImage;
import com.oddlabs.tt.resource.GLIntImage;

public abstract strictfp class OffscreenRenderer {
	private final int width;
	private final int height;
	private final GLImage image;
	private final boolean use_copyteximage;

	public final int getWidth() {
		return width;
	}

	public final int getHeight() {
		return height;
	}

	protected OffscreenRenderer(int width, int height, boolean use_copyteximage) {
		this.width = width;
		this.height = height;
		this.use_copyteximage = use_copyteximage;
		
		if (!use_copyteximage)
			image = new GLIntImage(width, height, GL11.GL_RGBA);
		else
			image = null;
	}

	protected final void init() {
		Renderer.initGL();
		GL11.glViewport(0, 0, width, height);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}

	public final void dumpToFile(String filename) {
		GLIntImage image = new GLIntImage(width, height, GL11.GL_RGBA);
		GL11.glReadPixels(0, 0, image.getWidth(), image.getHeight(), image.getGLFormat(), image.getGLType(), image.getPixels());
		System.out.println("filename = " + filename);
		com.oddlabs.util.Utils.flip(image.getPixels(), image.getWidth()*4, image.getHeight());
		image.saveAsPNG(filename);
	}

	public final void copyToTexture(Texture tex, int mip_level, int format, int x0, int y0, int x1, int y1) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex.getHandle());
		assert x0 >= 0 && y0 >= 0 && x1 <= width && y1 <= height;
		if (use_copyteximage) {
			GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, mip_level, format, x0, y0, x1, y1, 0);
		} else {
			GL11.glReadPixels(x0, y0, x1, y1, image.getGLFormat(), image.getGLType(), image.getPixels());
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, mip_level, format, x1 - x0, y1 - y0, 0, image.getGLFormat(), image.getGLType(), image.getPixels());
		}
	}

	public abstract boolean isLost();

	public final boolean destroy() {
		boolean success = !isLost();
		finish();
		return success;
	}

	protected abstract void finish();

	protected static void pushGLState() {
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GLStateStack.pushState();
	}

	protected static void popGLState() {
		GLStateStack.popState();
		GL11.glPopAttrib();
		GL11.glGetError(); // FIXME: Swallow error because of bug in (at least) the r300 DRI drivers
	}
}
