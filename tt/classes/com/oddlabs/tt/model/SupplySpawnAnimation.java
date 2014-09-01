package com.oddlabs.tt.model;

import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.util.StateChecksum;


public strictfp class SupplySpawnAnimation implements Animated {
	private final Supply supply;
	private final float limit;

	private float time = 0;
	
	public SupplySpawnAnimation(Supply supply, float limit) {
		this.supply = supply;
		this.limit = limit;
		supply.getWorld().getAnimationManagerGameTime().registerAnimation(this);
		supply.animateSpawn(0, 0);
	}

	public final void animate(float t) {
		time = StrictMath.min(time + t, limit);
		supply.animateSpawn(t, time/limit);
		if (time >= limit) {
			supply.getWorld().getAnimationManagerGameTime().removeAnimation(this);
			supply.spawnComplete();
		}
	}

	public void updateChecksum(StateChecksum checksum) {
	}
}
