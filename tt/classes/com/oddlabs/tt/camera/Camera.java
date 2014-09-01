package com.oddlabs.tt.camera;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;

import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.audio.Audio;
import com.oddlabs.tt.audio.AudioFile;
import com.oddlabs.tt.audio.AbstractAudioPlayer;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.KeyboardEvent;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.landscape.HeightMap;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.resource.Resources;
import com.oddlabs.tt.util.StateChecksum;
import com.oddlabs.tt.util.StrictGLU;
import com.oddlabs.tt.util.StrictMatrix4f;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.util.StrictVector3f;

public abstract strictfp class Camera implements Animated {
	private final static float LANDSCAPE_OFFSET = 5f;
	private final static float SMOOTHNESS_FACTOR = 15;

	private final int[] viewport = new int[4];
	private final StrictMatrix4f proj = new StrictMatrix4f();
	private final CameraState tmp_camera = new CameraState();
	private final float[] hit_result = new float[3];
	private final HeightMap heightmap;

	private final CameraState state;
	private float smoothness_factor = SMOOTHNESS_FACTOR;

	public Camera(HeightMap heightmap, CameraState state) {
		this.heightmap = heightmap;
		this.state = state;
	}

	protected final HeightMap getHeightMap() {
		return heightmap;
	}

	protected final void setSmoothnessFactor(float f) {
		smoothness_factor = f;
	}

	public final void updateChecksum(StateChecksum checksum) {
//System.out.println("camera_x = " + camera_x + " | camera_y = " + camera_y + " | camera_z = " + camera_z + " | dir_x = " + dir_x + " | dir_y = " + dir_y + " | dir_z = " + dir_z);
		state.updateChecksum(checksum);
	}

	public final void animate(float delta_t) {
		doAnimate(delta_t);
		state.animate(delta_t, smoothness_factor);
	}

	protected abstract void doAnimate(float delta_t);

	protected final void checkPosition() {
		int mid = heightmap.getMetersPerWorld()/2;
		float dx = (state.getTargetX() - mid);
		float dy = (state.getTargetY() - mid);
		float squared_dist = dx*dx + dy*dy;
		if (squared_dist > heightmap.getMetersPerWorld()*heightmap.getMetersPerWorld()) {
			float scale = heightmap.getMetersPerWorld()/(float)StrictMath.sqrt(squared_dist);
			state.setTargetX(dx*scale + mid);
			state.setTargetY(dy*scale + mid);
		}
		if (!bounce(state.getTargetX(), state.getTargetY(), state.getTargetZ())) {
			if (state.getTargetZ() > GameCamera.MAX_Z)
				state.setTargetZ(GameCamera.MAX_Z);
		}
	}

	protected final boolean bounce(float x, float y, float z) {
		boolean bounced = false;
		viewport[0] = 0;
		viewport[1] = 0;
		viewport[2] = LocalInput.getViewWidth();
		viewport[3] = LocalInput.getViewHeight();
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				proj.setIdentity();
				StrictGLU.gluPerspective(proj,
						Globals.FOV,
						LocalInput.getViewAspect(),
						Globals.VIEW_MIN,
						Globals.VIEW_MAX);

				tmp_camera.set(state);
				tmp_camera.setTargetView(proj);

				StrictGLU.gluUnProject(i*LocalInput.getViewWidth(),
						j*LocalInput.getViewHeight(),
						0f,
						tmp_camera.getModelView(), proj, viewport, hit_result);
				float hit_x = hit_result[0];
				float hit_y = hit_result[1];
				float hit_z = hit_result[2];

				float dx1 = hit_x - x;
				float dy1 = hit_y - y;
				float dz1 = hit_z - z;
				float inv_length = LANDSCAPE_OFFSET/(float)StrictMath.sqrt(dx1*dx1 + dy1*dy1 + dz1*dz1);
				dx1 *= inv_length;
				dy1 *= inv_length;
				dz1 *= inv_length;

				float min_height = StrictMath.max(heightmap.getNearestHeight(x + dx1, y + dy1),
						heightmap.getSeaLevelMeters());
				hit_z = z + dz1;
				if (hit_z < min_height) {
					bounced = true;
					z = z + min_height - hit_z;
				}
			}
		}
		float min_height = heightmap.getNearestHeight(x, y);
		if (z < min_height) {
			bounced = true;
			z = min_height;
		}
		if (bounced)
			state.setTargetZ(z);
		return bounced;
	}

	public final CameraState getState() {
		return state;
	}

	public final void disable() {
		LocalEventQueue.getQueue().getHighPrecisionManager().removeAnimation(this);
	}

	public void enable() {
		LocalEventQueue.getQueue().getHighPrecisionManager().registerAnimation(this);
	}

	public void keyPressed(KeyboardEvent event) {
	}

	public void keyReleased(KeyboardEvent event) {
	}

	public void mouseScrolled(int amount) {
	}

	public void mouseMoved(int x, int y) {
	}
}

