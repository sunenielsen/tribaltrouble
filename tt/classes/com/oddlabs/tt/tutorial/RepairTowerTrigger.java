package com.oddlabs.tt.tutorial;

import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.model.Building;

public final strictfp class RepairTowerTrigger extends TutorialTrigger {
	private final Building tower;
	
	public RepairTowerTrigger(Building tower) {
		super(.1f, 0f, "repair");
		this.tower = tower;
		tower.getOwner().enableRepairing(true);
	}

	protected final void run(Tutorial tutorial) {
		if (!tower.isDamaged()) {
			tutorial.next(new EmptyTowerTrigger(tower));
		}
	}
}
