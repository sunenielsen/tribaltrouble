package com.oddlabs.geometry;

import com.oddlabs.util.*;

import java.io.*;

public final strictfp class SpriteInfo implements Serializable {
	private final static long serialVersionUID = 1;

	private final short[] indices;
	private final ShortCompressedFloatArray vertices;
	private final ByteCompressedFloatArray normals;
	private final ShortCompressedFloatArray texcoords;
	private final byte[][] skin_names;
	private final float[][] skin_weights;
	private final String[][] textures;
	private final float[] clear_color;

	public SpriteInfo(String[][] textures, short[] indices, float[] vertices, float[] normals, float[] texcoords, byte[][] skin_names, float[][] skin_weights, float[] clear_color) {
		this.textures = textures;
		this.indices = indices;
		this.vertices = new ShortCompressedFloatArray(vertices, 3);
		this.normals = new ByteCompressedFloatArray(normals, 3);
		this.texcoords = new ShortCompressedFloatArray(texcoords, 2);
		this.skin_names = skin_names;
		this.skin_weights = skin_weights;
		this.clear_color = clear_color;
	}

	public final short[] getIndices() {
		return indices;
	}

	public final float[] getVertices() {
		return vertices.getFloatArray();
	}

	public final float[] getNormals() {
		return normals.getFloatArray();
	}

	public final float[] getTexCoords() {
		return texcoords.getFloatArray();
	}

	public final byte[][] getSkinNames() {
		return skin_names;
	}

	public final float[][] getSkinWeights() {
		return skin_weights;
	}

	public final String[][] getTextures() {
		return textures;
	}

/*	public final BoundingBox getBounds() {
		return bounds;
	}
*/
	public final float[] getClearColor() {
		return clear_color;
	}
}
