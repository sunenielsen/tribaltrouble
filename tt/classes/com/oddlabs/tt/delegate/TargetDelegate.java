package com.oddlabs.tt.delegate;

import org.lwjgl.input.Keyboard;

import com.oddlabs.tt.camera.GameCamera;
import com.oddlabs.tt.render.Picker;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.gui.*;

public strictfp class TargetDelegate extends ControllableCameraDelegate {
	private final int action;

	public TargetDelegate(WorldViewer viewer, GameCamera camera, int action) {
		super(viewer, camera);
		this.action = action;
	}

	public boolean canHoverBehind() {
		return true;
	}

	protected final int getCursorIndex() {
		return GUIRoot.CURSOR_TARGET;
	}

	public final void keyPressed(KeyboardEvent event) {
		getCamera().keyPressed(event);
		switch (event.getKeyCode()) {
			case Keyboard.KEY_ESCAPE:
				pop();
				break;
			case Keyboard.KEY_SPACE:
			case Keyboard.KEY_RETURN:
				break;
			default:
				super.keyPressed(event);
				break;
		}
	}

	public void keyReleased(KeyboardEvent event) {
		if (event.getKeyCode() != Keyboard.KEY_SPACE || event.getKeyCode() != Keyboard.KEY_RETURN)
			getCamera().keyReleased(event);
	}

	public void mousePressed(int button, int x, int y) {
		if (button == LocalInput.LEFT_BUTTON) {
			getViewer().getPicker().pickTarget(getViewer().getSelection().getCurrentSelection(), getViewer().getGUIRoot().getDelegate().getCamera().getState(), getViewer().getPeerHub().getPlayerInterface(), x, y, action);
			pop();
		} else {
			super.mousePressed(button, x, y);
		}
	}
}
