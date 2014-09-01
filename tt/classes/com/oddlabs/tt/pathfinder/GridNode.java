package com.oddlabs.tt.pathfinder;

import com.oddlabs.tt.landscape.HeightMap;

final strictfp class GridNode extends Node {
	private final static DirectionNode[][] dir_node_grid = new DirectionNode[3][3];

	private final static GridNode[][] pathfinder_grid;
	private final int local_grid_x;
	private final int local_grid_y;
	private Offset offset;

	static {
		pathfinder_grid = new GridNode[RegionBuilder.GRID_SIZE][RegionBuilder.GRID_SIZE];
		for (int y = 0; y < pathfinder_grid.length; y++)
			 for (int x = 0; x < pathfinder_grid.length; x++)
				 pathfinder_grid[y][x] = new GridNode(x, y);
		int unit = HeightMap.METERS_PER_UNIT_GRID;
		float inv_unit = 1f/HeightMap.METERS_PER_UNIT_GRID;
		float inv_sqrt_2 = 1f/(float)StrictMath.sqrt(unit*unit + unit*unit);
		dir_node_grid[0][0] = new DirectionNode(inv_sqrt_2, -1, -1);
		dir_node_grid[0][1] = new DirectionNode(inv_unit, -1, 0);
		dir_node_grid[0][2] = new DirectionNode(inv_sqrt_2, -1, 1);
		dir_node_grid[1][0] = new DirectionNode(inv_unit, 0, -1);
		dir_node_grid[1][1] = null;
		dir_node_grid[1][2] = new DirectionNode(inv_unit, 0, 1);
		dir_node_grid[2][0] = new DirectionNode(inv_sqrt_2, 1, -1);
		dir_node_grid[2][1] = new DirectionNode(inv_unit, 1, 0);
		dir_node_grid[2][2] = new DirectionNode(inv_sqrt_2, 1, 1);
	}

	private final static DirectionNode lookupDirectionNode(int dx, int dy) {
		return dir_node_grid[dx + 1][dy + 1];
	}

	public final PathNode newPath() {
		Node graph_node = this;
		GridPathNode current_node = null;
		while (graph_node.getParent() != null) {
			Node parent = graph_node.getParent();
			int dx = graph_node.getGridX() - parent.getGridX();
			int dy = graph_node.getGridY() - parent.getGridY();
			graph_node = parent;
			DirectionNode dir = lookupDirectionNode(dx, dy);
			current_node = new GridPathNode(current_node, dir);
		}
		return current_node;
	}

	public GridNode(int x, int y) {
		this.local_grid_x = x;
		this.local_grid_y = y;
	}

	public final int getGridX() {
		return local_grid_x + offset.offset_x;
	}

	public final int getGridY() {
		return local_grid_y + offset.offset_y;
	}

	public final static Offset setupPathFinding(int src_grid_x, int src_grid_y, int dst_grid_x, int dst_grid_y) {
		if (StrictMath.abs(dst_grid_x - src_grid_x) >= RegionBuilder.GRID_SIZE &&
				StrictMath.abs(dst_grid_y - src_grid_y) >= RegionBuilder.GRID_SIZE)
			return null;
		int path_offset_x = (dst_grid_x + src_grid_x - RegionBuilder.GRID_SIZE)/2;
		int path_offset_y = (dst_grid_y + src_grid_y - RegionBuilder.GRID_SIZE)/2;
		return new Offset(path_offset_x, path_offset_y);
	}

	public final static GridNode getPathfinderNode(Offset offset, int x, int y) {
		GridNode node = getPathfinderNodeOffset(x - offset.offset_x, y - offset.offset_y);
		if (node != null)
			node.offset = offset;
		return node;
	}

	private final static GridNode getPathfinderNodeOffset(int local_x, int local_y) {
		if (local_x < 0 || local_x >= pathfinder_grid.length ||
			local_y < 0 || local_y >= pathfinder_grid.length)
			return null;
		else
			return pathfinder_grid[local_y][local_x];
	}

	private final boolean addNeighbour(PathFinderAlgorithm finder, UnitGrid unit_grid, int x, int y, int cost) {
		GridNode node = getPathfinderNode(offset, x, y);
		if (node == null || node.isVisited())
			return false;
		Occupant occupant = unit_grid.getOccupant(node.getGridX(), node.getGridY());
		if (occupant != null) {
			if (finder.touchNeighbour(occupant))
				return true;
			int penalty = occupant.getPenalty();
			if (penalty >= Occupant.STATIC)
				return false;
			cost += penalty;
		}
		PathFinder.addToOpenList(finder, node, this, cost);
		return false;
	}

	public final boolean addNeighbours(PathFinderAlgorithm finder, UnitGrid unit_grid) {
		int x = getGridX();
		int y = getGridY();
		return	addNeighbour(finder, unit_grid, x, y - 1, RegionBuilder.STRAIGHT) ||
				addNeighbour(finder, unit_grid, x - 1, y, RegionBuilder.STRAIGHT) ||
				addNeighbour(finder, unit_grid, x + 1, y, RegionBuilder.STRAIGHT) ||
				addNeighbour(finder, unit_grid, x, y + 1, RegionBuilder.STRAIGHT) ||
				addNeighbour(finder, unit_grid, x - 1, y - 1, RegionBuilder.DIAGONAL) ||
				addNeighbour(finder, unit_grid, x + 1, y - 1, RegionBuilder.DIAGONAL) ||
				addNeighbour(finder, unit_grid, x - 1, y + 1, RegionBuilder.DIAGONAL) ||
				addNeighbour(finder, unit_grid, x + 1, y + 1, RegionBuilder.DIAGONAL);
	}

	final static strictfp class Offset {
		final int offset_x;
		final int offset_y;

		Offset(int x, int y) {
			offset_x = x;
			offset_y = y;
		}
	}
}
