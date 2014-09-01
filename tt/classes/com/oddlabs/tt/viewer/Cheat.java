package com.oddlabs.tt.viewer;

import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.landscape.World;

public final strictfp class Cheat {
	private final boolean can_enable;
	private boolean enabled = false;
	public boolean draw_trees = true;
	public boolean line_mode = false;

	public Cheat() {
		this(false);
	}

	Cheat(boolean can_enable) {
		this.can_enable = can_enable;
	}

	public final boolean isEnabled() {
		return enabled;
	}

	public final void enable() {
		if (Renderer.isRegistered() && can_enable)
			enabled = true;
	}
}
