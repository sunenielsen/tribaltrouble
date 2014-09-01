package com.oddlabs.tt.model.weapon;

import com.oddlabs.tt.model.Unit;

public final strictfp class LightningCloudFactory implements MagicFactory {
	private final float offset_x;
	private final float offset_y;
	private final float offset_z;
	private final float seconds_to_live;
	private final float seconds_per_hit;
	private final float meters_per_second;
	private final float hit_chance;
	private final int damage;
	private final float height;
	private final float seconds_per_anim;
	private final float init_ratio;
	private final float release_ratio;

	public LightningCloudFactory(float offset_x, float offset_y, float offset_z, float seconds_to_live, float seconds_per_hit, float meters_per_second, float hit_chance, int damage, float height, float seconds_per_anim, float init_ratio, float release_ratio) {
		this.offset_x = offset_x;
		this.offset_y = offset_y;
		this.offset_z = offset_z;
		this.seconds_to_live = seconds_to_live;
		this.seconds_per_hit = seconds_per_hit;
		this.meters_per_second = meters_per_second;
		this.hit_chance = hit_chance;
		this.damage = damage;
		this.height = height;
		this.seconds_per_anim = seconds_per_anim;
		this.init_ratio = init_ratio;
		this.release_ratio = release_ratio;
	}

	public final float getHitRadius() {
		return 0;
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
		float seconds_to_init = getSecondsPerRelease() - getSecondsPerInit();
		return new LightningCloud(src.getOwner().getWorld(), offset_x, offset_y, offset_z, seconds_to_live, seconds_per_hit, seconds_to_init, meters_per_second, hit_chance, damage, height, src);
	}
}
