package com.oddlabs.tt.tutorial;

import java.util.Iterator;
import java.util.Set;

import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class SupplyTrigger extends TutorialTrigger {
	private final static int TREE = 20;
	private final static int ROCK = 10;

	public SupplyTrigger(Player player) {
		super(.5f, 0f, "supply", new Object[]{new Integer(TREE), new Integer(ROCK)});
		player.enableHarvesting(true);
	}

	protected final void run(Tutorial tutorial) {
		Set set = tutorial.getViewer().getSelection().getCurrentSelection().getSet(); 
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Selectable s = (Selectable)it.next();
			if (s instanceof Building && s.getAbilities().hasAbilities(Abilities.BUILD_ARMIES)) {
				Building armory = (Building)s;
				if (armory.getSupplyContainer(com.oddlabs.tt.model.RockSupply.class).getNumSupplies() >= ROCK && 
						armory.getSupplyContainer(com.oddlabs.tt.landscape.TreeSupply.class).getNumSupplies() >= TREE)
					tutorial.next(new BuildMenuTrigger(tutorial.getViewer().getLocalPlayer()));
			}
		}

	}
}
