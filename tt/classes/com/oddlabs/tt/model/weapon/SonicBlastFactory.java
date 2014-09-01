package com.oddlabs.tt.model.weapon;

import com.oddlabs.tt.model.Unit;

public final strictfp class SonicBlastFactory implements MagicFactory {
	private final float offset_x;
	private final float offset_y;
	private final float offset_z;
	private final float hit_radius;
	private final float hit_chance_closest;
	private final float hit_chance_farthest;
	private final int damage_closest;
	private final int damage_farthest;
	private final float seconds;
	private final float seconds_per_anim;
	private final float init_ratio;
	private final float release_ratio;

	public SonicBlastFactory(float offset_x, float offset_y, float offset_z, float hit_radius, float hit_chance_closest, float hit_chance_farthest, int damage_closest, int damage_farthest, float seconds, float seconds_per_anim, float init_ratio, float release_ratio) {
		this.offset_x = offset_x;
		this.offset_y = offset_y;
		this.offset_z = offset_z;
		this.hit_radius = hit_radius;
		this.hit_chance_closest = hit_chance_closest;
		this.hit_chance_farthest = hit_chance_farthest;
		this.damage_closest = damage_closest;
		this.damage_farthest = damage_farthest;
		this.seconds = seconds;
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

	public final float getSecondsPerInit() {
		return init_ratio*seconds_per_anim;
	}

	public final float getSecondsPerRelease() {
		return release_ratio*seconds_per_anim;
	}

	public final Magic execute(Unit src) {
		return new SonicBlast(offset_x, offset_y, offset_z, hit_radius, hit_chance_closest, hit_chance_farthest, damage_closest, damage_farthest, seconds, src);
	}
}
