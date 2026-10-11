package com.oddlabs.tt.pathfinder;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class StartingSeaSpotFilter implements ScanFilter {

    private final @NonNull UnitGrid grid;
    private final int clearance;
    private int @Nullable [] spot;

    public StartingSeaSpotFilter(@NonNull UnitGrid grid, int clearance) {
        this.grid = grid;
        this.clearance = clearance;
    }

    @Override
    public int getMinRadius() {
        return 0;
    }

    @Override
    public int getMaxRadius() {
        return grid.getGridSize();
    }

    @Override
    public boolean filter(int grid_x, int grid_y, @Nullable Occupant occupant) {
        if (!grid.isDeepWater(grid_x, grid_y)) {
            return false;
        }
        for (int y = grid_y - clearance; y <= grid_y + clearance; y++) {
            for (int x = grid_x - clearance; x <= grid_x + clearance; x++) {
                if (x < 0 || y < 0 || x >= grid.getGridSize() || y >= grid.getGridSize()
                        || grid.isGridOccupied(x, y, UnitGrid.SEA)) {
                    return false;
                }
            }
        }
        spot = new int[]{grid_x, grid_y};
        return true;
    }

    public int @Nullable [] getSpot() {
        return spot;
    }
}
