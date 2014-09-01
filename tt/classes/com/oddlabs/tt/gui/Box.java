package com.oddlabs.tt.gui;

import com.oddlabs.util.Quad;

public final strictfp class Box {
	private final Quad[] left_bottom;
	private final Quad[] bottom;
	private final Quad[] right_bottom;
	private final Quad[] right;
	private final Quad[] right_top;
	private final Quad[] top;
	private final Quad[] left_top;
	private final Quad[] left;
	private final Quad[] center;
	private final int left_offset;
	private final int bottom_offset;
	private final int right_offset;
	private final int top_offset;

	private final int left_width;
	private final int right_width;
	private final int bottom_height;
	private final int top_height;

	public Box(Quad[] left_bottom,
			   Quad[] bottom,
			   Quad[] right_bottom,
			   Quad[] right,
			   Quad[] right_top,
			   Quad[] top,
			   Quad[] left_top,
			   Quad[] left,
			   Quad[] center,
			   int left_offset,
			   int bottom_offset,
			   int right_offset,
			   int top_offset) {
		this.left_bottom = left_bottom;
		this.bottom = bottom;
		this.right_bottom = right_bottom;
		this.right = right;
		this.right_top = right_top;
		this.top = top;
		this.left_top = left_top;
		this.left = left;
		this.center = center;
		this.left_offset = left_offset;
		this.bottom_offset = bottom_offset;
		this.right_offset = right_offset;
		this.top_offset = top_offset;

		left_width = left[Skin.NORMAL].getWidth();
		right_width = right[Skin.NORMAL].getWidth();
		bottom_height = bottom[Skin.NORMAL].getHeight();
		top_height = top[Skin.NORMAL].getHeight();
	}

	public final void render(float x, float y, int width, int height, int type) {
		int center_width = width - left_width - right_width;
		int center_height = height - bottom_height - top_height;
		left_bottom[type].render(x, y);
		bottom[type].render(x + left_width, y, center_width, bottom_height);
		right_bottom[type].render(x + left_width + center_width, y);
		right[type].render(x + left_width + center_width, y + bottom_height, right_width, center_height);
		right_top[type].render(x + left_width + center_width, y + bottom_height + center_height);
		top[type].render(x + left_width, y + bottom_height + center_height, center_width, top_height);
		left_top[type].render(x, y + bottom_height + center_height);
		left[type].render(x, y + bottom_height, left_width, center_height);
		center[type].render(x + left_width, y + bottom_height, center_width, center_height);
	}

	public final int getLeftOffset() {
		return left_offset;
	}

	public final int getBottomOffset() {
		return bottom_offset;
	}

	public final int getRightOffset() {
		return right_offset;
	}

	public final int getTopOffset() {
		return top_offset;
	}
}
