package com.oddlabs.tt.model;

public final strictfp class WorkerUnitContainerFactory implements UnitContainerFactory {
	public final UnitContainer createContainer(Building building) {
		return new WorkerUnitContainer(building);
	}
}
