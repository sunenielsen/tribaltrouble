package com.oddlabs.tt.model.behaviour;

import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.model.weapon.ThrowingFactory;

public final strictfp class EnterController extends Controller {
	private final Building building;
	private final Unit unit;

	public EnterController(Unit unit, Building building) {
		super(1);
		this.unit = unit;
		this.building = building;
	}

	public final void decide() {
		if (building.isDead()) {
			unit.popController();
		} else if (unit.isCloseEnough(0f, building)) {
			if (building.getUnitContainer() != null && building.getUnitContainer().canEnter(unit)) {
				if (building.getAbilities().hasAbilities(Abilities.SUPPLY_CONTAINER)) {
					if (unit.getAbilities().hasAbilities(Abilities.HARVEST)
							&& unit.getSupplyContainer().getNumSupplies() > 0) {
						Class type = unit.getSupplyContainer().getSupplyType();
						building.getSupplyContainer(type).increaseSupply(unit.getSupplyContainer().getNumSupplies());
					}
					if (unit.getWeaponFactory() instanceof ThrowingFactory) {
						Class type = unit.getWeaponFactory().getType();
						building.getSupplyContainer(type).increaseSupply(1);
					}
				}
				building.getUnitContainer().enter(unit);
			} else {
				unit.popController();
			}
		} else {
			if (shouldGiveUp(0)) {
				unit.popController();
			} else
				unit.setBehaviour(new WalkBehaviour(unit, building, 0, false));
		}
	}
}
