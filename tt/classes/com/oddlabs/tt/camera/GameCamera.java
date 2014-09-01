package com.oddlabs.tt.camera;

import org.lwjgl.input.Keyboard;

import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.KeyboardEvent;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.delegate.SelectionDelegate;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.render.Picker;
import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class GameCamera extends Camera {
	public final static int SCROLL_BUFFER = 5;
	private final static float INIT_DISTANCE = 50;
	private final static float ANGLE_DELTA = (float)(StrictMath.PI/2);
	public final static float MAX_Z = 100f;
	private final static float ZOOM_Z_DIR_MIN = -(float)StrictMath.tan(StrictMath.PI/6);
	private final static float SCROLL_ACCELERATION_SECONDS_MAX = 1f;
	private final static float SCROLL_ACCELERATION_FACTOR = 2.5f;
	private final static float SCROLL_START_MAX_SPEED = 60f;
	private final static float ROTATE_PICKING_ANGLE_MAX = (-(Globals.FOV) - 10)*((float)StrictMath.PI/180)*.5f;
	private final static float ZOOM_SPEED = 50f;

	private final WorldViewer viewer;

	private float left_dir_x;
	private float left_dir_y;
	private float scroll_x;
	private float scroll_y;
	private float scrolling_x;
	private float scrolling_y;
	private float scroll_acceleration_seconds;
	private float scroll_start_speed;
	private boolean scroll_start;
	private float zoom_time;
	private float default_rotate_radius;
	private float last_zoom_factor;

	private Target rotation_point = null;
	private SelectionDelegate owner;

	private boolean pitch_up;
	private boolean pitch_down;
	private boolean rotate_left;
	private boolean rotate_right;

	public GameCamera(WorldViewer viewer, CameraState camera) {
		super(viewer.getWorld().getHeightMap(), camera);
		this.default_rotate_radius = viewer.getWorld().getHeightMap().getMetersPerWorld()/4;
		this.viewer = viewer;
		checkPosition();
		updateDirection();
	}

/*	public GameCamera() {
		super();
	}
*/
	public final void setOwner(SelectionDelegate owner) {
		this.owner = owner;
	}

	public final float getScrollX() {
		return scrolling_x;
	}
	
	public final float getScrollY() {
		return scrolling_y;
	}

	public final void resetLastZoomFactor() {
		last_zoom_factor = 0f;
	}

	public final float getLastZoomFactor() {
		return last_zoom_factor;
	}

	public final boolean pitchUp() {
		return pitch_up;
	}

	public final boolean pitchDown() {
		return pitch_down;
	}

	public final boolean rotateRight() {
		return rotate_right;
	}

	public final boolean rotateLeft() {
		return rotate_left;
	}

/*
   float radius = (float)StrictMath.cos(old_vert_angle);
   float old_dir_x = (float)StrictMath.cos(getHorizAngle())*radius;
   float old_dir_y = (float)StrictMath.sin(getHorizAngle())*radius;
   float old_dir_z = (float)StrictMath.sin(old_vert_angle);
   old_x = x - old_dir_x*distance_to_landscape;
   old_y = y - old_dir_y*distance_to_landscape;
   old_z = World.getHeightMap().getNearestHeight(x, y) - old_dir_z*distance_to_landscape;

*/
	public final void reset() {
	}

	public final void reset(float x, float y) {
		float dx = x - .5f*getHeightMap().getMetersPerWorld();
		float dy = y - .5f*getHeightMap().getMetersPerWorld();
		float r = (float)StrictMath.sqrt(dx*dx + dy*dy);
		if (dy > 0) {
			getState().setCurrentHorizAngle((float)(StrictMath.PI + StrictMath.acos(dx/r)));
		} else {
			getState().setCurrentHorizAngle(-(float)(StrictMath.PI + StrictMath.acos(dx/r)));
		}
//		setHorizAngle(-(float)StrictMath.PI/2f);
		getState().setCurrentVertAngle(-45f*(float)StrictMath.PI/180f);

		setPos(x, y);

		zoom_time = 0f;
		updateDirection();
	}

	public final void setPos(float x, float y) {
		float radius = (float)StrictMath.cos(getState().getTargetVertAngle());
		float dir_x = (float)StrictMath.cos(getState().getTargetHorizAngle())*radius;
		float dir_y = (float)StrictMath.sin(getState().getTargetHorizAngle())*radius;
		float dir_z = (float)StrictMath.sin(getState().getTargetVertAngle());
		getState().setCurrentX(x - dir_x*INIT_DISTANCE);
		getState().setCurrentY(y - dir_y*INIT_DISTANCE);
		getState().setCurrentZ(getHeightMap().getNearestHeight(x, y) - dir_z*INIT_DISTANCE);
		checkPosition();
	}

	private final void updateDirection() {
		left_dir_x = -(float)StrictMath.sin(getState().getTargetHorizAngle());
		left_dir_y = (float)StrictMath.cos(getState().getTargetHorizAngle());
	}

	private final void doZoom(float time_delta) {
		zoom(zoom_time*time_delta*ZOOM_SPEED*getState().getTargetZ());
		if (zoom_time < 0f)
			zoom_time = StrictMath.min(0f, zoom_time + time_delta);
		else if (zoom_time > 0)
			zoom_time = StrictMath.max(0f, zoom_time - time_delta);
	}

	public final void zoom(float zoom_factor) {
		if (zoom_factor != 0f) {
			last_zoom_factor = zoom_factor;
			float radius = (float)StrictMath.cos(getState().getTargetVertAngle());
			float dir_x = (float)StrictMath.cos(getState().getTargetHorizAngle())*radius;
			float dir_y = (float)StrictMath.sin(getState().getTargetHorizAngle())*radius;
			float dir_z = (float)StrictMath.sin(getState().getTargetVertAngle());
			if (dir_z > ZOOM_Z_DIR_MIN) {
				dir_z = ZOOM_Z_DIR_MIN;
				float inv_length = 1/(float)StrictMath.sqrt(dir_x*dir_x + dir_y*dir_y + dir_z*dir_z);
				dir_x *= inv_length;
				dir_y *= inv_length;
				dir_z *= inv_length;
			}
			float temp_x = getState().getTargetX() + dir_x*zoom_factor;
			float temp_y = getState().getTargetY() + dir_y*zoom_factor;
			float temp_z = getState().getTargetZ() + dir_z*zoom_factor;
			float backup_x = getState().getTargetX();
			float backup_y = getState().getTargetY();
			float backup_z = getState().getTargetZ();

			int mid = getHeightMap().getMetersPerWorld()/2;
			float dx = (temp_x - mid);
			float dy = (temp_y - mid);
			float squared_dist = dx*dx + dy*dy;
			if (squared_dist < getHeightMap().getMetersPerWorld()*getHeightMap().getMetersPerWorld() && temp_z < MAX_Z) {
				getState().setTargetX(temp_x);
				getState().setTargetY(temp_y);
				getState().setTargetZ(temp_z);
				if (bounce(getState().getTargetX(), getState().getTargetY(), getState().getTargetZ())) {
					getState().setTargetX(backup_x);
					getState().setTargetY(backup_y);
					getState().setTargetZ(backup_z);
				} else {
					getState().setTargetX(temp_x);
					getState().setTargetY(temp_y);
					getState().setTargetZ(temp_z);
//					setScrollSpeed();
				}
				checkPosition();
			} 
		}
	}

	private final void doScroll(float time_delta) {
		if (!viewer.getGUIRoot().getDelegate().canScroll())
			return;
		float scroll_speed = scroll_start_speed*(.4f + (scroll_acceleration_seconds/SCROLL_ACCELERATION_SECONDS_MAX)*SCROLL_ACCELERATION_FACTOR);
		float scroll_factor = time_delta*scroll_speed;
		boolean blocked = viewer.getGUIRoot().getDelegate().keyboardBlocked();
		if (LocalInput.isKeyDown(Keyboard.KEY_LEFT) && !LocalInput.isKeyDown(Keyboard.KEY_RIGHT) && !blocked)
			scrolling_x = -1f;
		else if (LocalInput.isKeyDown(Keyboard.KEY_RIGHT) && !LocalInput.isKeyDown(Keyboard.KEY_LEFT) && !blocked)
			scrolling_x = 1f;
		else
			scrolling_x = scroll_x;

		if (LocalInput.isKeyDown(Keyboard.KEY_DOWN) && !LocalInput.isKeyDown(Keyboard.KEY_UP) && !blocked)
			scrolling_y = -1f;
		else if (LocalInput.isKeyDown(Keyboard.KEY_UP) && !LocalInput.isKeyDown(Keyboard.KEY_DOWN) && !blocked)
			scrolling_y = 1f;
		else
			scrolling_y = scroll_y;

		float new_x = getState().getTargetX() - (scrolling_x*left_dir_x + scrolling_y*-left_dir_y)*scroll_factor;
		float new_y = getState().getTargetY() - (scrolling_x*left_dir_y + scrolling_y*left_dir_x)*scroll_factor;
		if (new_x != getState().getTargetX() || new_y != getState().getTargetY()) {
			getState().setTargetX(new_x);
			getState().setTargetY(new_y);
			checkPosition();
		}

		scroll_acceleration_seconds += time_delta;
		if (scroll_acceleration_seconds > SCROLL_ACCELERATION_SECONDS_MAX)
			scroll_acceleration_seconds = SCROLL_ACCELERATION_SECONDS_MAX;
	}

	private final void doPitch(float time_delta) {
		checkKeys();
		if ((pitch_down && !Settings.getSettings().invert_camera_pitch) ||
			(pitch_up && Settings.getSettings().invert_camera_pitch)) {
			getState().setTargetVertAngle(getState().getTargetVertAngle() - time_delta*ANGLE_DELTA);
			checkPosition();
		}
		if ((pitch_up && !Settings.getSettings().invert_camera_pitch) ||
			(pitch_down && Settings.getSettings().invert_camera_pitch)) {
			getState().setTargetVertAngle(getState().getTargetVertAngle() + time_delta*ANGLE_DELTA);
			checkPosition();
		}
	}

	private final void doRotate(float time_delta) {
		checkKeys();
		if (rotate_left || rotate_right) {
			float dx;
			float dy;
			float da;

			float[] point = getRotationPoint();
			if (insideWorld(point[0], point[1])) {
				dx = getState().getTargetX() - point[0];
				dy = getState().getTargetY() - point[1];
			} else {
				dx = -left_dir_y*default_rotate_radius;
				dy = left_dir_x*default_rotate_radius;
			}

			if (rotate_left) {
				da = -time_delta*ANGLE_DELTA;
			} else {
				da = time_delta*ANGLE_DELTA;
			}
			getState().setTargetHorizAngle(getState().getTargetHorizAngle() + da);
			getState().setTargetX(getState().getTargetX() - dx + (float)(dx*StrictMath.cos(da) - dy*StrictMath.sin(da)));
			getState().setTargetY(getState().getTargetY() - dy + (float)(dx*StrictMath.sin(da) + dy*StrictMath.cos(da)));
			checkPosition();
		}
	}

	public final int getRotateY() {
		int center_y = LocalInput.getViewHeight()/2;
		if (getState().getTargetVertAngle() < ROTATE_PICKING_ANGLE_MAX) {
			return center_y;
		} else {
			float da = getState().getTargetVertAngle() - ROTATE_PICKING_ANGLE_MAX;
			float pixels_per_unit = 1f/GUIRoot.getUnitsPerPixel(Globals.VIEW_MIN);
			int pixels_to_screen = (int)(Globals.VIEW_MIN*pixels_per_unit);
			int dy = (int)(((float)StrictMath.tan(da))*pixels_to_screen);
			int y = center_y - dy;
			return y;
		}
	}

	private final boolean insideWorld(float x, float y) {
		return x > 0 && x < getHeightMap().getMetersPerWorld() && y > 0 && y < getHeightMap().getMetersPerWorld();
	}

	public final void doAnimate(float t) {
		doZoom(t);
		doScroll(t);
		doPitch(t);
		doRotate(t);
		updateDirection();
	}

	public final void mouseScrolled(int amount) {
		zoom_time += amount*.05f;
		if (zoom_time > .15f)
			zoom_time = .15f;
		else if (zoom_time < -.15f)
			zoom_time = -.15f;
	}

	public final void setRotationPoint(Target target) {
		rotation_point = target;
	}

	protected final float[] getRotationPoint() {
		float[] point = new float[2];
		if (rotation_point != null) {
			point[0] = rotation_point.getPositionX();
			point[1] = rotation_point.getPositionY();
		} else {
			point[0] = getState().getTargetX();
			point[1] = getState().getTargetY();
		}
		return point;
	}

	public final void mouseMoved(int x, int y) {
		if ((owner == null || !owner.isSelecting()) && (x < SCROLL_BUFFER || y < SCROLL_BUFFER ||
				x > LocalInput.getViewWidth() - 1 - SCROLL_BUFFER || y > LocalInput.getViewHeight() - 1 - SCROLL_BUFFER)) {
			if (scroll_start) {
				scroll_start = false;
				if (!scrollSpeedLocked(0)) {
					scroll_acceleration_seconds = 0;
					setScrollSpeed();
				}
			}
			scroll_x = (float)(x - LocalInput.getViewWidth()/2);
			scroll_y = (float)(y - LocalInput.getViewHeight()/2);
			float inv_length = 1f/(float)StrictMath.sqrt(scroll_x*scroll_x + scroll_y*scroll_y);
			scroll_x *= inv_length;
			scroll_y *= inv_length;
		} else {
			scroll_start = true;
			scroll_x = 0;
			scroll_y = 0;
		}
	}

	private final boolean scrollSpeedLocked(int key) {
		return scroll_x != 0
			|| scroll_y != 0
			|| (LocalInput.isKeyDown(Keyboard.KEY_UP) && key != Keyboard.KEY_UP)
			|| (LocalInput.isKeyDown(Keyboard.KEY_DOWN) && key != Keyboard.KEY_DOWN)
			|| (LocalInput.isKeyDown(Keyboard.KEY_LEFT) && key != Keyboard.KEY_LEFT)
			|| (LocalInput.isKeyDown(Keyboard.KEY_RIGHT) && key != Keyboard.KEY_RIGHT);
	}

	private final void setScrollSpeed() {
		viewer.getPicker().pickRotate(this);
		float[] landscape_point = getRotationPoint();
		float landscape_z = getHeightMap().getNearestHeight(landscape_point[0], landscape_point[1]);
		float dx = landscape_point[0] - getState().getTargetX();
		float dy = landscape_point[1] - getState().getTargetY();
		float dz = landscape_z - getState().getTargetZ();
		scroll_start_speed = StrictMath.min((float)StrictMath.sqrt(dx*dx + dy*dy + dz*dz), SCROLL_START_MAX_SPEED);
	}

	public final World getWorld() {
		return viewer.getWorld();
	}

	public final void keyPressed(KeyboardEvent event) {
		switch (event.getKeyCode()) {
			case Keyboard.KEY_HOME:
			case Keyboard.KEY_NUMPAD8:
				break;
			case Keyboard.KEY_END:
			case Keyboard.KEY_NUMPAD2:
				break;
			case Keyboard.KEY_INSERT:
			case Keyboard.KEY_NUMPAD6:
				viewer.getPicker().pickRotate(this);
				break;
			case Keyboard.KEY_DELETE:
			case Keyboard.KEY_NUMPAD4:
				viewer.getPicker().pickRotate(this);
				break;
			case Keyboard.KEY_PRIOR:
			case Keyboard.KEY_NUMPAD9:
				mouseScrolled(-2);
				break;
			case Keyboard.KEY_NEXT:
			case Keyboard.KEY_NUMPAD3:
				mouseScrolled(2);
				break;
			case Keyboard.KEY_UP:
				if (!scrollSpeedLocked(Keyboard.KEY_UP)) {
					scroll_acceleration_seconds = 0;
					setScrollSpeed();
				}
				break;
			case Keyboard.KEY_DOWN:
				if (!scrollSpeedLocked(Keyboard.KEY_DOWN)) {
					scroll_acceleration_seconds = 0;
					setScrollSpeed();
				}
				break;
			case Keyboard.KEY_LEFT:
				if (!scrollSpeedLocked(Keyboard.KEY_LEFT)) {
					scroll_acceleration_seconds = 0;
					setScrollSpeed();
				}
				break;
			case Keyboard.KEY_RIGHT:
				if (!scrollSpeedLocked(Keyboard.KEY_RIGHT)) {
					scroll_acceleration_seconds = 0;
					setScrollSpeed();
				}
				break;
		}
	}

	private final void checkKeys() {
		if (viewer.getGUIRoot().getDelegate().keyboardBlocked() || viewer.getGUIRoot().getModalDelegate() != null) {
			pitch_up = false;
			pitch_down = false;
			rotate_right = false;
			rotate_left = false;
			return;
		}

		if (LocalInput.isKeyDown(Keyboard.KEY_HOME) || LocalInput.isKeyDown(Keyboard.KEY_NUMPAD8))
			pitch_up = true;
		else
			pitch_up = false;

		if (LocalInput.isKeyDown(Keyboard.KEY_END) || LocalInput.isKeyDown(Keyboard.KEY_NUMPAD2))
			pitch_down = true;
		else
			pitch_down = false;

		if (LocalInput.isKeyDown(Keyboard.KEY_INSERT) || LocalInput.isKeyDown(Keyboard.KEY_NUMPAD6))
			rotate_right = true;
		else
			rotate_right = false;

		if (LocalInput.isKeyDown(Keyboard.KEY_DELETE) || LocalInput.isKeyDown(Keyboard.KEY_NUMPAD4))
			rotate_left = true;
		else
			rotate_left = false;
	}

	public final void enable() {
		super.enable();
		mouseMoved(LocalInput.getMouseX(), LocalInput.getMouseY());
	}
}
