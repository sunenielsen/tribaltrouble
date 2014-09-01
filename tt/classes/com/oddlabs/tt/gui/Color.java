package com.oddlabs.tt.gui;

public final strictfp class Color {
	private final float r;
	private final float g;
	private final float b;
	private final float a;

	public Color(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public final float getR() {
		return r;
	}

	public final float getG() {
		return g;
	}

	public final float getB() {
		return b;
	}

	public final float getA() {
		return a;
	}
}

