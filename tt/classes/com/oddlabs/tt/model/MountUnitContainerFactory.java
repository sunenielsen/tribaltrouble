package com.oddlabs.tt.model;

public final strictfp class MountUnitContainerFactory implements UnitContainerFactory {
	public final UnitContainer createContainer(Building building) {
		return new MountUnitContainer(building);
	}
}
