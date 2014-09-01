package com.oddlabs.tt.tutorial;

import com.oddlabs.tt.camera.GameCamera;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.delegate.SelectionDelegate;

public final strictfp class ZoomTrigger extends TutorialTrigger {
	private final boolean[] zoom_dirs = new boolean[2];

	public ZoomTrigger(WorldViewer viewer) {
		super(0f, 2f, "zoom");
		viewer.getCamera().resetLastZoomFactor();
	}

	protected final void run(Tutorial tutorial) {
		GameCamera camera = tutorial.getViewer().getCamera();
		if (camera.getLastZoomFactor() > 0f) {
			zoom_dirs[0] = true;
		} else if (camera.getLastZoomFactor() < 0f) {
			zoom_dirs[1] = true;
		}
		for (int i = 0; i < zoom_dirs.length; i++)
			if (!zoom_dirs[i])
				return;
		tutorial.next(new RotateTrigger());
	}
}
