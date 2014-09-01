package com.oddlabs.tt.model;

import com.oddlabs.tt.render.SpriteKey;

import java.util.Map;

public final strictfp class UnitSupplyContainer extends SupplyContainer {
	private final Map supply_sprite_renderers;

	private Class type;

	public UnitSupplyContainer(int max_resource_count, Map supply_sprite_renderers) {
		super(max_resource_count);
		this.supply_sprite_renderers = supply_sprite_renderers;
	}

	public final int increaseSupply(int amount) {
		throw new RuntimeException();
	}

	public final int increaseSupply(int amount, Class type) {
		if (this.type != type) {
			this.type = type;
			super.increaseSupply(-super.getNumSupplies());
		}
		return super.increaseSupply(amount);
	}

	public final Class getSupplyType() {
		return type;
	}

	public final SpriteKey getSupplySpriteRenderer(Class key) {
		return (SpriteKey)supply_sprite_renderers.get(key);
	}
}
