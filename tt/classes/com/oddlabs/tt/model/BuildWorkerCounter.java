package com.oddlabs.tt.model;

public final strictfp class BuildWorkerCounter extends SupplyCounter {
	public BuildWorkerCounter(Building building, Class supply_type) {
		super(building, supply_type);
		setDelta(building.getBuildSupplyContainer(supply_type).getNumOrders());
	}

	public final int getNumSupplies() {
		if (!getBuilding().isDead())
			return getBuilding().getSupplyContainer(getSupplyType()).getNumSupplies() - (getDelta() - getBuilding().getBuildSupplyContainer(getSupplyType()).getNumOrders());
		else
			return 0;
	}
}
