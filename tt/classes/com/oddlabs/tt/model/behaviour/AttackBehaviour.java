package com.oddlabs.tt.model.behaviour;

import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;

public final strictfp class AttackBehaviour implements Behaviour {
	private final static float SECONDS_PER_ATTACK = 2f;

	private final static int THROWING = 1;
	private final static int RELEASED = 2;

	private final Selectable target;
	private final Unit unit;
	private float anim_time;
	private int state;

	public AttackBehaviour(Unit unit, Selectable target) {
		this.unit = unit;
		this.target = target;
		init();
	}

	public final boolean isBlocking() {
		return true;
	}

	public final int animate(float t) {
		switch (state) {
			case THROWING:
				updateAttack(t);
				if (anim_time <= 0) {
					if (unit.isMounted())
						unit.getWeaponFactory().attack(unit, target, 3f);
					else
						unit.getWeaponFactory().attack(unit, target);

					anim_time += SECONDS_PER_ATTACK - unit.getWeaponFactory().getSecondsPerRelease(1f/SECONDS_PER_ATTACK);
					state = RELEASED;
				}
				return Selectable.UNINTERRUPTIBLE;
			case RELEASED:
				updateAttack(t);
				if (anim_time > 0)
					return Selectable.UNINTERRUPTIBLE;
				else
					return Selectable.DONE;
			default:
				throw new RuntimeException("Invalid state: " + state);
		}
	}

	private final void updateAttack(float t) {
		anim_time -= t;
		unit.aimAtTarget(target);
	}

	private final void init() {
		state = THROWING;
		anim_time += unit.getWeaponFactory().getSecondsPerRelease(1f/SECONDS_PER_ATTACK);
		unit.switchAnimation(1f/SECONDS_PER_ATTACK, Unit.ANIMATION_THROWING);
	}

	public final void forceInterrupted() {
	}
}
