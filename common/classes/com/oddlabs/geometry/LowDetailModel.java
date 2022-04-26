package com.oddlabs.geometry;

import java.io.*;

public final strictfp class LowDetailModel implements Serializable {
	private final static long serialVersionUID = 1;

	private final float[] vertices;
	private final float[] tex_coords;
	private final int[] indices;
	private final int poly_count;

/* Team Penguin */
	public LowDetailModel(int[] indices, float[] vertices, float[] tex_coords) {
		this.indices = indices;
		this.vertices = vertices;
		this.tex_coords = tex_coords;
		this.poly_count = indices.length/3;
	}

	public final int[] getIndices() {
		return indices;
	}
/* End Penguin */
	public final float[] getVertices() {
		return vertices;
	}

	public final float[] getTexCoords() {
		return tex_coords;
	}

	public final int getPolyCount() {
		return poly_count;
	}
}
