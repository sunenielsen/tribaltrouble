package com.oddlabs.tt.tutorial;

import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class SelectQuartersTrigger extends TutorialTrigger {
	public SelectQuartersTrigger() {
		super(.1f, 0f, "select_quarters");
	}

	protected final void run(Tutorial tutorial) {
		Building building = tutorial.getViewer().getSelection().getCurrentSelection().getBuilding();
		if (building != null && building.getAbilities().hasAbilities(Abilities.REPRODUCE))
			tutorial.next(new UnitsInQuartersTrigger());
	}
}
