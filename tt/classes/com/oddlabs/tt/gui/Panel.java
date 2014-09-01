package com.oddlabs.tt.gui;

public strictfp class Panel extends Group {
	private final PanelTab tab;

	public Panel(String caption) {
		tab = new PanelTab(caption);
	}

	public final PanelTab getTab() {
		return tab;
	}

	public final void compileCanvas() {
		Box box = Skin.getSkin().getPanelData().getBox();
		super.compileCanvas(box.getLeftOffset(), box.getBottomOffset(), box.getRightOffset(), box.getTopOffset());
	}
}
