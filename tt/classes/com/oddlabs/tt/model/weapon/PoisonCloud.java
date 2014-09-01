package com.oddlabs.tt.model.weapon;

import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.particle.ParametricEmitter;

public final strictfp class PoisonCloud {
	private final static float START_SCALE = .1f;
	private final ParametricEmitter emitter;
	private final float src_x;
	private final float src_y;
	private final float dst_x;
	private final float dst_y;
	private final float dx;
	private final float dy;
	private float total_time;

	private float time;

	public PoisonCloud(ParametricEmitter emitter, float src_x, float src_y, float dst_x, float dst_y, float velocity) {
		this.emitter = emitter;
		this.src_x = src_x;
		this.src_y = src_y;
		this.dst_x = dst_x;
		this.dst_y = dst_y;

		dx = dst_x - src_x;
		dy = dst_y - src_y;
		float dist = (float)StrictMath.sqrt(dx*dx + dy*dy);
		total_time = dist/velocity;
		emitter.scale(START_SCALE, START_SCALE, START_SCALE);
	}

	public final void animate(float t) {
		time += t;
		float x;
		float y;
		if (time < total_time) {
			x = src_x + dx*(time/total_time);
			y = src_y + dy*(time/total_time);
		} else {
			x = dst_x;
			y = dst_y;
		}

		float z = emitter.getWorld().getHeightMap().getNearestHeight(x, y) + PoisonFog.OFFSET_Z;
		float factor = (float)StrictMath.min(1f, time/total_time);
		float scale = START_SCALE + (1f - START_SCALE)*factor;
		emitter.scale(scale, scale, scale);
		emitter.getPosition().set(x, y, z);
	}
}
