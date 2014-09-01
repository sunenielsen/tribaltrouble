package com.oddlabs.tt.gui;

import com.oddlabs.tt.font.*;
import com.oddlabs.util.Quad;

public final strictfp class MultiColumnComboBoxData {
	private final Box box;
	private final Horizontal button_pressed;
	private final Horizontal button_unpressed;
	private final Quad[] descending;
	private final Quad[] ascending;
	private final Color color1;
	private final Color color2;
	private final Color color_marked;
	private final Font font;
	private final int caption_offset;

	public MultiColumnComboBoxData(Box box,
								   Horizontal button_pressed,
								   Horizontal button_unpressed,
								   Quad[] descending,
								   Quad[] ascending,
								   Color color1,
								   Color color2,
								   Color color_marked,
								   Font font,
								   int caption_offset) {
		this.box = box;
		this.button_pressed = button_pressed;
		this.button_unpressed = button_unpressed;
		this.descending = descending;
		this.ascending = ascending;
		this.color1 = color1;
		this.color2 = color2;
		this.color_marked = color_marked;
		this.font = font;
		this.caption_offset = caption_offset;
	}

	public final Box getBox() {
		return box;
	}

	public final Horizontal getButtonPressed() {
		return button_pressed;
	}

	public final Horizontal getButtonUnpressed() {
		return button_unpressed;
	}

	public final Quad[] getDescending() {
		return descending;
	}

	public final Quad[] getAscending() {
		return ascending;
	}

	public final Color getColor1() {
		return color1;
	}

	public final Color getColor2() {
		return color2;
	}

	public final Color getColorMarked() {
		return color_marked;
	}

	public final Font getFont() {
		return font;
	}

	public final int getCaptionOffset() {
		return caption_offset;
	}
}
