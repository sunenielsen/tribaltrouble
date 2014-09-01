package com.oddlabs.tt.delegate;

import com.oddlabs.tt.camera.GameCamera;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.render.Picker;
import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.gui.*;

public final strictfp class RallyPointDelegate extends TargetDelegate {
	private final Building building;

	public RallyPointDelegate(WorldViewer viewer, GameCamera camera, Building building) {
		super(viewer, camera, 0);
		this.building = building;
	}

	public final void mousePressed(int button, int x, int y) {
		if (building.isDead()) {
			pop();
			return;
		}
		Target target = getViewer().getPicker().pickRallyPoint(getViewer().getGUIRoot().getDelegate().getCamera().getState(), x, y, building);
		if (target == null)
			return;
		if (building.isValidRallyPoint(target)) {
			getViewer().getPeerHub().getPlayerInterface().setRallyPoint(building, target);
		} else {
			getViewer().getPeerHub().getPlayerInterface().setRallyPoint(building, target.getGridX(), target.getGridY());
		}
		pop();
	}
}
