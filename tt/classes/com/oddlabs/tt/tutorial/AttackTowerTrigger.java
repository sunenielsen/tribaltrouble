package com.oddlabs.tt.tutorial;

import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Race;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.util.Target;

public final strictfp class AttackTowerTrigger extends TutorialTrigger {
	private final static int NUM_UNITS = 12;
	
	private final Building tower;
	private final Player ai;
	
	public AttackTowerTrigger(Building tower) {
		super(.1f, 0f, "attack_tower");
		this.ai = tower.getOwner().getWorld().getPlayers()[1];
		this.tower = tower;
		Selectable[] units = new Selectable[NUM_UNITS];
		for (int i = 0; i < units.length; i++)
			units[i] = new Unit(ai, tower.getPositionX() - 50, tower.getPositionY() - 50, null, ai.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		ai.setTarget(units, tower, Target.ACTION_ATTACK, false);
	}

	public final void run(Tutorial tutorial) {
		if (ai.getUnitCountContainer().getNumSupplies() == 0) {
			tutorial.next(new RepairTowerTrigger(tower));
		}
	}
}
