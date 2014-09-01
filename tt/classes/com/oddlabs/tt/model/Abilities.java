package com.oddlabs.tt.model;

public final strictfp class Abilities {
	public final static int NONE = 0;
	public final static int BUILD = 1;
	public final static int ATTACK = 2;
	public final static int HARVEST = 4;
	public final static int SUPPLY_CONTAINER = 8;
	public final static int BUILD_ARMIES = 16;
	public final static int REPRODUCE = 32;
	public final static int TARGET = 64;
	public final static int THROW = 128;
	public final static int RALLY_TO = 256;
	public final static int MAGIC = 512;

	private int abilities;

	public Abilities(int abilities) {
		this.abilities = abilities;
	}

	public final boolean hasAbilities(int abilities) {
		return (this.abilities | abilities) == this.abilities;
	}

	public final void addAbilities(Abilities abilities) {
		addAbilities(abilities.abilities);
	}

	public final void addAbilities(int abilities) {
		this.abilities = this.abilities | abilities;
	}

	public final void removeAbilities(Abilities abilities) {
		removeAbilities(abilities.abilities);
	}

	public final void removeAbilities(int abilities) {
		this.abilities = this.abilities & ~abilities;
	}
}
