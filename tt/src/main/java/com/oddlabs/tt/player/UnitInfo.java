package com.oddlabs.tt.player;

import java.io.Serializable;

public record UnitInfo(boolean hasQuarters,
                       boolean hasArmory,
                       int numTowers,
                       boolean hasChieftain,
                       int numPeons,
                       int numRockWarriors,
                       int numIronWarriors,
                       int numRubberWarriors) implements Serializable {
    public UnitInfo() {
        this(false, false, 0, false, 0, 0, 0, 0);
    }

    public int numPeonsAndWarriors() {
        return numPeons + numRockWarriors + numIronWarriors + numRubberWarriors;
    }
}
