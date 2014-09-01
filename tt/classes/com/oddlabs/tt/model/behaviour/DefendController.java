package com.oddlabs.tt.model.behaviour;

import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.util.Target;

public final strictfp class DefendController extends Controller {
	private final Unit unit;
	private final Target target;

	public DefendController(Unit unit, Target t) {
		super(1);
		this.unit = unit;
		this.target = t;
	}

	public final void decide() {
		if (shouldGiveUp(0))
			unit.popController();
		else
			unit.setBehaviour(new WalkBehaviour(unit, target, 0f, true));
	}
}
