package com.oddlabs.tt.pathfinder;


public final strictfp class TargetFinderAlgorithm extends GridPathFinder {
	private final FinderFilter filter;
	
	private Occupant target;

	public TargetFinderAlgorithm(UnitGrid unit_grid, FinderFilter filter, Node dst_region, int dst_x, int dst_y, boolean allow_second_best) {
		super(unit_grid, dst_region, null, dst_x, dst_y, allow_second_best);
		this.filter = filter;
	}

	public final Occupant getOccupant() {
		return target;
	}

	public final boolean touchNeighbour(Occupant occ) {
		if (filter.acceptOccupant(occ)) {
			target = occ;
			return true;
		} else
			return false;
	}
}
