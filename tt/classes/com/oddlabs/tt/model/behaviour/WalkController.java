package com.oddlabs.tt.model.behaviour;

import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.util.Target;

public final strictfp class WalkController extends Controller {
	private final Unit unit;
	private final Target target;
	private final boolean scan_attack;

	public WalkController(Unit unit, Target t, boolean scan_attack) {
		super(1);
		this.unit = unit;
		this.target = t;
		this.scan_attack = scan_attack;
	}

	public final void decide() {
		if (shouldGiveUp(0))
			unit.popController();
		else
			unit.setBehaviour(new WalkBehaviour(unit, target, 0f, scan_attack));
	}

	public final boolean isAgressive() {
		return scan_attack;
	}
	
	public final Target getTarget() {
		return target;
	}
}
