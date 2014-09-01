package com.oddlabs.tt.tutorial;

import java.util.Iterator;
import java.util.Set;

import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.ChieftainContainer;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.model.Race;

public final strictfp class BuildingChieftainTrigger extends TutorialTrigger {
	public BuildingChieftainTrigger(Player player) {
		super(1f, 0f, "building_chieftain");
		player.enableRepairing(false);
		player.enableAttacking(false);
	//	player.enableQuarters(false);
		player.enableBuilding(Race.BUILDING_ARMORY, false);
		player.enableBuilding(Race.BUILDING_TOWER, false);
		player.enableHarvesting(false);
		player.enableWeapons(false);
		player.enableArmies(false);
		player.enableTransporting(false);
		player.enableRallyPoints(false);
	//	player.enableChieftains(false);
	}

	protected final void run(Tutorial tutorial) {
		Set set = tutorial.getViewer().getLocalPlayer().getUnits().getSet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Selectable s = (Selectable)it.next();
			if (s instanceof Building) {
				Building b = (Building)s;
				ChieftainContainer container = b.getChieftainContainer();
				if (container != null && container.isTraining())
					tutorial.next(new ChieftainBuiltTrigger());
			}
		}
	}
}
