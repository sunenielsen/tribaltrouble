package com.oddlabs.tt.util;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.render.Texture;

final strictfp class PbufferRenderer extends OffscreenRenderer {
	private final Pbuffer pbuffer;
	private final OffscreenRendererFactory factory;

	PbufferRenderer(int width, int height, PixelFormat format, boolean use_copyteximage, OffscreenRendererFactory factory) throws LWJGLException {
		super(width, height, use_copyteximage);
		this.factory = factory;
		pbuffer = new Pbuffer(width, height, format, null, null);
		GLStateStack state_stack = new GLStateStack();
		pbuffer.makeCurrent();
		GLStateStack.setCurrent(state_stack);
		try {
			pbuffer.makeCurrent();
			Renderer.dumpWindowInfo();
			init();
			if (!GLUtils.getGLBoolean(GL11.GL_DOUBLEBUFFER)) {
				GL11.glReadBuffer(GL11.GL_FRONT);
				GL11.glDrawBuffer(GL11.GL_FRONT);
			}
		} catch (LWJGLException e) {
			pbuffer.destroy();
			throw e;
		}
	}

	public final boolean isLost() {
		boolean is_lost = pbuffer.isBufferLost();
		if (is_lost)
			factory.pbufferFailed();
		return is_lost;
	}

	protected final void finish() {
		pbuffer.destroy();
		Renderer.makeCurrent();
	}
}
