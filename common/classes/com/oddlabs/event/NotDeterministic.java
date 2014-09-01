package com.oddlabs.event;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

public final strictfp class NotDeterministic extends Deterministic {
	public final boolean isPlayback() {
		return false;
	}

	public final void endLog() {
	}

	protected final byte log(byte b, byte def) {
		return b;
	}

	protected final char log(char c, char def) {
		return c;
	}

	protected final int log(int i, int def) {
		return i;
	}

	protected final long log(long l, long def) {
		return l;
	}

	protected final float log(float f, float def) {
		return f;
	}
	
	protected final Object logObject(Object o) {
		return o;
	}
	
	protected final void logBuffer(ByteBuffer b) {
		b.position(b.limit());
	}
}
