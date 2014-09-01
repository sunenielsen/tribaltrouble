package com.oddlabs.tt.resource;

import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.PixelFormat;

import com.oddlabs.tt.form.ProgressForm;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.procedural.Landscape;
import com.oddlabs.tt.landscape.HeightMap;
import com.oddlabs.tt.render.Texture;
import com.oddlabs.tt.util.GLState;
import com.oddlabs.tt.util.GLStateStack;
import com.oddlabs.tt.util.OffscreenRenderer;
import com.oddlabs.tt.util.OffscreenRendererFactory;

public final strictfp class IslandGenerator implements WorldGenerator {
	private final static long serialVersionUID = 1;

	private final static int TEXELS_PER_CHUNK = 512;
	private final static int IDEAL_TEXELS_PER_DETAIL = 256;
	private final static float IDEAL_DETAIL_ALPHA = .15f;

	private final int meters_per_world;
	private final int terrain_type;
	private final int grid_units;

	private final float hills;
	private final float vegetation_amount;
	private final float supplies_amount;
	private final int seed;

	public IslandGenerator(int meters_per_world, int terrain_type, float hills, float vegetation_amount, float supplies_amount, int seed) {
		this.hills = hills;
		this.vegetation_amount = vegetation_amount;
		this.supplies_amount = supplies_amount;
		this.seed = seed;
		this.grid_units = meters_per_world/HeightMap.METERS_PER_UNIT_GRID;
		this.meters_per_world = meters_per_world;
		this.terrain_type = terrain_type;
	}

	private final Texture createDetail(GLImage detail_image, int base_level) {
		GLImage[] detail_mipmaps = detail_image.buildMipMaps();
		GLImage.updateMipMapsArea(detail_mipmaps, base_level, Globals.LANDSCAPE_DETAIL_FADEOUT_FACTOR,
								  0, 0, detail_mipmaps[0].getWidth(), detail_mipmaps[0].getHeight(), false);
		return new Texture(detail_mipmaps, Globals.COMPRESSED_RGBA_FORMAT, GL11.GL_LINEAR_MIPMAP_LINEAR,
								  GL11.GL_LINEAR, GL11.GL_REPEAT, GL11.GL_REPEAT);
	}

	private final int getTexelsPerGridUnit() {
		int texels_per_grid_unit = Globals.TEXELS_PER_GRID_UNIT/(int)StrictMath.pow(2, Globals.TEXTURE_MIP_SHIFT[Settings.getSettings().graphic_detail]);
		return texels_per_grid_unit;
	}

	public final int getTerrainType() {
		return terrain_type;
	}
	
	public final int getMetersPerWorld() {
		return meters_per_world;
	}

	public final FogInfo createFogInfo() {
		return Landscape.getFogInfo(terrain_type, meters_per_world);
	}

	public final WorldInfo generate(int num_players, int initial_unit_count, float random_start_pos) {
		int colormap_size = grid_units*getTexelsPerGridUnit();
		int chunks_per_colormap = colormap_size/TEXELS_PER_CHUNK;

		// Build landscape
		long time_before = System.currentTimeMillis();
		int base_level = Globals.LANDSCAPE_DETAIL_FADEOUT_BASE_LEVEL;
		int detail_mip_level = IDEAL_TEXELS_PER_DETAIL/Globals.DETAIL_SIZE - 1;
		int detail_prefade_level = detail_mip_level - base_level;
		if (detail_prefade_level < 0)
			detail_prefade_level = 0;
		float detail_prefade = IDEAL_DETAIL_ALPHA*(float)StrictMath.pow(Globals.LANDSCAPE_DETAIL_FADEOUT_FACTOR, detail_prefade_level);
		base_level -= detail_mip_level;
		if (base_level < 1)
			base_level = 1;
		Landscape landscape = new Landscape(num_players, meters_per_world, terrain_type, detail_prefade, hills, vegetation_amount, supplies_amount, seed, initial_unit_count, random_start_pos);
		long time_after = System.currentTimeMillis();
System.out.println("Landscape created in = " + (time_after-time_before));
		BlendInfo[] blend_infos = landscape.getBlendInfos();
		Texture detail = createDetail(landscape.getDetail(), base_level);
		float[][] heightmap = landscape.getHeight();
/*for (int y = 0; y < heightmap.length; y++)
	for (int x = 0; x < heightmap[y].length; x++)
		heightmap[y][x] = y/10;*/
		List trees = landscape.getTrees();
		List palm_trees = landscape.getPalmtrees();
		List rock = landscape.getRock();
		List iron = landscape.getIron();
		float[][] plants = landscape.getPlants();
		boolean[][] access_grid = landscape.getAccessGrid();
		byte[][] build_grid= landscape.getBuildGrid();
		float[][] starting_locations = landscape.getStartingLocations();
		int alpha_size = grid_units;
		Texture[][] chunk_maps;
		OffscreenRendererFactory factory = new OffscreenRendererFactory();
		do {
			chunk_maps = blendTextures(factory, chunks_per_colormap, blend_infos, alpha_size, Globals.STRUCTURE_SIZE, colormap_size/alpha_size);
		} while (chunk_maps == null);
		ProgressForm.progress();
		return new WorldInfo(meters_per_world, landscape.getSeaLevelMeters(), colormap_size, chunks_per_colormap, chunk_maps, detail, heightmap, trees, palm_trees, rock, iron, plants, access_grid, build_grid, starting_locations);
	}

	private final static Texture[][] blendTextures(OffscreenRendererFactory factory, int chunks_per_colormap, BlendInfo[] blend_infos, int alpha_size, int structure_size, int scale) {
		boolean use_pbuffer = Settings.getSettings().usePbuffer();
		boolean use_fbo = Settings.getSettings().useFBO();
		OffscreenRenderer offscreen = factory.createRenderer(TEXELS_PER_CHUNK, TEXELS_PER_CHUNK, new PixelFormat(Globals.VIEW_BIT_DEPTH, 0, 0, 0, 0), Settings.getSettings().use_copyteximage, use_pbuffer, use_fbo);
		GL11.glColor4f(1f, 1f, 1f, 1f);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0f, TEXELS_PER_CHUNK, 0, TEXELS_PER_CHUNK, -1f, 1f);

		GL11.glMatrixMode(GL11.GL_TEXTURE);
		GLState.activeTexture(GL13.GL_TEXTURE1);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
		GL11.glDisable(GL11.GL_TEXTURE_GEN_S);
		GL11.glDisable(GL11.GL_TEXTURE_GEN_T);
		GL11.glLoadIdentity();
		GLState.activeTexture(GL13.GL_TEXTURE0);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
		GL11.glDisable(GL11.GL_TEXTURE_GEN_S);
		GL11.glDisable(GL11.GL_TEXTURE_GEN_T);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		float alpha_texel_size = 1f/(alpha_size*scale);
		float structure_texel_size = 1f/structure_size;
		float structure_texels_per_chunk_border = Globals.TEXELS_PER_CHUNK_BORDER*structure_texel_size;
		float structure_offset = TEXELS_PER_CHUNK*structure_texel_size - 2*structure_texels_per_chunk_border;
		float structure_length = structure_offset + 2*structure_texels_per_chunk_border;

		float alpha_texels_per_chunk_border = Globals.TEXELS_PER_CHUNK_BORDER*alpha_texel_size;
		float alpha_offset = TEXELS_PER_CHUNK*alpha_texel_size;
		float alpha_length = alpha_offset + 2*alpha_texels_per_chunk_border;

		Texture[][] chunk_maps = new Texture[chunks_per_colormap][chunks_per_colormap];
		FloatBuffer coordinates = BufferUtils.createFloatBuffer(4*3);
		coordinates.put(new float[]{0f, 0f, 0f,
									TEXELS_PER_CHUNK, 0f, 0f,
									TEXELS_PER_CHUNK, TEXELS_PER_CHUNK, 0f,
									0f, TEXELS_PER_CHUNK, 0f});
		coordinates.rewind();
		FloatBuffer structure_tex_coords = BufferUtils.createFloatBuffer(4*2);
		FloatBuffer alpha_tex_coords = BufferUtils.createFloatBuffer(4*2);
		GLStateStack.switchState(GLState.VERTEX_ARRAY | GLState.TEXCOORD0_ARRAY | GLState.TEXCOORD1_ARRAY);
		GL11.glVertexPointer(3, 0, coordinates);
		GL11.glTexCoordPointer(2, 0, structure_tex_coords);
		GLState.clientActiveTexture(GL13.GL_TEXTURE1);
		GL11.glTexCoordPointer(2, 0, alpha_tex_coords);
		GLState.clientActiveTexture(GL13.GL_TEXTURE0);
		for (int y = 0; y < chunk_maps.length; y++) {
			for (int x = 0; x < chunk_maps[y].length; x++) {
				chunk_maps[y][x] = new Texture(TEXELS_PER_CHUNK, TEXELS_PER_CHUNK, GL11.GL_LINEAR_MIPMAP_NEAREST, GL11.GL_LINEAR, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, Globals.NO_MIPMAP_CUTOFF);
				int mip_scale = 1;
				int mip_level = 0;
				int mip_size = TEXELS_PER_CHUNK;
				while (mip_size >= 1) {
					GL11.glLoadIdentity();
					GL11.glScalef(1f/mip_scale, 1f/mip_scale, 1f);
					GL11.glEnable(GL11.GL_BLEND);

					for (int i = 0; i < blend_infos.length; i++) {
						blend_infos[i].setup();
						float structure_offset_u = x*structure_offset - structure_texels_per_chunk_border;
						float structure_offset_v = y*structure_offset - structure_texels_per_chunk_border;
						float alpha_offset_u = x*alpha_offset - alpha_texels_per_chunk_border;
						float alpha_offset_v = y*alpha_offset - alpha_texels_per_chunk_border;
						structure_tex_coords.put(0, structure_offset_u).put(1, structure_offset_v);
						structure_tex_coords.put(2, structure_offset_u + structure_length).put(3, structure_offset_v);
						structure_tex_coords.put(4, structure_offset_u + structure_length).put(5, structure_offset_v + structure_length);
						structure_tex_coords.put(6, structure_offset_u).put(7, structure_offset_v + structure_length);
						alpha_tex_coords.put(0, alpha_offset_u).put(1, alpha_offset_v);
						alpha_tex_coords.put(2, alpha_offset_u + alpha_length).put(3, alpha_offset_v);
						alpha_tex_coords.put(4, alpha_offset_u + alpha_length).put(5, alpha_offset_v + alpha_length);
						alpha_tex_coords.put(6, alpha_offset_u).put(7, alpha_offset_v + alpha_length);
						GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);
						blend_infos[i].reset();
					}
/*if (mip_level == 0)
offscreen.dumpToFile("colormap-" + x + "-" + y);*/
					offscreen.copyToTexture(chunk_maps[y][x], mip_level, Globals.COMPRESSED_RGB_FORMAT, 0, 0, mip_size, mip_size);
					mip_scale <<= 1;
					mip_level++;
					mip_size >>= 1;
				}
			}
		}
		boolean succeeded = offscreen.destroy();
		if (!succeeded) {
/*			for (int y = 0; y < chunk_maps.length; y++)
				for (int x = 0; x < chunk_maps[y].length; x++)
					chunk_maps[y][x].delete();*/
			return null;
		} else
			return chunk_maps;
	}
}
