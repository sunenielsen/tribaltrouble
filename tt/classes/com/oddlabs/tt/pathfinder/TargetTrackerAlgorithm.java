package com.oddlabs.tt.pathfinder;

import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.util.Target;

public final strictfp class TargetTrackerAlgorithm implements TrackerAlgorithm {
	private final UnitGrid unit_grid;
	private final Target target;
	private final float max_dist;

	public TargetTrackerAlgorithm(UnitGrid unit_grid, float max_dist, Target target) {
		this.unit_grid = unit_grid;
		this.max_dist = max_dist;
		this.target = target;
	}
	
	public final boolean isDone(int x, int y) {
		return target.isDead() || Selectable.isCloseEnough(unit_grid, max_dist, x, y, target);
	}

	public final boolean acceptRegion(Region region) {
		return !target.isDead() && unit_grid.getRegion(target.getGridX(), target.getGridY()) == region;
	}
	
	public final Region findPathRegion(int src_x, int src_y) {
		if (!target.isDead())
			return PathFinder.findPathRegion(unit_grid, unit_grid.getRegion(src_x, src_y), unit_grid.getRegion(target.getGridX(), target.getGridY()));
		else
			return null;
	}

	public final GridPathNode findPathGrid(Region target_region, Region next_region, int src_x, int src_y, boolean allow_secondary_targets) {
		if (!target.isDead())
			return PathFinder.findPathGrid(unit_grid, next_region, null,
										   src_x, src_y,
										   target.getGridX(), target.getGridY(),
										   target, max_dist, allow_secondary_targets);
		else
			return null;
	}
}
