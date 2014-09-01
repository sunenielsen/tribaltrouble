package com.oddlabs.tt.util;

import java.nio.ShortBuffer;
import java.util.Arrays;

import org.lwjgl.BufferUtils;

public final strictfp class Stitcher {
	public static ShortBuffer stitch(Vertex[] vertices) {
		ShortBuffer indices = BufferUtils.createShortBuffer(vertices.length*3);
		vertices = (Vertex[])vertices.clone();
		Arrays.sort(vertices);
		int start_index = getStartIndex(vertices);
		Vertex right_vertex = vertices[start_index];
		Vertex left_vertex = vertices[(start_index + 1)%vertices.length];
		for (int i = 2; i < vertices.length + 2; i++) {
			Vertex next_vertex = vertices[(i + start_index)%vertices.length];
			indices.put(right_vertex.index).put(left_vertex.index).put(next_vertex.index);
			if (next_vertex.side == right_vertex.side)
				right_vertex = next_vertex;
			else {
				assert next_vertex.side == left_vertex.side;
				left_vertex = next_vertex;
			}
		}
		assert !indices.hasRemaining();
		indices.flip();
		return indices;
	}

	private static int getStartIndex(Vertex[] vertices) {
		int vertex_index;
		for (vertex_index = 0; vertex_index < vertices.length; vertex_index++)
			if (vertices[vertex_index%vertices.length].side >
					vertices[(vertex_index + 1)%vertices.length].side)
				break;
		if (vertex_index >= vertices.length)
			throw new RuntimeException("All vertices are on one side");
		return vertex_index;
	}

	public abstract static strictfp class Vertex implements Comparable {
		private final int side;
		private final short index;

		public Vertex(int index, int side) {
			this.index = (short)index;
			this.side = side;
		}

		public final short getIndex() {
			return index;
		}

		public final int getSide() {
			return side;
		}
	}
}
