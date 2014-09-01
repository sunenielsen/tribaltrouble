package com.oddlabs.tt.gui;

import com.oddlabs.util.Quad;

public final strictfp class IconLabel extends GUIObject implements Comparable {
	private final Quad icon;
	private final Label label;

	public IconLabel(Quad icon, Label label) {
		this.icon = icon;
		this.label = label;
		label.setPos(icon.getWidth(), 0);
		addChild(label);
		int width = icon.getWidth() + label.getWidth();
		int height = StrictMath.max(icon.getHeight(), label.getHeight());
		setDim(width, height);
	}

	protected void renderGeometry() {
		icon.render(0, 0);
	}

	private final Label getLabel() {
		return label;
	}

	public int compareTo(Object o) {
		return label.compareTo(((IconLabel)o).getLabel());
	}
}
