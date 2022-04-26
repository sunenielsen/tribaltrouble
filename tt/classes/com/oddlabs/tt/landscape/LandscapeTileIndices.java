package com.oddlabs.tt.landscape;

import java.nio.ShortBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

import java.util.List;
import java.util.ArrayList;
import com.oddlabs.util.IndexListOptimizer;
import com.oddlabs.tt.landscape.HeightMap;

/* http://www.gamasutra.com/features/20061221/dallaire_03.shtml */
public final strictfp class LandscapeTileIndices {
	private final static int NUM_LINK_TILES = 15;

	private final HeightMap heightmap;
	private final int patch_exp;
	private final int[] patch_indices_indices;
	private final LandscapeTileTriangle[][][] quad_to_planes;
	/* Team Penguin */
	private final IntBuffer indices;
	/* End Penguin */

	static int getIndex(int patch_exp, int x, int y) {
		int patch_size = getPatchSize(patch_exp);
		return getIndexFromSize(patch_size, x, y);
	}

	static int getIndexFromSize(int patch_size, int x, int y) {
		return x + y*patch_size;
	}

	public static int getPatchSize(int patch_exp) {
		return (1 << patch_exp) + 1;
	}

	static int getX(int patch_exp, int index) {
		int patch_size = getPatchSize(patch_exp);
		return index%patch_size;
	}

	static int getY(int patch_exp, int index) {
		int patch_size = getPatchSize(patch_exp);
		return index/patch_size;
	}


	public int getNumLOD() {
		return getNumLOD(patch_exp);
	}

	public static int getNumLOD(int patch_exp) {
		return patch_exp*2 + 1;
	}

	public static int getNumTriangles(int lod) {
		return 1 << (lod + 1);
	}

	private static int getNumLinkTriangles(int lod) {
		return 1 << (lod/2);
	}

	static int toQuad(int patch_exp, int num_quads_exp, int coord) {
		int quad_size_exp = patch_exp - num_quads_exp;
		return coord >> quad_size_exp;
	}

	static int getNumQuads(int lod) {
		return 1 << getNumQuadsExp(lod);
	}

	static int getNumQuadsExp(int lod) {
		return lod >> 1;
	}

	private static int getPlaneDirection(int quad_size, float x, float y) {
		if (x + y > quad_size) {
			if (x > y)
				return LandscapeTileTriangle.EAST_EXP;
			else
				return LandscapeTileTriangle.NORTH_EXP;
		} else {
			if (x > y)
				return LandscapeTileTriangle.SOUTH_EXP;
			else
				return LandscapeTileTriangle.WEST_EXP;
		}
	}

	private static int countBits(int v) {
		int count = 0;
		for (int i = 0; i < 31; i++) {
			count += v&1;
			v >>= 1;
		}
		return count;
	}

	private int getNumPatchesAndLinks(int lod) {
		return (lod >>1)*(NUM_LINK_TILES + 1 + 1) + (lod&1);
	}

	public LandscapeTileIndices(HeightMap heightmap, int patch_exp) {
		this.heightmap = heightmap;
		this.patch_exp = patch_exp;
		int patch_size = getPatchSize(patch_exp);
		int ne = getIndex(patch_exp, patch_size - 1, patch_size - 1);
		int se = getIndex(patch_exp, patch_size - 1, 0);
		int sw = getIndex(patch_exp, 0, 0);
		int nw = getIndex(patch_exp, 0, patch_size - 1);
		LandscapeTileTriangle lower = new LandscapeTileTriangle(patch_exp, 0, LandscapeTileTriangle.SOUTH_WEST, sw, se, nw);
		LandscapeTileTriangle higher = new LandscapeTileTriangle(patch_exp, 0, LandscapeTileTriangle.NORTH_EAST, ne, nw, se);
		int num_lod = getNumLOD();
		quad_to_planes = new LandscapeTileTriangle[num_lod][][];
		for (int i = 0; i < quad_to_planes.length; i++) {
			int num_quads = getNumQuads(i);
			quad_to_planes[i] = new LandscapeTileTriangle[num_quads*num_quads][4]; // 4 directions
		}
		lower.setupMapping(patch_exp, 0, quad_to_planes);
		higher.setupMapping(patch_exp, 0, quad_to_planes);
		int num_patches = getNumPatchesAndLinks(num_lod);
//int num_patches = num_lod;
		patch_indices_indices = new int[num_patches + 1];
		int index = 0;
		int triangle_index = 0;
		for (int i = 0; i < num_lod; i++) {
			patch_indices_indices[index++] = triangle_index;
			int num_triangles = getNumTriangles(i);
			triangle_index += num_triangles;
			if (i > 0 && i%2 == 1) { // Need link tiles
				int num_extra_link_triangles = getNumLinkTriangles(i);
				for (int j = 1; j <= NUM_LINK_TILES; j++) {
					patch_indices_indices[index++] = triangle_index;
					int size = num_triangles + countBits(j)*num_extra_link_triangles;
					triangle_index += size;
//System.out.println("1i = " + i + " | j = " + j + " | size = " + size);
				}
			}
		}
		patch_indices_indices[index++] = triangle_index;
		assert index == patch_indices_indices.length: index + " " + patch_indices_indices.length;
		/* Team Penguin */
		this.indices = BufferUtils.createIntBuffer(triangle_index*3);
		/* End Penguin */
		for (int i = 0; i < num_lod; i++) {
			buildTile(lower, higher, i, 0, indices);
			if (i%2 == 1) { // Need link tiles
				for (int j = 1; j <= NUM_LINK_TILES; j++) {
//int old = indices.position();
					buildTile(lower, higher, i, j, indices);
/*int size = (indices.position() - old)/3;
System.out.println("2i = " + i + " | j = " + j + " | size = " + size);*/
				}
			}
		}
		assert !indices.hasRemaining();
		indices.rewind();
/*for (int i = 2*3; i < 2*3 + 4*3; i++)
System.out.println("indices.get(i) = " + indices.get(i));
for (int i = 0; i < 6; i++)
System.out.println("indices.get(i) = " + indices.get(i));
System.out.println("indices.remaining() = " + indices.remaining());*/
	}

/* Team Penguin */
	public final IntBuffer getIndices() {
		return indices;
	}

	private static void buildTile(LandscapeTileTriangle lower, LandscapeTileTriangle higher, int lod, int border_set, IntBuffer indices) {
		int pos = indices.position();
		lower.putIndices(lod, border_set, indices);
		higher.putIndices(lod ,border_set, indices);
		int saved_pos = indices.position();
		int saved_limit = indices.limit();
		indices.limit(saved_pos);
		indices.position(pos);
		IndexListOptimizer.optimize(indices);
		indices.limit(saved_limit);
		indices.position(saved_pos);
	}
/* End Penguin */

	public final void fillCoverIndices(ShortBuffer buffer, int lod, int border_set, int start_x, int start_y, int end_x, int end_y) {
		border_set = adjustBorderSet(lod, border_set);
		int num_quads_exp = getNumQuadsExp(lod);
		int quad_size_exp = patch_exp - num_quads_exp;
		int quad_size = 1 << quad_size_exp;
		int start_quad_x = start_x>>quad_size_exp;
		int start_quad_y = start_y>>quad_size_exp;
		int end_quad_x = end_x>>quad_size_exp;
		int end_quad_y = end_y>>quad_size_exp;
		LandscapeTileTriangle[][] quads = quad_to_planes[lod];
		for (int x = start_quad_x; x <= end_quad_x; x++)
			for (int y = start_quad_y; y <= end_quad_y; y++) {
				LandscapeTileTriangle[] quad = quads[getQuadIndex(num_quads_exp, x, y)];
				LandscapeTileTriangle north = quad[LandscapeTileTriangle.NORTH_EXP];
				LandscapeTileTriangle south = quad[LandscapeTileTriangle.SOUTH_EXP];
				LandscapeTileTriangle east = quad[LandscapeTileTriangle.EAST_EXP];
				LandscapeTileTriangle west = quad[LandscapeTileTriangle.WEST_EXP];
				north.addIndices(buffer, border_set);
				south.addIndices(buffer, border_set);
				if (east != north && east != south) {
					east.addIndices(buffer, border_set);
					west.addIndices(buffer, border_set);
				}
			}
	}

	float getHeight(int lod, float x, float y) {
		int x_int = (int)x;
		int y_int = (int)y;
		int patch_size = getPatchSize(patch_exp);
		int num_quads_exp = getNumQuadsExp(lod);
		int quad_size_exp = patch_exp - num_quads_exp;
		int quad_size = 1 << quad_size_exp;
		int quad_mask = ~(quad_size - 1);
		int quad_x = x_int & quad_mask;
		int quad_y = y_int & quad_mask;
		int plane_direction = getPlaneDirection(quad_size, x - quad_x, y - quad_y);
		int mask = patch_size - 2;
		int patch_x = x_int & (~mask);
		int patch_y = y_int & (~mask);
		int patch_relative_x = x_int&mask;
		int patch_relative_y = y_int&mask;
		LandscapeTileTriangle[] quad = quad_to_planes[lod][getQuadIndex(patch_exp, num_quads_exp, patch_relative_x, patch_relative_y)];
		LandscapeTileTriangle plane = quad[plane_direction];
		plane.update(heightmap, patch_x, patch_y);
		float plane_height = plane.getHeight(heightmap, x, y);
/*if (debug) {
System.out.println("plane = " + plane);
System.out.println("x_int = " + x_int + " | y_int = " + y_int + " | patch_size = " + patch_size + " | mask = " + mask + " | patch_relative_x = " + patch_relative_x + " | patch_relative_y = " + patch_relative_y);
System.out.println("num_quads_exp = " + num_quads_exp + " | patch_x = " + patch_x + " | patch_y = " + patch_y);
System.out.println("plane_height = " + plane_height);
}*/
		return plane_height;
	}

	private static int getQuadIndex(int num_quads_exp, int quad_x, int quad_y) {
		return quad_x + (quad_y << num_quads_exp);
	}

	static int getQuadIndex(int patch_exp, int num_quads_exp, int x, int y) {
		int quad_y = toQuad(patch_exp, num_quads_exp, y);
		int quad_x = toQuad(patch_exp, num_quads_exp, x);
		return getQuadIndex(num_quads_exp, quad_x, quad_y);
	}

/*public static boolean debug;
	final void test() {
		for (int i = getNumLOD() - 1; i >= 0; i--) {
			int num_quads_exp = getNumQuadsExp(i);
			int skip = 1 << (patch_exp - num_quads_exp);
			for (int y = 0; y < heightmap.getGridUnitsPerWorld(); y += skip)
				for (int x = 0; x < heightmap.getGridUnitsPerWorld(); x += skip) {
					float height = heightmap.getWrappedHeight(x, y);
					float plane_height = getHeight(i, x, y);
					if (StrictMath.abs(plane_height - height) > .001f) {
System.out.println("getNumLOD() = " + getNumLOD());
System.out.println("i = " + i + " | num_quads_exp = " + num_quads_exp + " | patch_exp = " + patch_exp + " | skip = " + skip + " | x = " + x + " | y = " + y);
System.out.println("height = " + height + " | plane_height = " + plane_height);
						assert false;
					}
				}
		}
	}
*/
	public final Errors computeErrors(int patch_index_x, int patch_index_y) {
		float[] errors = new float[getNumLOD() - 1];
		boolean above = false;
		boolean below = false;

		int patch_size = getPatchSize(patch_exp);
		int offset_x = patch_index_x*(patch_size - 1);
		int offset_y = patch_index_y*(patch_size - 1);
		for (int i = 0; i < errors.length; i++) {
			float max_error = Float.NEGATIVE_INFINITY;
			LandscapeTileTriangle[][] current_quads = quad_to_planes[i];
			for (int j = 0; j < current_quads.length; j++)
				for (int k = 0; k < current_quads[j].length; k++) {
					current_quads[j][k].update(heightmap, offset_x, offset_y);
				}
			int num_quads_exp = getNumQuadsExp(i);
			int quad_size_exp = patch_exp - num_quads_exp;
			int quad_size = 1 << quad_size_exp;
			int quad_mask = quad_size - 1;
			for (int y = 0; y < patch_size - 1; y++) {
				int quad_y = y & quad_mask;
				for (int x = 0; x < patch_size - 1; x++) {
					int quad_x = x & quad_mask;
					int plane_direction = getPlaneDirection(quad_size, quad_x, quad_y);
					LandscapeTileTriangle plane = current_quads[getQuadIndex(patch_exp, num_quads_exp, x, y)][plane_direction];
					float plane_height = plane.getHeight(heightmap, offset_x + x, offset_y + y);
					float actual_height = heightmap.getWrappedHeight(offset_x + x, offset_y + y);
					float error = StrictMath.abs(plane_height - actual_height);
					if (error > max_error)
						max_error = error;

					if (actual_height < heightmap.getSeaLevelMeters())
						below = true;
					else if (actual_height > heightmap.getSeaLevelMeters())
						above = true;
				}
			}
			errors[i] = max_error;
		}
/*System.out.print("patch_index_x = " + patch_index_x + " | patch_index_y = " + patch_index_y);
for (int i = 0; i < errors.length; i++)
System.out.print(" " + errors[i]);
System.out.println();
*/
		return new Errors(errors, below && above);
	}

	private int adjustBorderSet(int lod, int border_set) {
		return (lod&1) == 1 ? border_set : 0;
	}

	public final int getPatchIndex(int lod, int border_set) {
		int border_offset = adjustBorderSet(lod, border_set);
		return getNumPatchesAndLinks(lod) + border_offset;
	}

	public final int getTriangleIndex(int patch_index) {
		return patch_indices_indices[patch_index];
	}
}
