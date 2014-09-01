package com.oddlabs.tt.tutorial;

import com.oddlabs.tt.camera.MapCamera;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.delegate.SelectionDelegate;

public final strictfp class MapModeTrigger extends TutorialTrigger {
	public MapModeTrigger() {
		super(.1f, 1f, "map_mode");
	}

	protected final void run(Tutorial tutorial) {
		if (tutorial.getViewer().getDelegate().getCamera() instanceof MapCamera)
			tutorial.next(new FromMapModeTrigger());
	}
}
