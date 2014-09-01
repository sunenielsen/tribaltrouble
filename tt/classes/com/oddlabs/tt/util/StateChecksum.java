package com.oddlabs.tt.util;

import java.util.zip.*;

public final strictfp class StateChecksum {
	private final Checksum crc = new Adler32();

	public final void update(long l) {
		int i0 = (int)(l & 0xffffffff);
		int i1 = (int)((l >> 32) & 0xffffffff);
		update(i0);
		update(i1);
//		com.oddlabs.tt.util.ChecksumLogger.log(l);
	}

	public final void update(int i) {
		int b0 = (i >> 24) & 0xff;
		int b1 = (i >> 16) & 0xff;
		int b2 = (i >> 8) & 0xff;
		int b3 = i & 0xff;
		crc.update(b0);
		crc.update(b1);
		crc.update(b2);
		crc.update(b3);
//		com.oddlabs.tt.util.ChecksumLogger.log(i);
	}

	public final void update(float f) {
		int f_int = Float.floatToRawIntBits(f);
		update(f_int);
//		com.oddlabs.tt.util.ChecksumLogger.log(f);
	}

	public final int getValue() {
		return (int)crc.getValue();
	}
}
