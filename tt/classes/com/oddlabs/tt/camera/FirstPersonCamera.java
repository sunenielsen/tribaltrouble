package com.oddlabs.tt.camera;

import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;

import com.oddlabs.tt.landscape.HeightMap;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.gui.InputState;
import com.oddlabs.tt.input.PointerInput;

public final strictfp class FirstPersonCamera extends Camera {
	private final static float SCALE_HORIZ = .002f;
	private final static float SCALE_VERT = .002f;

	private final int last_x;
	private final int last_y;

	public FirstPersonCamera(HeightMap heightmap, CameraState camera) {
		super(heightmap, camera);
		this.last_x = LocalInput.getMouseX();
		this.last_y = LocalInput.getMouseY();
	}

	public final void doAnimate(float t) {
		float dir_x = (float)StrictMath.cos(getState().getTargetHorizAngle());
		float dir_y = (float)StrictMath.sin(getState().getTargetHorizAngle());
		float left_dir_x = -dir_y;
		float left_dir_y = dir_x;

		float scrolling_x = 0;
		float scrolling_y = 0;
		if (LocalInput.isKeyDown(Keyboard.KEY_LEFT) && !LocalInput.isKeyDown(Keyboard.KEY_RIGHT))
			scrolling_x = -1f;
		else if (LocalInput.isKeyDown(Keyboard.KEY_RIGHT) && !LocalInput.isKeyDown(Keyboard.KEY_LEFT))
			scrolling_x = 1f;

		if (LocalInput.isKeyDown(Keyboard.KEY_DOWN) && !LocalInput.isKeyDown(Keyboard.KEY_UP))
			scrolling_y = -1f;
		else if (LocalInput.isKeyDown(Keyboard.KEY_UP) && !LocalInput.isKeyDown(Keyboard.KEY_DOWN))
			scrolling_y = 1f;

		float scroll_factor = getState().getTargetZ()*t;
		float new_x = getState().getTargetX() - (scrolling_x*left_dir_x + scrolling_y*-left_dir_y)*scroll_factor;
		float new_y = getState().getTargetY() - (scrolling_x*left_dir_y + scrolling_y*left_dir_x)*scroll_factor;

		if (new_x != getState().getTargetX() || new_y != getState().getTargetY()) {
			getState().setTargetX(new_x);
			getState().setTargetY(new_y);
			checkPosition();
		}
	}

	public final void mouseMoved(int x, int y) {
		int dx = x - last_x;
		int dy = y - last_y;
		getState().setTargetHorizAngle(getState().getTargetHorizAngle() - dx*SCALE_HORIZ);
		if (Settings.getSettings().invert_camera_pitch)
			getState().setTargetVertAngle(getState().getTargetVertAngle() - dy*SCALE_VERT);
		else
			getState().setTargetVertAngle(getState().getTargetVertAngle() + dy*SCALE_VERT);

		PointerInput.setCursorPosition(last_x, last_y);
	}
}
