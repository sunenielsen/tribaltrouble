package com.oddlabs.tt.trigger.campaign;

import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.behaviour.NullController;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.trigger.IntervalTrigger;

public final strictfp class SupplyGatheredTrigger extends IntervalTrigger {
	private final Runnable runnable;
	private final Class type;
	private final int goal;
	private final Player local_player;

	public SupplyGatheredTrigger(Player local_player, Runnable runnable, Class type, int goal) {
		super(local_player.getWorld(), .5f, 0f);
		this.local_player = local_player;
		this.runnable = runnable;
		this.type = type;
		this.goal = goal;
	}

	protected final void check() {
		int count = 0;
		Selectable[][] selectables = local_player.classifyUnits();

		for (int i = 0; i < selectables.length; i++) {
			Selectable s = selectables[i][0];
			if (s.getPrimaryController() instanceof NullController) {
				for (int j = 0; j < selectables[i].length; j++) {
					if (selectables[i][j].getAbilities().hasAbilities(Abilities.BUILD_ARMIES)) {
						count += ((Building)selectables[i][j]).getSupplyContainer(type).getNumSupplies();
					}
				}
			}
		}

		if (count >= goal)
			triggered();
	}

	protected final void done() {
		runnable.run();
	}
}
