package com.oddlabs.tt.model;

public abstract strictfp class UnitContainer extends SupplyContainer {
	public UnitContainer(int capacity) {
		super(capacity);
	}

	public abstract void enter(Unit unit);
	public abstract boolean canEnter(Unit unit);
	public abstract Unit exit();
	public abstract void animate(float t);
}
