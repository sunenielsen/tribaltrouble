package com.oddlabs.tt.model.behaviour;

import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;

public final strictfp class AttackController extends Controller {
	
	private final Selectable target;
	private final Unit unit;

	public AttackController(Unit unit, Selectable target) {
		super(0);
		this.unit = unit;
		this.target = target;
	}

	private final boolean canAttack() {
		return unit.isCloseEnough(unit.getRange(target), target);
	}

	public final void decide() {
		if (target.isDead() || !canAttack()) {
			unit.popController();
		} else {
			unit.setBehaviour(new AttackBehaviour(unit, target));
		}
	}
}
