package com.oddlabs.tt.tutorial;

import com.oddlabs.tt.camera.GameCamera;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.delegate.SelectionDelegate;

public final strictfp class RotateTrigger extends TutorialTrigger {
	private final boolean[] rotate_dirs = new boolean[2];

	public RotateTrigger() {
		super(.1f, 2f, "rotate");
	}

	protected final void run(Tutorial tutorial) {
		GameCamera camera = tutorial.getViewer().getCamera();
		if (camera.rotateRight()) {
			rotate_dirs[0] = true;
		} 
		if (camera.rotateLeft()) {
			rotate_dirs[1] = true;
		}
		for (int i = 0; i < rotate_dirs.length; i++)
			if (!rotate_dirs[i])
				return;
		tutorial.next(new PitchTrigger());
	}
}
