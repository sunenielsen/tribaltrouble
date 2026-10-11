package com.oddlabs.tt.model.behaviour;

import com.oddlabs.tt.landscape.TreeSupply;
import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Unit;
import org.jspecify.annotations.NonNull;

public final class RepairController extends Controller {
    private enum State {
        HARVEST,
        REPAIR
    }

    private final Building building;
    private final Unit unit;

    public RepairController(Unit unit, Building building) {
        super(State.values().length);
        this.unit = unit;
        this.building = building;
    }

    public Building getBuilding() {
        return building;
    }

    @Override
    public @NonNull String getKey() {
        return super.getKey() + "@" + building.getGridX() + "," + building.getGridY();
    }

    @Override
    public void decide() {
        if (building.isDead()) {
            unit.popController();
        } else if (unit.getSupplyContainer().getSupplyType() == TreeSupply.class
                && unit.getSupplyContainer().getNumSupplies() > 0) {
                    resetGiveUpCounter(State.HARVEST.ordinal());
                    if (unit.isCloseEnough(0f, building)) {
                        if (building.isDamaged()) {
                            unit.setBehaviour(new RepairBehaviour(unit, building));
                        } else if (building.getAbilities().hasAbilities(Abilities.SUPPLY_CONTAINER)
                                && unit.getOwner() == building.getOwner()) {
                                    unit.swapController(new EnterController(unit, building));
                                } else {
                                    unit.popController();
                                }
                    } else {
                        if (shouldGiveUp(State.REPAIR.ordinal())) {
                            unit.popController();
                        } else {
                            unit.setBehaviour(new WalkBehaviour(unit, building, 0, false));
                        }
                    }
                } else {
                    resetGiveUpCounter(State.REPAIR.ordinal());
                    if (!shouldGiveUp(State.HARVEST.ordinal())) {
                        unit.pushController(new HarvestController<>(unit, null, TreeSupply.class));
                    } else {
                        unit.popController();
                    }
                }
    }
}
