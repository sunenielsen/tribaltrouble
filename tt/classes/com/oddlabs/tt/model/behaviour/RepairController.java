package com.oddlabs.tt.model.behaviour;

import com.oddlabs.tt.landscape.TreeSupply;
import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Unit;

public final strictfp class RepairController extends Controller {
	private final static int HARVEST_STATE = 0;
	private final static int REPAIR_STATE = 1;

	private final Building building;
	private final Unit unit;

	public RepairController(Unit unit, Building building) {
		super(2);
		this.unit = unit;
		this.building = building;
	}

	public final Building getBuilding() {
		return building;
	}

	public final String getKey() {
		return super.getKey() + building.hashCode();
	}

	public final void decide() {
		if (building.isDead()) {
			unit.popController();
		} else if (unit.getSupplyContainer().getSupplyType() == TreeSupply.class && unit.getSupplyContainer().getNumSupplies() > 0) {
			resetGiveUpCounter(HARVEST_STATE);
			if (unit.isCloseEnough(0f, building)) {
				if (building.isDamaged()) {
					unit.setBehaviour(new RepairBehaviour(unit, building));
				} else if (building.getAbilities().hasAbilities(Abilities.SUPPLY_CONTAINER) && unit.getOwner() == building.getOwner()) {
					unit.swapController(new EnterController(unit, building));
				} else {
					unit.popController();
				}
			} else {
				if (shouldGiveUp(REPAIR_STATE)) {
					unit.popController();
				} else {
					unit.setBehaviour(new WalkBehaviour(unit, building, 0, false));
				}
			}
		} else {
			resetGiveUpCounter(REPAIR_STATE);
			if (!shouldGiveUp(HARVEST_STATE)) {
				unit.pushController(new HarvestController(unit, null, TreeSupply.class));
			} else {
				unit.popController();
			}
		}
	}
}
