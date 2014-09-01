package com.oddlabs.tt.pathfinder;

public strictfp interface PathFinderAlgorithm {
	public NodeResult touchNode(Node node);
	public NodeResult getBestNode();
	public int computeEstimatedCost(Node node);
	public boolean touchNeighbour(Occupant occ);
}
