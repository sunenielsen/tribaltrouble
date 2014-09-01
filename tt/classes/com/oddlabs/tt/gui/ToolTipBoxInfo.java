package com.oddlabs.tt.gui;

public final strictfp class ToolTipBoxInfo {
	private final Horizontal box;
	private final int left_offset;
	private final int bottom_offset;
	private final int right_offset;
	private final int top_offset;

	public ToolTipBoxInfo(Horizontal box, int left_offset, int bottom_offset, int right_offset, int top_offset) {
		this.box = box;
		this.left_offset = left_offset;
		this.bottom_offset = bottom_offset;
		this.right_offset = right_offset;
		this.top_offset = top_offset;
	}

	public final Horizontal getBox() {
		return box;
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
