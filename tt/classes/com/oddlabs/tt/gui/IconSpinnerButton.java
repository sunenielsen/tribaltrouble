package com.oddlabs.tt.gui;

import com.oddlabs.util.Quad;

public final strictfp class IconSpinnerButton extends NonFocusIconButton {
	private final IconSpinner owner;

	public IconSpinnerButton(Quad[] icon_quad, String tool_tip, IconSpinner owner) {
		super(icon_quad, tool_tip);
		this.owner = owner;
	}

	public final void appendToolTip(ToolTipBox tool_tip_box) {
		if (isDisabled())
			owner.appendToolTip(tool_tip_box);
		else
			super.appendToolTip(tool_tip_box);
	}
}
