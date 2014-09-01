package com.oddlabs.tt.pathfinder;

public strictfp interface TrackerAlgorithm {
	public boolean isDone(int x, int y);
	public boolean acceptRegion(Region region);
	public Region findPathRegion(int src_x, int src_y);
	public GridPathNode findPathGrid(Region target_region, Region next_region, int src_x, int src_y, boolean allow_secondary_targets);
}
