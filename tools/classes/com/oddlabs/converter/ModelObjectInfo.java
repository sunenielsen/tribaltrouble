package com.oddlabs.converter;

import java.io.*;

public final strictfp class ModelObjectInfo extends ObjectInfo {
	private final float[] clear_color;
	private final String[][] textures;

	public ModelObjectInfo(File file, String[][] textures, float[] clear_color) {
		super(file);
		this.textures = textures;
		this.clear_color = clear_color;
	}

	public final String[][] getTextures() {
		return textures;
	}
	
	public final float[] getClearColor() {
		return clear_color;
	}
}
