package com.oddlabs.tt.render;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import com.oddlabs.geometry.AnimationInfo;
import com.oddlabs.geometry.SpriteInfo;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.landscape.LandscapeResources;
import com.oddlabs.tt.procedural.GeneratorRespond;
import com.oddlabs.tt.model.Model;
import com.oddlabs.tt.resource.ResourceDescriptor;
import com.oddlabs.tt.resource.Resources;
import com.oddlabs.tt.resource.TextureFile;
import com.oddlabs.tt.util.BoundingBox;
import com.oddlabs.tt.util.GLState;
import com.oddlabs.tt.util.GLStateStack;
import com.oddlabs.tt.vbo.FloatVBO;
import com.oddlabs.tt.vbo.ShortVBO;
import com.oddlabs.tt.vbo.IntVBO;

final strictfp class Sprite {
	public final static int LOWDETAIL_NORMAL = 1;
	public final static int LOWDETAIL_TEAMDECAL = 2;
	private final static int TEXTURE_NORMAL = 0;
	private final static int TEXTURE_TEAM = 1;
	private final static float[] respond_color = new float[]{1f, 1f, 1f, 1f};
	private final static FloatBuffer decal_color = BufferUtils.createFloatBuffer(4);

	public static int global_size = 0;
	private final static FloatBuffer white_color;

	/* Team Penguin */
	private final IntVBO indices;
	/* End Penguin */
	private final FloatVBO vertices_and_normals;
	private final FloatVBO texcoords;

	private final Texture[][] textures;
	private final int num_triangles;
	private final int num_vertices;
	private final float[] clear_color;
	private final int[] buffer_indices;
	private final boolean alpha;
	private final boolean lighted;
	private final boolean culled;
	private final boolean modulate_color;
	private final float[] cpw_array;
	private final int[] animation_length_array;
	private final int[] type_array;
	private final Texture respond_texture;

	static {
		white_color = BufferUtils.createFloatBuffer(4).put(new float[]{1f, 1f, 1f, 1f});
		white_color.rewind();
	}

	public Sprite(SpriteInfo sprite_info, AnimationInfo[] animations, boolean alpha, boolean lighted, boolean culled, boolean modulate_color, boolean max_alpha, int mipmap_cutoff, BoundingBox[] bounds, float[] cpw_array, int[] type_array, int[] animation_length_array) {
		this.culled = culled;
		this.alpha = alpha;
		this.lighted = lighted;
		this.modulate_color = modulate_color;
		this.cpw_array = cpw_array;
		this.type_array = type_array;
		this.animation_length_array = animation_length_array;

		/* Team Penguin */
		int[] tmp_indices = sprite_info.getIndices();
		/* End Penguin */
		float[] tmp_texcoords = sprite_info.getTexCoords();
		num_vertices = tmp_texcoords.length/2;
		num_triangles = tmp_indices.length/3;

		// Expand animations
		float[][][] tmp_vertices = new float[animations.length][][];
		float[][][] tmp_normals = new float[animations.length][][];
		expandAnimation(animations, tmp_vertices, tmp_normals, sprite_info.getVertices(), sprite_info.getNormals(), sprite_info.getSkinNames(), sprite_info.getSkinWeights(), bounds);

		clear_color = sprite_info.getClearColor();

		/* Team Penguin */
		indices = new IntVBO(ARBBufferObject.GL_STATIC_DRAW_ARB, tmp_indices.length);
		/* End Penguin */
		indices.put(tmp_indices);
		
		texcoords = new FloatVBO(ARBBufferObject.GL_STATIC_DRAW_ARB, tmp_texcoords.length);
		texcoords.put(tmp_texcoords);
		
		int vert_and_normal_buffer_size = 0;
		int frame_size = num_vertices*3*2;
		buffer_indices = new int[tmp_vertices.length];
		for (int j = 0; j < tmp_vertices.length; j++) {
			int num_frames = tmp_vertices[j].length;
			buffer_indices[j] = vert_and_normal_buffer_size;
			vert_and_normal_buffer_size += num_frames*frame_size;
			for (int i = 1; i < tmp_vertices[j].length; i++)
				assert tmp_vertices[j][i].length + tmp_normals[j][i].length == frame_size;
		}
		FloatBuffer temp_vertices_and_normals = BufferUtils.createFloatBuffer(vert_and_normal_buffer_size);
		
		for (int j = 0; j < tmp_vertices.length; j++) {
			for (int i = 0; i < tmp_vertices[j].length; i++) {
				temp_vertices_and_normals.put(tmp_vertices[j][i]);
				temp_vertices_and_normals.put(tmp_normals[j][i]);
			}
		}
		vertices_and_normals = new FloatVBO(ARBBufferObject.GL_STATIC_DRAW_ARB, vert_and_normal_buffer_size);
		temp_vertices_and_normals.rewind();
		vertices_and_normals.put(temp_vertices_and_normals);

		int memory_size = tmp_indices.length*2 + (tmp_texcoords.length + vert_and_normal_buffer_size)*4;
		global_size += memory_size;
//		System.out.println("sprite size: " + memory_size);
		int color_format = Globals.COMPRESSED_RGB_FORMAT;
		if (alpha)
			color_format = Globals.COMPRESSED_RGBA_FORMAT;

		String[][] texture_names = sprite_info.getTextures();
		textures = new Texture[texture_names.length][2];
		for (int i = 0; i < texture_names.length; i++) {
			textures[i][TEXTURE_NORMAL] = getTextureForName(texture_names[i][0], color_format, mipmap_cutoff, max_alpha);
			if (texture_names[i][TEXTURE_TEAM] != null)
				textures[i][TEXTURE_TEAM] = getTextureForName(texture_names[i][1], Globals.COMPRESSED_RGB_FORMAT, mipmap_cutoff, max_alpha);
			else
				textures[i][TEXTURE_TEAM] = null;
		}
		this.respond_texture = ((Texture[])Resources.findResource(new GeneratorRespond()))[0];
	}

	public final boolean modulateColor() {
		return modulate_color;
	}

	public final int getTriangleCount() {
		return num_triangles;
	}

	static void setupDecalColor(float[] color) {
		decal_color.put(color).rewind();
		GL11.glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, decal_color);
	}

	private void transformAndColor(ModelState model, boolean respond) {
		model.transform();
		float[] color;
		if (respond) {
			color = respond_color;
		} else {
			color = model.getTeamColor();
		}
		assert color != null: "object = " + this;
		setupDecalColor(color);
	}

	public final void renderAll(List render_list, int tex_index, boolean respond) {
		for (int i = 0; i < render_list.size(); i++) {
			ModelState model = (ModelState)render_list.get(i);
			render_list.set(i, null);
			if (Globals.isBoundsEnabled(Globals.BOUNDING_PLAYERS))
				RenderTools.draw(model.getModel());
			if (Globals.draw_misc) {
				GL11.glPushMatrix();
				transformAndColor(model, respond);
				render(model.getModel().getAnimation(), model.getModel().getAnimationTicks());
				GL11.glPopMatrix();
			}
		}
	}

	private final void expandAnimation(AnimationInfo[] animations, float[][][] tmp_vertices, float[][][] tmp_normals, float[] initial_pose_vertices, float[] initial_pose_normals, byte[][] skin_names, float[][] skin_weights, BoundingBox[] bounding_boxes) {
		int num_bones = animations[0].getFrames()[0].length/12;
		Matrix4f[] frame_bones = new Matrix4f[num_bones];
		for (int bone = 0; bone < frame_bones.length; bone++)
			frame_bones[bone] = new Matrix4f();
		Vector4f v = new Vector4f();
		Vector4f n = new Vector4f();
		Vector4f temp = new Vector4f();
		FloatBuffer matrix_buffer = FloatBuffer.allocate(16);
		matrix_buffer.put(15, 1f);
		for (int anim = 0; anim < animations.length; anim++) {
			BoundingBox bounding_box = bounding_boxes[anim];
			int num_frames = animations[anim].getFrames().length;
			tmp_vertices[anim] = new float[num_frames][num_vertices*3];
			tmp_normals[anim] = new float[num_frames][num_vertices*3];
			for (int frame = 0; frame < num_frames; frame++) {
				float[] frame_animation = animations[anim].getFrames()[frame];
				for (int bone = 0; bone < num_bones; bone++) {
					matrix_buffer.clear();
					matrix_buffer.put(frame_animation, bone*12, 12);
					matrix_buffer.rewind();
					frame_bones[bone].loadTranspose(matrix_buffer);
				}
				float[] frame_normals = tmp_normals[anim][frame];
				float[] frame_vertices = tmp_vertices[anim][frame];
				float bmax_x = Float.NEGATIVE_INFINITY;
				float bmax_y = Float.NEGATIVE_INFINITY;
				float bmax_z = Float.NEGATIVE_INFINITY;
				float bmin_x = Float.POSITIVE_INFINITY;
				float bmin_y = Float.POSITIVE_INFINITY;
				float bmin_z = Float.POSITIVE_INFINITY;
				for (int vertex = 0; vertex < num_vertices; vertex++) {
					float x = initial_pose_vertices[vertex*3 + 0];
					float y = initial_pose_vertices[vertex*3 + 1];
					float z = initial_pose_vertices[vertex*3 + 2];
					float nx = initial_pose_normals[vertex*3 + 0];
					float ny = initial_pose_normals[vertex*3 + 1];
					float nz = initial_pose_normals[vertex*3 + 2];
					float result_x = 0, result_y = 0, result_z = 0, result_nx = 0, result_ny = 0, result_nz = 0;
					v.set(x, y, z, 1);
					n.set(nx, ny, nz, 0);
					byte[] vertex_skin_names = skin_names[vertex];
					float[] vertex_skin_weights = skin_weights[vertex];
					for (int bone = 0; bone < vertex_skin_names.length; bone++) {
						float weight = vertex_skin_weights[bone];
						Matrix4f bone_matrix = frame_bones[vertex_skin_names[bone]];
						Matrix4f.transform(bone_matrix, v, temp);
						result_x += temp.x*weight;
						result_y += temp.y*weight;
						result_z += temp.z*weight;
						// Assume matrix is only translation and scaling
						Matrix4f.transform(bone_matrix, n, temp);
						result_nx += temp.x*weight;
						result_ny += temp.y*weight;
						result_nz += temp.z*weight;
					}
					// Use Math.sqrt here for efficiency. Only used for normals (not gamestate affecting) anyway.
					float vec_len_inv = 1f/(float)Math.sqrt(result_nx*result_nx + result_ny*result_ny + result_nz*result_nz);
					result_nx *= vec_len_inv;
					result_ny *= vec_len_inv;
					result_nz *= vec_len_inv;
					frame_normals[vertex*3 + 0] = result_nx;
					frame_normals[vertex*3 + 1] = result_ny;
					frame_normals[vertex*3 + 2] = result_nz;

//result_x = x; result_y = y; result_z = z;
					if (result_x < bmin_x)
						bmin_x = result_x;
					else if (result_x > bmax_x)
						bmax_x = result_x;
					if (result_y < bmin_y)
						bmin_y = result_y;
					else if (result_y > bmax_y)
						bmax_y = result_y;
					if (result_z < bmin_z)
						bmin_z = result_z;
					else if (result_z > bmax_z)
						bmax_z = result_z;
					frame_vertices[vertex*3 + 0] = result_x;
					frame_vertices[vertex*3 + 1] = result_y;
					frame_vertices[vertex*3 + 2] = result_z;
				}
				bounding_box.checkBounds(bmin_x, bmax_x, bmin_y, bmax_y, bmin_z, bmax_z);
			}
		}
	}

	private final static Texture getTextureForName(String texture_name, int color_format, int mipmap_cutoff, boolean max_alpha) {
		String GENERATOR_STRING = "Generator:";
		if (texture_name.startsWith(GENERATOR_STRING)) {
			String generator_class_name = texture_name.substring(GENERATOR_STRING.length());
			try {
				Class generator_class = Class.forName(generator_class_name);
				ResourceDescriptor descriptor = (ResourceDescriptor)generator_class.newInstance();
				return ((Texture[])Resources.findResource(descriptor))[0];
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			return (Texture)Resources.findResource(new TextureFile("/textures/models/" + texture_name, color_format, GL11.GL_LINEAR_MIPMAP_NEAREST, GL11.GL_LINEAR, GL11.GL_REPEAT, GL11.GL_REPEAT, mipmap_cutoff, 100000, 0.1f, max_alpha));
		}
	}

	public final void setup(int tex_index, boolean respond) {
		setupWithColor(white_color, tex_index, respond, modulate_color);
	}

	public final void setupWithColor(FloatBuffer color, int tex_index, boolean respond, boolean modulate_color) {
		doSetup(color, tex_index, respond, modulate_color);
	}

	private final void doSetup(FloatBuffer color, int tex_index, boolean respond, boolean modulate_color) {
		int gl_flags = setupBasic();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures[tex_index][TEXTURE_NORMAL].getHandle());
		if (modulate_color) {
			GL11.glAlphaFunc(GL11.GL_GREATER, 0f);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		} else if (Globals.draw_light && lighted) {
			gl_flags = gl_flags | GLState.NORMAL_ARRAY;
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, color);
		}
		GL11.glColor4f(color.get(0), color.get(1), color.get(2), color.get(3));
		if (!modulate_color && (hasTeamDecal() || respond)) {
			gl_flags = gl_flags | GLState.TEXCOORD1_ARRAY;
			setupTeamDecal();
			if (respond) {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, respond_texture.getHandle());
			} else {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures[tex_index][TEXTURE_TEAM].getHandle());
			}
			GLState.clientActiveTexture(GL13.GL_TEXTURE1);
			texcoords.texCoordPointer(2, 0, 0);
			GLState.clientActiveTexture(GL13.GL_TEXTURE0);
		}
		GLStateStack.switchState(gl_flags);
	}

	private final int setupBasic() {
		int gl_flags = GLState.VERTEX_ARRAY | GLState.TEXCOORD0_ARRAY;
		if (!culled) {
			GL11.glDisable(GL11.GL_CULL_FACE);
		}
		if (alpha) {
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glAlphaFunc(GL11.GL_GREATER, .3f);
		}
		texcoords.texCoordPointer(2, 0, 0);
		return gl_flags;
	}

	public final boolean hasTeamDecal() {
		return textures[0][TEXTURE_TEAM] != null;
	}

	public final int getNumTextures() {
		return textures.length;
	}

	public final static void setupTeamDecal() {
		GLState.activeTexture(GL13.GL_TEXTURE1);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
	}

	public final static void resetTeamDecal() {
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_DECAL);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GLState.activeTexture(GL13.GL_TEXTURE0);
	}

	public final void reset(boolean respond, boolean modulate_color) {
		if (!modulate_color && (hasTeamDecal() || respond)) {
			resetTeamDecal();
		}
		if (modulate_color) {
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
		} else if (Globals.draw_light && lighted) {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
		}
		resetBasic();
	}

	private final void resetBasic() {
		if (!culled) {
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		if (alpha) {
			GL11.glDisable(GL11.GL_ALPHA_TEST);
		}
	}

	private final int getFrameCapped(int animation, int frame) {
		int anim_length = animation_length_array[animation];
		if (type_array[animation] == AnimationInfo.ANIM_LOOP)
			return frame%anim_length;
		else
			return StrictMath.min(frame, anim_length - 1);
	}

	public final void render(int animation, float anim_ticks) {
		float anim_position = anim_ticks*cpw_array[animation];
		int frame_non_capped = (int)(anim_position*animation_length_array[animation]);
		int frame = getFrameCapped(animation, frame_non_capped);
		int frame_size = num_vertices*3;
		int frame_index = frame*2;
		int vertex_index = buffer_indices[animation] + frame_index*frame_size;
		int normal_index = vertex_index + frame_size;
		
		vertices_and_normals.normalPointer(0, normal_index);
		vertices_and_normals.vertexPointer(3, 0, vertex_index);
		indices.drawRangeElements(GL11.GL_TRIANGLES, 0, num_vertices - 1, num_triangles*3, 0);
	}

	public final void renderModel(int tex_index) {
		int gl_flags = setupBasic();
		GLStateStack.switchState(gl_flags);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures[tex_index][TEXTURE_NORMAL].getHandle());
		render(0, 0); // Render 1st frame of 1st animation to low detail texture
		resetBasic();
	}

	public final float[] getModelClearColor() {
		return getClearColor();
	}

	public final float[] getClearColor() {
		return clear_color;
	}
}
