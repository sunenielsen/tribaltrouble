package com.oddlabs.tt.delegate;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.oddlabs.tt.camera.FirstPersonCamera;
import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.input.PointerInput;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.gui.*;

public strictfp class FirstPersonDelegate extends InGameDelegate {
	private final boolean key_pressed;
	private final int created_tick;

	private boolean done = false;

	public FirstPersonDelegate(WorldViewer viewer, CameraState camera_state, boolean key_pressed) {
		super(viewer, new FirstPersonCamera(viewer.getWorld().getHeightMap(), camera_state));
		this.key_pressed = key_pressed;
		created_tick = LocalEventQueue.getQueue().getManager().getTick();
	}

	private final void release() {
		done = true;
	}

	public void keyPressed(KeyboardEvent event) {
	}

	public void keyReleased(KeyboardEvent event) {
		if (key_pressed && !done) {
			switch (event.getKeyCode()) {
				case Keyboard.KEY_F:
					pop();
					break;
			}
		}
	}

	public void mouseScrolled(int amount) {
	}

	public void mouseMoved(int x, int y) {
		if (!done)
			getCamera().mouseMoved(x, y);
	}

	public void mouseDragged(int button, int x, int y, int relative_x, int relative_y, int absolute_x, int absolute_y) {
		if (created_tick == LocalEventQueue.getQueue().getManager().getTick())
			return;
		if ((button == LocalInput.MIDDLE_BUTTON || key_pressed) && !done && getGUIRoot().getModalDelegate() == null) {
			getCamera().mouseMoved(x, y);
		}
	}

	public void mousePressed(int button, int x, int y) {
	}

	public void mouseReleased(int button, int x, int y) {
		if (button == LocalInput.MIDDLE_BUTTON && !key_pressed && !done) {
			pop();
		}
	}

	protected int getCursorIndex() {
		return GUIRoot.CURSOR_NULL;
	}

	public final void doRemove() {
		super.doRemove();
		if (!done) {
			release();
		}
	}
}
