package com.oddlabs.tt.model;

public strictfp class SupplyContainer {
	private final int max_supply_count;

	private int supply_count = 0;
	private int num_preparing = 0;

	public SupplyContainer(int max_supply_count) {
		this.max_supply_count = max_supply_count;
	}

	public int increaseSupply(int amount) {
		int capped_amount = capAmount(amount);
		supply_count += capped_amount;
		return capped_amount;
	}

	public int capAmount(int amount) {
		return StrictMath.max(0, StrictMath.min(supply_count + amount, max_supply_count)) - supply_count;
	}

	public final void prepareDeploy(int amount) {
		int result = increaseSupply(-amount);
		assert result == -amount;
		num_preparing += amount;
	}

	public final boolean isSupplyFull() {
		return supply_count == max_supply_count;
	}

	public final int getNumSupplies() {
		return supply_count;
	}

	public final int getNumPreparing() {
		return num_preparing;
	}

	public final int getMaxSupplyCount() {
		return max_supply_count;
	}
}
