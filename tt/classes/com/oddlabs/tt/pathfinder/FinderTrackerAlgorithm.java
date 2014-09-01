package com.oddlabs.tt.pathfinder;

import com.oddlabs.tt.model.Selectable;

public final strictfp class FinderTrackerAlgorithm implements TrackerAlgorithm {
	private final FinderFilter filter;
	private final UnitGrid unit_grid;
	private Occupant target;
	
	public FinderTrackerAlgorithm(UnitGrid unit_grid, FinderFilter filter) {
		this.unit_grid = unit_grid;
		this.filter = filter;
	}

	public final boolean isDone(int x, int y) {
		return target != null && !target.isDead() && Selectable.isCloseEnough(unit_grid, 0f, x, y, target);
	}

	public final boolean acceptRegion(Region region) {
		return filter.getOccupantFromRegion(region, true) != null;
	}

	public final Region findPathRegion(int src_x, int src_y) {
		TargetRegionFinder region_finder = new TargetRegionFinder(unit_grid, filter);
		Region region = PathFinder.findPathRegion(unit_grid, region_finder, unit_grid.getRegion(src_x, src_y));
		if (region == null)
			return null;
		return region;
	}

	public final Occupant getOccupant() {
		if (target != null && target.isDead())
			return null;
		else
			return target;
	}

	public final GridPathNode findPathGrid(Region target_region, Region next_region, int src_x, int src_y, boolean allow_secondary_targets) {
		Occupant hint_occupant = filter.getOccupantFromRegion(target_region, true);
		TargetFinderAlgorithm grid_finder = new TargetFinderAlgorithm(unit_grid, filter, next_region, hint_occupant.getGridX(), hint_occupant.getGridY(), allow_secondary_targets);
		GridPathNode path = PathFinder.findPathGrid(unit_grid, grid_finder, src_x, src_y);
		target = grid_finder.getOccupant();
		return path;
	}
}
