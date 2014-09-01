package com.oddlabs.tt.pathfinder;

final strictfp class RegionPathFinder extends AStarAlgorithm {
	private final Node dst_region;
	
	public RegionPathFinder(UnitGrid unit_grid, Node dst_region) {
		super(unit_grid, dst_region.getGridX(), dst_region.getGridY(), false);
		this.dst_region = dst_region;
	}

	public final boolean touchNeighbour(Occupant occ) {
		return false;
	}

	protected final boolean isPathComplete(int dist_squared, Node node) {
		return node == dst_region;
	}
}
