package com.oddlabs.tt.gui;

public abstract strictfp class ButtonObject extends GUIObject {
	public final static int ALIGN_LEFT = 1;
	public final static int ALIGN_CENTER = 2;
	public final static int ALIGN_RIGHT = 3;

	private boolean pressed = false;

	public ButtonObject() {
		setCanFocus(true);
	}

	protected final boolean isPressed() {
		return pressed;
	}

	protected final void mouseReleased(int button, int x, int y) {
		pressed = false;
	}

	protected final void mousePressed(int button, int x, int y) {
		pressed = true;
	}

	protected void mouseHeld(int button, int x, int y) {
		if (pressed)
			mousePressedAll(button, x, y);
	}

	public void setDisabled(boolean disabled) {
		if (disabled)
			pressed = false;
		super.setDisabled(disabled);
	}
}
