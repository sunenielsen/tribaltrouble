package com.oddlabs.tt.landscape;

import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.pathfinder.UnitGrid;

public strictfp class LandscapeTarget implements Target {
	private final int grid_x;
	private final int grid_y;

	public LandscapeTarget(int grid_x, int grid_y) {
		this.grid_x = grid_x;
		this.grid_y = grid_y;
	}

	public final float getPositionX() {
		return UnitGrid.coordinateFromGrid(grid_x);
	}

	public final float getPositionY() {
		return UnitGrid.coordinateFromGrid(grid_y);
	}

	public final int getGridX() {
		return grid_x;
	}

	public final int getGridY() {
		return grid_y;
	}

	public final float getSize() {
		return 0;
	}

	public final boolean isDead() {
		return false;
	}

	public final String toString() {
		return "LandscapeTarget: grid_x = " + grid_x + " | grid_y = " + grid_y;
	}

	public final void startRespond() {
		throw new RuntimeException();
	}
	
	public final void stopRespond() {
		throw new RuntimeException();
	}
}
