package com.oddlabs.tt.util;

import org.lwjgl.LWJGLException;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.opengl.EXTFramebufferObject;

import java.nio.IntBuffer;

import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.render.Texture;
import com.oddlabs.tt.resource.GLImage;
import com.oddlabs.tt.resource.GLIntImage;

public final strictfp class FramebufferRenderer extends OffscreenRenderer {
	private final int fb_id;
	private final int rb_id;

	protected FramebufferRenderer(int width, int height, boolean has_alpha, boolean use_copyteximage) throws Exception {
		super(width, height, use_copyteximage);
		pushGLState();
		IntBuffer tmp = BufferUtils.createIntBuffer(4);
		EXTFramebufferObject.glGenFramebuffersEXT(tmp);
		fb_id = tmp.get(0);
		assert fb_id != 0;
		EXTFramebufferObject.glGenRenderbuffersEXT(tmp);
		rb_id = tmp.get(0);
		assert rb_id != 0;
		int internal_format = has_alpha ? GL11.GL_RGBA8 : GL11.GL_RGB8;
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fb_id);
		EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, rb_id);
		EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, internal_format, width, height);
		EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
				EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, rb_id);
		int status = EXTFramebufferObject.glCheckFramebufferStatusEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT);
		if (status != EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT) {
			assert status == EXTFramebufferObject.GL_FRAMEBUFFER_UNSUPPORTED_EXT: status;
			deleteBuffers();
			throw new Exception("Failed to setup FBO");
		}
		GL11.glDrawBuffer(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT);
		GL11.glReadBuffer(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT);
		Renderer.dumpWindowInfo();
		init();
	}

	public final boolean isLost() {
		return false;
	}

	private void deleteBuffers() {
		IntBuffer tmp = BufferUtils.createIntBuffer(4);
		tmp.put(0, fb_id);
		EXTFramebufferObject.glDeleteFramebuffersEXT(tmp);
		tmp.put(0, rb_id);
		EXTFramebufferObject.glDeleteRenderbuffersEXT(tmp);
	}

	protected final void finish() {
		deleteBuffers();
		popGLState();
	}
}
