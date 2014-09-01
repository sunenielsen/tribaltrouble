package com.oddlabs.tt.gui;

import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.animation.AnimationManager;
import com.oddlabs.tt.font.Font;
import com.oddlabs.tt.util.StateChecksum;

public final strictfp class CounterLabel extends Label implements Animated {
	private final static String colon = ":";
	private final float initial_seconds;
	private final boolean render_seconds;
	private AnimationManager manager;
	private float seconds;

	public CounterLabel(float seconds, Font font, boolean render_seconds) {
		super("", font, 300);
		this.render_seconds = render_seconds;
		initial_seconds = seconds;
		setTime(initial_seconds);
	}

	public final void start(AnimationManager manager) {
		this.manager = manager;
		setTime(initial_seconds);
		manager.registerAnimation(this);
	}

	public final void stop() {
		if (manager != null)
			manager.removeAnimation(this);
	}

	private final void setTime(float seconds) {
		this.seconds = seconds;
		int rest_seconds = ((int)seconds)%60;
		int minutes = (((int)seconds)/60)%60;
		int hours = (((int)seconds)/60)/60;
		if (!render_seconds && seconds > 0) {
			minutes++;
			if (minutes == 60) {
				hours++;
				minutes = 0;
			}
		}
		clear();
		append(hours);
		append(colon);
		if (minutes < 10)
			append(0);
		append(minutes);
		if (render_seconds) {
			append(colon);
			if (rest_seconds < 10)
				append(0);
			append(rest_seconds);
		}
	}

	public final void animate(float t) {
		setTime(seconds - t);
	}

	public final void updateChecksum(StateChecksum check_sum) {
	}
}

