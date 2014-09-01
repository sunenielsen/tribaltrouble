package com.oddlabs.tt.delegate;

import com.oddlabs.tt.camera.NullCamera;
import com.oddlabs.tt.gui.*;

public strictfp class NullDelegate extends CameraDelegate {
	private final boolean render_cursor;

	public NullDelegate(GUIRoot gui_root, boolean render_cursor) {
		super(gui_root, new NullCamera());
		this.render_cursor = render_cursor;
	}

	public void keyPressed(KeyboardEvent event) {
	}

	public void keyReleased(KeyboardEvent event) {
	}

	public void mouseScrolled(int amount) {
	}

	public void mouseMoved(int x, int y) {
	}

	public void mouseDragged(int button, int x, int y, int relative_x, int relative_y, int absolute_x, int absolute_y) {
	}

	public void mousePressed(int button, int x, int y) {
	}

	public void mouseReleased(int button, int x, int y) {
	}

	public final boolean renderCursor() {
		return render_cursor;
	}
}
