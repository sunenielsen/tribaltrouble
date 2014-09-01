package com.oddlabs.tt.render;

import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.oddlabs.tt.landscape.HeightMap;
import com.oddlabs.tt.landscape.LandscapeLeaf;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.util.GLState;
import com.oddlabs.tt.util.GLStateStack;
import com.oddlabs.tt.util.GLUtils;
import com.oddlabs.tt.vbo.VBO;

abstract strictfp class ShadowRenderer {
	private static ShortBuffer shadow_indices = BufferUtils.createShortBuffer(HeightMap.GRID_UNITS_PER_PATCH*HeightMap.GRID_UNITS_PER_PATCH*2*3);
//	private static int shadow_number = 1;
	
	protected static void setupShadows() {
		VBO.releaseIndexVBO();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		GL11.glDepthFunc(GL11.GL_EQUAL);
		GLStateStack.switchState(GLState.VERTEX_ARRAY);
		GL11.glEnable(GL11.GL_TEXTURE_GEN_S);
		GL11.glEnable(GL11.GL_TEXTURE_GEN_T);
		
		// Workaround, See comment in renderShadow
		GL11.glMatrixMode(GL11.GL_TEXTURE);
	}

	protected static void resetShadows() {
		// Workaround, See comment in renderShadow
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		GL11.glDisable(GL11.GL_TEXTURE_GEN_S);
		GL11.glDisable(GL11.GL_TEXTURE_GEN_T);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
	}

	protected final void renderShadow(LandscapeRenderer renderer, float shadow_size, float f_x, float f_y) {
		assert shadow_size >= 0;
		// Redundant glLoadIdentity on the texture matrix stack to work around GL_OBJECT_PLANE not always updating
		GL11.glLoadIdentity();
		float texture_scale = 1f/shadow_size;
		float half_shadow_size = shadow_size*0.5f;
		float texture_x = f_x - half_shadow_size;
		float texture_y = f_y - half_shadow_size;
		int meters_per_grid_unit = HeightMap.METERS_PER_UNIT_GRID;
		int grid_units_per_patch = renderer.getHeightMap().getGridUnitsPerPatch();
		int grid_start_x = StrictMath.max(0, (int)(texture_x/meters_per_grid_unit));
		int grid_start_y = StrictMath.max(0, (int)(texture_y/meters_per_grid_unit));
		int max_grid_index = renderer.getHeightMap().getGridUnitsPerWorld() - 1;
		int grid_end_x = StrictMath.min(max_grid_index, (int)((f_x + half_shadow_size)/meters_per_grid_unit));
		int grid_end_y = StrictMath.min(max_grid_index, (int)((f_y + half_shadow_size)/meters_per_grid_unit));
		int patch_start_x = grid_start_x/grid_units_per_patch;
		int patch_start_y = grid_start_y/grid_units_per_patch;
		int patch_end_x = grid_end_x/grid_units_per_patch;
		int patch_end_y = grid_end_y/grid_units_per_patch;
		
		GLUtils.setupTexGen(texture_scale, texture_scale, -texture_x, -texture_y);
		
		for (int patch_y = patch_start_y; patch_y <= patch_end_y; patch_y++) {
			for (int patch_x = patch_start_x; patch_x <= patch_end_x; patch_x++) {
				int local_start_x = StrictMath.max(grid_start_x, patch_x*grid_units_per_patch)&(grid_units_per_patch - 1);
				int local_start_y = StrictMath.max(grid_start_y, patch_y*grid_units_per_patch)&(grid_units_per_patch - 1);
				int local_end_x = StrictMath.min(grid_end_x, (patch_x + 1)*grid_units_per_patch - 1)&(grid_units_per_patch - 1);
				int local_end_y = StrictMath.min(grid_end_y, (patch_y + 1)*grid_units_per_patch - 1)&(grid_units_per_patch - 1);
				renderer.renderShadow(patch_x, patch_y, local_start_x, local_start_y, local_end_x, local_end_y);
/*//				int number = shadow_number++;
				for (int y = local_start_y; y <= local_end_y; y++) {
					for (int x = local_start_x; x <= local_end_x; x++) {
						leaf.renderShadow(x, y, number, shadow_indices);
					}
				}
				shadow_indices.flip();
				GL11.glDrawElements(GL11.GL_TRIANGLES, shadow_indices);
				shadow_indices.clear();*/
			}
		}
	}
}
