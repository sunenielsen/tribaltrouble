package com.oddlabs.tt.landscape;

import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.model.Element;
import com.oddlabs.tt.model.ElementVisitor;
import com.oddlabs.tt.model.AbstractElementNode;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.util.StateChecksum;
import com.oddlabs.tt.camera.CameraState;

public final strictfp class LandscapeTargetRespond extends Element implements Animated {
	public final static int SIZE = 128;
	private final static float SECOND_PER_PICK_RESPOND = 1f/3f;

	private float time;

	public LandscapeTargetRespond(World world, float x, float y) {
		super(world.getElementRoot());
		setPosition(x, y);
		setPositionZ(world.getHeightMap().getNearestHeight(x, y));
		setBounds(x - SIZE/2, x + SIZE/2, y - SIZE/2, y + SIZE/2, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
		register();
	}

	public final void animate(float t) {
		if (time > 0) {
			time = StrictMath.max(0, time - t);
		} else {
			remove();
		}
	}

	public final float getProgress() {
		return time/SECOND_PER_PICK_RESPOND;
	}

	public final void updateChecksum(StateChecksum checksum) {
	}

	protected void register() {
		super.register();
		time = SECOND_PER_PICK_RESPOND;
		LocalEventQueue.getQueue().getManager().registerAnimation(this);
	}

	public final void visit(ElementVisitor visitor) {
		visitor.visitRespond(this);
	}

	protected final void remove() {
		super.remove();
		LocalEventQueue.getQueue().getManager().removeAnimation(this);
	}
}
