package com.oddlabs.tt.gui;

/* Team Penguin - Make clicks Atomic */
import java.util.concurrent.atomic.AtomicInteger;

public final strictfp class KeyboardEvent {
	private final int key_code;
	private final char key_char;
	private final boolean shift_down;
	private final boolean control_down;
	private final AtomicInteger clicks = new AtomicInteger(0);

	public KeyboardEvent(int key_code, char key_char, boolean shift_down, boolean control_down) {
		this.key_code = key_code;
		this.key_char = key_char;
		this.shift_down = shift_down;
		this.control_down = control_down;
		this.clicks.set(1);
	}

	public KeyboardEvent(int key_code, char key_char, boolean shift_down, boolean control_down, AtomicInteger clicks) {
		this.key_code = key_code;
		this.key_char = key_char;
		this.shift_down = shift_down;
		this.control_down = control_down;
		this.clicks.set(clicks.get());
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
		return clicks.get();
	}
/* End Penguin */
}
