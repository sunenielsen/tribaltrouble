package com.oddlabs.tt.model.behaviour;

import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.BuildingFinder;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.pathfinder.FinderTrackerAlgorithm;

public final strictfp class TransferUnitController extends Controller {
	private final Unit unit;
	private FinderTrackerAlgorithm building_tracker;

	public TransferUnitController(Unit unit) {
		super(1);
		this.unit = unit;
	}

	public final void decide() {
		if (building_tracker != null && building_tracker.getOccupant() != null && unit.isCloseEnough(0f, building_tracker.getOccupant())) {
			Building building = (Building)building_tracker.getOccupant();
			if (building.getUnitContainer().canEnter(unit))
				building.getUnitContainer().enter(unit);
			else
				unit.popController();
		} else if (!shouldGiveUp(0)) {
			building_tracker = new FinderTrackerAlgorithm(unit.getUnitGrid(), new BuildingFinder(unit.getOwner(), Abilities.SUPPLY_CONTAINER));
			unit.setBehaviour(new WalkBehaviour(unit, building_tracker, false));
		} else {
			unit.popController();
		}
	}
}
