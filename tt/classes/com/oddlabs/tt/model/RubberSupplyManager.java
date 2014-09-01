package com.oddlabs.tt.model;

import com.oddlabs.tt.landscape.World;

public final strictfp class RubberSupplyManager extends SupplyManager {
	private final static float SLEEP_TICKS = 60;
	private final static int MAX_NUM_GROUPS = 3;

	private int current_groups = 0;
	
	public RubberSupplyManager(World world) {
		super(world);
	}

	protected float getSleepTime() {
		return SLEEP_TICKS;
	}

	protected final boolean shouldSpawn() {
		return current_groups < MAX_NUM_GROUPS;
	}

	protected final void insertSupply() {
		new RubberGroup(getWorld());
	}

	public final void newGroup() {
		current_groups++;
	}

	public final void emptyGroup() {
		current_groups--;
	}
}
