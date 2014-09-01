package com.oddlabs.tt.vbo;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.ARBVertexBufferObject;

import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.resource.NativeResource;

public abstract strictfp class VBO extends NativeResource {
	private final int handle;
	private final int target;
	private final int size;
	protected final boolean use_vbo;
	private final static IntBuffer handle_buffer;

	private ByteBuffer saved_buffer;
//	private ByteBuffer mapped_buffer;

	static {
		handle_buffer = BufferUtils.createIntBuffer(1);
	}

	private final int createBuffer(int target, int usage, int size) {
		ARBBufferObject.glGenBuffersARB(handle_buffer);
		int handle = handle_buffer.get(0);
		assert handle != 0;
		makeCurrent(target, handle);
		ARBBufferObject.glBufferDataARB(target, size, usage);
		return handle;
	}

	private final static void makeCurrent(int target, int handle) {
		ARBBufferObject.glBindBufferARB(target, handle);
	}

	public final static void releaseAll() {
		if (Settings.getSettings().useVBO()) {
			makeCurrent(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
		}
		releaseIndexVBO();
	}

	public final static void releaseIndexVBO() {
		if (Settings.getSettings().useVBO()) {
			makeCurrent(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, 0);
		}
	}

	protected final void makeCurrent() {
		assert use_vbo;
		makeCurrent(target, handle);
	}

	public VBO(int target, int usage, int size) {
		this.use_vbo = Settings.getSettings().useVBO();
		this.target = target;
		this.size = size;
		//		mapped_buffer = null;
		if (use_vbo) {
			handle = createBuffer(target, usage, size);
			saved_buffer = null;
		} else {
			handle = 0;
			saved_buffer = BufferUtils.createByteBuffer(size);
		}
	}

	protected final void doDelete() {
		if (use_vbo) {
			handle_buffer.put(0, handle);
			ARBBufferObject.glDeleteBuffersARB(handle_buffer);
		}
	}

	protected final int getTarget() {
		return target;
	}

/*	protected final boolean doMap(int access) {
		assert mapped_buffer == null;
		if (use_vbo) {
			makeCurrent();
			mapped_buffer = ARBBufferObject.glMapBufferARB(target, access, size, saved_buffer);
			assert mapped_buffer != null;
			mapped_buffer.order(ByteOrder.nativeOrder());
			boolean result = mapped_buffer == saved_buffer;
			saved_buffer = mapped_buffer;
			return result;
		} else {
			mapped_buffer = saved_buffer;
			boolean result = !first_time;
			first_time = false;
			return result;
		}
	}

	protected final boolean doUnmap() {
		assert mapped_buffer != null;
		mapped_buffer.clear();
		mapped_buffer = null;
		if (use_vbo) {
			makeCurrent();
			return ARBBufferObject.glUnmapBufferARB(target);
		} else
			return true;
	}
*/
	protected final ByteBuffer getSavedBuffer() {
		return saved_buffer;
	}
/*
	protected final ByteBuffer getMappedBuffer() {
		return mapped_buffer;
	}
*/
	protected final int getSize() {
		return size;
	}
	
	public abstract int capacity();
}
