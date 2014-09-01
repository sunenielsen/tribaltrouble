package com.oddlabs.tt.gui;

public strictfp interface Scrollable {
	public void setOffsetY(int new_offset);
	public int getOffsetY();
	public int getStepHeight();
	public void jumpPage(boolean up);
	public float getScrollBarRatio();
	public float getScrollBarOffset();
	public void setScrollBarOffset(float offset);
}
