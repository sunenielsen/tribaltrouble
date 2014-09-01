package com.oddlabs.tt.trigger.campaign;

import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.trigger.IntervalTrigger;

public final strictfp class VictoryTrigger extends IntervalTrigger {
	private final WorldViewer viewer;
	private final Runnable runnable;

	public VictoryTrigger(WorldViewer viewer, Runnable runnable) {
		super(viewer.getWorld(), .5f, 0f);
		this.viewer = viewer;
		this.runnable = runnable;
	}

	protected final void check() {
		Player[] players = viewer.getWorld().getPlayers();
		Player local = viewer.getLocalPlayer();

		for (int i = 0; i < players.length; i++) {
			Player current = players[i];
			if (local.isEnemy(current)) {
				int units = current.getUnitCountContainer().getNumSupplies();
				if (units > 0 || current.hasActiveChieftain()) {
					return;
				}
			}
		}
		triggered();
	}

	protected final void done() {
		runnable.run();
	}
}
