package com.oddlabs.tt.gui;

import com.oddlabs.tt.font.Font;

public final strictfp class SortedLabel extends Label {
	private final int index;

	public SortedLabel(String text, int index, Font font) {
		super(text, font);
		this.index = index;
	}

	public final int compareTo(Object o) {
		if (o instanceof IntegerLabel) {
			SortedLabel other = (SortedLabel)o;
			return index - other.index;
		} else
			return -1;
	}
}

