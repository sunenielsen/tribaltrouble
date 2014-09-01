package com.oddlabs.tt.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.landscape.TreeSupply;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.util.StateChecksum;

public strictfp class SupplyManager implements Animated {
	private final static float SLEEP_TIME = 10f;
	private final static float SPAWN_TIME = 3f;
	private final static float MAX_EMPTY_SUPPLIES = .75f;
	
	private final List empty_supplies = new ArrayList();
	private final World world;

	private int total_num_supplies = 0;
	private float time;

	
	public SupplyManager(World world) {
		this.world = world;
		world.getAnimationManagerGameTime().registerAnimation(this);
		resetCounter();
	}

	protected final World getWorld() {
		return world;
	}

	private final void resetCounter() {
		time = getSleepTime();
	}

	protected float getSleepTime() {
		return SLEEP_TIME;
	}

	public final void debugSpawnSupply() {
		if (empty_supplies.size() > 0)
			insertSupply();
	}

	public final void newSupply() {
		total_num_supplies++;
	}

	public final void emptySupply(Supply supply) {
		empty_supplies.add(supply);
	}

	public final void animate(float t) {
		if (time < 0) {
			resetCounter();
			if (shouldSpawn())
				insertSupply();
		}
		time -= t;
	}

	protected boolean shouldSpawn() {
		return (int)(total_num_supplies*MAX_EMPTY_SUPPLIES) < empty_supplies.size();
	}

	public void updateChecksum(StateChecksum checksum) {
	}

	protected void insertSupply() {
		int index = world.getRandom().nextInt(empty_supplies.size());
		Supply supply = (Supply)empty_supplies.get(index);
		boolean occupied = world.getUnitGrid().isGridOccupied(supply.getGridX(), supply.getGridY());
		if (!occupied) {
			empty_supplies.remove(supply);
			Supply new_supply = supply.respawn();
			new SupplySpawnAnimation(new_supply, SPAWN_TIME);
		}
	}
}
