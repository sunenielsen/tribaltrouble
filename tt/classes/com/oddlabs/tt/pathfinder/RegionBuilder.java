package com.oddlabs.tt.pathfinder;

import com.oddlabs.tt.form.ProgressForm;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.util.PocketList;

public final strictfp class RegionBuilder {
	public final static int MAX_EXAMINED_NODES_PER_PATH = 600;
	public final static int REGION_PATH_MAX_COST = 70;
	public final static int MAX_PATH_COST = 1024;
	public final static int GRID_SIZE = 128;
		
	public final static int DIAGONAL = 3;
	public final static int STRAIGHT = 2;
		
	private final static Occupant unreachable_obj = new StaticOccupant();

	public final static void buildRegions(UnitGrid unit_grid, float start_x_f, float start_y_f) {
		boolean[][] access_grid = unit_grid.getHeightMap().getAccessGrid();
		int grid_size = access_grid.length;
		int start_x = UnitGrid.toGridCoordinate(start_x_f);
		int start_y = UnitGrid.toGridCoordinate(start_y_f);

		RegionBuilderNode[][] dir_finder_grid = new RegionBuilderNode[grid_size][grid_size];
		int num_occupied = 0;
		for (int y = 0; y < grid_size; y++)
			for (int x = 0; x < grid_size; x++) {
				RegionBuilderNode finder_node = new RegionBuilderNode(x, y);
				dir_finder_grid[y][x] = finder_node;
				if (!access_grid[y][x]) {
					unit_grid.occupyGrid(finder_node.getGridX(), finder_node.getGridY(), unreachable_obj);
					num_occupied++;
				}
			}
		RegionBuilderNode start_node = dir_finder_grid[start_y][start_x];
		QueueArray start_nodes = new QueueArray(grid_size*grid_size);
		PocketList region_nodes = new PocketList(grid_size);
		start_nodes.addLast(start_node);
		int actual_num_regions = 0;
		while ((start_node = findStartNode(unit_grid, region_nodes, start_nodes)) != null) {
			assert !unit_grid.isGridOccupied(start_node.getGridX(), start_node.getGridY()): "Starting location ("+ start_x +","+ start_y +") occupied";
			Region region = new Region();
			addRegionNodes(unit_grid, dir_finder_grid, start_nodes, region, start_node.getGridX(), start_node.getGridY(), region_nodes);
			actual_num_regions++;
		}
		for (int y = 0; y < grid_size; y++)
			for (int x = 0; x < grid_size; x++) {
				Region region = unit_grid.getRegion(x, y);
				if (region != null)
					updateRegionNeighbours(unit_grid, x, y, region);
			}
		ProgressForm.progress(1f);
System.out.println("actual_num_regions = " + actual_num_regions);
	}

	private final static void testNeighbour(UnitGrid unit_grid, int grid_x, int grid_y, Region region) {
		Region neighbour_region = unit_grid.getRegion(grid_x, grid_y);
		Region.link(neighbour_region, region);
	}

	private final static void updateRegionNeighbours(UnitGrid unit_grid, int grid_x, int grid_y, Region region) {
		testNeighbour(unit_grid, grid_x + 1, grid_y, region);
		testNeighbour(unit_grid, grid_x + 1, grid_y + 1, region);
		testNeighbour(unit_grid, grid_x, grid_y + 1, region);
		testNeighbour(unit_grid, grid_x - 1, grid_y + 1, region);
		testNeighbour(unit_grid, grid_x - 1, grid_y, region);
		testNeighbour(unit_grid, grid_x - 1, grid_y - 1, region);
		testNeighbour(unit_grid, grid_x, grid_y - 1, region);
		testNeighbour(unit_grid, grid_x + 1, grid_y - 1, region);
	}

	private final static void addRegionNodes(UnitGrid unit_grid, RegionBuilderNode[][] dir_finder_grid, QueueArray start_nodes, Region region, int start_x, int start_y, PocketList region_nodes) {
		int min_x = start_x;
		int max_x = start_x;
		int min_y = start_y;
		int max_y = start_y;
		while (region_nodes.size() > 0) {
			RegionBuilderNode node = (RegionBuilderNode)region_nodes.removeBest();
			if (unit_grid.getRegion(node.getGridX(), node.getGridY()) != null)
				continue;
			if (node.getTotalCost() > REGION_PATH_MAX_COST) {
				start_nodes.addLast(node);
				continue;
			}
			
			int nx = node.getGridX();
			int ny = node.getGridY();
			if (max_x < nx) 
				max_x = nx;
			if (min_x > nx)
				min_x = nx;
			if (max_y < ny) 
				max_y = ny;
			if (min_y > ny)
				min_y = ny;
				
			unit_grid.setRegion(node.getGridX(), node.getGridY(), region);
			addNeighbours(unit_grid, dir_finder_grid, region_nodes, node);
		}
		region.setPosition((max_x + min_x)/2, (max_y + min_y)/2);
	}

	private final static void addNeighbour(UnitGrid unit_grid, RegionBuilderNode[][] dir_finder_grid, PocketList region_nodes, int x, int y, int cost) {
		RegionBuilderNode node = dir_finder_grid[y][x];
		if (unit_grid.getRegion(node.getGridX(), node.getGridY()) != null)
			return;
		node.setTotalCost(cost);
		if (!unit_grid.isGridOccupied(node.getGridX(), node.getGridY()))
			region_nodes.add(node.getTotalCost(), node);
	}

	private final static void addNeighbours(UnitGrid unit_grid, RegionBuilderNode[][] dir_finder_grid, PocketList region_nodes, RegionBuilderNode node) {
		int x = node.getGridX();
		int y = node.getGridY();
		int cost = node.getTotalCost();
		addNeighbour(unit_grid, dir_finder_grid, region_nodes, x - 1, y - 1, cost + DIAGONAL);
		addNeighbour(unit_grid, dir_finder_grid, region_nodes, x - 1, y, cost + STRAIGHT);
		addNeighbour(unit_grid, dir_finder_grid, region_nodes, x - 1, y + 1, cost + DIAGONAL);
		addNeighbour(unit_grid, dir_finder_grid, region_nodes, x, y - 1, cost + STRAIGHT);
		addNeighbour(unit_grid, dir_finder_grid, region_nodes, x, y + 1, cost + STRAIGHT);
		addNeighbour(unit_grid, dir_finder_grid, region_nodes, x + 1, y - 1, cost + DIAGONAL);
		addNeighbour(unit_grid, dir_finder_grid, region_nodes, x + 1, y, cost + STRAIGHT);
		addNeighbour(unit_grid, dir_finder_grid, region_nodes, x + 1, y + 1, cost + DIAGONAL);
	}

	private final static RegionBuilderNode findStartNode(UnitGrid unit_grid, PocketList region_nodes, QueueArray start_nodes) {
		region_nodes.clear();
		while (!start_nodes.isEmpty()) {
			RegionBuilderNode node = start_nodes.removeFirst();
			if (unit_grid.getRegion(node.getGridX(), node.getGridY()) == null) {
				node.setTotalCost(0);
				region_nodes.add(node.getTotalCost(), node);
				return node;
			}
		}
		return null;
	}
}
