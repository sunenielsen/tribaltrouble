package com.oddlabs.tt.model;

import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.landscape.World;

public final strictfp class ReproduceUnitContainer extends UnitContainer {
	private final Building building;
	

	private float unit_reproduction = 0f;

	public ReproduceUnitContainer(Building building) {
		super(building.getOwner().getWorld().getMaxUnitCount());
		this.building = building;
	}

	public final float getBuildProgress() {
		ChieftainContainer chieftain_container = building.getChieftainContainer();
		if (chieftain_container.isTraining()) {
			return 0;
		} else {
			return unit_reproduction;
		}
	}

	public final void resetProgress() {
		unit_reproduction = 0f;
	}

	public final void enter(Unit unit) {
		assert canEnter(unit);
		unit.removeNow();
		increaseSupply(1);
	}

	public final boolean canEnter(Unit unit) {
		return !unit.getAbilities().hasAbilities(Abilities.THROW) && getTotalSupplies() != getMaxSupplyCount();
	}

	private final int getTotalSupplies() {
//		return getNumSupplies() + building.getBuildSupplyContainer(Unit.class).getNumSupplies() == getMaxSupplyCount();
		return getNumSupplies() + getNumPreparing();
	}

	public final Unit exit() {
		assert getNumSupplies() > 0;
		increaseSupply(-1);
		return null;
	}

	public int increaseSupply(int amount) {
		int result = building.getOwner().getUnitCountContainer().increaseSupply(amount);
		assert result == amount: "result = " + result + " | amount = " + amount;
		return super.increaseSupply(amount);
	}

	public final void animate(float t) {
		ChieftainContainer chieftain_container = building.getChieftainContainer();
		
		if ((building.getOwner().getUnitCountContainer().getNumSupplies() < getMaxSupplyCount() && getTotalSupplies() != getMaxSupplyCount())
				|| chieftain_container.isTraining()) {
			float units = (float)StrictMath.max(building.getUnitContainer().getNumSupplies(), .5f);
			unit_reproduction += ((1f/11f)*StrictMath.pow(units, 1f/3f))*t;
			while (unit_reproduction >= 1f) {
				unit_reproduction -= 1f;
				if (chieftain_container.isTraining()) {
					chieftain_container.progress();
				} else {
					increaseSupply(1);
				}
			}
		} else {
			unit_reproduction = 0;
		}
	}
}
