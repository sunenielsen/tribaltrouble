package com.oddlabs.tt.pathfinder;

import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.landscape.LandscapeTarget;

public final strictfp class FindTargetsFilter implements ScanFilter {
	private final int max_radius;
	private final Target[] result;
	private final boolean grid_targets_only;
	private int index;

	public FindTargetsFilter(int num_targets, int max_radius, boolean grid_targets_only) {
		result = new Target[num_targets];
		this.max_radius = max_radius;
		this.grid_targets_only = grid_targets_only;
		index = 0;
	}

	public final int getMinRadius() {
		return 0;
	}

	public final int getMaxRadius() {
		return max_radius;
	}

	public final boolean filter(int grid_x, int grid_y, Occupant occupant) {
		if ((!grid_targets_only || ((grid_x+grid_y)&1) == 0) && occupant == null) {
			result[index] = new LandscapeTarget(grid_x, grid_y);
			index++;
		}
		return index == result.length;
	}

	public final Target[] getTargets() {
		return result;
	}
}
