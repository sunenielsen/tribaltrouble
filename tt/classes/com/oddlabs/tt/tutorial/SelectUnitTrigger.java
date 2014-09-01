package com.oddlabs.tt.tutorial;

import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class SelectUnitTrigger extends TutorialTrigger {
	public SelectUnitTrigger() {
		super(.1f, 15f, "select_unit");
	}

	protected final void run(Tutorial tutorial) {
		if (tutorial.getViewer().getSelection().getCurrentSelection().getNumUnits() > 0)
			tutorial.next(new MoveUnitTrigger(tutorial.getViewer().getLocalPlayer()));
	}
}
