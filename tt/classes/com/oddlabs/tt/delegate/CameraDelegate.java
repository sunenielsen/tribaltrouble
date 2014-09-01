package com.oddlabs.tt.delegate;

import com.oddlabs.tt.camera.Camera;
import com.oddlabs.tt.gui.*;

public strictfp abstract class CameraDelegate extends Delegate {
	private final GUIRoot gui_root;
	private Camera camera;

	public CameraDelegate(GUIRoot gui_root, Camera camera) {
		super();
		this.camera = camera;
		this.gui_root = gui_root;
	}

	protected final GUIRoot getGUIRoot() {
		return gui_root;
	}

	public final void setCamera(Camera camera) {
		this.camera = camera;
	}

	public final Camera getCamera() {
		return camera;
	}

	protected void doAdd() {
		super.doAdd();
		getCamera().enable();
	}

	protected void doRemove() {
		super.doRemove();
		getCamera().disable();
	}

	public boolean renderCursor() {
		return true;
	}

	public boolean canScroll() {
		return false;
	}

	public boolean forceRender() {
		return false;
	}

	public final void pop() {
		gui_root.removeDelegate(this);
	}
}
