package com.oddlabs.tt.trigger.campaign;

import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.trigger.IntervalTrigger;

public final strictfp class ReinforcementsTrigger extends IntervalTrigger {
	private final Player player;
	private final int type;

	private int units_deployed = 0;

	public ReinforcementsTrigger(Player player, int type) {
		super(player.getWorld(), .5f, 0f);
		this.player = player;
		this.type = type;
	}

	protected final void check() {
		if (player.getArmory() == null) {
			triggered();
		} else if (units_deployed < player.getUnitsLost()) {
			int reinforcements = player.getUnitsLost() - units_deployed;
			if (reinforcements > player.getArmory().getUnitContainer().getNumSupplies()) {
				reinforcements = player.getArmory().getUnitContainer().getNumSupplies();
			}
			if (reinforcements > 0) {
				player.deployUnits(player.getArmory(), type, reinforcements);
				units_deployed += reinforcements;
			}
		}
	}

	protected final void done() {
	}
}
