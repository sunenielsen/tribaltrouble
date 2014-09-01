package com.oddlabs.tt.gui;

import org.lwjgl.input.Keyboard;

public final strictfp class ScrollButton extends GUIObject {
	public ScrollButton() {
		setCanFocus(true);
//		setupPos();
	}

	public final void setupPos(ScrollBar owner) {
		setPos(owner.getButtonX(), owner.getButtonY());
		setDim(Skin.getSkin().getScrollBarData().getScrollButton().getWidth(), owner.getButtonHeight());
	}

	public final void keyPressed(KeyboardEvent event) {
	}

	public final void keyRepeat(KeyboardEvent event) {
		switch (event.getKeyCode()) {
			case Keyboard.KEY_TAB:
				super.keyRepeat(event);
				break;
		}
	}

	public final void keyReleased(KeyboardEvent event) {
	}

	protected final void renderGeometry() {
		Vertical button = Skin.getSkin().getScrollBarData().getScrollButton();
		if (isDisabled()) {
			button.render(0, 0, getHeight(), Skin.DISABLED);
		} else if (isActive()) {
			button.render(0, 0, getHeight(), Skin.ACTIVE);
		} else {
			button.render(0, 0, getHeight(), Skin.NORMAL);
		}
	}

	public final void mouseClicked(int button, int x, int y, int clicks) {
	}
}
