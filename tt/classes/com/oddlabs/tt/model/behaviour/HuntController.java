package com.oddlabs.tt.model.behaviour;

import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;

public final strictfp class HuntController extends Controller {
	private final Selectable target;
	private final Unit unit;

	public HuntController(Unit unit, Selectable target) {
		super(1);
		this.unit = unit;
		this.target = target;
	}

	private final boolean canAttack() {
		return unit.isCloseEnough(unit.getRange(target), target);
	}

	public final void decide() {
		if (target.isDead()) {
			unit.popController();
		} else if (canAttack()) {
			unit.setBehaviour(new AttackBehaviour(unit, target));
			resetGiveUpCounter(0);
		} else if (!shouldGiveUp(0)) {
			unit.setBehaviour(new WalkBehaviour(unit, target, unit.getRange(target), false));
		} else
			unit.popController();
	}
}
