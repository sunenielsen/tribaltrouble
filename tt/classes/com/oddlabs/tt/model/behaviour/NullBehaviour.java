package com.oddlabs.tt.model.behaviour;

import com.oddlabs.tt.model.Selectable;

public final strictfp class NullBehaviour implements Behaviour {
	public NullBehaviour() {
	}

	public final int animate(float t) {
		return Selectable.INTERRUPTIBLE;
	}

	public final boolean isBlocking() {
		return true;
	}

	public final void forceInterrupted() {
	}
}
