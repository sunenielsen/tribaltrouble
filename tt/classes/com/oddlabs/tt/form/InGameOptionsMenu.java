package com.oddlabs.tt.form;

import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class InGameOptionsMenu extends AbstractOptionsMenu {
	private final WorldViewer viewer;

	public InGameOptionsMenu(GUIRoot gui_root, WorldViewer viewer) {
		super(gui_root);
		this.viewer = viewer;
		chooseGamespeed(getGamespeed());
	}

	private int getGamespeed() {
		int gamespeed = viewer.getLocalPlayer().getGamespeed();
		if (!World.isValidGamespeed(gamespeed))
			gamespeed = viewer.getWorld().getGamespeed();
		return gamespeed;
	}

	protected final void changeGamespeed(int index) {
		super.changeGamespeed(index);
		viewer.getPeerHub().getPlayerInterface().setPreferredGamespeed(index);
	}
}
