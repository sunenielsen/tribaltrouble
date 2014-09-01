package com.oddlabs.converter;

import java.io.*;

public final strictfp class AnimObjectInfo extends ObjectInfo {
	private final float wpc;
	private final int type;

	public AnimObjectInfo(File file, float wpc, int type) {
		super(file);
		this.wpc = wpc;
		this.type = type;
	}

	public final int getType() {
		return type;
	}

	public final float getWPC() {
		return wpc;
	}
}
