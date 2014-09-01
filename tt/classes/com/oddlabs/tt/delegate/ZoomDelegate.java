package com.oddlabs.tt.delegate;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.oddlabs.tt.camera.GameCamera;
import com.oddlabs.tt.input.PointerInput;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.gui.*;

public strictfp class ZoomDelegate extends InGameDelegate {
	private final static float ZOOM_FACTOR_CORRECTION = .25f;

	private final int start_x;
	private final int start_y;

	private final GameCamera game_camera;

	private boolean done = false;

	public ZoomDelegate(WorldViewer viewer, GameCamera camera) {
		super(viewer, camera);
		game_camera = camera;
		start_x = LocalInput.getMouseX();
		start_y = LocalInput.getMouseY();
	}

	private final void release() {
		done = true;
	}

	public final void doRemove() {
		super.doRemove();
		if (!done) {
			release();
		}
	}

	public void keyPressed(KeyboardEvent event) {
	}

	public void keyReleased(KeyboardEvent event) {
		if (!done) {
			switch (event.getKeyCode()) {
				case Keyboard.KEY_Z:
					pop();
					break;
			}
		}
	}

	public void mouseScrolled(int amount) {
	}

	public void mouseMoved(int x, int y) {
		if (!done) {
			int dy = y - start_y;

			float zoom_factor = dy*ZOOM_FACTOR_CORRECTION;
			game_camera.zoom(zoom_factor);
			PointerInput.setCursorPosition(start_x, start_y);
		}
	}

	public void mouseDragged(int button, int x, int y, int relative_x, int relative_y, int absolute_x, int absolute_y) {
	}

	public void mousePressed(int button, int x, int y) {
	}

	public void mouseReleased(int button, int x, int y) {
	}

	protected int getCursorIndex() {
		return GUIRoot.CURSOR_NULL;
	}
}
