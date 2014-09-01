package com.oddlabs.tt.trigger.campaign;

import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.trigger.IntervalTrigger;

public final strictfp class DeathTrigger extends IntervalTrigger {
	private final Selectable selectable;
	private final Runnable runnable;

	public DeathTrigger(Selectable selectable, Runnable runnable) {
		super(selectable.getOwner().getWorld(), .5f, 0f);
		this.selectable = selectable;
		this.runnable = runnable;
	}

	protected final void check() {
		if (selectable.isDead())
			triggered();
	}

	protected final void done() {
		runnable.run();
	}
}
