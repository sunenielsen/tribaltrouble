package com.oddlabs.tt.pathfinder;

public strictfp interface ScanFilter {
	public int getMinRadius();
	public int getMaxRadius();
	public boolean filter(int grid_x, int grid_y, Occupant occ);
}
