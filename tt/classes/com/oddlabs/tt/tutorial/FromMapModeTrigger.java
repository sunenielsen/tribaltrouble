package com.oddlabs.tt.tutorial;

import com.oddlabs.tt.camera.GameCamera;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.delegate.SelectionDelegate;

public final strictfp class FromMapModeTrigger extends TutorialTrigger {
	public FromMapModeTrigger() {
		super(.1f, 1f, "from_map_mode");
	}

	protected final void run(Tutorial tutorial) {
		if (tutorial.getViewer().getDelegate().getCamera() instanceof GameCamera)
			tutorial.next(new SelectUnitTrigger());
	}
}
