package com.oddlabs.tt.animation;

import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.util.StateChecksum;
import com.oddlabs.tt.animation.AnimationManager;

public final strictfp class TimerAnimation implements Animated {
	private final AnimationManager manager;
	private float time = 0;
	private float interval;
	private Updatable timer_owner = null;
	private boolean running = false;

	public final void updateChecksum(StateChecksum checksum) {
		checksum.update(time);
	}

	public TimerAnimation(AnimationManager manager, Updatable owner, float interval) {
		this.manager = manager;
		this.interval = interval;
		this.timer_owner = owner;
	}

	public TimerAnimation(Updatable owner, float interval) {
		this(LocalEventQueue.getQueue().getManager(), owner, interval);
	}

	public final String toString() {
		return "TimerAnimation: owner = " + timer_owner; 
	}

	public final boolean isRunning() {
		return running;
	}

	public final void stop() {
		running = false;
		manager.removeAnimation(this);
	}

	public final void start() {
		running = true;
		manager.registerAnimation(this);
	}

	public final void setTimerOwner(Updatable obj) {
		this.timer_owner = obj;
	}

	public final Updatable getTimerOwner() {
		return timer_owner;
	}

	public final void setTimerInterval(float interval) {
		this.interval = interval;
	}

	public final void resetTime() {
		time = 0;
	}

	public final void animate(float t) {
		time += t;
		while (time > interval) {
			time -= StrictMath.max(t, interval);
			if (timer_owner != null)
				timer_owner.update(this);
		}
	}
}
