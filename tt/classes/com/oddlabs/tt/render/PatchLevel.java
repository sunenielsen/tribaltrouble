package com.oddlabs.tt.render;

import com.oddlabs.tt.landscape.LandscapeTileTriangle;

final strictfp class PatchLevel {
	private PatchLevel right_neighbour;
	private PatchLevel left_neighbour;
	private PatchLevel top_neighbour;
	private PatchLevel bottom_neighbour;
	private int level;

	public final int getLevel() {
		return level;
	}
	
	public final void setLevel(int level) {
		this.level = level;
	}

	public final int getBorderSet() {
		return addNeighbourBorderBit(right_neighbour, LandscapeTileTriangle.EAST) |
			addNeighbourBorderBit(left_neighbour, LandscapeTileTriangle.WEST) |
			addNeighbourBorderBit(top_neighbour, LandscapeTileTriangle.NORTH) |
			addNeighbourBorderBit(bottom_neighbour, LandscapeTileTriangle.SOUTH);
	}

	private final int addNeighbourBorderBit(PatchLevel neighbour, int bit) {
		return neighbour.level > level ? bit : 0;
	}

	public final void adjustLevel() {
		level = getAdjustedLevel();
		adjustNeighbour(right_neighbour);
		adjustNeighbour(left_neighbour);
		adjustNeighbour(top_neighbour);
		adjustNeighbour(bottom_neighbour);
	}

	private void adjustNeighbour(PatchLevel neighbour) {
		if (neighbour.level < level - 1)
			neighbour.adjustLevel();
	}

	private int getAdjustedLevel() {
		int adjusted_level = level;
		adjusted_level = StrictMath.max(adjusted_level, right_neighbour.level - 1);
		adjusted_level = StrictMath.max(adjusted_level, left_neighbour.level - 1);
		adjusted_level = StrictMath.max(adjusted_level, top_neighbour.level - 1);
		adjusted_level = StrictMath.max(adjusted_level, bottom_neighbour.level - 1);
		return adjusted_level;
	}

	public final void init(PatchLevel right, PatchLevel top) {
		initTopNeighbour((PatchLevel)top);
		initRightNeighbour((PatchLevel)right);
	}

	private final void initTopNeighbour(PatchLevel top_neighbour) {
		this.top_neighbour = top_neighbour;
		top_neighbour.bottom_neighbour = this;
	}

	private final void initRightNeighbour(PatchLevel right_neighbour) {
		this.right_neighbour = right_neighbour;
		right_neighbour.left_neighbour = this;
	}

}
