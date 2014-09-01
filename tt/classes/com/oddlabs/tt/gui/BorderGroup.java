package com.oddlabs.tt.gui;

public final strictfp class BorderGroup extends Group {
	private final Label label;

	public BorderGroup() {
		label = null;
	}

	public BorderGroup(String caption) {
		GroupData data = Skin.getSkin().getGroupData();
		label = new Label(caption, data.getCaptionFont());
	}

	public final void compileCanvas() {
		GroupData data = Skin.getSkin().getGroupData();
		Box group = data.getGroup();
		if (label != null) {
			super.compileCanvas(group.getLeftOffset(),
								group.getBottomOffset(),
								group.getRightOffset(),
								group.getTopOffset() + data.getCaptionOffset());
			label.setPos(data.getCaptionLeft(), getHeight() - data.getCaptionY());
			addChild(label);
		} else {
			super.compileCanvas(group.getLeftOffset(), group.getBottomOffset(), group.getRightOffset(), group.getTopOffset());
		}
		setCanFocus(true);
	}

	protected final void renderGeometry() {
		Skin.getSkin().getGroupData().getGroup().render(0, 0, getWidth(), getHeight(), Skin.NORMAL);
	}
}
