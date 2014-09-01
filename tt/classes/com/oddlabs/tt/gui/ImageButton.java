package com.oddlabs.tt.gui;

public strictfp class ImageButton extends ButtonObject {
	private final GUIObject normal;
	private final GUIObject hovered;
	private final GUIObject disabled;

	public ImageButton(GUIObject normal, GUIObject hovered, GUIObject disabled) {
		setDim(normal.getWidth(), normal.getHeight());
		this.normal = normal;
		this.hovered = hovered;
		this.disabled = disabled;
	}

	public final void setPos(int x, int y) {
		super.setPos(x, y);
		normal.setPos(x, y);
		hovered.setPos(x, y);
		disabled.setPos(x, y);
	}

	protected final void renderGeometry() {
		if (isDisabled())
			disabled.renderGeometry();
		else if (isHovered() || isActive())
			hovered.renderGeometry();
		else
			normal.renderGeometry();
	}

	protected void mouseClicked(int button, int x, int y, int clicks) {
	}
}
