package com.oddlabs.tt.model;

import com.oddlabs.tt.audio.Audio;
import com.oddlabs.tt.render.SpriteKey;
import com.oddlabs.tt.model.weapon.WeaponFactory;
import com.oddlabs.tt.render.ShadowListKey;

public final strictfp class UnitTemplate extends Template {
	private final float meters_per_second;
	private final WeaponFactory weapon_factory;
	private final SpriteKey sprite_renderer;
	private final UnitSupplyContainerFactory supply_container_factory;
	private final Audio death_sound;
	private final float death_pitch;
	private final float selection_radius;
	private final float selection_height;
	private final int max_hit_points;
	private final float stun_x;
	private final float stun_y;
	private final float stun_z;
	private final int status_value;

	public UnitTemplate(float selection_radius,
			float selection_height,
			Abilities abilities,
			float meters_per_second,
			WeaponFactory weapon_factory,
			SpriteKey sprite_renderer,
			float shadow_diameter,
			ShadowListKey shadow_renderer,
			UnitSupplyContainerFactory supply_container_factory,
			Audio death_sound,
			float death_pitch,
			float[] hit_offset_z,
			float no_detail_size,
			float defense_chance,
			String name,
			int max_hit_points,
			float stun_x,
			float stun_y,
			float stun_z,
			int status_value) {
		super(abilities, shadow_diameter, shadow_renderer,  hit_offset_z, no_detail_size, defense_chance, name);
		this.selection_radius = selection_radius;
		this.selection_height = selection_height;
		this.meters_per_second = meters_per_second;
		this.weapon_factory = weapon_factory;
		this.sprite_renderer = sprite_renderer;
		this.supply_container_factory = supply_container_factory;
		this.death_sound = death_sound;
		this.death_pitch = death_pitch;
		this.max_hit_points = max_hit_points;
		this.stun_x = stun_x;
		this.stun_y = stun_y;
		this.stun_z = stun_z;
		this.status_value = status_value;
	}

	public final float getSelectionRadius() {
		return selection_radius;
	}

	public final float getSelectionHeight() {
		return selection_height;
	}

	public final float getMetersPerSecond() {
		return meters_per_second;
	}

	public final WeaponFactory getWeaponFactory() {
		return weapon_factory;
	}

	public final SpriteKey getSpriteRenderer() {
		return sprite_renderer;
	}

	public final UnitSupplyContainerFactory getUnitSupplyContainerFactory() {
		return supply_container_factory;
	}

	public final Audio getDeathSound() {
		return death_sound;
	}

	public final float getDeathPitch() {
		return death_pitch;
	}

	public final int getMaxHitPoints() {
		return max_hit_points;
	}

	public final float getStunX() {
		return stun_x;
	}

	public final float getStunY() {
		return stun_y;
	}

	public final float getStunZ() {
		return stun_z;
	}

	public final int getStatusValue() {
		return status_value;
	}
}
