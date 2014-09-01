package com.oddlabs.tt.model;

import com.oddlabs.tt.pathfinder.Occupant;
import com.oddlabs.tt.pathfinder.ScanFilter;
import com.oddlabs.tt.player.Player;

public final strictfp class AttackScanFilter implements ScanFilter {
	public final static int PRIORITY_QUARTERS = 1;
	public final static int PRIORITY_ARMORY = 1;
	public final static int PRIORITY_TOWER = 2;
	public final static int PRIORITY_PEON = 3;
	public final static int PRIORITY_WARRIOR = 4;
	
	public final static int UNIT_RANGE = 8;
	public final static int TOWER_RANGE = (int)(RacesResources.THROW_RANGE + MountUnitContainer.ATTACK_RANGE_INCREASE);
	
	private final int max_range;
	
	private final Player owner;
	
	private Selectable target = null;
	private int target_priority = 0;

	public AttackScanFilter(Player owner, int max_range) {
		this.owner = owner;
		this.max_range = max_range;
	}

	public final Selectable removeTarget() {
		Selectable result = target;
		target = null;
		target_priority = 0;
		return result;
	}

	public final int getMinRadius() {
		return 1;
	}

	public final int getMaxRadius() {
		return max_range;
	}

	public final boolean filter(int grid_x, int grid_y, Occupant occ) {
		if (occ instanceof Selectable) {
			Selectable s = (Selectable)occ;
			if (owner.isEnemy(s.getOwner())) {
				int priority = s.getAttackPriority();
				if (target_priority < priority) {
					target_priority = priority;
					target = s;
				}
			}
		}
		return false;
	}
}
