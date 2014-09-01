package com.oddlabs.tt.tutorial;

import java.util.Iterator;
import java.util.Set;

import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class QuartersTrigger extends TutorialTrigger {
	public QuartersTrigger() {
		super(1f, 0f, "quarters");
	}

	protected final void run(Tutorial tutorial) {
		Set set = tutorial.getViewer().getLocalPlayer().getUnits().getSet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Selectable s = (Selectable)it.next();
			if (s instanceof Building)
				tutorial.next(new SelectQuartersTrigger());
		}
	}
}
