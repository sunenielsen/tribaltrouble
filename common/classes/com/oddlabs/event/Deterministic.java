package com.oddlabs.event;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

public abstract strictfp class Deterministic {
	protected final static int BUFFER_SIZE = 4096;
	protected final static int DEFAULTS_SIZE = 2;
	protected final static int MIN_DEFAULTS = Short.MIN_VALUE;

	private final static long CHECKPOINT_SIGNATURE = 0xdeadbabecafebeefL;
	
	private boolean enabled = true;

	public final boolean log(boolean b) {
		return log(b, false);
	}

	private final boolean log(boolean b, boolean def) {
		assert enabled;
		return log(b ? (byte)1 : (byte)0, def ? (byte)1 : (byte)0) != 0;
	}
	
	public final byte log(byte b) {
		assert enabled;
		return log(b, (byte)0);
	}
	
	protected abstract byte log(byte b, byte def);

	public final char log(char c) {
		assert enabled;
		return log(c, (char)0);
	}
	
	protected abstract char log(char c, char def);

	public final int log(int i) {
		assert enabled;
		return log(i, 0);
	}
	
	protected abstract int log(int i, int def);

	public final long log(long l) {
		assert enabled;
		return log(l, 0);
	}

	protected abstract long log(long l, long def);

	public final float log(float f) {
		assert enabled;
		return log(f, 0f);
	}

	protected abstract float log(float f, float def);
	
	public final Object log(Object o) {
		assert enabled;
		return logObject(o);
	}

	protected abstract Object logObject(Object o);

	public final void log(ByteBuffer o) {
		assert enabled;
		logBuffer(o);
	}

	protected abstract void logBuffer(ByteBuffer o);

	public abstract void endLog();

	public abstract boolean isPlayback();

	public final void setEnabled(boolean enable) {
		this.enabled = enable;
	}

	public final void checkpoint() {
		checkpoint(CHECKPOINT_SIGNATURE);
	}
	public final void checkpoint(long value) {
		long logged_value = log(value);
		assert logged_value == value: logged_value + " != " + value;
	//	assert logged_value == value: "0x" + Long.toHexString(logged_value) + " != 0x" + Long.toHexString(value);
	}

	public final void checkpoint(boolean value) {
		boolean logged_value = log(value);
		assert logged_value == value: logged_value + " != " + value;
	//	assert logged_value == value: "0x" + Long.toHexString(logged_value) + " != 0x" + Long.toHexString(value);
	}

	public final void checkpoint(float value) {
		float logged_value = log(value);
		assert logged_value == value: logged_value + " != " + value;
	//	assert logged_value == value: "0x" + Long.toHexString(logged_value) + " != 0x" + Long.toHexString(value);
	}

	protected final static int getTraceId() {
		Throwable t = new Throwable();
		StackTraceElement[] stack_trace_elements = t.getStackTrace();
		int hash = 0;
		for (int i = 0; i < stack_trace_elements.length; i++) {
			if (stack_trace_elements[i].getClassName().startsWith(Deterministic.class.getPackage().getName()))
				continue;
			hash += stack_trace_elements[i].getMethodName().hashCode();
		}
		return hash;
	}
}
