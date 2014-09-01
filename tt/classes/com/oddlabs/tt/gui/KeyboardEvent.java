package com.oddlabs.tt.gui;

public final strictfp class KeyboardEvent {
	private final int key_code;
	private final char key_char;
	private final boolean shift_down;
	private final boolean control_down;
	private final int clicks;

	public KeyboardEvent(int key_code, char key_char, boolean shift_down, boolean control_down) {
		this(key_code, key_char, shift_down, control_down, 1);
	}

	public KeyboardEvent(int key_code, char key_char, boolean shift_down, boolean control_down, int clicks) {
		this.key_code = key_code;
		this.key_char = key_char;
		this.shift_down = shift_down;
		this.control_down = control_down;
		this.clicks = clicks;
	}

	public final int getKeyCode() {
		return key_code;
	}

	public final char getKeyChar() {
		return key_char;
	}

	public final boolean isShiftDown() {
		return shift_down;
	}

	public final boolean isControlDown() {
		return control_down;
	}

	public final int getNumClicks() {
		return clicks;
	}
}
