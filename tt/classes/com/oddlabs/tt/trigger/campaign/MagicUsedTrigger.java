package com.oddlabs.tt.trigger.campaign;

import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.model.behaviour.MagicController;
import com.oddlabs.tt.trigger.IntervalTrigger;

public final strictfp class MagicUsedTrigger extends IntervalTrigger {
	private final Unit chieftain;
	private final float x;
	private final float y;
	private final float r;
	private final int index;
	private final Runnable runnable;

	private boolean blowing = false;

	public MagicUsedTrigger(Unit chieftain, float x, float y, float r, int index, Runnable runnable) {
		super(chieftain.getOwner().getWorld(), 0f, 0f);
		this.chieftain = chieftain;
		this.x = x;
		this.y = y;
		this.r = r;
		this.index = index;
		this.runnable = runnable;
	}

	protected final void check() {
		float dx = chieftain.getPositionX() - x;
		float dy = chieftain.getPositionY() - y;
		if (!chieftain.isDead()) {
			if (r*r > dx*dx + dy*dy) {
				if (!blowing && chieftain.getPrimaryController() instanceof MagicController && chieftain.getLastMagicIndex() == index)
					blowing = true;
				if (blowing && !(chieftain.getPrimaryController() instanceof MagicController))
					triggered();
			}
		}
	}

	protected final void done() {
		runnable.run();
	}
}
