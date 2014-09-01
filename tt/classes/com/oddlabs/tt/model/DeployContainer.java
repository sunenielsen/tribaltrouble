package com.oddlabs.tt.model;

public strictfp class DeployContainer extends SupplyContainer {
	private final Building building;
	private final int deploy_type;
	private final Class supply_type;
	private final float seconds_per_deploy;

	private float time = 0;
	private int num_orders = 0;


	public DeployContainer(Building building, float seconds_per_deploy, int deploy_type, Class supply_type) {
		super(Integer.MAX_VALUE);
		this.building = building;
		this.seconds_per_deploy = seconds_per_deploy;
		this.deploy_type = deploy_type;
		this.supply_type = supply_type;
	}

	public void orderSupply(int orders) {
		int capped_amount = capAmount(orders);
		int result = -building.getUnitContainer().capAmount(-capped_amount);
		if (supply_type != null)
			result = -building.getSupplyContainer(supply_type).capAmount(-result);
		if (result > 0) {
			if (supply_type != null)
				building.getSupplyContainer(supply_type).prepareDeploy(result);
			building.getUnitContainer().prepareDeploy(result);
			orderSupply(result, orders);
		} else {
			orderSupply(result, orders);
			building.getUnitContainer().prepareDeploy(result);
			if (supply_type != null) {
				SupplyContainer supply_container = building.getSupplyContainer(supply_type);
				if (!supply_container.isSupplyFull())
					supply_container.prepareDeploy(result);
			}
		}
	}

	private final void orderSupply(int amount, int orders) {
		increaseSupply(amount);
		num_orders += orders;
	}

	public final int getNumOrders() {
		return num_orders;
	}

	public final void deploy(float amount) {
		time += amount;
		if (time >= seconds_per_deploy) {
			time = 0;
			increaseSupply(-1);
			doDeploy();
		}
	}

	private final void doDeploy() {
		switch (deploy_type) {
			case Building.KEY_DEPLOY_ROCK_WARRIOR:
				building.createArmy(0, 1, 0, 0);
				break;
			case Building.KEY_DEPLOY_IRON_WARRIOR:
				building.createArmy(0, 0, 1, 0);
				break;
			case Building.KEY_DEPLOY_RUBBER_WARRIOR:
				building.createArmy(0, 0, 0, 1);
				break;
			case Building.KEY_DEPLOY_PEON:
				building.createArmy(1, 0, 0, 0);
				break;
			case Building.KEY_DEPLOY_PEON_HARVEST_TREE:
				building.createHarvesters(1, 0, 0, 0);
				break;
			case Building.KEY_DEPLOY_PEON_TRANSPORT_TREE:
				building.createTransporters(1, 0, 0, 0);
				break;
			case Building.KEY_DEPLOY_PEON_HARVEST_ROCK:
				building.createHarvesters(0, 1, 0, 0);
				break;
			case Building.KEY_DEPLOY_PEON_TRANSPORT_ROCK:
				building.createTransporters(0, 1, 0, 0);
				break;
			case Building.KEY_DEPLOY_PEON_HARVEST_IRON:
				building.createHarvesters(0, 0, 1, 0);
				break;
			case Building.KEY_DEPLOY_PEON_TRANSPORT_IRON:
				building.createTransporters(0, 0, 1, 0);
				break;
			case Building.KEY_DEPLOY_PEON_HARVEST_RUBBER:
				building.createHarvesters(0, 0, 0, 1);
				break;
			case Building.KEY_DEPLOY_PEON_TRANSPORT_RUBBER:
				building.createTransporters(0, 0, 0, 1);
				break;
		}
	}

	public int increaseSupply(int amount) {
		int result = building.getOwner().getUnitCountContainer().increaseSupply(amount);
		assert result == amount;
		return super.increaseSupply(amount);
	}

	public final float getBuildProgress() {
		return time/seconds_per_deploy;
	}
}
