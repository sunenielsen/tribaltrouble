package com.oddlabs.tt.model;

import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.util.BoundingBox;
import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.render.SpriteKey;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.util.ListElement;

public abstract strictfp class Model extends Element implements ListElement {
	private final World world;

	protected Model(World world) {
		super(world.getElementRoot());
		this.world = world;
	}

	public abstract float getShadowDiameter();

	public abstract float getOffsetZ();
	public abstract int getAnimation();
	public abstract float getAnimationTicks();
	public abstract SpriteKey getSpriteRenderer();
	public abstract float getNoDetailSize();

	private final void updateBounds() {
		float x = getPositionX();
		float y = getPositionY();
		float z = getPositionZ();
		BoundingBox unit_bounds = getSpriteRenderer().getBounds(getAnimation());
		float error = getZError();
		setBounds(unit_bounds.bmin_x + x, unit_bounds.bmax_x + x, unit_bounds.bmin_y + y, unit_bounds.bmax_y + y, unit_bounds.bmin_z + z - error, unit_bounds.bmax_z + z + error);
	}

	protected float getZError() {
		return 0f;
	}

	protected final float getLandscapeError() {
		return world.getHeightMap().getLeafFromCoordinates(getPositionX(), getPositionY()).getMaxError();
	}

	public final World getWorld() {
		return world;
	}

	public final void setPosition(float x, float y) {
		super.setPosition(x, y);
		reinsert();
	}

	protected final void reinsert() {
		if (isRegistered()) {
			setPositionZ(world.getHeightMap().getNearestHeight(getPositionX(), getPositionY()) + getOffsetZ());
			updateBounds();
			reregister();
		}
	}
}
