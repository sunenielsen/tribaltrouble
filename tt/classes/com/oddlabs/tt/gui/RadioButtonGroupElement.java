package com.oddlabs.tt.gui;

public abstract strictfp class RadioButtonGroupElement extends GUIObject {
	private boolean marked = false;

	private final RadioButtonGroup group;

	public RadioButtonGroupElement(boolean marked, RadioButtonGroup group) {
		this.group = group;
		group.add(this);
		if (marked)
			group.mark(this);
	}

	public final boolean isMarked() {
		return marked;
	}

	protected final void setMarked(boolean marked) {
		this.marked = marked;
	}

	protected void mouseClicked(int button, int x, int y, int clicks) {
		group.mark(this);
	}
}
