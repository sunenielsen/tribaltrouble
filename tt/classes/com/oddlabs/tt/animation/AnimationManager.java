package com.oddlabs.tt.animation;

import java.util.ArrayList;
import java.util.List;

import com.oddlabs.net.NetworkSelector;
import com.oddlabs.net.MonotoneTimeManager;
import com.oddlabs.net.TimeManager;
import com.oddlabs.event.Deterministic;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.GUIObject;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.input.KeyboardInput;
import com.oddlabs.tt.input.PointerInput;
import com.oddlabs.tt.pathfinder.PathFinder;
import com.oddlabs.tt.render.Picker;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.StatCounter;
import com.oddlabs.tt.util.StateChecksum;
import com.oddlabs.tt.gui.GUI;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.form.QuitForm;
import com.oddlabs.tt.camera.CameraState;
import org.lwjgl.opengl.Display;

public final strictfp class AnimationManager {
	public final static int ANIMATION_MILLISECONDS_PER_TICK = 20;
	public final static int ANIMATION_MILLISECONDS_PER_PRECISION_TICK = ANIMATION_MILLISECONDS_PER_TICK/5;
	public final static float ANIMATION_SECONDS_PER_TICK = ANIMATION_MILLISECONDS_PER_TICK/1000f;
	public final static float ANIMATION_SECONDS_PER_PRECISION_TICK = ANIMATION_MILLISECONDS_PER_PRECISION_TICK/1000f;
	private final static int ANIMATION_MILLISECONDS_PER_CHECKSUM = 2000;
	public final static int MAX_STEP_MILLIS = 30000;

	public final static StatCounter pathfinds_per_tick = new StatCounter(100);

	private final static StatCounter frame_time = new StatCounter(10);
	private final static MonotoneTimeManager time_source;

	private static long current_time ;
	private static long last_frame_time;
	private static int execution_time = 0;
	private static float execution_time_precision = 0;
	private static long time_warp;
	private static boolean time_stopped;
	private static boolean time_frozen;
	private static long frozen_start_time;
	private static long frozen_start_time_warped;
	private static long checksum_millisecond_counter;
	private static boolean checksum_complain = true;

	private final List animations = new ArrayList();
	private final List deleted_animations = new ArrayList();

	private int tick;
/*
	private static int[] medium = new int[]{};
	private static int[] big = new int[]{
		11910,
			31936,
			61978,
			82012,
			112072,
			152166,
			202388,
			262714,
			333179,
			403776,
			484491,
			575347,
			666317,
			768888,
			873315,
			979754,
			1088180,
			1198760,
			1311452,
			1426295,
			1542397,
			1658489,
			1776352,
			1894373,
			2012758,
			2132205,
			2253154,
			2374883
	};
*/
	static {
		time_source = new MonotoneTimeManager(new TimeManager() {
			public final long getMillis() {
				return System.currentTimeMillis();
			}
		});
		current_time = getSystemTime();
		last_frame_time = current_time;
		freezeTime();
	}

	public final static void warpTime(long warp_delta) {
		time_warp += warp_delta;
	}

	public final static long getSystemTime() {
		if (time_frozen) {
			return frozen_start_time_warped;
		} else
			return time_source.getMillis() + time_warp;
	}

	public static void toggleTimeStop() {
		if (time_frozen)
			unfreezeTime();
		else
			freezeTime();
		time_stopped = time_frozen;
System.out.println("time_stopped = " + time_stopped);
	}

	private final static void unfreezeTime() {
		if (!time_frozen)
			return;
		time_frozen = false;
		time_warp -= time_source.getMillis() - frozen_start_time;
	}
	
	public final static void freezeTime() {
		if (time_frozen)
			return;
		frozen_start_time_warped = getSystemTime();
		time_frozen = true;
		frozen_start_time = time_source.getMillis();
	}

	public final static void runGameLoop(NetworkSelector network, GUI gui, boolean grab_frames) {
		KeyboardInput.checkMagicKeys();
		if (time_frozen && !time_stopped)
			unfreezeTime();
		if (grab_frames) {
			current_time += Settings.getSettings().frame_grab_milliseconds_per_frame;
		} else {
			current_time = getSystemTime();
		}
		long time_diff = current_time - last_frame_time;
		last_frame_time = current_time;
		Deterministic deterministic = LocalEventQueue.getQueue().getDeterministic();
		if (time_diff > MAX_STEP_MILLIS && !deterministic.isPlayback()) {
			System.out.println("Skipping large time diff: " + time_diff + " ms.");
			time_diff = 0;
		}

		frame_time.updateAbsolute(time_diff);
		execution_time_precision += frame_time.getAveragePerUpdate();
		deterministic.setEnabled(true);
		while (execution_time_precision >= ANIMATION_MILLISECONDS_PER_PRECISION_TICK && !Renderer.isFinished()) {
/*
// Used for replaying warp control in clientload
int tick = LocalEventQueue.getQueue().getHighPrecisionManager().getTick();
for (int i = 0; i < big.length; i++) {
	if (big[i] == tick)
		warpTime(com.oddlabs.tt.input.KeyboardInput.LARGE_WARP);
}
for (int i = 0; i < medium.length; i++) {
	if (medium[i] == tick)
		warpTime(com.oddlabs.tt.input.KeyboardInput.MEDIUM_WARP);
}
*/
			execution_time_precision -= ANIMATION_MILLISECONDS_PER_PRECISION_TICK;
			execution_time += ANIMATION_MILLISECONDS_PER_PRECISION_TICK;
			LocalEventQueue.getQueue().tickHighPrecision(ANIMATION_SECONDS_PER_PRECISION_TICK);
			while (execution_time >= ANIMATION_MILLISECONDS_PER_TICK && !Renderer.isFinished()) {
				network.tick();
				PointerInput.poll(gui.getGUIRoot());
				KeyboardInput.poll(gui.getGUIRoot());
				if (deterministic.log(Display.isCreated() && Display.isCloseRequested())) {
					gui.getGUIRoot().addModalForm(new QuitForm(gui.getGUIRoot()));
				}
				pathfinds_per_tick.updateAbsolute(PathFinder.stat_pathfinder_per_frame);
				PathFinder.stat_pathfinder_per_frame = 0;
				LocalEventQueue.getQueue().tickLowPrecision(ANIMATION_SECONDS_PER_TICK);
				execution_time -= ANIMATION_MILLISECONDS_PER_TICK;
				checksum_millisecond_counter += ANIMATION_MILLISECONDS_PER_TICK;
				if (checksum_millisecond_counter >= ANIMATION_MILLISECONDS_PER_CHECKSUM) {
					checksum_millisecond_counter -= ANIMATION_MILLISECONDS_PER_CHECKSUM;
					int checksum = LocalEventQueue.getQueue().computeChecksum();
					int logged_checksum = deterministic.log(checksum);
					if (checksum != logged_checksum && checksum_complain) {
						System.out.println("********** ERROR: Checksum mismatch at tick " + LocalEventQueue.getQueue().getHighPrecisionManager().getTick() + " | checksum = " + checksum + " | logged_checksum = " + logged_checksum + " **********");
						checksum_complain = false;
					}
				}
				if (!Globals.frustum_freeze) {
					gui.pickHover();
				}
			}
// Only for debugging
/*
			if (LocalEventQueue.getQueue().getHighPrecisionManager().getTick() < 2467619 + 10000)
			{
				execution_time_precision += ANIMATION_MILLISECONDS_PER_PRECISION_TICK;
				freezeTime();
			}

			if (LocalEventQueue.getQueue().getHighPrecisionManager().getTick() > 2529461 + 2000) {
				System.out.println("FORCE QUIT: getHighPrecisionManager().getTick() = " + LocalEventQueue.getQueue().getHighPrecisionManager().getTick());
				com.oddlabs.tt.Main.shutdown();
			}*/
		}
		deterministic.setEnabled(false);
	}

	public final int getTick() {
		return tick;
	}

	public final void registerAnimation(Animated anim) {
		deleted_animations.remove(anim);
		if (!animations.contains(anim)) {
			animations.add(anim);
		}
	}

	public final void removeAnimation(Animated anim) {
		if (animations.contains(anim) && !deleted_animations.contains(anim)) {
			deleted_animations.add(anim);
		}
	}

	private final void flushAnimations() {
		for (int i = 0; i < deleted_animations.size(); i++)
			animations.remove(deleted_animations.get(i));
		deleted_animations.clear();
	}

	public final void updateChecksum(StateChecksum checksum) {
		flushAnimations();
		for (int i = 0; i < animations.size(); i++) {
			((Animated)animations.get(i)).updateChecksum(checksum);
		}
	}

	public final void runAnimations(float t) {
		tick++;
		flushAnimations();
		for (int i = 0; i < animations.size(); i++) {
			Animated current = (Animated)animations.get(i);
			if (deleted_animations.contains(current))
				continue;
			current.animate(t);
		}
	}

	public final void debugPrintAnimations() {
		flushAnimations();
		for (int i = 0; i < animations.size(); i++) {
System.out.println("anim = " + animations.get(i));
		}
	}
}
