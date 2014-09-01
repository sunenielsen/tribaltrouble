package com.oddlabs.tt.gui;

public final strictfp class ColumnInfo {
	private final String caption;
	private final int width;

	public ColumnInfo(String caption, int width) {
		this.caption = caption;
		this.width = width;
	}

	public final String getCaption() {
		return caption;
	}

	public final int getWidth() {
		return width;
	}
}
