package com.oddlabs.tt.viewer;

import com.oddlabs.tt.animation.*;
import com.oddlabs.tt.gui.*;
import com.oddlabs.tt.audio.*;
import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.event.LocalEventQueue;

public strictfp class Notification implements Updatable {
	private final static float ACTIVE_SECONDS = 5f;
	
	private final float center_x;
	private final float center_y;
	private final NotificationManager manager;
	private final TimerAnimation timer;
	private final Arrow arrow;

	public Notification(World world, GUIRoot gui_root, float x, float y, NotificationManager manager, float r, float g, float b, Audio sound, boolean show_always, AnimationManager animation_manager) {
		this.center_x = x;
		this.center_y = y;
		this.manager = manager;
		this.timer = new TimerAnimation(animation_manager, this, ACTIVE_SECONDS);
		timer.start();
		this.arrow = new Arrow(world.getHeightMap(), gui_root, center_x, center_y, r, g, b, show_always);
		gui_root.addChild(arrow);
		world.getAudio().newAudio(new AudioParameters(sound, 0f, 0f, 0f, AudioPlayer.AUDIO_RANK_NOTIFICATION, AudioPlayer.AUDIO_DISTANCE_NOTIFICATION, .25f, 1f, 1f, false, true));
	}

	public void remove() {
		arrow.remove();
		timer.stop();
	}

	public void update(Object anim) {
		remove();
		manager.removeNotification(this);
	}

	protected final Arrow getArrow() {
		return arrow;
	}

	protected final TimerAnimation getTimer() {
		return timer;
	}

	protected final NotificationManager getManager() {
		return manager;
	}

	public final float getX() {
		return center_x;
	}
	
	public final float getY() {
		return center_y;
	}
}
