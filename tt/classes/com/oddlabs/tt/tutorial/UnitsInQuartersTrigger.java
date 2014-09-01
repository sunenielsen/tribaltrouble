package com.oddlabs.tt.tutorial;

import java.util.Iterator;
import java.util.Set;

import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class UnitsInQuartersTrigger extends TutorialTrigger {
	public UnitsInQuartersTrigger() {
		super(1f, 0f, "units_in_quarters");
	}

	protected final void run(Tutorial tutorial) {
		Set set = tutorial.getViewer().getLocalPlayer().getUnits().getSet();
		Iterator it = set.iterator();
		int count = 0;
		while (it.hasNext()) {
			Selectable s = (Selectable)it.next();
			if (s instanceof Unit)
				count++;
		}
		if (count == 0)
			tutorial.next(new RallyPointTrigger());
	}
}
