package com.oddlabs.tt.camera;

import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.delegate.CameraDelegate;
import com.oddlabs.tt.delegate.JumpDelegate;
import com.oddlabs.tt.render.Picker;

public final strictfp class JumpCamera extends Camera {
	private final static float DEFAULT_METERS_PER_SECOND = 300f;
	private final static float DEFAULT_MAX_SECONDS = .5f;

	private final CameraDelegate delegate;

	private final float dst_x;
	private final float dst_y;
	private final float dst_z;
	private final float dx;
	private final float dy;
	private final float dz;
	private final float factor;
	private final float z_accel;
	private float seconds;
	private float z_speed;
	private float temp_z;

	public JumpCamera(JumpDelegate delegate, GameCamera old_camera, float x, float y) {
		this(delegate, old_camera, x, y, DEFAULT_METERS_PER_SECOND, DEFAULT_MAX_SECONDS);
	}

	public JumpCamera(JumpDelegate delegate, GameCamera old_camera, float x, float y, float meters_per_second, float max_seconds) {
		super(old_camera.getHeightMap(), old_camera.getState());
		this.delegate = delegate;
		delegate.getViewer().getPicker().pickRotate(old_camera);
		float[] target = old_camera.getRotationPoint();
		float dx = target[0] - getState().getTargetX();
		float dy = target[1] - getState().getTargetY();
		float dz = getHeightMap().getNearestHeight(target[0], target[1]) - getState().getTargetZ();
		float distance_to_landscape = (float)StrictMath.sqrt(dx*dx + dy*dy + dz*dz);
		float dir_x = dx/distance_to_landscape;
		float dir_y = dy/distance_to_landscape;
		float dir_z = dz/distance_to_landscape;
		dst_x = (int)x - dir_x*distance_to_landscape;
		dst_y = (int)y - dir_y*distance_to_landscape;
		dst_z = getHeightMap().getNearestHeight((int)x, (int)y) - dir_z*distance_to_landscape;
		this.dx = dst_x - getState().getTargetX();
		this.dy = dst_y - getState().getTargetY();
		this.dz = dst_z - getState().getTargetZ();
		float distance_to_dst = (float)StrictMath.sqrt(this.dx*this.dx + this.dy*this.dy + this.dz*this.dz);
		seconds = StrictMath.min(distance_to_dst/meters_per_second, max_seconds);
		factor = 1f/seconds;
		z_accel = -(distance_to_dst/32)/(seconds*.5f);
		z_speed = this.dz*factor - z_accel*(seconds/2f);

		temp_z = getState().getTargetZ();
	}
	
	public final void doAnimate(float t) {
		if (seconds <= 0f) {
			getState().setTargetX(dst_x);
			getState().setTargetY(dst_y);
			getState().setTargetZ(dst_z);
			delegate.pop();
			return;
		}
		seconds -= t;
		getState().setTargetX(getState().getTargetX() + dx*factor*t);
		getState().setTargetY(getState().getTargetY() + dy*factor*t);
		temp_z += z_speed*t;
		getState().setTargetZ(temp_z);
		z_speed += z_accel*t;
		bounce(getState().getTargetX(), getState().getTargetY(), getState().getTargetZ());
	}
}
