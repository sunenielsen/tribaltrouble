package com.oddlabs.tt.tutorial;

import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class UnitInTowerTrigger extends TutorialTrigger {
	private final Building tower;
	
	public UnitInTowerTrigger(Building tower) {
		super(.1f, 0f, "unit_in_tower");
		this.tower = tower;
	}

	protected final void run(Tutorial tutorial) {
		if (tower.getUnitContainer().getNumSupplies() > 0) {
			tutorial.next(new AttackTowerTrigger(tower));
		}
	}
}
