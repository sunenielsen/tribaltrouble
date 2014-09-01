package com.oddlabs.tt.gui;

public final strictfp class PanelData {
	private final Box box;
	private final Horizontal tab;
	private final int left_caption_offset;
	private final int right_caption_offset;
	private final int bottom_caption_offset;
	private final int left_tab_offset;
	private final int bottom_tab_offset;

	public PanelData(Box box,
					 Horizontal tab,
					 int left_caption_offset,
					 int right_caption_offset,
					 int bottom_caption_offset,
					 int left_tab_offset,
					 int bottom_tab_offset) {
		this.box = box;
		this.tab = tab;
		this.left_caption_offset = left_caption_offset;
		this.right_caption_offset = right_caption_offset;
		this.bottom_caption_offset = bottom_caption_offset;
		this.left_tab_offset = left_tab_offset;
		this.bottom_tab_offset = bottom_tab_offset;
	}

	public final Box getBox() {
		return box;
	}

	public final Horizontal getTab() {
		return tab;
	}

	public final int getLeftCaptionOffset() {
		return left_caption_offset;
	}

	public final int getRightCaptionOffset() {
		return right_caption_offset;
	}

	public final int getBottomCaptionOffset() {
		return bottom_caption_offset;
	}

	public final int getLeftTabOffset() {
		return left_tab_offset;
	}

	public final int getBottomTabOffset() {
		return bottom_tab_offset;
	}
}
