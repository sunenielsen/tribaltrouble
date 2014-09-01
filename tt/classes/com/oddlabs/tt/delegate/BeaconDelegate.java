package com.oddlabs.tt.delegate;

import com.oddlabs.tt.camera.GameCamera;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.render.LandscapeLocation;
import com.oddlabs.tt.render.Picker;
import com.oddlabs.tt.gui.*;

public final strictfp class BeaconDelegate extends TargetDelegate {
	public BeaconDelegate(WorldViewer viewer, GameCamera camera) {
		super(viewer, camera, 0);
	}

	public final void mousePressed(int button, int x, int y) {
		LandscapeLocation landscape_hit = new LandscapeLocation();
		getViewer().getPicker().pickLocation(getCamera().getState(), landscape_hit);
		getViewer().getPeerHub().sendBeacon(landscape_hit.x, landscape_hit.y);
		pop();
	}
}
