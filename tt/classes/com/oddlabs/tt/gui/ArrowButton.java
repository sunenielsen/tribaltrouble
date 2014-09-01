package com.oddlabs.tt.gui;

import org.lwjgl.input.Keyboard;
import com.oddlabs.util.Quad;

public final strictfp class ArrowButton extends ButtonObject {
	private final Quad[] pressed;
	private final Quad[] unpressed;
	private final Quad[] arrow;

	public ArrowButton(Quad[] pressed, Quad[] unpressed, Quad[] arrow) {
		setDim(pressed[Skin.NORMAL].getWidth(), pressed[Skin.NORMAL].getHeight());
		this.pressed = pressed;
		this.unpressed = unpressed;
		this.arrow = arrow;
	}

	public final void keyPressed(KeyboardEvent event) {
		switch (event.getKeyCode()) {
			case Keyboard.KEY_SPACE:
			case Keyboard.KEY_RETURN:
				mousePressedAll(LocalInput.LEFT_BUTTON, 0, 0);
				break;
		}
	}

	public final void keyRepeat(KeyboardEvent event) {
		switch (event.getKeyCode()) {
			case Keyboard.KEY_TAB:
				super.keyRepeat(event);
				break;
		}
	}

	public final void keyReleased(KeyboardEvent event) {
		switch (event.getKeyCode()) {
			case Keyboard.KEY_SPACE:
			case Keyboard.KEY_RETURN:
				mouseReleasedAll(LocalInput.LEFT_BUTTON, 0, 0);
				break;
		}
	}

	protected final void renderGeometry() {
		if (isDisabled()) {
			unpressed[Skin.DISABLED].render(0, 0);
			arrow[Skin.DISABLED].render(0, 0);
		} else if (isPressed() && isHovered()) {
			pressed[Skin.ACTIVE].render(0, 0);
			arrow[Skin.ACTIVE].render(0, 0);
		} else if (isActive()) {
			unpressed[Skin.ACTIVE].render(0, 0);
			arrow[Skin.ACTIVE].render(0, 0);
		} else {
			unpressed[Skin.NORMAL].render(0, 0);
			arrow[Skin.NORMAL].render(0, 0);
		}
	}

	protected final void mouseClicked(int button, int x, int y, int clicks) {
		// Steal click from scrollbar
	}
}
