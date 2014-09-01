package com.oddlabs.tt.trigger.campaign;

import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.trigger.IntervalTrigger;

public final strictfp class PlayerEleminatedTrigger extends IntervalTrigger {
	private final Runnable runnable;
	private final Player player;

	public PlayerEleminatedTrigger(Runnable runnable, Player player) {
		super(player.getWorld(), .5f, 0f);
		this.runnable = runnable;
		this.player = player;
	}

	protected final void check() {
		int units = player.getUnitCountContainer().getNumSupplies();
		if (units == 0 && !player.hasActiveChieftain()) {
			triggered();
		}
	}

	protected final void done() {
		runnable.run();
	}
}
