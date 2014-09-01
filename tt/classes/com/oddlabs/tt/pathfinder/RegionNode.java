package com.oddlabs.tt.pathfinder;

public final strictfp class RegionNode implements PathNode {
	private final RegionNode parent;
	private final Region region;

	public RegionNode(RegionNode parent, Region region) {
		this.parent = parent;
		this.region = region;
	}

	public final Region getRegion() {
		return region;
	}

	public final PathNode getParent() {
		return parent;
	}
}
