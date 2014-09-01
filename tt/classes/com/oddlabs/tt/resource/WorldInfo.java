package com.oddlabs.tt.resource;

import com.oddlabs.tt.render.*;

import java.util.List;

public final strictfp class WorldInfo {
	public final Texture[][] colormaps;
	public final Texture detail;
	public final float[][] heightmap;
	public final List trees;
	public final List palm_trees;
	public final List rocks;
	public final List iron;
	public final float[][] plants;
	public final boolean[][] access_grid;
	public final byte[][] build_grid;
	public final int meters_per_world;
	public final float sea_level_meters;
	public final int texels_per_colormap;
	public final int chunks_per_colormap;
	public final float[][] starting_locations;

	public WorldInfo(int meters_per_world, float sea_level_meters, int texels_per_colormap, int chunks_per_colormap, Texture[][] colormaps, Texture detail, float[][] heightmap, List trees, List palm_trees, List rocks, List iron, float[][] plants, boolean[][] access_grid, byte[][] build_grid, float[][] starting_locations) {
		this.texels_per_colormap = texels_per_colormap;
		this.chunks_per_colormap = chunks_per_colormap;
		this.sea_level_meters = sea_level_meters;
		this.meters_per_world = meters_per_world;
		this.colormaps = colormaps;
		this.detail = detail;
		this.heightmap = heightmap;
		this.trees = trees;
		this.rocks = rocks;
		this.iron = iron;
		this.plants = plants;
		this.palm_trees = palm_trees;
		this.access_grid = access_grid;
		this.build_grid = build_grid;
		this.starting_locations = starting_locations;
	}
}
