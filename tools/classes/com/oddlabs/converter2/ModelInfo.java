package com.oddlabs.converter2;

import java.util.Map;

public final strictfp class ModelInfo {
	/* Team Penguin */
	public final int[] indices;
	/* End Penguin */
	public final float[] normals;
	public final float[] vertices;
	public final float[] colors;
	public final float[] texcoords;
	public final float[] texcoords2;
	public final byte[][] skin_names;
	public final float[][] skin_weights;
//	public final String tex_name;

	/* Team Penguin */
	public ModelInfo(/*String tex_name, */int[] indices, float[] vertices, float[] normals, float[] colors, float[] texcoords, float[] texcoords2, byte[][] skin_names, float[][] skin_weights) {
	/* End Penguin */
		this.normals = normals;
		this.vertices = vertices;
		this.indices = indices;
		this.colors = colors;
		this.texcoords = texcoords;
		this.texcoords2 = texcoords2;
		this.skin_names = skin_names;
		this.skin_weights = skin_weights;
//		this.tex_name = tex_name;
	}
}
