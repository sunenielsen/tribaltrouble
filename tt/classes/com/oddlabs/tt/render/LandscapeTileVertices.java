package com.oddlabs.tt.render;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBBufferObject;

import com.oddlabs.tt.vbo.FloatVBO;
import com.oddlabs.tt.landscape.LandscapeTileIndices;
import com.oddlabs.tt.landscape.HeightMap;

final strictfp class LandscapeTileVertices {
	private final FloatVBO patch_vertex_buffer;
	private final FloatBuffer edit_buffer;
	private final int patch_size;
	private final int elements_per_patch;
	private final int num_patches;
	private final HeightMap heightmap;

	public LandscapeTileVertices(HeightMap heightmap, int patch_exp, int num_patches) {
		this.heightmap = heightmap;
		this.num_patches = num_patches;
		this.patch_size = LandscapeTileIndices.getPatchSize(patch_exp);
		this.elements_per_patch = patch_size*patch_size*3;
		this.edit_buffer = BufferUtils.createFloatBuffer(elements_per_patch);
		int buffer_size = elements_per_patch*num_patches*num_patches;
		FloatBuffer vertices = BufferUtils.createFloatBuffer(buffer_size);
		for (int patch_y = 0; patch_y < num_patches; patch_y++) {
			for (int patch_x = 0; patch_x < num_patches; patch_x++) {
				fillVertexData(vertices, patch_x, patch_y);
			}
		}
		assert !vertices.hasRemaining();
		vertices.rewind();
		patch_vertex_buffer = new FloatVBO(ARBBufferObject.GL_DYNAMIC_DRAW_ARB, vertices.remaining());
		patch_vertex_buffer.put(vertices);
	}

	public final void reload(int patch_x, int patch_y) {
		edit_buffer.clear();
		fillVertexData(edit_buffer, patch_x, patch_y);
		edit_buffer.flip();
		patch_vertex_buffer.putSubData(getVertexIndex(patch_x, patch_y), edit_buffer);
	}

	private int getVertexIndex(int patch_x, int patch_y) {
		return (patch_x + patch_y*num_patches)*elements_per_patch;
	}

	public final void bind(int patch_x, int patch_y) {
		int position = getVertexIndex(patch_x, patch_y);
//System.out.println("patch_x = " + patch_x + " | patch_y = " + patch_y + " | position = " + position);
		patch_vertex_buffer.vertexPointer(3, 0, position);
	}

	private void fillVertexData(FloatBuffer vertex_array, int grid_origin_x, int grid_origin_y) {
		grid_origin_x *= patch_size - 1;
		grid_origin_y *= patch_size - 1;
		int world_border_mask = ~(patch_size - 2);
		for (int y = 0; y < patch_size; y++) {
			for (int x = 0; x < patch_size; x++) {
				int y_coord = grid_origin_y + y;
				int x_coord = grid_origin_x + x;
				boolean is_y_border = y_coord == 0 || y_coord == heightmap.getGridUnitsPerWorld();
				boolean is_x_border = x_coord == 0 || x_coord == heightmap.getGridUnitsPerWorld();
				if (is_y_border)
					x_coord = x_coord&world_border_mask;
				if (is_x_border)
					y_coord = y_coord&world_border_mask;
				float yf = y_coord*HeightMap.METERS_PER_UNIT_GRID;
				float xf = x_coord*HeightMap.METERS_PER_UNIT_GRID;
				float zf = heightmap.getWrappedHeight(x_coord, y_coord);
				vertex_array.put(xf);
				vertex_array.put(yf);
				vertex_array.put(zf);
			}
		}
	}
}
