package com.oddlabs.tt.pathfinder;

public final strictfp class GridPathNode implements PathNode {
	private final GridPathNode parent;
	private final DirectionNode dir;

	public GridPathNode(GridPathNode parent, DirectionNode dir) {
		this.parent = parent;
		this.dir = dir;
	}

	public final DirectionNode getDirection() {
		return dir;
	}

	public final PathNode getParent() {
		return parent;
	}
}
