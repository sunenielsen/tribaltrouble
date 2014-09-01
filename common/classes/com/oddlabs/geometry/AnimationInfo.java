package com.oddlabs.geometry;

import java.io.Serializable;

public final strictfp class AnimationInfo implements Serializable {
	private final static long serialVersionUID = 1;

	public final static int ANIM_LOOP = 1;
	public final static int ANIM_PLAIN = 2;

	private final float[][] frames;
	private final int type;
	private final float wpc;

	public AnimationInfo(float[][] frames, int type, float wpc) {
		this.frames = frames;
		this.type = type;
		this.wpc = wpc;
	}

	public final float[][] getFrames() {
		return frames;
	}

	public final int getType() {
		return type;
	}

	public final float getWPC() {
		return wpc;
	}
}
