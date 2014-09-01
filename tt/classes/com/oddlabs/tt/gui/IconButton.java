package com.oddlabs.tt.gui;

import com.oddlabs.util.Quad;

public strictfp class IconButton extends ButtonObject {
	private final Quad[] icon_quad;
	private IconDisabler icon_disabler = null;

	public IconButton(Quad[] icon_quad) {
		setDim(icon_quad[Skin.NORMAL].getWidth(), icon_quad[Skin.NORMAL].getHeight());
		this.icon_quad = icon_quad;
	}

	public final void setIconDisabler(IconDisabler icon_disabler) {
		this.icon_disabler = icon_disabler;
	}

	public final void doUpdate() {
		setDisabled(icon_disabler != null && icon_disabler.isDisabled());
	}

	protected final void renderGeometry() {
//		setDisabled(icon_disabler != null && icon_disabler.isDisabled()); FUCK DET
		if (isDisabled())
			icon_quad[Skin.DISABLED].render(0, 0);
		else if (isHovered() || isActive())
			icon_quad[Skin.ACTIVE].render(0, 0);
		else
			icon_quad[Skin.NORMAL].render(0, 0);
	}
}
