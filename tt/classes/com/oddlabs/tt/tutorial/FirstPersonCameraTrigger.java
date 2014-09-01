package com.oddlabs.tt.tutorial;

import com.oddlabs.tt.delegate.Delegate;
import com.oddlabs.tt.delegate.FirstPersonDelegate;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class FirstPersonCameraTrigger extends TutorialTrigger {
	public FirstPersonCameraTrigger() {
		super(.1f, 2f, "fpc");
	}

	protected final void run(Tutorial tutorial) {
		Delegate delegate = tutorial.getViewer().getGUIRoot().getDelegate();
		if (delegate instanceof FirstPersonDelegate)
			tutorial.next(new MapModeTrigger());
	}
}
