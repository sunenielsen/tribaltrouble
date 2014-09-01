package com.oddlabs.tt.gui;

import com.oddlabs.tt.font.Font;

public strictfp class PanelTab extends GUIObject {
	private final static float[] HIGHLIGHT_COLOR = new float[]{0f, 1f, 0f, 1f};
	private boolean selected;
	private Label label;

	public PanelTab(String caption) {
		PanelData data = Skin.getSkin().getPanelData();
		Font font = Skin.getSkin().getButtonFont();
		label = new Label(caption, font);
		label.setPos(data.getLeftCaptionOffset(), (data.getTab().getHeight() - font.getHeight())/2 + data.getBottomCaptionOffset());
		addChild(label);
		setDim(data.getLeftCaptionOffset() + label.getWidth() + data.getRightCaptionOffset(), data.getTab().getHeight());
		setCanFocus(true);
	}

	public final void select(boolean selected) {
		this.selected = selected;
		focusNotifyAll(false);
		if (selected)
			label.setColor(Label.DEFAULT_COLOR);
	}

	public final int getRenderState() {
		if (isDisabled())
			return Skin.DISABLED;
		else if (isActive() || selected)
			return Skin.ACTIVE;
		else
			return Skin.NORMAL;
	}

	protected final void renderGeometry() {
		Skin.getSkin().getPanelData().getTab().render(0, 0, getWidth(), getRenderState());
	}

	public final void updateNotify() {
		if (!selected)
			label.setColor(HIGHLIGHT_COLOR);
	}
}
