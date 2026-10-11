package com.oddlabs.tt.pathfinder;

import com.oddlabs.tt.form.ProgressForm;
import com.oddlabs.tt.landscape.IslandInfo;
import com.oddlabs.tt.util.PocketList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public final class RegionBuilder {
    public static final int MAX_EXAMINED_NODES_PER_PATH = 600;
    public static final int REGION_PATH_MAX_COST = 70;
    public static final int REGION_PATH_MAX_COST_WATER = 200;
    public static final int MAX_PATH_COST = 2048;
    public static final int GRID_SIZE = 128;

    public static final int DIAGONAL = 3;
    public static final int STRAIGHT = 2;

    private static final Occupant unreachable_obj = new StaticOccupant();

    public static void buildRegions(@NonNull UnitGrid unit_grid, float start_x_f, float start_y_f) {
        boolean[][] access_grid = unit_grid.getHeightMap().getAccessGrid();
        int grid_size = access_grid.length;
        int start_x = UnitGrid.toGridCoordinate(start_x_f);
        int start_y = UnitGrid.toGridCoordinate(start_y_f);

        RegionBuilderNode[][] dir_finder_grid = new RegionBuilderNode[grid_size][grid_size];
        int num_occupied = 0;
        for (int y = 0; y < grid_size; y++) {
            for (int x = 0; x < grid_size; x++) {
                RegionBuilderNode finder_node = new RegionBuilderNode(x, y);
                dir_finder_grid[y][x] = finder_node;
                if (!access_grid[y][x]) {
                    unit_grid.occupyGrid(finder_node.getGridX(), finder_node.getGridY(), unreachable_obj);
                    num_occupied++;
                }
            }
        }
        RegionBuilderNode start_node = dir_finder_grid[start_y][start_x];
        QueueArray start_nodes = new QueueArray(grid_size * grid_size);
        PocketList<RegionBuilderNode> region_nodes = new PocketList<>(grid_size);
        start_nodes.addLast(start_node);
        int actual_num_regions = 0;
        while ((start_node = findStartNode(unit_grid, region_nodes, start_nodes)) != null) {
            assert !unit_grid.isGridOccupied(start_node.getGridX(),
                    start_node.getGridY()) : "Starting location (" + start_x + "," + start_y + ") occupied";
            Region region = new Region();
            addRegionNodes(unit_grid, dir_finder_grid, start_nodes, region, start_node.getGridX(),
                    start_node.getGridY(), region_nodes);
            actual_num_regions++;
        }
        for (int y = 0; y < grid_size; y++) {
            for (int x = 0; x < grid_size; x++) {
                Region region = unit_grid.getRegion(x, y);
                if (region != null)
                    updateRegionNeighbours(unit_grid, x, y, region);
            }
        }
        buildWaterRegions(unit_grid);
        ProgressForm.progress(1f);
        IO.println("actual_num_regions = " + actual_num_regions);
    }

    /* Building regions with multiple islands */
    public static void buildRegions(@NonNull UnitGrid unit_grid) {
        boolean[][] access_grid = unit_grid.getHeightMap().getAccessGrid();
        List<IslandInfo> island_infos = unit_grid.getHeightMap().getIslandInfos();
        int grid_size = access_grid.length;

        RegionBuilderNode[][] dir_finder_grid = new RegionBuilderNode[grid_size][grid_size];
        int num_occupied = 0;
        for (int y = 0; y < grid_size; y++) {
            for (int x = 0; x < grid_size; x++) {
                RegionBuilderNode finder_node = new RegionBuilderNode(x, y);
                dir_finder_grid[y][x] = finder_node;
                if (!access_grid[y][x]) {
                    unit_grid.occupyGrid(finder_node.getGridX(), finder_node.getGridY(), unreachable_obj);
                    num_occupied++;
                }
            }
        }

        ProgressForm.progress(.5f);

        int actual_num_regions = 0;
        for (IslandInfo island_info : island_infos) {
            RegionBuilderNode start_node = dir_finder_grid[island_info.startY()][island_info.startX()];
            QueueArray start_nodes = new QueueArray(grid_size * grid_size);
            PocketList<RegionBuilderNode> region_nodes = new PocketList<>(grid_size);
            start_nodes.addLast(start_node);
            while ((start_node = findStartNode(unit_grid, region_nodes, start_nodes)) != null) {
                assert !unit_grid.isGridOccupied(
                        start_node.getGridX(),
                        start_node.getGridY()) : "Starting location (" + island_info.startX() + "," + island_info.startY() + ") occupied";
                Region region = new Region();
                addRegionNodes(
                        unit_grid,
                        dir_finder_grid,
                        start_nodes,
                        region,
                        start_node.getGridX(),
                        start_node.getGridY(),
                        region_nodes);
                actual_num_regions++;
            }
        }

        for (int y = 0; y < grid_size; y++) {
            for (int x = 0; x < grid_size; x++) {
                Region region = unit_grid.getRegion(x, y);
                if (region != null)
                    updateRegionNeighbours(unit_grid, x, y, region);
            }
        }
        buildWaterRegions(unit_grid);
        ProgressForm.progress(.5f);
        IO.println("actual_num_regions = " + actual_num_regions);
    }

    public static void buildWaterRegions(@NonNull UnitGrid unit_grid) {
        byte[][] water_grid = unit_grid.getHeightMap().getWaterGrid();
        int grid_size = water_grid.length;

        RegionBuilderNode[][] dir_finder_grid = new RegionBuilderNode[grid_size][grid_size];
        for (int y = 0; y < grid_size; y++) {
            for (int x = 0; x < grid_size; x++) {
                RegionBuilderNode finder_node = new RegionBuilderNode(x, y);
                dir_finder_grid[y][x] = finder_node;
                if (water_grid[y][x] != 2)
                    unit_grid.occupyGrid(finder_node.getGridX(), finder_node.getGridY(), unreachable_obj,
                            UnitGrid.SEA);
            }
        }

        RegionBuilderNode start_node = dir_finder_grid[0][0];
        QueueArray start_nodes = new QueueArray(grid_size * grid_size);
        PocketList<RegionBuilderNode> region_nodes = new PocketList<>(grid_size);
        start_nodes.addLast(start_node);
        int actual_num_regions = 0;
        while ((start_node = findStartNodeWater(unit_grid, region_nodes, start_nodes)) != null) {
            Region region = new Region();
            addRegionNodesWater(unit_grid, dir_finder_grid, start_nodes, region, start_node.getGridX(),
                    start_node.getGridY(), region_nodes);
            actual_num_regions++;
        }
        for (int y = 0; y < grid_size; y++) {
            for (int x = 0; x < grid_size; x++) {
                Region region = unit_grid.getRegion(x, y, UnitGrid.SEA);
                if (region != null)
                    updateRegionNeighboursWater(unit_grid, x, y, region);
            }
        }
        IO.println("actual_num_water_regions = " + actual_num_regions);
    }

    private static void testNeighbourWater(@NonNull UnitGrid unit_grid, int grid_x, int grid_y, Region region) {
        int grid_size = unit_grid.getGridSize();
        if (grid_x < 0 || grid_y < 0 || grid_x >= grid_size || grid_y >= grid_size)
            return;
        Region neighbour_region = unit_grid.getRegion(grid_x, grid_y, UnitGrid.SEA);
        if (neighbour_region == null || neighbour_region == region || neighbour_region.isNeighbour(region))
            return;
        ShipTrajectoryPoint p0 = new ShipTrajectoryPoint(region.getGridX(), region.getGridY());
        ShipTrajectoryPoint p1 = new ShipTrajectoryPoint(neighbour_region.getGridX(), neighbour_region.getGridY());
        if (!ShipTrajectory.checkLandCollision(unit_grid, p0, p1)) {
            Region.link(neighbour_region, region);
        }
    }

    private static void updateRegionNeighboursWater(@NonNull UnitGrid unit_grid, int grid_x, int grid_y,
            Region region) {
        testNeighbourWater(unit_grid, grid_x + 1, grid_y, region);
        testNeighbourWater(unit_grid, grid_x + 1, grid_y + 1, region);
        testNeighbourWater(unit_grid, grid_x, grid_y + 1, region);
        testNeighbourWater(unit_grid, grid_x - 1, grid_y + 1, region);
        testNeighbourWater(unit_grid, grid_x - 1, grid_y, region);
        testNeighbourWater(unit_grid, grid_x - 1, grid_y - 1, region);
        testNeighbourWater(unit_grid, grid_x, grid_y - 1, region);
        testNeighbourWater(unit_grid, grid_x + 1, grid_y - 1, region);
    }

    private static void addRegionNodesWater(@NonNull UnitGrid unit_grid,
            RegionBuilderNode @NonNull [] @NonNull [] dir_finder_grid, @NonNull QueueArray start_nodes,
            @NonNull Region region, int start_x, int start_y, @NonNull PocketList<RegionBuilderNode> region_nodes) {
        int min_x = start_x;
        int max_x = start_x;
        int min_y = start_y;
        int max_y = start_y;
        while (!region_nodes.isEmpty()) {
            RegionBuilderNode node = region_nodes.removeBest();
            if (unit_grid.getRegion(node.getGridX(), node.getGridY(), UnitGrid.SEA) != null)
                continue;
            if (node.getTotalCost() > REGION_PATH_MAX_COST_WATER) {
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

            unit_grid.setRegion(node.getGridX(), node.getGridY(), region, UnitGrid.SEA);
            addNeighboursWater(unit_grid, dir_finder_grid, region_nodes, node);
        }
        region.setPosition((max_x + min_x) / 2, (max_y + min_y) / 2);
    }

    private static void addNeighbourWater(@NonNull UnitGrid unit_grid,
            RegionBuilderNode @NonNull [] @NonNull [] dir_finder_grid,
            @NonNull PocketList<RegionBuilderNode> region_nodes, int x, int y, int cost) {
        if (x < 0 || y < 0 || y >= dir_finder_grid.length || x >= dir_finder_grid[y].length)
            return;
        RegionBuilderNode node = dir_finder_grid[y][x];
        if (unit_grid.getRegion(node.getGridX(), node.getGridY(), UnitGrid.SEA) != null)
            return;
        node.setTotalCost(cost);
        if (!unit_grid.isGridOccupied(node.getGridX(), node.getGridY(), UnitGrid.SEA))
            region_nodes.add(node.getTotalCost(), node);
    }

    private static void addNeighboursWater(@NonNull UnitGrid unit_grid,
            RegionBuilderNode @NonNull [] @NonNull [] dir_finder_grid,
            @NonNull PocketList<RegionBuilderNode> region_nodes, @NonNull RegionBuilderNode node) {
        int x = node.getGridX();
        int y = node.getGridY();
        int cost = node.getTotalCost();
        addNeighbourWater(unit_grid, dir_finder_grid, region_nodes, x - 1, y - 1, cost + DIAGONAL);
        addNeighbourWater(unit_grid, dir_finder_grid, region_nodes, x - 1, y, cost + STRAIGHT);
        addNeighbourWater(unit_grid, dir_finder_grid, region_nodes, x - 1, y + 1, cost + DIAGONAL);
        addNeighbourWater(unit_grid, dir_finder_grid, region_nodes, x, y - 1, cost + STRAIGHT);
        addNeighbourWater(unit_grid, dir_finder_grid, region_nodes, x, y + 1, cost + STRAIGHT);
        addNeighbourWater(unit_grid, dir_finder_grid, region_nodes, x + 1, y - 1, cost + DIAGONAL);
        addNeighbourWater(unit_grid, dir_finder_grid, region_nodes, x + 1, y, cost + STRAIGHT);
        addNeighbourWater(unit_grid, dir_finder_grid, region_nodes, x + 1, y + 1, cost + DIAGONAL);
    }

    private static @Nullable RegionBuilderNode findStartNodeWater(@NonNull UnitGrid unit_grid,
            @NonNull PocketList<RegionBuilderNode> region_nodes, @NonNull QueueArray start_nodes) {
        region_nodes.clear();
        while (!start_nodes.isEmpty()) {
            RegionBuilderNode node = start_nodes.removeFirst();
            if (unit_grid.getRegion(node.getGridX(), node.getGridY(), UnitGrid.SEA) == null) {
                node.setTotalCost(0);
                region_nodes.add(node.getTotalCost(), node);
                return node;
            }
        }
        return null;
    }

    private static void testNeighbour(@NonNull UnitGrid unit_grid, int grid_x, int grid_y, Region region) {
        Region neighbour_region = unit_grid.getRegion(grid_x, grid_y);
        Region.link(neighbour_region, region);
    }

    private static void updateRegionNeighbours(@NonNull UnitGrid unit_grid, int grid_x, int grid_y, Region region) {
        testNeighbour(unit_grid, grid_x + 1, grid_y, region);
        testNeighbour(unit_grid, grid_x + 1, grid_y + 1, region);
        testNeighbour(unit_grid, grid_x, grid_y + 1, region);
        testNeighbour(unit_grid, grid_x - 1, grid_y + 1, region);
        testNeighbour(unit_grid, grid_x - 1, grid_y, region);
        testNeighbour(unit_grid, grid_x - 1, grid_y - 1, region);
        testNeighbour(unit_grid, grid_x, grid_y - 1, region);
        testNeighbour(unit_grid, grid_x + 1, grid_y - 1, region);
    }

    private static void addRegionNodes(@NonNull UnitGrid unit_grid,
            RegionBuilderNode @NonNull [] @NonNull [] dir_finder_grid, @NonNull QueueArray start_nodes,
            @NonNull Region region, int start_x, int start_y, @NonNull PocketList<RegionBuilderNode> region_nodes) {
        int min_x = start_x;
        int max_x = start_x;
        int min_y = start_y;
        int max_y = start_y;
        while (!region_nodes.isEmpty()) {
            RegionBuilderNode node = region_nodes.removeBest();
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
        region.setPosition((max_x + min_x) / 2, (max_y + min_y) / 2);
    }

    private static void addNeighbour(@NonNull UnitGrid unit_grid,
            RegionBuilderNode @NonNull [] @NonNull [] dir_finder_grid,
            @NonNull PocketList<RegionBuilderNode> region_nodes, int x, int y, int cost) {
        RegionBuilderNode node = dir_finder_grid[y][x];
        if (unit_grid.getRegion(node.getGridX(), node.getGridY()) != null)
            return;
        node.setTotalCost(cost);
        if (!unit_grid.isGridOccupied(node.getGridX(), node.getGridY()))
            region_nodes.add(node.getTotalCost(), node);
    }

    private static void addNeighbours(@NonNull UnitGrid unit_grid,
            RegionBuilderNode @NonNull [] @NonNull [] dir_finder_grid,
            @NonNull PocketList<RegionBuilderNode> region_nodes, @NonNull RegionBuilderNode node) {
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

    private static @Nullable RegionBuilderNode findStartNode(@NonNull UnitGrid unit_grid,
            @NonNull PocketList<RegionBuilderNode> region_nodes, @NonNull QueueArray start_nodes) {
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

    private RegionBuilder() {
    }
}
