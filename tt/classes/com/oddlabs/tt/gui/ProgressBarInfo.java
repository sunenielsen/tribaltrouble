package com.oddlabs.tt.gui;

import com.oddlabs.tt.font.Font;

public final strictfp class ProgressBarInfo {
	private final Label label;
	private final float weight;
	private int waypoint;

	public ProgressBarInfo(String title, float weight) {
		Font font = Skin.getSkin().getProgressBarData().getFont();
		label = new Label(title, font);
		this.weight = weight;
	}

	public final float getWeight() {
		return weight;
	}

	public final void setWaypoint(int waypoint) {
		this.waypoint = waypoint;
	}

	public final int getWaypoint() {
		return waypoint;
	}

	public final Label getLabel() {
		return label;
	}
}
