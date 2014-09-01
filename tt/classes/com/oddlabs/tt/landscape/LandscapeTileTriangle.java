package com.oddlabs.tt.landscape;

import java.util.List;

import java.nio.ShortBuffer;

import com.oddlabs.tt.util.StrictVector3f;

public final strictfp class LandscapeTileTriangle {
	static final int INNER_EXP = 20;
	static final int INNER = 1 << INNER_EXP;
	
	static final int NORTH_EXP = 0;
	static final int EAST_EXP = 1;
	static final int SOUTH_EXP = 2;
	static final int WEST_EXP = 3;
	public static final int NORTH = 1 << NORTH_EXP;
	public static final int EAST = 1 << EAST_EXP;
	public static final int SOUTH = 1 << SOUTH_EXP;
	public static final int WEST = 1 << WEST_EXP;

	static final int NORTH_EAST = NORTH | EAST;
	static final int NORTH_WEST = NORTH | WEST;
	static final int SOUTH_EAST = SOUTH | EAST;
	static final int SOUTH_WEST = SOUTH | WEST;

	private final int direction;
	private final int border_direction;
	private final short i0;
	private final short i1;
	private final short i2;

	// x, y derived from indices
	private final int i0_x;
	private final int i0_y;
	private final int i1_x;
	private final int i1_y;
	private final int i2_x;
	private final int i2_y;

	private final LandscapeTileTriangle t0;
	private final LandscapeTileTriangle t1;

	private final StrictVector3f current_plane = new StrictVector3f();

	private static int getDirection(int patch_exp, int i0, int i1, int i2) {
		int dir = getDirection(patch_exp, i0, i1);
		if (dir == INNER) {
			dir = getDirection(patch_exp, i0, i2);
			if (dir == INNER) {
				dir = getDirection(patch_exp, i1, i2);
			}
		}
		return dir;
	}

	private static int getDirection(int patch_exp, int i0, int i1) {
		int patch_size = LandscapeTileIndices.getPatchSize(patch_exp);
		int i0_x = LandscapeTileIndices.getX(patch_exp, i0);
		int i0_y = LandscapeTileIndices.getY(patch_exp, i0);
		int i1_x = LandscapeTileIndices.getX(patch_exp, i1);
		int i1_y = LandscapeTileIndices.getY(patch_exp, i1);
		if (i0_x == 0 && i1_x == 0)
			return WEST;
		else if (i0_x == patch_size - 1 && i1_x == patch_size - 1)
			return EAST;
		else if (i0_y == 0 && i1_y == 0)
			return SOUTH;
		else if (i0_y == patch_size - 1 && i1_y == patch_size - 1)
			return NORTH;
		else
			return INNER;
	}

	private static short castIndex(int index) {
		assert index <= Short.MAX_VALUE && index >= Short.MIN_VALUE;
		return (short)index;
	}

	public LandscapeTileTriangle(int patch_exp, int lod_level, int dir, int i0, int i1, int i2) {
//		this.direction = getDirection(patch_exp, i0, i1, i2);
		this.direction = dir;
		this.border_direction = getDirection(patch_exp, i0, i1, i2);
		this.i0 = castIndex(i0);
		this.i1 = castIndex(i1);
		this.i2 = castIndex(i2);
		this.i0_x = LandscapeTileIndices.getX(patch_exp, i0);
		this.i0_y = LandscapeTileIndices.getY(patch_exp, i0);
		this.i1_x = LandscapeTileIndices.getX(patch_exp, i1);
		this.i1_y = LandscapeTileIndices.getY(patch_exp, i1);
		this.i2_x = LandscapeTileIndices.getX(patch_exp, i2);
		this.i2_y = LandscapeTileIndices.getY(patch_exp, i2);
		if (lod_level != LandscapeTileIndices.getNumLOD(patch_exp) - 1) {
			int patch_size = LandscapeTileIndices.getPatchSize(patch_exp);
			int i1_x = LandscapeTileIndices.getX(patch_exp, i1);
			int i1_y = LandscapeTileIndices.getY(patch_exp, i1);
			int i2_x = LandscapeTileIndices.getX(patch_exp, i2);
			int i2_y = LandscapeTileIndices.getY(patch_exp, i2);
			int split_x = (i1_x + i2_x)/2;
			int split_y = (i1_y + i2_y)/2;
			int t0_dir;
			int t1_dir;
			switch (dir) {
				case NORTH:
					t0_dir = NORTH_WEST;
					t1_dir = NORTH_EAST;
					break;
				case EAST:
					t0_dir = NORTH_EAST;
					t1_dir = SOUTH_EAST;
					break;
				case SOUTH:
					t0_dir = SOUTH_EAST;
					t1_dir = SOUTH_WEST;
					break;
				case WEST:
					t0_dir = SOUTH_WEST;
					t1_dir = NORTH_WEST;
					break;
				case NORTH_EAST:
					t0_dir = NORTH;
					t1_dir = EAST;
					break;
				case SOUTH_EAST:
					t0_dir = EAST;
					t1_dir = SOUTH;
					break;
				case SOUTH_WEST:
					t0_dir = SOUTH;
					t1_dir = WEST;
					break;
				case NORTH_WEST:
					t0_dir = WEST;
					t1_dir = NORTH;
					break;
				default:
					throw new RuntimeException("Unknown dir: " + dir);
			}
			int split_index = LandscapeTileIndices.getIndex(patch_exp, split_x, split_y);
			t0 = new LandscapeTileTriangle(patch_exp, lod_level + 1, t0_dir, split_index, i0, i1);
			t1 = new LandscapeTileTriangle(patch_exp, lod_level + 1, t1_dir, split_index, i2, i0);
		} else {
			t0 = null;
			t1 = null;
		}
	}

	final void addIndices(ShortBuffer buffer, int border_set) {
		if (containsBorder(border_set)) {
			t0.addIndices(buffer);
			t1.addIndices(buffer);
		} else
			addIndices(buffer);
	}

	private void addIndices(ShortBuffer buffer) {
		buffer.put(i0).put(i1).put(i2);
	}

	final void setupMapping(int patch_exp, int lod, LandscapeTileTriangle[][][] quad_to_planes) {
		int min_x = StrictMath.min(i0_x, StrictMath.min(i1_x, i2_x));
		int min_y = StrictMath.min(i0_y, StrictMath.min(i1_y, i2_y));
		int num_quads_exp = LandscapeTileIndices.getNumQuadsExp(lod);
		int quad_index = LandscapeTileIndices.getQuadIndex(patch_exp, num_quads_exp, min_x, min_y);
		LandscapeTileTriangle[] quad_planes = quad_to_planes[lod][quad_index];
		mapIndex(quad_planes, NORTH_EXP);
		mapIndex(quad_planes, EAST_EXP);
		mapIndex(quad_planes, SOUTH_EXP);
		mapIndex(quad_planes, WEST_EXP);
		if (t0 != null) {
			t0.setupMapping(patch_exp, lod + 1, quad_to_planes);
			t1.setupMapping(patch_exp, lod + 1, quad_to_planes);
		}
	}

	private void mapIndex(LandscapeTileTriangle[] quad_planes, int check_direction_exp) {
		if ((direction & (1 << check_direction_exp)) != 0)
			quad_planes[check_direction_exp] = this;
	}

	private boolean containsBorder(int border_set) {
		return (border_set & border_direction) != 0;
	}

	public final void putIndices(int lod, int border_set, ShortBuffer indices) {
//System.out.println("lod = " + lod + " | Integer.toHexString(border_set) = " + Integer.toHexString(border_set) + " | Integer.toHexString(border_direction) = " + Integer.toHexString(border_direction));
		if (lod > 0 || (lod == 0 && containsBorder(border_set))) {
			t0.putIndices(lod - 1, border_set, indices);
			t1.putIndices(lod - 1, border_set, indices);
		} else {
			indices.put(i0);
			indices.put(i1);
			indices.put(i2);
		}
	}

	public final void update(HeightMap heightmap, int offset_x, int offset_y) {
		heightmap.makePlaneVector(i0_x + offset_x, i0_y + offset_y, i1_x + offset_x, i1_y + offset_y, i2_x + offset_x, i2_y + offset_y, current_plane);
	}

	public final float getHeight(HeightMap heightmap, float x, float y) {
		return heightmap.planeHeight(x, y, current_plane);
	}

	public final String toString() {
		return "i0_x = " + i0_x + " | i0_y = " + i0_y + " | i1_x = " + i1_x + " | i1_y = " + i1_y + " | i2_x = " + i2_x + " | i2_y = " + i2_y;
	}
}
