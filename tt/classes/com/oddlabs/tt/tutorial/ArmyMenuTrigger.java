package com.oddlabs.tt.tutorial;

import com.oddlabs.tt.gui.ActionButtonPanel;
import com.oddlabs.tt.delegate.SelectionDelegate;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class ArmyMenuTrigger extends TutorialTrigger {
	public ArmyMenuTrigger(Player local_player) {
		super(.1f, 1f, "army_menu");
		local_player.enableArmies(true);
	}

	protected final void run(Tutorial tutorial) {
		if (tutorial.getViewer().getPanel().inArmyMenu())
			tutorial.next(new ArmyTrigger(tutorial.getViewer().getLocalPlayer()));
	}
}
