package com.oddlabs.tt.model.behaviour;

import com.oddlabs.tt.gui.ToolTipBox;
import com.oddlabs.tt.model.AttackScanFilter;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.pathfinder.Occupant;
import com.oddlabs.tt.pathfinder.Movable;
import com.oddlabs.tt.pathfinder.PathTracker;
import com.oddlabs.tt.pathfinder.TargetTrackerAlgorithm;
import com.oddlabs.tt.pathfinder.TrackerAlgorithm;
import com.oddlabs.tt.util.Target;

public final strictfp class WalkBehaviour implements Behaviour {
	private final static float WAIT_RETRY_DELAY = 1f/2f;
	private final static float MAX_WAIT_RETRY_DELAY = 5f;

	private final Unit unit;
	private final TrackerAlgorithm tracker_algorithm;
	private final AttackScanFilter scan_filter;
	private final boolean scan_attack;

	private Movable blocking_movable;
	private int blocker_x;
	private int blocker_y;
	private float retry_delay_counter;
	private float retry_delay;

	private int state;

	public WalkBehaviour(Unit unit, TrackerAlgorithm tracker_algorithm, boolean scan_attack) {
		this.unit = unit;
		this.tracker_algorithm = tracker_algorithm;
		this.scan_attack = scan_attack;
		scan_filter = new AttackScanFilter(unit.getOwner(), AttackScanFilter.UNIT_RANGE);
		retry_delay = WAIT_RETRY_DELAY;
		init();
	}

	public WalkBehaviour(Unit unit, Target t, float range, boolean scan_attack) {
		this(unit, new TargetTrackerAlgorithm(unit.getUnitGrid(), range, t), scan_attack);
	}

	public final boolean isBlocking() {
		return state == PathTracker.BLOCKED;
	}
	
	public void appendToolTip(ToolTipBox tool_tip_box) {
		tool_tip_box.append("WalkBehaviour: state=");
		switch (state) {
			case PathTracker.OK:
				tool_tip_box.append("OK");
				break;
			case PathTracker.OK_INTERRUPTIBLE:
				tool_tip_box.append("OK_INTERRUPTIBLE");
				break;
			case PathTracker.DONE:
				tool_tip_box.append("DONE");
				break;
			case PathTracker.BLOCKED:
				tool_tip_box.append("BLOCKED");
				break;
			case PathTracker.SOFTBLOCKED:
				tool_tip_box.append("SOFTBLOCKED");
				break;
		}
		tool_tip_box.append(" | retry_delay=");
		tool_tip_box.append((int)retry_delay);
		tool_tip_box.append("(");
		tool_tip_box.append((int)retry_delay);
		tool_tip_box.append("s)");
		unit.getTracker().appendToolTip(tool_tip_box);
	}

	private final void switchToMoving() {
		unit.switchAnimation(unit.getMetersPerSecond(), Unit.ANIMATION_MOVING);
	}

	public final int animate(float t) {
		retry_delay_counter -= t;
		boolean blocker_moved = blocking_movable != null && (blocking_movable.getGridX() != blocker_x || blocking_movable.getGridY() != blocker_y);
		if (retry_delay_counter > 0 && !blocker_moved) {
			return Selectable.INTERRUPTIBLE;
		}
		retry_delay_counter = 0;
		blocking_movable = null;
		PathTracker tracker = unit.getTracker();
		state = tracker.animate(unit.getMetersPerSecond()*t);
		switch (state) {
			case PathTracker.OK:
				switchToMoving();
				return Selectable.UNINTERRUPTIBLE;
			case PathTracker.OK_INTERRUPTIBLE:
				retry_delay = WAIT_RETRY_DELAY;
				switchToMoving();
				scan();
				return Selectable.INTERRUPTIBLE;
			case PathTracker.DONE:
				return Selectable.DONE;
			case PathTracker.BLOCKED: /* fall through */
			case PathTracker.SOFTBLOCKED:
				Occupant blocker = tracker.getBlocker();
				if (blocker instanceof Movable) {
					blocking_movable = (Movable)blocker;
					if (!blocking_movable.isDead()) {
						blocking_movable.markBlocking();
						blocker_x = blocking_movable.getGridX();
						blocker_y = blocking_movable.getGridY();
					} else {
						blocking_movable = null;
					}
				}
				scan();
				retry_delay = StrictMath.min(2*retry_delay, MAX_WAIT_RETRY_DELAY);
				return doRetry();
			default:
				throw new RuntimeException("Invalid tracker state: " + state);
		}
	}

	private final void scan() {
		if (scan_attack) {
			unit.scanVicinity(scan_filter);
			Selectable s = scan_filter.removeTarget();
			if (s != null) {
				unit.getCurrentController().resetGiveUpCounters();
				unit.pushController(new HuntController(unit, s));
			}
		}
	}

/*	public final void moveNextAnimate() {
		retry_delay_counter = 0f;
	}
*/
	private final int doRetry() {
		retry_delay_counter = retry_delay;
		unit.switchToIdleAnimation();
		return Selectable.INTERRUPTIBLE;
	}

	private final void init() {
		unit.getTracker().setTarget(tracker_algorithm);
	}

	public final void forceInterrupted() {
	}
}
