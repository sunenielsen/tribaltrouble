package com.oddlabs.tt.trigger.campaign;

import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.trigger.IntervalTrigger;

public final strictfp class GameStartedTrigger extends IntervalTrigger {
	private final Runnable runnable;

	public GameStartedTrigger(World world, Runnable runnable) {
		super(world, .25f, 0f);
		this.runnable = runnable;
	}

	protected final void check() {
		triggered();
	}

	protected final void done() {
		runnable.run();
	}
}
