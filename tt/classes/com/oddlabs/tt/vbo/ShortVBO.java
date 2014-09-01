package com.oddlabs.tt.vbo;

import com.oddlabs.tt.global.*;
import com.oddlabs.tt.util.*;
import com.oddlabs.tt.render.Renderer;

import java.nio.*;

import org.lwjgl.opengl.*;

public final strictfp class ShortVBO extends VBO {
	private ShortBuffer saved_buffer = null;
//	private ShortBuffer mapped_buffer = null;

	public ShortVBO(int usage, int size) {
		super(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, usage, size<<1);
		ByteBuffer buffer = getSavedBuffer();
		if (buffer != null)
			saved_buffer = buffer.asShortBuffer();
	}

	public ShortVBO(int usage, ShortBuffer initial_data) {
		this(usage, initial_data.remaining());
		put(initial_data);
	}

/*	public final void map(int access) {
		if (!doMap(access))
			saved_buffer = getMappedBuffer().asShortBuffer();
		mapped_buffer = saved_buffer;
	}

	public final ShortBuffer buffer() {
		return mapped_buffer;
	}

	public final boolean unmap() {
		saved_buffer.clear();
		mapped_buffer = null;
		return doUnmap();
	}
*/

	private static void registerTrianglesRendered(int mode, int count) {
		int num_triangles = getNumTriangles(mode, count);
		Renderer.registerTrianglesRendered(num_triangles);
	}

	private static int getNumTriangles(int mode, int count) {
		switch (mode) {
			case GL11.GL_TRIANGLES:
				return count/3;
			case GL11.GL_QUADS:
				return count >> 2;
			case GL11.GL_TRIANGLE_FAN:
			case GL11.GL_TRIANGLE_STRIP:
				return count - 2;
			case GL11.GL_QUAD_STRIP:
				return count - 3;
			case GL11.GL_LINES:
				return count; // Assume a line is two triangles
			case GL11.GL_POINTS:
				return count*3; // assume a line is one triangle;
			case GL11.GL_LINE_STRIP:
				return (count - 1)*2;
			default:
				throw new RuntimeException("Unknown primitive type: 0x" + Integer.toHexString(mode));
		}
	}

	public final void drawRangeElements(int mode, int start, int end, int count, int index) {
		registerTrianglesRendered(mode, count);
		if (!use_vbo) {
			saved_buffer.position(index);
			saved_buffer.limit(index + count);
			if (GLContext.getCapabilities().OpenGL12)
				GL12.glDrawRangeElements(mode, start, end, saved_buffer);
			else
				GL11.glDrawElements(mode, saved_buffer);
			saved_buffer.clear();
		} else {
			makeCurrent();
			if (Settings.getSettings().use_vbo_draw_range_elements && GLContext.getCapabilities().OpenGL12)
				GL12.glDrawRangeElements(mode, start, end, count, GL11.GL_UNSIGNED_SHORT, index<<1);
			else
				GL11.glDrawElements(mode, count, GL11.GL_UNSIGNED_SHORT, index<<1);
		}
	}

	public final void put(ShortBuffer buffer) {
		if (!use_vbo) {
//			saved_buffer.position(index);
			saved_buffer.put(buffer);
		} else {
			makeCurrent();
			ARBBufferObject.glBufferSubDataARB(getTarget(), 0, buffer);
			buffer.position(buffer.limit());
		}
//		do {
//			map(ARBBufferObject.GL_WRITE_ONLY_ARB);
//			buffer().put(buffer);
//			buffer.clear();
//		} while (!unmap());
	}

	public final void put(short[] buffer) {
		put(Utils.toBuffer(buffer));
//		do {
//			map(ARBBufferObject.GL_WRITE_ONLY_ARB);
//			buffer().put(buffer);
//		} while (!unmap());
	}

	public final void drawElements(int mode, int count, int index) {
		registerTrianglesRendered(mode, count);
		if (!use_vbo) {
			saved_buffer.position(index);
			saved_buffer.limit(index + count);
			GL11.glDrawElements(mode, saved_buffer);
			saved_buffer.clear();
		} else {
			makeCurrent();
			GL11.glDrawElements(mode, count, GL11.GL_UNSIGNED_SHORT, index<<1);
		}
	}

	public final int capacity() {
		return getSize()>>1;
	}
}
