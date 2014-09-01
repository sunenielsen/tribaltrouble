package com.oddlabs.tt.tutorial;

import java.util.Iterator;
import java.util.Set;

import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Selectable;

public final strictfp class WeaponTrigger extends TutorialTrigger {
	private final static int WEAPONS = 10;

	public WeaponTrigger(Player local_player) {
		super(.5f, 0f, "weapon", new Object[]{new Integer(WEAPONS)});
		local_player.enableHarvesting(true);
	}

	protected final void run(Tutorial tutorial) {
		Set set = tutorial.getViewer().getSelection().getCurrentSelection().getSet(); 
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Selectable s = (Selectable)it.next();
			if (s instanceof Building && s.getAbilities().hasAbilities(Abilities.BUILD_ARMIES)) {
				Building armory = (Building)s;
				if (armory.getSupplyContainer(com.oddlabs.tt.model.weapon.RockAxeWeapon.class).getNumSupplies() >= WEAPONS)
					tutorial.next(new ArmyMenuTrigger(tutorial.getViewer().getLocalPlayer()));
			}
		}
	}
}
