package com.oddlabs.tt.model;

public abstract strictfp class BuildSupplyContainer extends SupplyContainer {
	private int num_orders = 0;

	public BuildSupplyContainer(int max_supply_count) {
		super(max_supply_count);
	}

	public void orderSupply(int amount) {
		orderSupply(amount, amount);
	}
	
	protected final void orderSupply(int amount, int orders) {
		increaseSupply(amount);
		num_orders += orders;
	}

	public final int getNumOrders() {
		return num_orders;
	}
}
