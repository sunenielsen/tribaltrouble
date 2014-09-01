package com.oddlabs.tt.pathfinder;

public strictfp interface FinderFilter {
	public Occupant getOccupantFromRegion(Region region, boolean one_region);
	public Occupant getBest();
	public boolean acceptOccupant(Occupant occ);
}
