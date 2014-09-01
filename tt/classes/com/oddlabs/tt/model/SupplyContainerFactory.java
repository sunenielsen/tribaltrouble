package com.oddlabs.tt.model;

public abstract strictfp class SupplyContainerFactory {
	private final int max_resource_count;

	public SupplyContainerFactory(int max_resource_count) {
		this.max_resource_count = max_resource_count;
	}

	public abstract SupplyContainer createContainer(Selectable selectable);

	protected final int getMaxResourceCount() {
		return max_resource_count;
	}
}
