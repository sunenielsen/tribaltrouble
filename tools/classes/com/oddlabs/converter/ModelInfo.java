package com.oddlabs.converter;

import java.util.Map;

public final strictfp class ModelInfo {
	public final short[] indices;
	public final float[] normals;
	public final float[] vertices;
	public final float[] colors;
	public final float[] texcoords;
	public final byte[][] skin_names;
	public final float[][] skin_weights;
//	public final String tex_name;

	public ModelInfo(/*String tex_name, */short[] indices, float[] vertices, float[] normals, float[] colors, float[] texcoords, byte[][] skin_names, float[][] skin_weights) {
		this.normals = normals;
		this.vertices = vertices;
		this.indices = indices;
		this.colors = colors;
		this.texcoords = texcoords;
		this.skin_names = skin_names;
		this.skin_weights = skin_weights;
//		this.tex_name = tex_name;
	}
}
