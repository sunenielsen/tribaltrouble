package com.oddlabs.tt.gui;

import com.oddlabs.util.Quad;

public final strictfp class Horizontal {
	private final Quad[] left;
	private final Quad[] center;
	private final Quad[] right;
	private final int height;
	private final int left_width;
	private final int right_width;

	public Horizontal(Quad[] left, Quad[] center, Quad[] right) {
		this.left = left;
		this.center = center;
		this.right = right;
		height = left[Skin.NORMAL].getHeight();
		left_width = left[Skin.NORMAL].getWidth();
		right_width = right[Skin.NORMAL].getWidth();
	}

	public final void render(float x, float y, int width, int type) {
		int center_width = width - left_width - right_width;
		left[type].render(x, y);
		center[type].render(x + left_width, y, center_width, height);
		right[type].render(x + left_width + center_width, y);
	}

	public final int getHeight() {
		return height;
	}
}
