package com.oddlabs.converter;

public final strictfp class Bone {
	private final String name;
	private final byte index;
	private final Bone[] children;

	public Bone(String name, byte index, Bone[] children) {
		this.name = name;
		this.children = children;
		this.index = index;
	}

	public final Bone[] getChildren() {
		return children;
	}

	public final byte getIndex() {
		return index;
	}

	public final String getName() {
		return name;
	}
}
