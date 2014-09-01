package com.oddlabs.tt.landscape;

import java.util.List;

import com.oddlabs.tt.camera.Camera;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.procedural.Landscape;
import com.oddlabs.tt.util.StrictVector3f;

public final strictfp class HeightMap {
	public final static int METERS_PER_UNIT_GRID = 2;
	public final static int GRID_UNITS_PER_PATCH_EXP = 4;
	public final static int GRID_UNITS_PER_PATCH = 1 << GRID_UNITS_PER_PATCH_EXP;

	final static int MIN_INTERSECTING_LEVEL = 5;
	private final static StrictVector3f vec1 = new StrictVector3f();
	private final static StrictVector3f vec2 = new StrictVector3f();
	private final static StrictVector3f plane = new StrictVector3f();

	private final float[][] world;
	private final LandscapeLeaf[][] landscape_leaves;
	private final List trees;
	private final boolean[][] access_grid;
	private final byte[][] build_grid;
	private final int meters_per_world;
	private final int patches_per_world;
	private final int meters_per_patch;
	private final int grid_units_per_world;
	private final float inv_meters_per_patch;
	private final float inv_meters_per_grid_unit;
	private final float sea_level_meters;
	private final int meters_per_chunk;
	private final int quadtree_min_level;
	private final int patches_per_chunk;
	private final float meters_per_chunk_border;
	private final float chunk_tex_scale;
	private final World world_instance;

	public HeightMap(World world_instance, int meters_per_world, float sea_level_meters, int texels_per_colormap, int chunks_per_colormap, float[][] world, List trees, boolean[][] access_grid, byte[][] build_grid) {
		this.world = world;
		this.world_instance = world_instance;
		this.trees = trees;
		this.access_grid = access_grid;
		this.build_grid = build_grid;
		this.meters_per_world = meters_per_world;
		this.sea_level_meters = sea_level_meters;
		patches_per_world = world.length/GRID_UNITS_PER_PATCH;
		meters_per_patch = GRID_UNITS_PER_PATCH*METERS_PER_UNIT_GRID;
		grid_units_per_world = getPatchesPerWorld()*GRID_UNITS_PER_PATCH;
		inv_meters_per_patch = 1f/getMetersPerPatch();
		inv_meters_per_grid_unit = 1f/METERS_PER_UNIT_GRID;
		meters_per_chunk = getMetersPerWorld()/chunks_per_colormap;
		quadtree_min_level = (int)(StrictMath.log(chunks_per_colormap)/StrictMath.log(2));
		patches_per_chunk = meters_per_chunk/getMetersPerPatch();

		int texels_per_colormap_noborder = texels_per_colormap - 2*Globals.TEXELS_PER_CHUNK_BORDER*chunks_per_colormap;
		float meters_per_texel = (float)getMetersPerWorld()/texels_per_colormap_noborder;
		meters_per_chunk_border = meters_per_texel*Globals.TEXELS_PER_CHUNK_BORDER;
		chunk_tex_scale = 1f/(meters_per_chunk + 2f*meters_per_chunk_border);

		landscape_leaves = new LandscapeLeaf[getPatchesPerWorld()][getPatchesPerWorld()];
	}

	public final boolean isGridInside(int x, int y) {
		boolean inside_world = x >= 0 && y >= 0 && x < getGridUnitsPerWorld() && y < getGridUnitsPerWorld();
		return inside_world;
	}

	public final boolean isInside(float x, float y) {
		boolean inside_world = x >= 0 && y >= 0 && x <= getMetersPerWorld() && y <= getMetersPerWorld();
		return inside_world;
	}

	public final int getMetersPerWorld() {
		return meters_per_world;
	}

	public final int getGridUnitsPerPatch() {
		return GRID_UNITS_PER_PATCH;
	}

	public final float getInvMetersPerGridUnit() {
		return inv_meters_per_grid_unit;
	}

	public final int getPatchesPerWorld() {
		return patches_per_world;
	}

	public final int getMetersPerPatch() {
		return meters_per_patch;
	}

	public final int getGridUnitsPerWorld() {
		return grid_units_per_world;
	}

	public final float getSeaLevelMeters() {
		return sea_level_meters;
	}

	public final int getMetersPerChunk() {
		return meters_per_chunk;
	}

	public final int getQuadtreeMinLevel() {
		return quadtree_min_level;
	}

	public final int getPatchesPerChunk() {
		return patches_per_chunk;
	}

	public final float getMetersPerChunkBorder() {
		return meters_per_chunk_border;
	}

	public final float getChunkTexScale() {
		return chunk_tex_scale;
	}

	protected final void registerLeaf(int x, int y, LandscapeLeaf leaf) {
		landscape_leaves[y][x] = leaf;
	}

	public final List getTrees() {
		return trees;
	}

	public final boolean[][] getAccessGrid() {
		return access_grid;
	}

	final void makePlaneVector(int x0, int y0, int x1, int y1, int x2, int y2, StrictVector3f plane) {
		makePlaneVector(x0, y0, getWrappedHeight(x0, y0),
				x1, y1, getWrappedHeight(x1, y1),
				x2, y2, getWrappedHeight(x2, y2), plane);
	}

	private final static void makePlaneVector(float h1x, float h1y, float h1z, float h2x, float h2y, float h2z, float h3x, float h3y, float h3z, StrictVector3f plane) {
		float v1x = h2x - h1x;
		float v1y = h2y - h1y;
		float v1z = h2z - h1z;
		float v2x = h3x - h1x;
		float v2y = h3y - h1y;
		float v2z = h3z - h1z;

		vec1.set(v1x, v1y, v1z);
		vec2.set(v2x, v2y, v2z);
		StrictVector3f.cross(vec2, vec1, vec2);

		// Optimization for planeHeight!
		float inv_z = -1f/vec2.z;
		plane.set(vec2.x*inv_z, vec2.y*inv_z, (-vec2.x*h1x - vec2.y*h1y)*inv_z + h1z);
	}

	static float planeHeight(float x, float y, StrictVector3f plane) {
		return plane.x*x + plane.y*y + plane.z;
	}

	private final static float doPlane(float x, float y, float h1x, float h1y, float h1z, float h2x, float h2y, float h2z, float h3x, float h3y, float h3z) {
		makePlaneVector(h1x, h1y, h1z, h2x, h2y, h2z, h3x, h3y, h3z, plane);
		return planeHeight(x, y, plane);
	}

	public final boolean isBelowSeaLevel(int patch_x, int patch_y) {
		int offset_x = patch_x*getGridUnitsPerPatch();
		int offset_y = patch_y*getGridUnitsPerPatch();
		for (int y = 0; y < getGridUnitsPerPatch(); y++) {
			for (int x = 0; x < getGridUnitsPerPatch(); x++) {
				float height = getWrappedHeight(offset_x + x, offset_y + y);
				if (height < getSeaLevelMeters())
					return true;
			}
		}
		return false;
	}

	public final LandscapeLeaf getLeafFromCoordinates(float x_f, float y_f) {
		int patch_x = coordinateToPatch(x_f);
		int patch_y = coordinateToPatch(y_f);
		return landscape_leaves[patch_y][patch_x];
	}

	public final int coordinateToPatch(float f) {
		return (int)(f*inv_meters_per_patch);
	}

	public final float computeInterpolatedHeight(int lod, float x_f, float y_f) {
		x_f *= inv_meters_per_grid_unit;
		y_f *= inv_meters_per_grid_unit;
		return world_instance.getLandscapeIndices().getHeight(lod, x_f, y_f);
	}

	public final float getNearestHeight(float x_f, float y_f) {
		float y_z = computeInterpolatedHeight(world_instance.getLandscapeIndices().getNumLOD() - 1, x_f, y_f);
		return y_z;
	}

	public final float getClampedHeight(int grid_x, int grid_y) {
		if (grid_x < 0 || grid_x >= world.length)
			grid_x = 0;
		if (grid_y < 0 || grid_y >= world.length)
			grid_y = 0;
			
		return getHeight(grid_x, grid_y);
	}

	public final byte getBuildValue(int grid_x, int grid_y) {
		return build_grid[grid_y][grid_x];
	}

	public final boolean canBuild(int grid_x, int grid_y, int val) {
		grid_x = wrapGridCoord(grid_x);
		grid_y = wrapGridCoord(grid_y);
		return build_grid[grid_y][grid_x] >= val;
	}

	public final float getWrappedHeight(int grid_x, int grid_y) {
		grid_x = wrapGridCoord(grid_x);
		grid_y = wrapGridCoord(grid_y);
		return getHeight(grid_x, grid_y);
	}

	private final int wrapGridCoord(int coord) {
		return (coord + getGridUnitsPerWorld())&(getGridUnitsPerWorld() - 1);
	}

	public final float getHeight(int grid_x, int grid_y) {
		return world[grid_y][grid_x];
	}

	public final void editHeight(int grid_x, int grid_y, float height) {
		grid_x = wrapGridCoord(grid_x);
		grid_y = wrapGridCoord(grid_y);
		world[grid_y][grid_x] = height;
		int patch_x1 = grid_x/GRID_UNITS_PER_PATCH;
		int patch_y1 = grid_y/GRID_UNITS_PER_PATCH;
		boolean x_border = patch_x1*GRID_UNITS_PER_PATCH == grid_x;
		boolean y_border = patch_y1*GRID_UNITS_PER_PATCH == grid_y;
		int patch_x0 = (patch_x1 - (x_border ? 1 : 0) + patches_per_world)&(patches_per_world - 1);
		int patch_y0 = (patch_y1 - (y_border ? 1 : 0) + patches_per_world)&(patches_per_world - 1);

		for (int y = patch_y0; y <= patch_y1; y++)
			for (int x = patch_x0; x <= patch_x1; x++)
				landscape_leaves[y][x].editHeight(height);
		world_instance.getNotificationListener().patchesEdited(patch_x0, patch_y0, patch_x1, patch_y1);
	}
}
