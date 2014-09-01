package com.oddlabs.tt.util;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.render.Texture;
import com.oddlabs.tt.resource.GLImage;
import com.oddlabs.tt.resource.GLIntImage;

public final strictfp class BackBufferRenderer extends OffscreenRenderer {
	private static boolean back_buffer_dirty = false;

	public final static boolean isBackBufferDirty() {
		boolean result = back_buffer_dirty;
		back_buffer_dirty = false;
		return result;
	}

	protected BackBufferRenderer(int width, int height, boolean use_copyteximage) {
		super(width, height, use_copyteximage);
		pushGLState();
		back_buffer_dirty = true;
		Display.isDirty();
		init();
	}

	public final boolean isLost() {
		return Display.isDirty();
	}

	protected final void finish() {
		popGLState();
	}
}
