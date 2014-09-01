package com.oddlabs.tt.tutorial;

import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.delegate.SelectionDelegate;

public final strictfp class HarvestMenuTrigger extends TutorialTrigger {
	public HarvestMenuTrigger(Player local_player) {
		super(.1f, 0f, "harvest_menu");
		local_player.enableHarvesting(true);
	}

	protected final void run(Tutorial tutorial) {
		if (tutorial.getViewer().getPanel().inHarvestMenu())
			tutorial.next(new SupplyTrigger(tutorial.getViewer().getLocalPlayer()));
	}
}
