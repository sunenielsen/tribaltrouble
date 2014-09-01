package com.oddlabs.tt.pathfinder;

abstract strictfp class AStarAlgorithm implements PathFinderAlgorithm {
	private final int dst_x;
	private final int dst_y;
	private final Region dst_region;
	private final boolean allow_second_best;
	private final UnitGrid unit_grid;

	private int nodes_visited;
	private int best_dist_squared = Integer.MAX_VALUE;
	private Node second_best_node;

	protected AStarAlgorithm(UnitGrid unit_grid, int dst_x, int dst_y, boolean allow_second_best) {
		this.dst_x = dst_x;
		this.dst_y = dst_y;
		this.dst_region = unit_grid.getRegion(dst_x, dst_y);
		this.allow_second_best = allow_second_best;
		this.unit_grid = unit_grid;
	}

	protected final UnitGrid getUnitGrid() {
		return unit_grid;
	}

	public final int computeEstimatedCost(Node node) {
		return node.estimateCost(dst_x, dst_y);
	}

	protected abstract boolean isPathComplete(int dist_squared, Node node);

	private final NodeResult defaultTouchNode() {
		nodes_visited++;
		if (nodes_visited == RegionBuilder.MAX_EXAMINED_NODES_PER_PATH) {
			if (allow_second_best) {
				Node result = second_best_node;
				second_best_node = null;
				return new NodeResult(result);
			} else
				return new NodeResult(null);
		} else
			return null;
	}

	public final NodeResult touchNode(Node node) {
		int dx = node.getGridX() - dst_x;
		int dy = node.getGridY() - dst_y;
		int dist_squared = dx*dx + dy*dy;
		Region region = unit_grid.getRegion(node.getGridX(), node.getGridY());
		if (region == dst_region && dist_squared < best_dist_squared) {
			second_best_node = node;
			best_dist_squared = dist_squared;
		}
		if (isPathComplete(dist_squared, node)) {
			assert node != null : this + " " + dist_squared + " " + dx + " " + dy;
			return new NodeResult(node);
		}
		return defaultTouchNode();
	}

	public final NodeResult getBestNode() {
		return null;
	}
}
