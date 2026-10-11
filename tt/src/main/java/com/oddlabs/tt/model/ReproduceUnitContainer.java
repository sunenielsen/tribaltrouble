package com.oddlabs.tt.model;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ReproduceUnitContainer extends UnitContainer {
    private final @NonNull Building building;


    private float unit_reproduction = 0f;

    public ReproduceUnitContainer(@NonNull Building building) {
        super(building.getOwner().getWorld().getMaxUnitCount());
        this.building = building;
    }

    public float getBuildProgress() {
        ChieftainContainer chieftain_container = building.getChieftainContainer();
        if (chieftain_container.isTraining()) {
            return 0;
        } else {
            return unit_reproduction;
        }
    }

    public void resetProgress() {
        unit_reproduction = 0f;
    }

    @Override
    public void enter(@NonNull Unit unit) {
        assert canEnter(unit);
        unit.removeNow();
        increaseSupply(1);
    }

    @Override
    public boolean canEnter(@NonNull Unit unit) {
        return !unit.getAbilities().hasAbilities(Abilities.THROW) && getTotalSupplies() != getMaxSupplyCount();
    }

    private int getTotalSupplies() {
//		return getNumSupplies() + building.getBuildSupplyContainer(Unit.class).getNumSupplies() == getMaxSupplyCount();
        return getNumSupplies() + getNumPreparing();
    }

    @Override
    public @Nullable Unit exit() {
        assert getNumSupplies() > 0;
        increaseSupply(-1);
        return null;
    }

    @Override
    public int increaseSupply(int amount) {
        int result = building.getOwner().getUnitCountContainer().increaseSupply(amount);
        assert result == amount : "result = " + result + " | amount = " + amount;
        return super.increaseSupply(amount);
    }

    @Override
    public void animate(float t) {
        ChieftainContainer chieftain_container = building.getChieftainContainer();

        if ((building.getOwner().getUnitCountContainer().getNumSupplies() < getMaxSupplyCount()
                && getTotalSupplies() != getMaxSupplyCount())
                || chieftain_container.isTraining()) {
            float units = Math.max(building.getUnitContainer().getNumSupplies(), .5f);
            unit_reproduction += ((1f / 11f) * StrictMath.pow(units, 1f / 3f)) * t;
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
