package com.oddlabs.tt.model.weapon;

import com.oddlabs.tt.landscape.HeightMap;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.player.campaign.Campaign;
import com.oddlabs.tt.player.campaign.CampaignState;
import com.oddlabs.tt.util.Target;

public abstract strictfp class WeaponFactory {
	private final static float TERRAIN_MAX_BONUS = .25f;
	private final static float TERRAIN_BONUS_PER_HEIGHT = TERRAIN_MAX_BONUS/20f;

	private final float hit_chance;
	private final float range;
	private final float release_ratio;

	protected WeaponFactory(float hit_chance, float range, float release_ratio) {
		this.hit_chance = hit_chance;
		this.range = range;
		this.release_ratio = release_ratio;
	}

	public final float getSecondsPerRelease(float anim_per_second) {
		return release_ratio/anim_per_second;
	}

	public final float getRange() {
		return range;
	}

	private final static float computeTerrainBonus(HeightMap heightmap, Target src, Target dst) {
		float src_z = heightmap.getNearestHeight(src.getPositionX(), src.getPositionY());
		float dst_z = heightmap.getNearestHeight(dst.getPositionX(), dst.getPositionY());
		float bonus = (src_z - dst_z)*TERRAIN_BONUS_PER_HEIGHT;
		bonus = StrictMath.min(TERRAIN_MAX_BONUS, StrictMath.max(-TERRAIN_MAX_BONUS, bonus));
		return bonus;
	}

	public final void attack(Unit src, Selectable target, float factor) {
		/* GAMEPLAY: Terrain bonus, according to who is positioned highest */
		float terrain_bonus = computeTerrainBonus(src.getOwner().getWorld().getHeightMap(), src, target);
		float difficulty_bonus = src.getOwner().getHitBonus();
		boolean hit = target.getOwner().getWorld().getRandom().nextFloat() < factor*(difficulty_bonus + terrain_bonus + hit_chance)*(1 - target.getDefenseChance());
		doAttack(hit, src, target);
	}

	public final void attack(Unit src, Selectable target) {
		attack(src, target, 1f);
	}

	protected abstract void doAttack(boolean hit, Unit src, Selectable target);

	public abstract Class getType();
}
