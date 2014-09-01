package com.oddlabs.tt.util;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.ARBMultitexture;
import org.lwjgl.opengl.ARBTextureCompression;

import java.nio.ByteBuffer;

public final strictfp class GLState implements Cloneable {
	public final static int VERTEX_ARRAY = 1 << 0;
	public final static int NORMAL_ARRAY = 1 << 1;
	public final static int TEXCOORD0_ARRAY = 1 << 2;
	public final static int TEXCOORD1_ARRAY = 1 << 3;
	public final static int COLOR_ARRAY = 1 << 4;

	/* state */
	private boolean vertex_array;
	private boolean normal_array;
	private boolean texcoord0_array;
	private boolean texcoord1_array;
	private boolean color_array;

	public final Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	private final void matchGLClientState(int gl_flag, boolean enable) {
		if (enable)
			GL11.glEnableClientState(gl_flag);
		else
			GL11.glDisableClientState(gl_flag);
	}
	
	public final static void activeTexture(int texture) {
		if (GLContext.getCapabilities().OpenGL13)
			GL13.glActiveTexture(texture);
		else
			ARBMultitexture.glActiveTextureARB(texture);
	}

	public final static void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, ByteBuffer pData) {
		if (GLContext.getCapabilities().OpenGL13)
			GL13.glCompressedTexImage2D(target, level, internalformat, width, height, border, pData);
		else
			ARBTextureCompression.glCompressedTexImage2DARB(target, level, internalformat, width, height, border, pData);
	}

	public final static void clientActiveTexture(int texture) {
		if (GLContext.getCapabilities().OpenGL13)
			GL13.glClientActiveTexture(texture);
		else
			ARBMultitexture.glClientActiveTextureARB(texture);
	}

	public final void switchState(int client_flags) {
		boolean target_vertex_array = (client_flags & VERTEX_ARRAY) != 0;
//		assert GLUtils.getGLInteger(GL13.GL_CLIENT_ACTIVE_TEXTURE) == GL13.GL_TEXTURE0;
		if (target_vertex_array != vertex_array) {
			matchGLClientState(GL11.GL_VERTEX_ARRAY, target_vertex_array);
			vertex_array = target_vertex_array;
		}
		boolean target_normal_array = (client_flags & NORMAL_ARRAY) != 0;
		if (target_normal_array != normal_array) {
			matchGLClientState(GL11.GL_NORMAL_ARRAY, target_normal_array);
			normal_array = target_normal_array;
		}
		boolean target_texcoord0_array = (client_flags & TEXCOORD0_ARRAY) != 0;
		if (target_texcoord0_array != texcoord0_array) {
			matchGLClientState(GL11.GL_TEXTURE_COORD_ARRAY, target_texcoord0_array);
			texcoord0_array = target_texcoord0_array;
		}
		boolean target_texcoord1_array = (client_flags & TEXCOORD1_ARRAY) != 0;
		if (target_texcoord1_array != texcoord1_array) {
			clientActiveTexture(GL13.GL_TEXTURE1);
			matchGLClientState(GL11.GL_TEXTURE_COORD_ARRAY, target_texcoord1_array);
			clientActiveTexture(GL13.GL_TEXTURE0);
			texcoord1_array = target_texcoord1_array;
		}
		boolean target_color_array = (client_flags & COLOR_ARRAY) != 0;
		if (target_color_array != color_array) {
			matchGLClientState(GL11.GL_COLOR_ARRAY, target_color_array);
			color_array = target_color_array;
		}
	}
}
