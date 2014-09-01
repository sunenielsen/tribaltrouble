package com.oddlabs.tt.gui;

import org.lwjgl.input.Keyboard;
import com.oddlabs.util.Quad;

public final strictfp class SliderButton extends ButtonObject {
	private final Slider slider;
	private final Quad[] button;

	public SliderButton(Slider slider, Quad[] button) {
		setDim(button[Skin.NORMAL].getWidth(), button[Skin.NORMAL].getHeight());
		this.slider = slider;
		this.button = button;
	}

	protected final void renderGeometry() {
		GUIObject parent = (GUIObject)getParent();
		if (parent.isDisabled()) {
			button[Skin.DISABLED].render(0, 0);
		} else if (isHovered() || parent.isActive()) {
			button[Skin.ACTIVE].render(0, 0);
		} else {
			button[Skin.NORMAL].render(0, 0);
		}
	}

	public final void mouseHeld(int button, int x, int y) {
	}

	public final void keyPressed(KeyboardEvent event) {
		switch (event.getKeyCode()) {
			case Keyboard.KEY_RIGHT:
				slider.setValue(slider.getValue() + 1);
				break;
			case Keyboard.KEY_LEFT:
				slider.setValue(slider.getValue() - 1);
				break;
		}
	}
}
