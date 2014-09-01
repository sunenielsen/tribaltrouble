package com.oddlabs.tt.render;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.HashMap;

import com.oddlabs.tt.util.StateChecksum;
import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.animation.AnimationManager;
import com.oddlabs.tt.animation.Animated;

public final strictfp class RespondManager implements Animated {
	private final static float SECONDS_PER_PICK_RESPOND = 1f/3f;

	private final SortedMap respond_timeouts = new TreeMap();
	private final Map respond_targets = new HashMap();

	private int current_id;

	private float time;

	RespondManager(AnimationManager manager) {
		manager.registerAnimation(this);
	}

	public final void animate(float t) {
		time += t;
		timeout();
	}

	private void timeout() {
		Timeout head_timeout;
		while (!respond_timeouts.isEmpty() && (head_timeout = (Timeout)respond_timeouts.firstKey()).timeout <= time) {
			removeResponder(head_timeout.target);
		}
	}

	public final void addResponder(Object target) {
		addResponder(target, null);
	}

	final void addResponder(Object target, Runnable stop_action) {
		addResponder(SECONDS_PER_PICK_RESPOND, target, null);
	}

	private void addResponder(float respond_time, Object target, Runnable stop_action) {
		removeResponder(target);
		Timeout timeout = new Timeout(time + respond_time, current_id++, target, stop_action);
		respond_targets.put(target, timeout);
		respond_timeouts.put(timeout, target);
	}

	private void removeResponder(Object target) {
		Timeout timeout = (Timeout)respond_targets.remove(target);
		if (timeout != null) {
			respond_timeouts.remove(timeout);
			if (timeout.stop_action != null)
				timeout.stop_action.run();
		}
	}

	final boolean isResponding(Object target) {
		if (respond_targets.size() == 0)
			return false; // Quick exit in the common case of no responding targets
		else
			return isResponding((Timeout)respond_targets.get(target));
	}

	private boolean isResponding(Timeout timeout) {
		if (timeout == null)
			return false;
		float time_diff = timeout.timeout - time;
		float blink = SECONDS_PER_PICK_RESPOND/4f;
		return time_diff > 0 && (time_diff >= SECONDS_PER_PICK_RESPOND - blink || time_diff <= blink);
	}

	public final void updateChecksum(StateChecksum checksum) {
	}

	private final static strictfp class Timeout implements Comparable {
		private final float timeout;
		private final int id;
		private final Object target;
		private final Runnable stop_action;

		Timeout(float timeout, int id, Object target, Runnable stop_action) {
			this.timeout = timeout;
			this.id = id;
			this.target = target;
			this.stop_action = stop_action;
		}

		public final boolean equals(Object other) {
			Timeout timeout_obj = (Timeout)other;
			return timeout_obj.timeout == timeout && timeout_obj.id == id;
		}

		public final int compareTo(Object other) {
			Timeout timeout_obj = (Timeout)other;
			float diff = timeout - timeout_obj.timeout;
			if (diff != 0f)
				return (int)diff;
			else
				return id - timeout_obj.id;
		}
	}
}
