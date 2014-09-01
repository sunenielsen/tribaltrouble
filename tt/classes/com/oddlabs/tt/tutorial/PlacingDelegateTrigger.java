package com.oddlabs.tt.tutorial;

import com.oddlabs.tt.model.Race;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.delegate.PlacingDelegate;

public final strictfp class PlacingDelegateTrigger extends TutorialTrigger {
	public PlacingDelegateTrigger(Player player) {
		super(.1f, 0f, "placing");
		player.enableRepairing(false);
		player.enableAttacking(false);
		player.enableBuilding(Race.BUILDING_ARMORY, false);
		player.enableBuilding(Race.BUILDING_TOWER, false);
		player.enableChieftains(false);
	}

	protected final void run(Tutorial tutorial) {
		if (tutorial.getViewer().getGUIRoot().getDelegate() instanceof PlacingDelegate)
			tutorial.next(new QuartersTrigger());
	}
}
