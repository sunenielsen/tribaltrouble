package com.oddlabs.tt.tutorial;

import com.oddlabs.tt.form.TutorialForm;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.delegate.TutorialOverDelegate;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.player.Player;

public final strictfp class TutorialOverTrigger extends TutorialTrigger {

	public TutorialOverTrigger() {
		super(1f, 0f, "tutorial_over");
	}

	protected final void run(Tutorial tutorial) {
		Player[] players = tutorial.getViewer().getWorld().getPlayers();
		Player local_player = tutorial.getViewer().getLocalPlayer();

		for (int i = 0; i < players.length; i++) {
			Player current = players[i];
			int units = current.getUnitCountContainer().getNumSupplies();
			int buildings = current.getBuildingCountContainer().getNumSupplies();
			if (units == 0 && (current == local_player || buildings == 0)) {
				tutorial.done(TutorialForm.TUTORIAL_BATTLE);
			}
		}
	}
}
