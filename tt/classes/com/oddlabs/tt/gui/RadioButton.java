package com.oddlabs.tt.gui;

public final strictfp class RadioButton extends RadioButtonGroupElement {
	private boolean pressed = false;

	public RadioButton(boolean marked, RadioButtonGroup group, String text) {
		super(marked, group);
		Label label = new Label(text, Skin.getSkin().getEditFont());
		addChild(label);
		label.setPos(Skin.getSkin().getRadioButtonMarked()[Skin.NORMAL].getWidth(), (Skin.getSkin().getRadioButtonMarked()[Skin.NORMAL].getHeight() - label.getHeight())/2);
		setDim(Skin.getSkin().getRadioButtonMarked()[Skin.NORMAL].getWidth() + label.getWidth(), Skin.getSkin().getRadioButtonMarked()[Skin.NORMAL].getHeight());
		setCanFocus(true);
	}

	protected final void mouseReleased(int button, int x, int y) {
		pressed = false;
	}

	protected final void mousePressed(int button, int x, int y) {
		pressed = true;
	}

	protected final void renderGeometry() {
		if (isDisabled()) {
			if (isMarked())
				Skin.getSkin().getRadioButtonMarked()[Skin.DISABLED].render(0, 0);
			else
				Skin.getSkin().getRadioButtonUnmarked()[Skin.DISABLED].render(0, 0);
		} else if (isActive()) {
			if (isMarked()) {
				Skin.getSkin().getRadioButtonMarked()[Skin.ACTIVE].render(0, 0);
			} else {
				if (pressed && isHovered())
					Skin.getSkin().getRadioButtonMarked()[Skin.ACTIVE].render(0, 0);
				else
					Skin.getSkin().getRadioButtonUnmarked()[Skin.ACTIVE].render(0, 0);
			}
		} else {
			if (isMarked())
				Skin.getSkin().getRadioButtonMarked()[Skin.NORMAL].render(0, 0);
			else
				Skin.getSkin().getRadioButtonUnmarked()[Skin.NORMAL].render(0, 0);
		}
	}
}
