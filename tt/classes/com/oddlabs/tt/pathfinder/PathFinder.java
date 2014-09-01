package com.oddlabs.tt.pathfinder;

import java.util.ArrayList;
import java.util.List;

import com.oddlabs.tt.util.PocketList;
import com.oddlabs.tt.util.Target;

public final strictfp class PathFinder {
	private final static PocketList open_list = new PocketList(RegionBuilder.MAX_PATH_COST);
	public final static List visited_list = new ArrayList();
	public static int stat_pathfinder_per_frame = 0;

	public final static Region findPathRegion(UnitGrid unit_grid, Region src_region, Region dst_region) {
/*		Node src_region = unit_grid.getRegion(src_grid_x, src_grid_y);
		Node dst_region = unit_grid.getRegion(dst_grid_x, dst_grid_y);*/
		assert src_region != null;// : "src_grid_x = " + src_grid_x + " | src_grid_y = " + src_grid_y;
		assert dst_region != null;// : "dst_grid_x = " + dst_grid_x + " | dst_grid_y = " + dst_grid_y;
		PathFinderAlgorithm finder = new RegionPathFinder(unit_grid, dst_region);
		return (Region)doFindPath(finder, src_region, unit_grid);
	}

	public final static Region findPathRegion(UnitGrid unit_grid, PathFinderAlgorithm finder, Region current_region) {
//		Node current_region = UnitGrid.getGrid().getRegion(src_grid_x, src_grid_y);
		assert current_region != null;// : "src_grid_x = " + src_grid_x + " | src_grid_y = " + src_grid_y + " | occupant " + UnitGrid.getGrid().getOccupant(src_grid_x, src_grid_y);
		return (Region)doFindPath(finder, current_region, unit_grid);
	}

	public final static GridPathNode findPathGrid(UnitGrid unit_grid, PathFinderAlgorithm finder, int src_grid_x, int src_grid_y) {
		GridNode.Offset offset = GridNode.setupPathFinding(src_grid_x, src_grid_y, src_grid_x, src_grid_y);
		if (offset == null)
			return null;
		Node current_node = GridNode.getPathfinderNode(offset, src_grid_x, src_grid_y);
		Node grid_node = doFindPath(finder, current_node, unit_grid);
		if (grid_node != null)
			return (GridPathNode)grid_node.newPath();
		else
			return null;
	}

	public final static GridPathNode findPathGrid(UnitGrid unit_grid, Region dst_region, Region dst_region2, int src_grid_x, int src_grid_y, int dst_grid_x, int dst_grid_y, Target target, float max_dist, boolean allow_second_best) {
		GridNode.Offset offset = GridNode.setupPathFinding(src_grid_x, src_grid_y, src_grid_x, src_grid_y);
		if (offset == null)
			return null;
		Node current_node = GridNode.getPathfinderNode(offset, src_grid_x, src_grid_y);
		PathFinderAlgorithm finder = new TargetGridPathFinder(unit_grid, max_dist, dst_region, dst_region2, dst_grid_x, dst_grid_y, target, allow_second_best);
		Node grid_node = doFindPath(finder, current_node, unit_grid);
		if (grid_node != null)
			return (GridPathNode)grid_node.newPath();
		else
			return null;
	}

	private final static Node doFindPath(PathFinderAlgorithm finder, Node start_node, UnitGrid unit_grid) {
		if (start_node == null)
			return null;
		Node current_node = start_node;
		stat_pathfinder_per_frame++;
		initSearch();
		current_node.setPathInitial(finder.computeEstimatedCost(current_node));
		addToLists(current_node);
		while (open_list.size() != 0) {
			current_node = (Node)open_list.removeBest();
			NodeResult result = finder.touchNode(current_node);
			if (result != null)
				return result.get();
			boolean neighbour_result = current_node.addNeighbours(finder, unit_grid);
			if (neighbour_result)
				return current_node;
		}
		NodeResult result = finder.getBestNode();
		if (result != null)
			return result.get();
		else
			return null;
	}

	public final static void addToOpenList(PathFinderAlgorithm finder, Node current_node, Node parent, int cost) {
		current_node.setPath(parent, cost, finder.computeEstimatedCost(current_node));
		addToLists(current_node);
	}

	private final static void addToLists(Node current_node) {
		open_list.add(current_node.getTotalCost(), current_node);
		visited_list.add(current_node);
	}

	private final static void initSearch() {
		open_list.clear();
		for (int i = 0; i < visited_list.size(); i++) {
			Node node = (Node)visited_list.get(i);
			node.reset();
		}
		visited_list.clear();
	}
}
