package com.oddlabs.tt.gui;

import com.oddlabs.tt.font.Font;

public final strictfp class IntegerLabel extends Label {
	private final int val;

	public IntegerLabel(int val, Font font, int width) {
		super("" + val, font, width, ALIGN_RIGHT);
		this.val = val;
	}

	public IntegerLabel(int val, Font font) {
		super("" + val, font);
		this.val = val;
	}

	public final int compareTo(Object o) {
		if (o instanceof IntegerLabel) {
			IntegerLabel other = (IntegerLabel)o;
			return val - other.val;
		} else
			return -1;
	}
}

