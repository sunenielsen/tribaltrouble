package com.oddlabs.tt.event;

import java.io.EOFException;
import java.io.IOException;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.oddlabs.event.*;
import com.oddlabs.net.ARMIEvent;
import com.oddlabs.net.ARMIEventBroker;
import com.oddlabs.tt.animation.AnimationManager;
import com.oddlabs.tt.util.StateChecksum;

public final strictfp class LocalEventQueue {
	private final static LocalEventQueue queue_instance = new LocalEventQueue();

	private final StateChecksum checksum = new StateChecksum();
	private final AnimationManager manager = new AnimationManager();
	private final AnimationManager high_precision_manager = new AnimationManager();
	private boolean checksum_complain = true;
	private Deterministic deterministic;
	private float time = 0;

	public final float getTime() {
		return time;
	}

	public final long getMillis() {
		return high_precision_manager.getTick()*AnimationManager.ANIMATION_MILLISECONDS_PER_PRECISION_TICK;
	}

	public final static LocalEventQueue getQueue() {
		return queue_instance;
	}

	public final void setEventsLogged(File log_file) {
		assert deterministic == null;
		this.deterministic = new SaveDeterministic(log_file);
	}

	public final void dispose() {
		if (deterministic != null)
			deterministic.endLog();
		deterministic = null;
	}

//public static Deterministic stack_deterministic;
	public final void loadEvents(File log_file, boolean zipped) {
		this.deterministic = new LoadDeterministic(log_file, zipped);
/*		File stack_file = new File("stack.log");
		if (stack_file.exists())
			stack_deterministic = new LoadDeterministic(stack_file, false);
		else
			stack_deterministic = new SaveDeterministic(stack_file);
		this.deterministic = new StackTraceDeterministic(deterministic, stack_deterministic);*/
	}

	public final AnimationManager getHighPrecisionManager() {
		return high_precision_manager;
	}

	public final AnimationManager getManager() {
		return manager;
	}

	public final int computeChecksum() {
		checksum.update(getManager().getTick());
		checksum.update(getHighPrecisionManager().getTick());
		manager.updateChecksum(checksum);
		high_precision_manager.updateChecksum(checksum);
		return checksum.getValue();
	}

	public final Deterministic getDeterministic() {
		return deterministic;
	}

	public final void tickHighPrecision(float t) {
		time += t;
		getHighPrecisionManager().runAnimations(t);
	}

	public final void tickLowPrecision(float t) {
		getManager().runAnimations(t);
	}

	public final void debugPrintAnimations() {
		getManager().debugPrintAnimations();
	}
}
