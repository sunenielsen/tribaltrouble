package com.oddlabs.tt.gui;

public final strictfp class Diode extends GUIObject {
	private boolean lit;

	public Diode() {
		setDim(Skin.getSkin().getDiode()[Skin.NORMAL].getWidth(), Skin.getSkin().getDiode()[Skin.NORMAL].getHeight());
		lit = false;
	}

	public void setLit(boolean lit) {
		this.lit = lit;
	}

	protected final void renderGeometry() {
		if (isDisabled()) {
			Skin.getSkin().getDiode()[Skin.DISABLED].render(0, 0);
		} else if (lit) {
			Skin.getSkin().getDiode()[Skin.ACTIVE].render(0, 0);
		} else {
			Skin.getSkin().getDiode()[Skin.NORMAL].render(0, 0);
		}
	}
}
