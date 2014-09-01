package com.oddlabs.tt.scenery;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.landscape.HeightMap;
import com.oddlabs.tt.procedural.GeneratorClouds;
import com.oddlabs.tt.render.Texture;
import com.oddlabs.tt.resource.ResourceDescriptor;
import com.oddlabs.tt.resource.Resources;
import com.oddlabs.tt.util.Stitcher;
import com.oddlabs.tt.util.GLState;
import com.oddlabs.tt.util.GLStateStack;
import com.oddlabs.tt.vbo.FloatVBO;
import com.oddlabs.tt.vbo.ShortVBO;
import com.oddlabs.tt.util.GLUtils;
import com.oddlabs.tt.render.LandscapeRenderer;

public final strictfp class Sky {
	private final static float[] SKYDOME_SPEED_OUTER = {0.2f, 0f};
	private final static float[] SKYDOME_SPEED_INNER = {0.4f, 0f};
	private final static float SKYDOME_HEIGHT = 0f;
	private final static int SKYDOME_GRADIENT_LENGTH = 20;
	private final static int SKYDOME_DEFAULT_COLOR = 8;
	
	private final static float[][] SKYDOME_INITCOLOR = {{0.90f, 0.95f, 1f}, {1.50f, 0.90f, 0.65f}};
	private final static float[][] SKYDOME_GRADIENT = {{0.75f, 0.825f, 0.95f}, {0.6f, 0.6f, 0.85f}};

	private final static float SKYDOME_OUTER_UTILING = 8f;
	private final static float SKYDOME_OUTER_VTILING = 8f;
	private final static float SKYDOME_INNER_UTILING = 8f;
	private final static float SKYDOME_INNER_VTILING = 8f;

	private final static int NUM_WATER_RINGS = 6;

	private final static float START_ANGLE = -(float)StrictMath.PI/4f;

	private final static float[][] tex_env_color = new float[][] {{0.95f, 0.975f, 1f, 1f}, {1f, 0.95f, 0.8f, 1f}};

	private final FloatBuffer color;
	private final ShortVBO[] strip_indices;
	private final ShortVBO fan_indices;
	private final FloatVBO water_vertices;
	private final FloatVBO bottom_vertices;
	private final ShortVBO water_indices;
	private final LandscapeRenderer landscape_renderer;
	private FloatVBO sky_vertices;
	private FloatVBO sky_tex0;
	private FloatVBO sky_tex1;
	private FloatVBO sky_colors;

	private final Texture[] clouds;
	private final int subdiv_axis;
	private final int subdiv_height;
	private final int terrain_type;

	public Sky(LandscapeRenderer renderer, int terrain_type) {
		this(renderer, terrain_type, (float)(renderer.getHeightMap().getMetersPerWorld()*StrictMath.sqrt(2)/2), 6000f, 20, 20, SKYDOME_OUTER_UTILING, SKYDOME_OUTER_VTILING, SKYDOME_INNER_UTILING, SKYDOME_INNER_VTILING, renderer.getHeightMap().getMetersPerWorld()/2, renderer.getHeightMap().getMetersPerWorld()/2, SKYDOME_HEIGHT);
	}

	private final void setupSky() {
		float time = LocalEventQueue.getQueue().getTime();
		float speed_scale = 0.01f;
		float outer_dx = SKYDOME_SPEED_OUTER[0] * time*speed_scale;
		float outer_dy = SKYDOME_SPEED_OUTER[1] * time*speed_scale;
		float inner_dx = SKYDOME_SPEED_INNER[0] * time*speed_scale;
		float inner_dy = SKYDOME_SPEED_INNER[1] * time*speed_scale;
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, clouds[GeneratorClouds.INNER].getHandle());
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
		GL11.glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, color);
		GL11.glMatrixMode(GL11.GL_TEXTURE);
		GL11.glTranslatef(outer_dx, outer_dy, 0);
		GLState.activeTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, clouds[GeneratorClouds.OUTER].getHandle());
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glTranslatef(inner_dx, inner_dy, 0);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
		GL11.glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, color);
		

		GLStateStack.switchState(GLState.VERTEX_ARRAY | GLState.COLOR_ARRAY | GLState.TEXCOORD0_ARRAY | GLState.TEXCOORD1_ARRAY);
		sky_vertices.vertexPointer(3, 0, 0);
		sky_tex0.texCoordPointer(2, 0, 0);
		sky_colors.colorPointer(3, 0, 0);
		GLState.clientActiveTexture(GL13.GL_TEXTURE1);
		sky_tex1.texCoordPointer(2, 0, 0);
		GLState.clientActiveTexture(GL13.GL_TEXTURE0);
	}

	private final void resetSky() {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glLoadIdentity();
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_DECAL);
		GLState.activeTexture(GL13.GL_TEXTURE0);
		GL11.glLoadIdentity();
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}

	public final void render() {
		setupSky();

		int end = subdiv_axis*(subdiv_height - 1);
		for (int i = 0; i < strip_indices.length; i++)
		   strip_indices[i].drawRangeElements(GL11.GL_TRIANGLE_STRIP, 0, end, subdiv_axis*2 + 2, 0);
		fan_indices.drawRangeElements(GL11.GL_TRIANGLE_FAN, 0, end, subdiv_axis + 2, 0);
		resetSky();
	}

	public final void renderSeaBottom() {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(Globals.SEA_BOTTOM_COLOR[terrain_type][0], Globals.SEA_BOTTOM_COLOR[terrain_type][1], Globals.SEA_BOTTOM_COLOR[terrain_type][2]);

		GLStateStack.switchState(GLState.VERTEX_ARRAY);
		if (Globals.draw_detail) {
			GLState.activeTexture(GL13.GL_TEXTURE1);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			landscape_renderer.bindDetail();
			GL11.glMatrixMode(GL11.GL_TEXTURE);
			GL11.glLoadIdentity();
			GL11.glEnable(GL11.GL_TEXTURE_GEN_S);
			GL11.glEnable(GL11.GL_TEXTURE_GEN_T);
			GLUtils.setupTexGen(1, 1, 0, 0);
			GL11.glScalef(Globals.LANDSCAPE_DETAIL_REPEAT_RATE, Globals.LANDSCAPE_DETAIL_REPEAT_RATE, 1);
		}

		bottom_vertices.vertexPointer(3, 0, 0);
		water_indices.drawRangeElements(GL11.GL_TRIANGLES, 0, bottom_vertices.capacity()/3, water_indices.capacity(), 0);

		if (Globals.draw_detail) {
			GL11.glDisable(GL11.GL_TEXTURE_GEN_S);
			GL11.glDisable(GL11.GL_TEXTURE_GEN_T);
			GL11.glLoadIdentity();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GLState.activeTexture(GL13.GL_TEXTURE0);
		}

		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private Sky(LandscapeRenderer landscape_renderer, int terrain_type, float inner_radius, float radius, int subdiv_axis, int subdiv_height, float outer_utile, float outer_vtile, float inner_utile, float inner_vtile, float origin_x, float origin_y, float origin_z) {
		this.landscape_renderer = landscape_renderer;
		this.terrain_type = terrain_type;
		this.subdiv_axis = subdiv_axis;
		this.subdiv_height = subdiv_height;
		this.color = BufferUtils.createFloatBuffer(4).put(tex_env_color[terrain_type]);
		color.rewind();
		ResourceDescriptor clouds_desc = new GeneratorClouds(terrain_type);
		clouds = (Texture[])Resources.findResource(clouds_desc);
		makeSkyVertices(radius, outer_utile, outer_vtile, inner_utile, inner_vtile, origin_x, origin_y, origin_z);
		strip_indices = makeSkyStripIndices();
		fan_indices = makeSkyFanIndices();
		List vertices_stitch_list = new ArrayList();
		List stitch_indices_list = new ArrayList();
		int num_vertices = 0;
		int num_indices = 0;
		SkyStitchVertex[] previous_vertices = makeLandscapeVertices(landscape_renderer.getHeightMap());
		vertices_stitch_list.add(previous_vertices);
		num_vertices += previous_vertices.length;
		for (int i = 0; i < NUM_WATER_RINGS; i++) {
			float radius_factor = (float)(i + 1)/NUM_WATER_RINGS;
			float ring_radius = inner_radius + (float)StrictMath.pow(radius - inner_radius, radius_factor);
			SkyStitchVertex[] ring_vertices = makeDomeVertices(landscape_renderer.getHeightMap(), i + 1, num_vertices, ring_radius, origin_x, origin_y);
			vertices_stitch_list.add(ring_vertices);
			num_vertices += ring_vertices.length;
			SkyStitchVertex[] stitch_vertices = new SkyStitchVertex[ring_vertices.length + previous_vertices.length];
			System.arraycopy(previous_vertices, 0, stitch_vertices, 0, previous_vertices.length);
			System.arraycopy(ring_vertices, 0, stitch_vertices, previous_vertices.length, ring_vertices.length);
			ShortBuffer stitch_indices = Stitcher.stitch(stitch_vertices);
			stitch_indices_list.add(stitch_indices);
			num_indices += stitch_indices.remaining();
			previous_vertices = ring_vertices;
		}
		SkyStitchVertex[] all_vertices = new SkyStitchVertex[num_vertices];
		int index = 0;
		for (int i = 0; i < vertices_stitch_list.size(); i++) {
			SkyStitchVertex[] vertices = (SkyStitchVertex[])vertices_stitch_list.get(i);
			System.arraycopy(vertices, 0, all_vertices, index, vertices.length);
			index += vertices.length;
		}
		assert index == all_vertices.length;
		ShortBuffer all_indices = BufferUtils.createShortBuffer(num_indices);
		for (int i = 0; i < stitch_indices_list.size(); i++) {
			ShortBuffer indices = (ShortBuffer)stitch_indices_list.get(i);
			all_indices.put(indices);
		}
		assert !all_indices.hasRemaining();
		all_indices.flip();
		water_indices = new ShortVBO(ARBBufferObject.GL_STATIC_DRAW_ARB, all_indices);
		water_vertices = toVBO(all_vertices, landscape_renderer.getHeightMap().getSeaLevelMeters());
		bottom_vertices = toVBO(all_vertices, 0);
	}

	private static FloatVBO toVBO(SkyStitchVertex[] vertices, float height) {
		FloatBuffer vertex_buffer = BufferUtils.createFloatBuffer(vertices.length*3);
		for (int i = 0; i < vertices.length; i++) {
			SkyStitchVertex vertex = vertices[i];
			assert vertex.getIndex() == i: vertex.getIndex() + " " + i;
			float x = vertex.x;
			float y = vertex.y;
			float z = (height*(NUM_WATER_RINGS - vertex.getSide()))/NUM_WATER_RINGS;
//System.out.println("z = " + z + " | vertex.getSide() = " + vertex.getSide() + " | NUM_WATER_RIGS = " + NUM_WATER_RINGS + " | height = " + height + " " + (z == height));
			vertex_buffer.put(x).put(y).put(z);
		}
		assert !vertex_buffer.hasRemaining();
		vertex_buffer.flip();
		return new FloatVBO(ARBBufferObject.GL_STATIC_DRAW_ARB, vertex_buffer);
	}

	public final FloatVBO getWaterVertices() {
		return water_vertices;
	}
	
	public final ShortVBO getWaterIndices() {
		return water_indices;
	}

	private final void makeSkyVertices(float radius, float outer_utile, float outer_vtile, float inner_utile, float inner_vtile, float origin_x, float origin_y, float origin_z) {
		float r;
		float x, y, z;
		float height_coeff;
		float dome_height = radius;
		float h_angle_inc = ((float)java.lang.StrictMath.PI/2)/(subdiv_height - 1);
		float a_angle_inc = (float)java.lang.StrictMath.PI*2/subdiv_axis;
//		vertices.dumpInfo();
		float offset_angle = a_angle_inc/2f;
		int num_vertices = subdiv_axis*(subdiv_height - 1) + 1;
		float[] vertices = new float[num_vertices*3];
		float[] tex0 = new float[num_vertices*2];
		float[] tex1 = new float[num_vertices*2];
		float[] colors = new float[num_vertices*3];


		// calculate skydome gradient colors
		float[] skydome_default_color = new float[] {
			(float)StrictMath.pow(SKYDOME_GRADIENT[terrain_type][0], SKYDOME_DEFAULT_COLOR),
			(float)StrictMath.pow(SKYDOME_GRADIENT[terrain_type][1], SKYDOME_DEFAULT_COLOR),
			(float)StrictMath.pow(SKYDOME_GRADIENT[terrain_type][2], SKYDOME_DEFAULT_COLOR)
		};
		float[][] skydome_gradient = new float[SKYDOME_GRADIENT_LENGTH][3];
		skydome_gradient[0] = SKYDOME_INITCOLOR[terrain_type];
		
		float alpha;
		for (int i = 1; i < SKYDOME_GRADIENT_LENGTH; i++) {
			alpha = (float)i/(SKYDOME_GRADIENT_LENGTH - 1);
			skydome_gradient[i] = new float[] {
				alpha*skydome_default_color[0] + (1f - alpha)*skydome_gradient[i - 1][0]*SKYDOME_GRADIENT[terrain_type][0],
				alpha*skydome_default_color[1] + (1f - alpha)*skydome_gradient[i - 1][1]*SKYDOME_GRADIENT[terrain_type][1],
				alpha*skydome_default_color[2] + (1f - alpha)*skydome_gradient[i - 1][2]*SKYDOME_GRADIENT[terrain_type][2]
			};
		}

		for (int i = 0; i < subdiv_height - 1; i++) {
			z = (float)java.lang.StrictMath.sin(h_angle_inc*i)*radius;
			r = (float)java.lang.StrictMath.cos(h_angle_inc*i)*radius;

			if (java.lang.StrictMath.abs(z) < 250f)
				height_coeff = dome_height/250f;
			else
				height_coeff = dome_height/z;

			for (int j = 0; j < subdiv_axis; j++) {
				x = (float)java.lang.StrictMath.cos(START_ANGLE + a_angle_inc*j + offset_angle*i)*r;
				y = (float)java.lang.StrictMath.sin(START_ANGLE + a_angle_inc*j + offset_angle*i)*r;
				if (i < SKYDOME_GRADIENT_LENGTH)
					putArray(skydome_gradient[i], i*subdiv_axis + j, colors);
				else
					putArray(skydome_default_color, i*subdiv_axis + j, colors);

				putArray(new float[]{x + origin_x, y + origin_y, z + origin_z}, i*subdiv_axis + j, vertices);
				putArray(new float[]{x*height_coeff/(radius*outer_utile) + 0.5f, y*height_coeff/(radius*outer_vtile) + 0.5f}, i*subdiv_axis + j, tex0);
				putArray(new float[]{x*height_coeff/(radius*inner_utile) + 0.5f, y*height_coeff/(radius*inner_vtile) + 0.5f}, i*subdiv_axis + j, tex1);
			}
		}
		int last_index = subdiv_axis*(subdiv_height - 1);
		if (subdiv_height - 1 < SKYDOME_GRADIENT_LENGTH)
			putArray(skydome_gradient[subdiv_height - 1], last_index, colors);
		else
			putArray(skydome_default_color, last_index, colors);

		putArray(new float[]{origin_x, origin_y, radius + origin_z}, last_index, vertices);
		putArray(new float[]{0.5f, 0.5f}, last_index, tex0);
		putArray(new float[]{0.5f, 0.5f}, last_index, tex1);

		
		sky_vertices = new FloatVBO(ARBBufferObject.GL_STATIC_DRAW_ARB, vertices);
		sky_tex0 = new FloatVBO(ARBBufferObject.GL_STATIC_DRAW_ARB, tex0);
		sky_tex1 = new FloatVBO(ARBBufferObject.GL_STATIC_DRAW_ARB, tex1);
		sky_colors = new FloatVBO(ARBBufferObject.GL_STATIC_DRAW_ARB, colors);
	}

	private final void putArray(float[] src, int offset, float[] dest) {
		for (int i = 0; i < src.length; i++)
			dest[i + (offset*src.length)] = src[i];
	}

	private final ShortVBO[] makeSkyStripIndices() {
		ShortVBO[] strip_indices = new ShortVBO[subdiv_height - 2];
		for (int i = 0; i < strip_indices.length; i++) {
			int size = subdiv_axis*2 + 2;
			ShortBuffer temp = BufferUtils.createShortBuffer(size);
			for (int j = 0; j < subdiv_axis; j++) {
				temp.put(j*2, (short)(i*subdiv_axis + j));
				temp.put(j*2 + 1, (short)((i + 1)*subdiv_axis + j));
			}
			temp.put(subdiv_axis*2, (short)(i*subdiv_axis));
			temp.put(subdiv_axis*2 + 1, (short)((i + 1)*subdiv_axis));
			strip_indices[i] = new ShortVBO(ARBBufferObject.GL_STATIC_DRAW_ARB, size);
			temp.rewind();
			strip_indices[i].put(temp);
		}
		return strip_indices;
	}

	private final ShortVBO makeSkyFanIndices() {
		int size = subdiv_axis + 2;
		ShortBuffer temp = BufferUtils.createShortBuffer(size);
		temp.put(0, (short)(sky_vertices.capacity()/3 - 1));
		for (int i = 0; i < subdiv_axis; i++) {
		   temp.put(i + 1, (short)((subdiv_height - 1)*subdiv_axis - i - 1));
		}
		temp.put(subdiv_axis + 1, (short)((subdiv_height - 1)*subdiv_axis - 1));
		
		ShortVBO fan_indices = new ShortVBO(ARBBufferObject.GL_STATIC_DRAW_ARB, size);
		temp.rewind();
		fan_indices.put(temp);
		return fan_indices;
	}

	private final SkyStitchVertex[] makeDomeVertices(HeightMap heightmap, int ring_id, int index_offset, float radius, float origin_x, float origin_y) {
		int size = subdiv_axis;
		SkyStitchVertex[] result = new SkyStitchVertex[size];
		float a_angle_inc = (float)StrictMath.PI*2/subdiv_axis;
		for (int i = 0; i < subdiv_axis; i++) {
			int index = i + index_offset;
			result[i] = new SkyStitchVertex(heightmap, index, ring_id,
					(float)java.lang.StrictMath.cos(START_ANGLE + a_angle_inc*i)*radius + origin_x,
					(float)java.lang.StrictMath.sin(START_ANGLE + a_angle_inc*i)*radius + origin_y);
		}
		return result;
	}

	private final SkyStitchVertex[] makeLandscapeVertices(HeightMap heightmap) {
		int size = 4*heightmap.getPatchesPerWorld();
		SkyStitchVertex[] result = new SkyStitchVertex[size];
		
		for (int i = 0; i < heightmap.getPatchesPerWorld(); i++) {
			int index = i;
			result[index] = new SkyStitchVertex(heightmap, index, 0, 0, heightmap.getMetersPerPatch()*i);
			index = i +  heightmap.getPatchesPerWorld();
			result[index] = new SkyStitchVertex(heightmap, index, 0,
					heightmap.getMetersPerPatch()*i,  heightmap.getMetersPerWorld());
			index = i + heightmap.getPatchesPerWorld()*2;
			result[index] = new SkyStitchVertex(heightmap, index, 0,
					 heightmap.getMetersPerWorld(),
					 heightmap.getMetersPerWorld() - heightmap.getMetersPerPatch()*i);
			index = i + heightmap.getPatchesPerWorld()*3;
			result[index] = new SkyStitchVertex(heightmap, index, 0,
					heightmap.getMetersPerWorld() - heightmap.getMetersPerPatch()*i,
					0);
		}
		return result;
	}

	private static strictfp class SkyStitchVertex extends Stitcher.Vertex {
		private final float x;
		private final float y;
		private final float theta;
		private final HeightMap heightmap;

		private SkyStitchVertex(HeightMap heightmap, int index, int side, float x, float y) {
			super(index, side);
			this.heightmap = heightmap;
			this.x = x;
			this.y = y;
			float half_world_size = heightmap.getMetersPerWorld()*.5f;
			this.theta = (float)StrictMath.atan2(y - half_world_size, x - half_world_size);
		}

		public final int compareTo(Object o) {
			SkyStitchVertex other = (SkyStitchVertex)o;
			if (equals(o))
				return 0;
			return theta < other.theta ? 1 : -1;
		}

		public final boolean equals(Object o) {
			SkyStitchVertex other = (SkyStitchVertex)o;
			return other == this || other.theta == theta;
		}

		public final String toString() {
			float half_world_size = heightmap.getMetersPerWorld()*.5f;
			float x0 = x - half_world_size;
			float y0 = y - half_world_size;
			float inv_len = (float)(1/StrictMath.sqrt(x0*x0 + y0*y0));
			x0 *= inv_len;
			y0 *= inv_len;
			return x + " " + y + "\t\t" + x0 + " " + y0 + " " + theta/StrictMath.PI + " " + super.toString();
		}
	}
}
