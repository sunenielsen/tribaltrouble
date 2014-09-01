package com.oddlabs.tt.camera;

import java.util.ResourceBundle;

import org.lwjgl.input.Keyboard;

import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.KeyboardEvent;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.delegate.SelectionDelegate;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.util.Utils;

public final strictfp class MapCamera extends Camera {
	private final static float MAP_THRESHOLD = .1f;
	private final static float MAP_TIME_FACTOR = 1000f;
	private final static float SMOOTHNESS_FACTOR = 200f;
	public final static float MAP_Z_FACTOR = 1.3f;

	// map modes
	private final static int TO_MAP   = 1;
	private final static int IN_MAP   = 2;
	private final static int FROM_MAP = 3;

	private final SelectionDelegate delegate;
	private float old_x;
	private float old_y;
	private float old_z;
	private float old_vert_angle;
	private final float distance_to_landscape;
	private final Label label = new Label(Utils.getBundleString(ResourceBundle.getBundle(MapCamera.class.getName()), "map_mode"), Skin.getSkin().getHeadlineFont());

	private int map_mode = TO_MAP;

	public MapCamera(SelectionDelegate delegate, GameCamera old_camera) {
		super(old_camera.getHeightMap(), old_camera.getState());
		this.delegate = delegate;
		old_x = getState().getTargetX();
		old_y = getState().getTargetY();
		old_z = getState().getTargetZ();
		old_vert_angle = getState().getTargetVertAngle();
		float[] target = old_camera.getRotationPoint();
		float dx = target[0] - old_x;
		float dy = target[1] - old_y;
		float dz = getHeightMap().getNearestHeight(target[0], target[1]) - old_z;
		distance_to_landscape = (float)StrictMath.sqrt(dx*dx + dy*dy + dz*dz);

		setSmoothnessFactor(SMOOTHNESS_FACTOR);
	}

	public final void doAnimate(float t) {
		float factor = t*1000f/StrictMath.max(t*1000f, Settings.getSettings().mapmode_delay*MAP_TIME_FACTOR);
		float dx;
		float dy;
		float dz;
		float da;
		float map_x = getHeightMap().getMetersPerWorld()/2;
		float map_y = getHeightMap().getMetersPerWorld()/2;
		float map_z = getHeightMap().getMetersPerWorld()*MAP_Z_FACTOR;

		switch (map_mode) {
			case TO_MAP:
				dx = map_x - old_x;
				dy = map_y - old_y;
				dz = map_z - old_z;
				getState().setTargetX(getState().getTargetX() + dx*factor);
				getState().setTargetY(getState().getTargetY() + dy*factor);
				getState().setTargetZ(getState().getTargetZ() + dz*factor);
				if (getState().getTargetZ() > map_z - MAP_THRESHOLD) {
					getState().setTargetX(map_x);
					getState().setTargetY(map_y);
					getState().setTargetZ(map_z);
					changeMode(IN_MAP);
				}

				da = CameraState.MIN_ANGLE - old_vert_angle;
				getState().setTargetVertAngle(getState().getTargetVertAngle() + da*factor);
				break;
			case IN_MAP:
				break;
			case FROM_MAP:
				dx = old_x - map_x;
				dy = old_y - map_y;
				dz = old_z - map_z;
				getState().setTargetX(getState().getTargetX() + dx*factor);
				getState().setTargetY(getState().getTargetY() + dy*factor);
				getState().setTargetZ(getState().getTargetZ() + dz*factor);
				if (getState().getTargetZ() <= old_z) {
					getState().setTargetX(old_x);
					getState().setTargetY(old_y);
					getState().setTargetZ(old_z);
					getState().setTargetVertAngle(old_vert_angle);
					checkPosition();
					delegate.exitMapMode();
					break;
				}

				da = old_vert_angle - CameraState.MIN_ANGLE;
				getState().setTargetVertAngle(getState().getTargetVertAngle() + da*factor);
				break;
		}
	}

	private final void changeMode(int mode) {
		map_mode = mode;
		if (mode == IN_MAP) {
			label.setPos((LocalInput.getViewWidth() - label.getWidth())/2, LocalInput.getViewHeight() - label.getHeight());
			delegate.addChild(label);
			getState().setNoDetailMode(true);
		} else if (mode == FROM_MAP) {
			label.remove();
			getState().setNoDetailMode(false);
		}
	}

	public final void mapGoto(float x, float y) {
		this.mapGoto(x, y, false);
	}

	public final void mapGoto(float x, float y, boolean override) {
		if (map_mode == IN_MAP || override) {
			//	float radius = (float)StrictMath.cos(getVertAngle());
			//	float old_dir_x = (float)StrictMath.cos(getHorizAngle())*radius;
			//	float old_dir_y = (float)StrictMath.sin(getHorizAngle())*radius;
			//	float old_dir_z = (float)StrictMath.sin(getVertAngle());
			float radius = (float)StrictMath.cos(old_vert_angle);
			float old_dir_x = (float)StrictMath.cos(getState().getHorizAngle())*radius;
			float old_dir_y = (float)StrictMath.sin(getState().getHorizAngle())*radius;
			float old_dir_z = (float)StrictMath.sin(old_vert_angle);
			old_x = x - old_dir_x*distance_to_landscape;
			old_y = y - old_dir_y*distance_to_landscape;
			old_z = getHeightMap().getNearestHeight(x, y) - old_dir_z*distance_to_landscape;
			changeMode(FROM_MAP);
		}
	}

	public final void keyPressed(KeyboardEvent event) {
		switch (event.getKeyCode()) {
			case Keyboard.KEY_SPACE:
			case Keyboard.KEY_NUMPAD5:
				if (map_mode == TO_MAP || map_mode == IN_MAP)
					changeMode(FROM_MAP);
				else
					changeMode(TO_MAP);
				break;
		}
	}
}
