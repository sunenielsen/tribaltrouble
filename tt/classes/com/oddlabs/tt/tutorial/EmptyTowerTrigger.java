package com.oddlabs.tt.tutorial;

import com.oddlabs.tt.form.TutorialForm;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.delegate.TutorialOverDelegate;
import com.oddlabs.tt.model.Building;

public final strictfp class EmptyTowerTrigger extends TutorialTrigger {
	private final Building tower;
	
	public EmptyTowerTrigger(Building tower) {
		super(.1f, 0f, "empty_tower");
		this.tower = tower;
		tower.getOwner().enableTowerExits(true);
	}

	protected final void run(Tutorial tutorial) {
		if (tower.getUnitContainer().getNumSupplies() == 0) {
			tutorial.done(TutorialForm.TUTORIAL_TOWER);
		}
	}
}
