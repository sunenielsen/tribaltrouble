package com.oddlabs.tt.render;

import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.GL11;

import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.landscape.AbstractTreeGroup;
import com.oddlabs.tt.landscape.TreeNodeVisitor;
import com.oddlabs.tt.landscape.TreeSupply;
import com.oddlabs.tt.landscape.TreeLeaf;
import com.oddlabs.tt.landscape.TreeGroup;
import com.oddlabs.tt.landscape.LandscapeResources;
import com.oddlabs.geometry.LowDetailModel;
import com.oddlabs.tt.util.StrictVector4f;
import com.oddlabs.tt.util.StrictMatrix4f;
import com.oddlabs.tt.util.GLState;
import com.oddlabs.tt.util.GLStateStack;
import com.oddlabs.tt.resource.Resources;
import com.oddlabs.tt.resource.TextureFile;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.procedural.Landscape;
import com.oddlabs.tt.vbo.FloatVBO;
import com.oddlabs.tt.vbo.ShortVBO;

public final strictfp class TreeLowDetail {
	private final static StrictVector4f src = new StrictVector4f();
	private final static StrictVector4f dest = new StrictVector4f();
	private final static FloatBuffer update_buffer = BufferUtils.createFloatBuffer(1000);

	private final FloatVBO vertices;
	private final FloatVBO texcoords;
	private final ShortVBO tree_indices;
	private final Texture[] lowdetail_textures;
	private final Tree[] trees;
	private final LowDetailModel[] low_details;

	private final int terrain_type;
	
	private int current_vertex_index;

	public TreeLowDetail(World world, Tree[] trees, LowDetailModel[] tree_low_details, List tree_positions, List palm_tree_positions, int terrain_type) {
		lowdetail_textures = new Texture[]{
			(Texture)Resources.findResource(new TextureFile("/textures/models/lowdetail_tree", Globals.COMPRESSED_RGBA_FORMAT)),
				(Texture)Resources.findResource(new TextureFile("/textures/models/viking_lowdetail_tree", Globals.COMPRESSED_RGBA_FORMAT))};
		int[] num_trees;
		switch (terrain_type) {
			case Landscape.NATIVE:
				num_trees = new int[]{tree_positions.size(), palm_tree_positions.size(), 0, 0};
				break;
			case Landscape.VIKING:
				num_trees = new int[]{0, 0, tree_positions.size(), palm_tree_positions.size()};
				break;
			default:
				throw new RuntimeException();
		}

		this.low_details = tree_low_details;
		this.trees = trees;
		this.terrain_type = terrain_type;
		current_vertex_index = 0;
		int vertex_count = 0;
		int index_count = 0;
		for (int i = 0; i < num_trees.length; i++) {
			vertex_count += num_trees[i]*low_details[i].getVertices().length/3;
			index_count += num_trees[i]*low_details[i].getIndices().length;
		}
		vertices = new FloatVBO(ARBBufferObject.GL_DYNAMIC_DRAW_ARB, vertex_count*3);
		texcoords = new FloatVBO(ARBBufferObject.GL_STATIC_DRAW_ARB, vertex_count*2);
		tree_indices = new ShortVBO(ARBBufferObject.GL_STATIC_DRAW_ARB, index_count);
	}

	final Tree[] getTrees() {
		return trees;
	}

	final void build(AbstractTreeGroup tree_root) {
		int index_count = tree_indices.capacity();

		BuildVisitor visitor = new BuildVisitor();
		tree_root.visit(visitor);
		assert visitor.end == index_count: "end index " + visitor.end + " != num coords " + index_count;
		assert current_vertex_index == vertices.capacity()/3: "vertex index index " + current_vertex_index + " != num coords " + vertices.capacity()/3;
		vertices.put(visitor.vertex_array);
		texcoords.put(visitor.texcoord_array);
		tree_indices.put(visitor.tree_index_array);
	}

	private final int numTreesTotal(int[] num_trees) {
		int count = 0;
		for (int i = 0; i < num_trees.length; i++)
			count += num_trees[i];
		return count;
	}

	final void loadMatrix(StrictMatrix4f matrix) {
		update_buffer.clear();
		matrix.store(update_buffer);
		update_buffer.flip();
		GL11.glMultMatrix(update_buffer);
	}

	private final int putCoordinate(int index, float x, float y, float z, float u, float v, float[] vertice_array, float[] texcoord_array) {
		vertice_array[index*3] = x;
		vertice_array[index*3 + 1] = y;
		vertice_array[index*3 + 2] = z;
		
		texcoord_array[index*2] = u;
		texcoord_array[index*2 + 1] = v;
		return index + 1;
	}

	private final int putIndex(int index, int tree_index, short[] tree_indice_array) {
		assert tree_index <= Character.MAX_VALUE;
		short tree_char_index = (short)tree_index;
		tree_indice_array[index] = tree_char_index;
		return index + 1;
	}

	private int[] putLowDetail(int start_index, StrictMatrix4f matrix, LowDetailModel low_detail_model, float[] vertice_array, float[] texcoord_array, short[] tree_indice_array) {
		float[] vertices = low_detail_model.getVertices();
		float[] tex_coords = low_detail_model.getTexCoords();
		short[] indices = low_detail_model.getIndices();
		int end = start_index;
		int start_vertex_index = current_vertex_index;
		for (int i = 0; i < indices.length; i++)
			end = putIndex(end, indices[i] + current_vertex_index, tree_indice_array);
		for (int i = 0; i < vertices.length/3; i++) {
			src.set(vertices[i*3], vertices[i*3 + 1], vertices[i*3 + 2], 1f);
			StrictMatrix4f.transform(matrix, src, dest);
			float u = tex_coords[i*2];
			float v = tex_coords[i*2 + 1];
			current_vertex_index = putCoordinate(current_vertex_index, dest.x, dest.y, dest.z, u, v, vertice_array, texcoord_array);
		}
		return new int[]{end, start_vertex_index};
	}

	public final void updateLowDetail(StrictMatrix4f matrix, TreeSupply tree) {
		int start_vertex_index = tree.getLowDetailStartIndex();
		int tree_type_index = tree.getTreeTypeIndex();
		LowDetailModel low_detail_model = low_details[tree_type_index];
		float[] vertex_array = low_detail_model.getVertices();
		update_buffer.clear();
		update_buffer.limit(vertex_array.length);
		for (int i = 0; i < vertex_array.length/3; i++) {
			src.set(vertex_array[i*3], vertex_array[i*3 + 1], vertex_array[i*3 + 2], 1f);
			StrictMatrix4f.transform(matrix, src, dest);
			update_buffer.put(i*3, dest.getX());
			update_buffer.put(i*3 + 1, dest.getY());
			update_buffer.put(i*3 + 2, dest.getZ());
		}
		vertices.putSubData(start_vertex_index*3, update_buffer);
	}

	final void setupTrees() {
		bindTreeTexture();
		GLStateStack.switchState(GLState.VERTEX_ARRAY | GLState.TEXCOORD0_ARRAY);
		vertices.vertexPointer(3, 0, 0);
		texcoords.texCoordPointer(2, 0, 0);
	}

	protected final void bindTreeTexture() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, lowdetail_textures[terrain_type].getHandle());
	}

	final void renderLowDetail(int start, int count) {
		tree_indices.drawElements(GL11.GL_TRIANGLES, count, start);
	}

	private final strictfp class BuildVisitor implements TreeNodeVisitor {
		private int end = 0;
		private final float[] vertex_array = new float[vertices.capacity()];
		private final float[] texcoord_array = new float[texcoords.capacity()];
		private final short[] tree_index_array = new short[tree_indices.capacity()];

		public final void visitLeaf(TreeLeaf tree_leaf) {
			int start = end;
			tree_leaf.visitTrees(this);
			tree_leaf.initLowDetailBuffer(start, end);
		}

		public final void visitNode(TreeGroup tree_group) {
			int start = end;
			tree_group.visitChildren(this);
			tree_group.initLowDetailBuffer(start, end);
		}

		public final void visitTree(TreeSupply tree_supply) {
			int start_index = end;
			int tree_type_index = tree_supply.getTreeTypeIndex();
			int[] values = putLowDetail(end, tree_supply.getMatrix(), low_details[tree_type_index], vertex_array, texcoord_array, tree_index_array);
			int end_index = values[0];
			tree_supply.setLowDetailStartIndex(values[1]);
			tree_supply.initLowDetailBuffer(start_index, end_index);
			end = end_index;
		}
	}
}
