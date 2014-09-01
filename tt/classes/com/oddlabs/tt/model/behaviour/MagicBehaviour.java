package com.oddlabs.tt.model.behaviour;

import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.model.weapon.Magic;
import com.oddlabs.tt.model.weapon.MagicFactory;

public final strictfp class MagicBehaviour implements Behaviour {
	private final static int PREPARING = 1;
	private final static int CASTING = 2;
	private final static int ENDING = 3;

	private final Unit unit;
	private final MagicFactory magic_factory;
	private final MagicController controller;
	private Magic magic;

	private float anim_time;
	private int state;

	public MagicBehaviour(Unit unit, MagicFactory magic_factory, MagicController controller) {
		this.unit = unit;
		this.magic_factory = magic_factory;
		this.controller = controller;
		init();
	}

	public final boolean isBlocking() {
		return true;
	}

	public final int animate(float t) {
		anim_time -= t;
		switch (state) {
			case PREPARING:
				if (anim_time <= 0) {
					state = CASTING;
					magic = magic_factory.execute(unit);
					anim_time += magic_factory.getSecondsPerRelease() - magic_factory.getSecondsPerInit();
				}
				return Selectable.UNINTERRUPTIBLE;
			case CASTING:
				if (anim_time <= 0) {
					state = ENDING;
					unit.getOwner().getWorld().getAnimationManagerGameTime().registerAnimation(magic);
					anim_time += magic_factory.getSecondsPerAnim() - magic_factory.getSecondsPerRelease();
				}
				return Selectable.UNINTERRUPTIBLE;
			case ENDING:
				if (anim_time > 0)
					return Selectable.UNINTERRUPTIBLE;
				else {
					controller.popNextTime();
					return Selectable.DONE;
				}
			default:
				throw new RuntimeException("Invalid state: " + state);
		}
	}

	private final void init() {
		state = PREPARING;
		anim_time = magic_factory.getSecondsPerInit();
		unit.switchAnimation(1f/magic_factory.getSecondsPerAnim(), Unit.ANIMATION_MAGIC);
	}

	public final void forceInterrupted() {
		if (magic != null)
			magic.interrupt();
	}
}
