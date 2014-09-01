package com.oddlabs.tt.model.behaviour;

import com.oddlabs.tt.model.Unit;

public final strictfp class DieController extends Controller {
	private final Unit unit;

	public DieController(Unit unit) {
		super(0);
		this.unit = unit;
	}

	public final void decide() {
		unit.setBehaviour(new DieBehaviour(unit));
	}
}
