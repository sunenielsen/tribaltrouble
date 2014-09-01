package com.oddlabs.tt.player;

public final strictfp class UnitInfo {
	private final boolean has_quarters;
	private final boolean has_armory;
	private final int num_towers;
	private final boolean has_chieftain;
	private final int num_peons;
	private final int num_rock_warriors;
	private final int num_iron_warriors;
	private final int num_rubber_warriors;

	public UnitInfo() {
		this(false, false, 0, false, 0, 0, 0, 0);
	}

	public UnitInfo(boolean has_quarters,
			boolean has_armory,
			int num_towers,
			boolean has_chieftain,
			int num_peons,
			int num_rock_warriors,
			int num_iron_warriors,
			int num_rubber_warriors) {
		this.has_quarters = has_quarters;
		this.has_armory = has_armory;
		this.num_towers = num_towers;
		this.has_chieftain = has_chieftain;
		this.num_peons = num_peons;
		this.num_rock_warriors = num_rock_warriors;
		this.num_iron_warriors = num_iron_warriors;
		this.num_rubber_warriors = num_rubber_warriors;
	}

	public final boolean hasQuarters() {
		return has_quarters;
	}

	public final boolean hasArmory() {
		return has_armory;
	}

	public final int getNumTowers() {
		return num_towers;
	}

	public final boolean hasChieftain() {
		return has_chieftain;
	}

	public final int getNumPeons() {
		return num_peons;
	}

	public final int getNumRockWarriors() {
		return num_rock_warriors;
	}

	public final int getNumIronWarriors() {
		return num_iron_warriors;
	}

	public final int getNumRubberWarriors() {
		return num_rubber_warriors;
	}
}
