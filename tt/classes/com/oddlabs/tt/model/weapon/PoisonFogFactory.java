package com.oddlabs.tt.model.weapon;

import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.landscape.World;

public final strictfp class PoisonFogFactory implements MagicFactory {
	private final float offset_x;
	private final float offset_y;
	private final float offset_z;
	private final float hit_radius;
	private final float hit_chance;
	private final float interval;
	private final float time;
	private final int damage;
	private final float seconds_per_anim;
	private final float init_ratio;
	private final float release_ratio;

	public PoisonFogFactory(float offset_x, float offset_y, float offset_z, float hit_radius, float hit_chance, float interval, float time, int damage, float seconds_per_anim, float init_ratio, float release_ratio) {
		this.offset_x = offset_x;
		this.offset_y = offset_y;
		this.offset_z = offset_z;
		this.hit_radius = hit_radius;
		this.hit_chance = hit_chance;
		this.interval = interval;
		this.time = time;
		this.damage = damage;
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
		return new PoisonFog(offset_x, offset_y, offset_z, hit_radius, hit_chance, interval, time, damage, src);
	}
}
