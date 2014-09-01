package com.oddlabs.tt.gui;

import com.oddlabs.tt.model.SupplyCounter;
import com.oddlabs.tt.util.ToolTip;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.util.Quad;

import java.util.ResourceBundle;

public strictfp class StatusIcon extends GUIObject implements ToolTip {
	private final Quad icon_quad;
	private final TextField label;
	private final String tooltip;

	private SupplyCounter counter;
	private int text_count = -1;
	
	public StatusIcon(int label_width, Quad icon, String tooltip) {
		this.tooltip = tooltip;
		setDim(icon.getWidth() + label_width, icon.getHeight());
		setCanFocus(true); //only to enable tool tips. focus is given to delegate
		this.icon_quad = icon;
		label = new Label("", Skin.getSkin().getEditFont(), label_width, Label.ALIGN_RIGHT);
		addChild(label);
//		label.setPos(icon.getWidth(), (getHeight() - label.getFont().getHeight())/2);
		label.setPos(0, (getHeight() - label.getFont().getHeight())/2);
	}

	public final void setCounter(SupplyCounter counter) {
		this.counter = counter;
	}

	public final void doUpdate() {
		int count = counter.getNumSupplies();
		if (count != text_count) {
			text_count = count;
			label.clear();
			label.append(count);
		}
	}

	public final void appendToolTip(ToolTipBox tool_tip_box) {
		String tooltip_str = Utils.getBundleString(ResourceBundle.getBundle(StatusIcon.class.getName()), "max", new Object[]{tooltip, new Integer(counter.getMaxSupplies())});
		tool_tip_box.append(tooltip_str);
	}

	protected void renderGeometry() {
		int x = getWidth() - icon_quad.getWidth();
		int y = (getHeight() - icon_quad.getHeight())/2;
		icon_quad.render(x, y);
	}
}
