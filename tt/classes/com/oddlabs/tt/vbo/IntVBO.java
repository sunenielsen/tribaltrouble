package com.oddlabs.tt.vbo;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.*;

import com.oddlabs.tt.global.*;
import com.oddlabs.tt.util.*;
import com.oddlabs.tt.render.Renderer;

public final strictfp class IntVBO extends VBO {
	private IntBuffer saved_buffer = null;
//	private IntBuffer mapped_buffer = null;

	public IntVBO(int usage, int size) {
		super(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, usage, size<<2);
		ByteBuffer buffer = getSavedBuffer();
		if (buffer != null)
			saved_buffer = buffer.asIntBuffer();
	}

	public IntVBO(int usage, IntBuffer initial_data) {
		this(usage, initial_data.remaining());
		put(initial_data);
	}

	public IntVBO(int usage, int[] initial_data) {
		this(usage, initial_data.length);
		put(initial_data);
	}

/*	public final void map(int access) {
		if (!doMap(access))
			saved_buffer = getMappedBuffer().asIntBuffer();
		mapped_buffer = saved_buffer;
	}

	public final boolean unmap() {
		saved_buffer.clear();
		mapped_buffer = null;
		return doUnmap();
	}

	public final IntBuffer buffer() {
		return mapped_buffer;
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

	public final void vertexPointer(int size, int stride, int index) {
		if (!use_vbo) {
			saved_buffer.position(index);
			GL11.glVertexPointer(size, stride, saved_buffer);
		} else {
			makeCurrent();
			GL11.glVertexPointer(size, GL11.GL_FLOAT, stride, index<<2);
		}
	}

	public final void texCoordPointer(int size, int stride, int index) {
		if (!use_vbo) {
			saved_buffer.position(index);
			GL11.glTexCoordPointer(size, stride, saved_buffer);
		} else {
			makeCurrent();
			GL11.glTexCoordPointer(size, GL11.GL_FLOAT, stride, index<<2);
		}
	}

	public final void normalPointer(int stride, int index) {
		if (!use_vbo) {
			saved_buffer.position(index);
			GL11.glNormalPointer(stride, saved_buffer);
		} else {
			makeCurrent();
			GL11.glNormalPointer(GL11.GL_FLOAT, stride, index<<2);
		}
	}

	public final void put(IntBuffer buffer) {
		putSubData(0, buffer);
	}
	
	public final void put(int[] buffer) {
		putSubData(0, Utils.toBuffer(buffer));
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

	public final void putSubData(int index, IntBuffer buffer) {
		if (!use_vbo) {
			saved_buffer.position(index);
			saved_buffer.put(buffer);
		} else {
			makeCurrent();
			ARBBufferObject.glBufferSubDataARB(getTarget(), index<<2, buffer);
			buffer.position(buffer.limit());
		}
	}

	public final int capacity() {
	/* java.lang.AssertionError: end index 5940 != num coords 11880
	 *         at com.oddlabs.tt.render.TreeLowDetail.build(TreeLowDetail.java:90)
	 *
	 *	return getSize()>>1;
	 */
		return getSize()>>2;
	}
}
