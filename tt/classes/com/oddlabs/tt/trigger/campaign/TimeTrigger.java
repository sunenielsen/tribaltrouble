package com.oddlabs.tt.trigger.campaign;

import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.trigger.IntervalTrigger;

public final strictfp class TimeTrigger extends IntervalTrigger {
	private final Runnable runnable;

	public TimeTrigger(World world, float time, Runnable runnable) {
		super(time, 0f, world.getAnimationManagerGameTime());
		this.runnable = runnable;
	}

	protected final void check() {
		triggered();
	}

	protected final void done() {
		runnable.run();
	}
}
