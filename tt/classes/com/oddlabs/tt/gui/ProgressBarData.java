package com.oddlabs.tt.gui;

import com.oddlabs.tt.font.*;
import com.oddlabs.util.Quad;

public final strictfp class ProgressBarData {
	private final Horizontal progress_bar;
	private final Quad[] left_fill;
	private final Quad[] center_fill;
	private final Quad[] right_fill;
	private final Font font;

	public ProgressBarData(Horizontal progress_bar, Quad[] left_fill, Quad[] center_fill, Quad[] right_fill, Font font) {
		this.progress_bar = progress_bar;
		this.left_fill = left_fill;
		this.center_fill = center_fill;
		this.right_fill = right_fill;
		this.font = font;
	}

	public final Horizontal getProgressBar() {
		return progress_bar;
	}

	public final Quad[] getLeftFill() {
		return left_fill;
	}

	public final Quad[] getCenterFill() {
		return center_fill;
	}

	public final Quad[] getRightFill() {
		return right_fill;
	}

	public final Font getFont() {
		return font;
	}
}
