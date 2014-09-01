package com.oddlabs.tt.model.behaviour;

import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;

public final strictfp class StunBehaviour implements Behaviour {
	private final StunController controller;
	private final Unit unit;

	public StunBehaviour(StunController controller, Unit unit) {
		this.controller = controller;
		this.unit = unit;
	}

	public final int animate(float t) {
		unit.switchToIdleAnimation();
		if (!controller.shouldSleep(t))
			return Selectable.DONE;
		else
			return Selectable.UNINTERRUPTIBLE;
	}

	public final boolean isBlocking() {
		return true;
	}

	public final void forceInterrupted() {
	}
}
