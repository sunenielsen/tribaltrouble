package com.oddlabs.tt.model.weapon;

import com.oddlabs.tt.model.Unit;

public final strictfp class StunFactory implements MagicFactory {
	private final float offset_x;
	private final float offset_y;
	private final float offset_z;
	private final float hit_radius;
	private final float stun_time_closest;
	private final float stun_time_farthest;
	private final float seconds_per_anim;
	private final float init_ratio;
	private final float release_ratio;

	public StunFactory(float offset_x, float offset_y, float offset_z, float hit_radius, float stun_time_closest, float stun_time_farthest, float seconds_per_anim, float init_ratio, float release_ratio) {
		this.offset_x = offset_x;
		this.offset_y = offset_y;
		this.offset_z = offset_z;
		this.hit_radius = hit_radius;
		this.stun_time_closest = stun_time_closest;
		this.stun_time_farthest = stun_time_farthest;
		this.seconds_per_anim = seconds_per_anim;
		this.init_ratio = init_ratio;
		this.release_ratio = release_ratio;
	}

	public final float getHitRadius() {
		return hit_radius;
	}

	public final float getSecondsPerAnim() {
		return seconds_per_anim;
	}

	public final float getSecondsPerRelease() {
		return release_ratio*seconds_per_anim;
	}

	public final float getSecondsPerInit() {
		return init_ratio*seconds_per_anim;
	}

	public final Magic execute(Unit src) {
		return new Stun(offset_x, offset_y, offset_z, hit_radius, stun_time_closest, stun_time_farthest, src);
	}
}
