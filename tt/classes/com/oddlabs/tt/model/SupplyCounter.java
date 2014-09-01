package com.oddlabs.tt.model;

public strictfp class SupplyCounter {
	private final Building building;
	private final Class supply_type;

	private int delta;

	public SupplyCounter(Building building, Class supply_type) {
		this.building = building;
		this.supply_type = supply_type;
		delta = 0;
	}

	public final void increaseSupply(int amount) {
		delta += amount;
	}

	public int getNumSupplies() {
		if (!building.isDead()) {
			return building.getSupplyContainer(supply_type).getNumSupplies() + delta;
		} else {
			return 0;
		}
	}

	protected final Building getBuilding() {
		return building;
	}

	protected final Class getSupplyType() {
		return supply_type;
	}
	public final int getDelta() {
		return delta;
	}

	protected final void setDelta(int delta) {
		this.delta = delta;
	}

	public final int getMaxSupplies() {
		return !building.isDead() ? building.getSupplyContainer(supply_type).getMaxSupplyCount() : 0;
	}
}
