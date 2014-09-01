package com.oddlabs.tt.gui;

import com.oddlabs.util.Quad;

public strictfp class GUIIcon extends GUIObject {
	private final Quad icon_quad;
	
	public GUIIcon(Quad icon) {
		setDim(icon.getWidth(), icon.getHeight());
		setCanFocus(false);
		this.icon_quad = icon;
	}

	public void renderGeometry() {
		icon_quad.render(0, 0);
	}
}
