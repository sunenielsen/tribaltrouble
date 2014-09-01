package com.oddlabs.converter2;

import java.util.Map;

public final strictfp class ModelInfo {
	public final short[] indices;
	public final float[] normals;
	public final float[] vertices;
	public final float[] colors;
	public final float[] texcoords;
	public final float[] texcoords2;
	public final byte[][] skin_names;
	public final float[][] skin_weights;
//	public final String tex_name;

	public ModelInfo(/*String tex_name, */short[] indices, float[] vertices, float[] normals, float[] colors, float[] texcoords, float[] texcoords2, byte[][] skin_names, float[][] skin_weights) {
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
