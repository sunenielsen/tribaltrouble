package com.oddlabs.tt.model;

import java.util.Map;

public final strictfp class UnitSupplyContainerFactory extends SupplyContainerFactory {
	private final Map supply_sprite_lists;

	public UnitSupplyContainerFactory(int max_resource_count, Map supply_sprite_lists) {
		super(max_resource_count);
		this.supply_sprite_lists = supply_sprite_lists;
	}

	public SupplyContainer createContainer(Selectable selectable) {
		return new UnitSupplyContainer(getMaxResourceCount(), supply_sprite_lists);
	}
}
